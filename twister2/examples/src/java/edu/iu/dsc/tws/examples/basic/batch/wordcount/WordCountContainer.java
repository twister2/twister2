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
package edu.iu.dsc.tws.examples.basic.batch.wordcount;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.iu.dsc.tws.common.config.Config;
import edu.iu.dsc.tws.comms.api.MessageType;
import edu.iu.dsc.tws.comms.core.TWSCommunication;
import edu.iu.dsc.tws.comms.core.TWSNetwork;
import edu.iu.dsc.tws.comms.core.TaskPlan;
import edu.iu.dsc.tws.comms.dfw.DataFlowMultiGather;
import edu.iu.dsc.tws.comms.dfw.io.gather.GatherMultiBatchFinalReceiver;
import edu.iu.dsc.tws.comms.dfw.io.gather.GatherMultiBatchPartialReceiver;
import edu.iu.dsc.tws.examples.utils.WordCountUtils;
import edu.iu.dsc.tws.rsched.spi.container.IContainer;
import edu.iu.dsc.tws.rsched.spi.resource.ResourcePlan;

public class WordCountContainer implements IContainer {
  private static final Logger LOG = Logger.getLogger(WordCountContainer.class.getName());

  private DataFlowMultiGather keyGather;

  private TWSNetwork network;

  private TWSCommunication channel;

  private static final int NO_OF_TASKS = 16;

  private Config config;

  private ResourcePlan resourcePlan;

  private int id;

  private int noOfTasksPerExecutor;

  private Set<Integer> sources;
  private Set<Integer> destinations;
  private TaskPlan taskPlan;

  @Override
  public void init(Config cfg, int containerId, ResourcePlan plan) {
    this.config = cfg;
    this.resourcePlan = plan;
    this.id = containerId;
    this.noOfTasksPerExecutor = NO_OF_TASKS / plan.noOfContainers();

    // set up the tasks
    setupTasks();
    // setup the network
    setupNetwork();
    // create the communication
    Map<String, Object> newCfg = new HashMap<>();
    keyGather = (DataFlowMultiGather) channel.keyedGather(newCfg, MessageType.OBJECT,
        destinations, sources, destinations,
        new GatherMultiBatchFinalReceiver(new WordAggregator()),
        new GatherMultiBatchPartialReceiver());
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
    if (id < 2) {
      for (int i = 0; i < noOfTasksPerExecutor; i++) {
        // the map thread where data is produced
        Thread mapThread = new Thread(new BatchWordSource(config, keyGather, 1000,
            new ArrayList<>(destinations), noOfTasksPerExecutor * id + i, 200));
        mapThread.start();
      }
    }
  }

  private void setupNetwork() {
    network = new TWSNetwork(config, taskPlan);
    channel = network.getDataFlowTWSCommunication();

    //first get the communication config file
    network = new TWSNetwork(config, taskPlan);
    channel = network.getDataFlowTWSCommunication();
  }

  private void progress() {
    // we need to progress the communication
    while (true) {
      try {
        // progress the channel
        channel.progress();
        // we should progress the communication directive
        keyGather.progress();
      } catch (Throwable t) {
        LOG.log(Level.SEVERE, "Something bad happened", t);
      }
    }
  }
}
