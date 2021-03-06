//  Licensed under the Apache License, Version 2.0 (the "License");
//  you may not use this file except in compliance with the License.
//  You may obtain a copy of the License at
//
//  http://www.apache.org/licenses/LICENSE-2.0
//
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  See the License for the specific language governing permissions and
//  limitations under the License.
package edu.iu.dsc.tws.examples.basic.batch.sort;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.iu.dsc.tws.api.JobConfig;
import edu.iu.dsc.tws.api.Twister2Submitter;
import edu.iu.dsc.tws.api.basic.job.BasicJob;
import edu.iu.dsc.tws.api.net.Network;
import edu.iu.dsc.tws.common.config.Config;
import edu.iu.dsc.tws.common.discovery.IWorkerDiscoverer;
import edu.iu.dsc.tws.comms.api.MessageType;
import edu.iu.dsc.tws.comms.api.TWSChannel;
import edu.iu.dsc.tws.comms.core.TaskPlan;
import edu.iu.dsc.tws.comms.dfw.DataFlowPartition;
import edu.iu.dsc.tws.comms.dfw.io.partition.PartitionBatchFinalReceiver;
import edu.iu.dsc.tws.comms.dfw.io.partition.PartitionPartialReceiver;
import edu.iu.dsc.tws.comms.op.EdgeGenerator;
import edu.iu.dsc.tws.comms.op.OperationSemantics;
import edu.iu.dsc.tws.examples.utils.WordCountUtils;
import edu.iu.dsc.tws.rsched.core.ResourceAllocator;
import edu.iu.dsc.tws.rsched.core.SchedulerContext;
import edu.iu.dsc.tws.rsched.spi.container.IPersistentVolume;
import edu.iu.dsc.tws.rsched.spi.container.IVolatileVolume;
import edu.iu.dsc.tws.rsched.spi.container.IWorker;
import edu.iu.dsc.tws.rsched.spi.resource.ResourceContainer;
import edu.iu.dsc.tws.rsched.spi.resource.ResourcePlan;

public class SortJob implements IWorker {
  private static final Logger LOG = Logger.getLogger(SortJob.class.getName());

  private DataFlowPartition partition;

  private TWSChannel channel;

  private static final int NO_OF_TASKS = 4;

  private Config config;

  private ResourcePlan resourcePlan;

  private int id;

  private int noOfTasksPerExecutor;

  private Set<Integer> sources;
  private Set<Integer> destinations;
  private TaskPlan taskPlan;

  @Override
  public void init(Config cfg, int wID, ResourcePlan plan,
                   IWorkerDiscoverer workerController,
                   IPersistentVolume persistentVolume,
                   IVolatileVolume volatileVolume) {
    this.config = cfg;
    this.resourcePlan = plan;
    this.id = wID;
    // setup the network
    setupNetwork(cfg, workerController, plan);

//    // wait and get all workers
//    List<WorkerNetworkInfo> workerList = workerController.waitForAllWorkersToJoin(50000);
//    if (workerList != null) {
//      LOG.info("All workers joined. " + WorkerNetworkInfo.workerListAsString(workerList));
//    } else {
//      LOG.severe(
//              "Can not get all workers to join. Something wrong. Exiting the Worker..........");
//      return;
//    }

    // set up the tasks
    setupTasks();
    // we get the number of containers after initializing the network
    this.noOfTasksPerExecutor = NO_OF_TASKS / plan.noOfContainers();

    partition = new DataFlowPartition(config, channel, taskPlan, sources, destinations,
        new PartitionBatchFinalReceiver(new RecordSave(), false, true,
            new IntegerComparator()),
        new PartitionPartialReceiver(), DataFlowPartition.PartitionStratergy.DIRECT,
        MessageType.BYTE, MessageType.BYTE, MessageType.INTEGER, MessageType.INTEGER,
        OperationSemantics.STREAMING_BATCH, new EdgeGenerator(0));
    // start the threads
    scheduleTasks();
    // progress the work
    progress();
  }

  private void setupTasks() {
    taskPlan = WordCountUtils.createWordCountPlan(config, resourcePlan, NO_OF_TASKS);
    sources = new HashSet<>();
    for (int i = 0; i < NO_OF_TASKS / 2; i++) {
      sources.add(i);
    }
    destinations = new HashSet<>();
    for (int i = 0; i < NO_OF_TASKS / 2; i++) {
      destinations.add(NO_OF_TASKS / 2 + i);
    }
  }

  private void scheduleTasks() {
    if (id < NO_OF_TASKS / 2) {
      for (int i = 0; i < noOfTasksPerExecutor; i++) {
        // the map thread where data is produced
        Thread mapThread = new Thread(new RecordSource(config, partition,
            new ArrayList<>(destinations), id, 1000, 10000,
            NO_OF_TASKS / 2));
        mapThread.start();
      }
    }
  }

  private void setupNetwork(Config cfg, IWorkerDiscoverer controller, ResourcePlan rPlan) {
    channel = Network.initializeChannel(cfg, controller, rPlan);
  }

  private class IntegerComparator implements Comparator<Object> {
    @Override
    public int compare(Object o1, Object o2) {
      int[] o11 = (int[]) o1;
      int[] o21 = (int[]) o2;
      try {
        return Integer.compare(o11[0], o21[0]);
      } catch (ArrayIndexOutOfBoundsException e) {
        LOG.info("Sizes of keys: " + o11.length + " " + o21.length);
        throw new RuntimeException("Err", e);
      }
    }
  }

  private void progress() {
    // we need to progress the communication
    while (true) {
      try {
        // progress the channel
        channel.progress();
        // we should progress the communication directive
        partition.progress();
      } catch (Throwable t) {
        LOG.log(Level.SEVERE, "Something bad happened", t);
      }
    }
  }

  public static void main(String[] args) {
    // first load the configurations from command line and config files
    Config config = ResourceAllocator.loadConfig(new HashMap<>());

    // build JobConfig
    HashMap<String, Object> configurations = new HashMap<>();
    configurations.put(SchedulerContext.THREADS_PER_WORKER, 8);

    // build JobConfig
    JobConfig jobConfig = new JobConfig();
    jobConfig.putAll(configurations);

    BasicJob.BasicJobBuilder jobBuilder = BasicJob.newBuilder();
    jobBuilder.setName("sort-job");
    jobBuilder.setContainerClass(SortJob.class.getName());
    jobBuilder.setRequestResource(new ResourceContainer(2, 1024), NO_OF_TASKS);
    jobBuilder.setConfig(jobConfig);

    // now submit the job
    Twister2Submitter.submitContainerJob(jobBuilder.build(), config);
  }
}
