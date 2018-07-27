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
package edu.iu.dsc.tws.examples.basic.memory;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.tuple.ImmutablePair;

import edu.iu.dsc.tws.common.config.Config;
import edu.iu.dsc.tws.comms.api.BatchReceiver;
import edu.iu.dsc.tws.comms.api.DataFlowOperation;
import edu.iu.dsc.tws.comms.api.MessageFlags;
import edu.iu.dsc.tws.comms.api.MessageType;
import edu.iu.dsc.tws.comms.core.TWSCommunication;
import edu.iu.dsc.tws.comms.core.TWSNetwork;
import edu.iu.dsc.tws.comms.core.TaskPlan;
import edu.iu.dsc.tws.comms.dfw.io.KeyedContent;
import edu.iu.dsc.tws.comms.dfw.io.gather.GatherBatchFinalReceiver;
import edu.iu.dsc.tws.comms.dfw.io.gather.GatherBatchPartialReceiver;
import edu.iu.dsc.tws.examples.Utils;
import edu.iu.dsc.tws.examples.utils.RandomString;
import edu.iu.dsc.tws.rsched.spi.container.IContainer;
import edu.iu.dsc.tws.rsched.spi.resource.ResourcePlan;

public class BasicMemoryManagerByteKeyedGatherCommunication implements IContainer {
  private static final Logger LOG = Logger.
      getLogger(BasicMemoryManagerByteKeyedGatherCommunication.class.getName());

  private DataFlowOperation aggregate;

  private ResourcePlan resourcePlan;

  private int id;

  private Config config;

  private static final int NO_OF_TASKS = 8;

  private int noOfTasksPerExecutor = 2;

  private RandomString randomString;

  private long startTime = 0;

  @Override
  public void init(Config cfg, int containerId, ResourcePlan plan) {
    LOG.log(Level.INFO, "Starting the example with container id: " + plan.getThisId());

    this.config = cfg;
    this.resourcePlan = plan;
    this.id = containerId;
    this.noOfTasksPerExecutor = NO_OF_TASKS / plan.noOfContainers();
    this.randomString = new RandomString(128000, new Random(), RandomString.ALPHANUM);

    // lets create the task plan
    TaskPlan taskPlan = Utils.createReduceTaskPlan(cfg, plan, NO_OF_TASKS);
    //first get the communication config file
    TWSNetwork network = new TWSNetwork(cfg, taskPlan);

    TWSCommunication channel = network.getDataFlowTWSCommunication();

    Set<Integer> sources = new HashSet<>();
    for (int i = 0; i < NO_OF_TASKS; i++) {
      sources.add(i);
    }
    int dest = NO_OF_TASKS;

    Map<String, Object> newCfg = new HashMap<>();

    LOG.info("Setting up keyed gather MM dataflow operation");

    try {
      // this method calls the init method
      // I think this is wrong

      aggregate = channel.gather(newCfg, MessageType.BYTE, MessageType.BYTE, 0, sources,
          dest, new GatherBatchFinalReceiver(new FinalGatherReceive()),
          new GatherBatchPartialReceiver(dest));
//      aggregate.setMemoryMapped(true);

//      aggregate = channel.gather(newCfg, MessageType.OBJECT, 0, sources,
//          dest, new FinalGatherReceive());

      for (int i = 0; i < noOfTasksPerExecutor; i++) {
        // the map thread where data is produced
        LOG.info(String.format("%d Starting %d", id, i + id * noOfTasksPerExecutor));
        Thread mapThread = new Thread(new MapWorker(i + id * noOfTasksPerExecutor));
        mapThread.start();
      }
      // we need to progress the communication
      while (true) {
        try {
          // progress the channel
          channel.progress();
          // we should progress the communication directive
          aggregate.progress();
          Thread.yield();
        } catch (Throwable t) {
          t.printStackTrace();
        }
      }
    } catch (Throwable t) {
      t.printStackTrace();
    }
  }

  /**
   * We are running the map in a separate thread
   */
  private class MapWorker implements Runnable {
    private int task = 0;
    private int sendCount = 0;

    MapWorker(int task) {
      this.task = task;
    }

    @Override
    public void run() {
      try {
        LOG.log(Level.INFO, "Starting map worker: " + id);
//      MPIBuffer data = new MPIBuffer(1024);
        startTime = System.nanoTime();
        for (int i = 0; i < 1; i++) {
//          byte[] data = ByteBuffer.allocate(8).putInt(task).putInt(task * 100).array();
          byte[] data = new byte[12];
          data[0] = 'a';
          data[1] = 'b';
          data[2] = 'c';
          data[3] = 'd';
          data[4] = 'd';
          data[5] = 'd';
          data[6] = 'd';
          data[7] = 'd';
          int keyint = task * 111;
          byte[] key = ByteBuffer.allocate(4).putInt(keyint).array();

          // lets generate a message
//          KeyedContent mesage = new KeyedContent(task, data,
//              MessageType.INTEGER, MessageType.OBJECT);
//
          int flags = MessageFlags.FLAGS_LAST;
          KeyedContent mesage = new KeyedContent(key, data,
              MessageType.BYTE, MessageType.BYTE);
          while (!aggregate.send(task, mesage, flags)) {
            // lets wait a litte and try again
            try {
              Thread.sleep(1);
            } catch (InterruptedException e) {
              e.printStackTrace();
            }
          }
          Thread.yield();
        }
        LOG.info(String.format("%d Done sending", id));
      } catch (Throwable t) {
        t.printStackTrace();
      }
    }
  }

  private class FinalGatherReceive implements BatchReceiver {
    // lets keep track of the messages
    // for each task we need to keep track of incoming messages
    private List<Integer> dataList;

    private int count = 0;

    private long start = System.nanoTime();

    @Override
    public void init(Config cfg, DataFlowOperation op, Map<Integer, List<Integer>> expectedIds) {
      dataList = new ArrayList<Integer>();
    }

    @Override
    @SuppressWarnings("unchecked")
    public void receive(int target, Iterator<Object> it) {
      int itercount = 0;
      Object temp;
      while (it.hasNext()) {
        itercount++;
        temp = it.next();
        if (temp instanceof ImmutablePair) {
          ImmutablePair<Object, Object> data = (ImmutablePair<Object, Object>) temp;
          if (data.getValue() instanceof List) {
            byte[] tempData = (byte[]) ((List) data.getValue()).get(0);
            LOG.info("Ordered results for keyed gather : "
                + ByteBuffer.wrap((byte[]) data.getKey()).getInt()
                + " : " + Arrays.toString(tempData));
          } else {
            LOG.info("Ordered results for keyed gather : "
                + ByteBuffer.wrap((byte[]) data.getKey()).getInt()
                + " : " + Arrays.toString((byte[]) data.getValue()));
          }
        }
      }

    }

    @SuppressWarnings("unchecked")
    public boolean onMessage(int source, int path, int target, int flags, Object object) {

      // add the object to the map

//      boolean canAdd = true;
//      if (count == 0) {
//        start = System.nanoTime();
//      }
//      if (object instanceof List) {
//        System.out.println("LIST LIST LIST");
//        List<Object> datalist = (List<Object>) object;
//        for (Object o : datalist) {
//          int[] data = (int[]) o;
//          dataList.add(data[0]);
//        }
//      } else {
//        int[] data = (int[]) object;
//        dataList.add(data[0]);
//      }
      LOG.info("Gather results (only the first int of each array)"
          + Arrays.toString(dataList.toArray()));

      return true;
    }

    public void progress() {

    }
  }
}

