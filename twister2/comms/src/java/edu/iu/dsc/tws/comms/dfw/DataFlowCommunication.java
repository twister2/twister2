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
package edu.iu.dsc.tws.comms.dfw;

import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import edu.iu.dsc.tws.common.config.Config;
import edu.iu.dsc.tws.comms.api.CompletionListener;
import edu.iu.dsc.tws.comms.api.DataFlowOperation;
import edu.iu.dsc.tws.comms.api.MessageReceiver;
import edu.iu.dsc.tws.comms.api.MessageType;
import edu.iu.dsc.tws.comms.api.MultiMessageReceiver;
import edu.iu.dsc.tws.comms.api.ReduceFunction;
import edu.iu.dsc.tws.comms.api.ReduceReceiver;
import edu.iu.dsc.tws.comms.api.TWSChannel;
import edu.iu.dsc.tws.comms.core.TWSCommunication;
import edu.iu.dsc.tws.comms.core.TaskPlan;

public class DataFlowCommunication implements TWSCommunication {
  private static final Logger LOG = Logger.getLogger(DataFlowCommunication.class.getName());

  private TWSChannel channel;

  /**
   * The configuration read from the configuration file
   */
  protected Config config;

  /**
   * Instance plan containing mappings from communication specific ids to higher level task ids
   */
  protected TaskPlan instancePlan;


  public DataFlowCommunication() {
  }

  @Override
  public void init(Config cfg, TaskPlan taskPlan, TWSChannel ch) {
    this.instancePlan = taskPlan;
    this.config = cfg;
    this.channel = ch;
  }

  @Override
  public void progress() {
    channel.progress();
  }

  public DataFlowOperation reduce(Map<String, Object> properties, MessageType type, int edge,
                                  Set<Integer> sourceTasks, int destTask,
                                  MessageReceiver reduceReceiver, MessageReceiver partialReceiver) {
    // merge with the user specified configuration, user specified will take precedence
    Config mergedCfg = Config.newBuilder().putAll(config).putAll(properties).build();

    // create the dataflow operation
    DataFlowReduce dataFlowOperation = new DataFlowReduce(channel, sourceTasks,
        destTask, reduceReceiver, partialReceiver);

    // intialize the operation
    dataFlowOperation.init(mergedCfg, type, instancePlan, edge);

    return dataFlowOperation;
  }

  public DataFlowOperation reduce(Map<String, Object> properties, MessageType type, int edge,
                                  Set<Integer> sourceTasks, int destTask,
                                  MessageReceiver reduceReceiver, MessageReceiver partialReceiver,
                                  CompletionListener compListener) {
    // merge with the user specified configuration, user specified will take precedence
    Config mergedCfg = Config.newBuilder().putAll(config).putAll(properties).build();

    // create the dataflow operation
    DataFlowReduce dataFlowOperation = new DataFlowReduce(channel, sourceTasks,
        destTask, reduceReceiver, partialReceiver, compListener);

    // intialize the operation
    dataFlowOperation.init(mergedCfg, type, instancePlan, edge);

    return dataFlowOperation;
  }

  public DataFlowOperation broadCast(Map<String, Object> properties, MessageType type, int edge,
                                     int sourceTask, Set<Integer> destTasks,
                                     MessageReceiver receiver) {
    // merge with the user specified configuration, user specified will take precedence
    Config mergedCfg = Config.newBuilder().putAll(config).putAll(properties).build();

    // create the dataflow operation
    DataFlowBroadcast dataFlowOperation = new DataFlowBroadcast(channel, sourceTask,
        destTasks, receiver);

    // intialize the operation
    dataFlowOperation.init(mergedCfg, type, instancePlan, edge);
    return dataFlowOperation;
  }

  public DataFlowOperation direct(Map<String, Object> properties, MessageType type, int edge,
                                  Set<Integer> sourceTasks, int destTask,
                                  MessageReceiver receiver) {
    // merge with the user specified configuration, user specified will take precedence
    Config mergedCfg = Config.newBuilder().putAll(config).putAll(properties).build();

    // create the dataflow operation
    DataFlowDirect dataFlowOperation = new DataFlowDirect(channel,
        sourceTasks, destTask, receiver);

    // intialize the operation
    dataFlowOperation.init(mergedCfg, type, instancePlan, edge);
    return dataFlowOperation;
  }

  public DataFlowOperation loadBalance(Map<String, Object> properties, MessageType type, int edge,
                                       Set<Integer> sourceTasks, Set<Integer> destTasks,
                                       MessageReceiver receiver) {
    // merge with the user specified configuration, user specified will take precedence
    Config mergedCfg = Config.newBuilder().putAll(config).putAll(properties).build();

    // create the dataflow operation
    DataFlowLoadBalance dataFlowOperation = new DataFlowLoadBalance(channel,
        sourceTasks, destTasks, receiver);

    // intialize the operation
    dataFlowOperation.init(mergedCfg, type, instancePlan, edge);
    return dataFlowOperation;
  }

  public DataFlowOperation keyedReduce(Map<String, Object> properties, MessageType type,
                                       Set<Integer> edge,
                                       Set<Integer> sourceTasks, Set<Integer> destTasks,
                                       MultiMessageReceiver receiver,
                                       MultiMessageReceiver partial) {
    // merge with the user specified configuration, user specified will take precedence
    Config mergedCfg = Config.newBuilder().putAll(config).putAll(properties).build();

    // create the dataflow operation
    DataFlowMultiReduce dataFlowOperation = new DataFlowMultiReduce(channel,
        sourceTasks, destTasks, receiver, partial, edge);

    // intialize the operation
    dataFlowOperation.init(mergedCfg, type, instancePlan, 0);
    return dataFlowOperation;
  }

  public DataFlowOperation allReduce(Map<String, Object> properties, MessageType type,
                                     int edge1, int edge2,
                                     Set<Integer> sourceTasks, Set<Integer> destTasks,
                                     int middleTask,
                                     ReduceFunction reduceFunction,
                                     ReduceReceiver receiver,
                                     boolean stream) {
    // merge with the user specified configuration, user specified will take precedence
    Config mergedCfg = Config.newBuilder().putAll(config).putAll(properties).build();
    // create the dataflow operation
    DataFlowAllReduce dataFlowOperation = new DataFlowAllReduce(channel,
        sourceTasks, destTasks, middleTask, reduceFunction, receiver, edge1, edge2, stream);
    // intialize the operation
    dataFlowOperation.init(mergedCfg, type, instancePlan, 0);
    return dataFlowOperation;
  }

  public DataFlowOperation gather(Map<String, Object> properties, MessageType type,
                                  int edge1,
                                  Set<Integer> sourceTasks, int destTask,
                                  MessageReceiver receiver) {
    // merge with the user specified configuration, user specified will take precedence
    Config mergedCfg = Config.newBuilder().putAll(config).putAll(properties).build();

    // create the dataflow operation
    DataFlowGather dataFlowOperation = new DataFlowGather(channel,
        sourceTasks, destTask, receiver, 0, 0, mergedCfg, type, instancePlan, edge1);

    // intialize the operation
    dataFlowOperation.init(mergedCfg, type, instancePlan, edge1);
    return dataFlowOperation;
  }

  @Override
  public DataFlowOperation gather(Map<String, Object> properties, MessageType type,
                                  MessageType keyType, int edge1, Set<Integer> sourceTasks,
                                  int destTask, MessageReceiver receiver) {
    // merge with the user specified configuration, user specified will take precedence
    Config mergedCfg = Config.newBuilder().putAll(config).putAll(properties).build();

    // create the dataflow operation
    DataFlowGather dataFlowOperation = new DataFlowGather(channel,
        sourceTasks, destTask, receiver, 0, 0, mergedCfg, type, keyType, instancePlan, edge1);

    // intialize the operation
    dataFlowOperation.init(mergedCfg, type, instancePlan, edge1);
    return dataFlowOperation;
  }

  @Override
  public DataFlowOperation gather(Map<String, Object> properties, MessageType type,
                                  MessageType keyType, int edge1, Set<Integer> sourceTasks,
                                  int destTask, MessageReceiver receiver,
                                  MessageReceiver partialRecvr) {
    // merge with the user specified configuration, user specified will take precedence
    Config mergedCfg = Config.newBuilder().putAll(config).putAll(properties).build();

    // create the dataflow operation
    DataFlowGather dataFlowOperation = new DataFlowGather(channel, sourceTasks, destTask,
        receiver, partialRecvr, 0, 0, mergedCfg, type, keyType, instancePlan, edge1);

    // intialize the operation
    dataFlowOperation.init(mergedCfg, type, instancePlan, edge1);
    return dataFlowOperation;
  }

  @Override
  public DataFlowOperation gather(Map<String, Object> properties, MessageType type,
                                  int edge1, Set<Integer> sourceTasks, int destTask,
                                  MessageReceiver receiver, MessageReceiver partialRecvr) {
    // merge with the user specified configuration, user specified will take precedence
    Config mergedCfg = Config.newBuilder().putAll(config).putAll(properties).build();

    // create the dataflow operation
    DataFlowGather dataFlowOperation = new DataFlowGather(channel,
        sourceTasks, destTask, receiver, partialRecvr, 0, 0, mergedCfg, type, instancePlan, edge1);

    // intialize the operation
    dataFlowOperation.init(mergedCfg, type, instancePlan, edge1);
    return dataFlowOperation;
  }

  @Override
  public DataFlowOperation allGather(Map<String, Object> properties, MessageType type,
                                     int edge1, int edge2,
                                     Set<Integer> sourceTasks, Set<Integer> destTasks,
                                     int middleTask,
                                     MessageReceiver finalRecvr) {
    // merge with the user specified configuration, user specified will take precedence
    Config mergedCfg = Config.newBuilder().putAll(config).putAll(properties).build();

    // create the dataflow operation
    DataFlowAllGather dataFlowOperation = new DataFlowAllGather(channel,
        sourceTasks, destTasks, middleTask, finalRecvr, edge1, edge2);

    // intialize the operation
    dataFlowOperation.init(mergedCfg, type, instancePlan, edge1);
    return dataFlowOperation;
  }

  @Override
  public DataFlowOperation keyedGather(Map<String, Object> properties, MessageType type,
                                       Set<Integer> edge,
                                       Set<Integer> sourceTasks, Set<Integer> destTasks,
                                       MultiMessageReceiver receiver) {
    // merge with the user specified configuration, user specified will take precedence
    Config mergedCfg = Config.newBuilder().putAll(config).putAll(properties).build();

    // create the dataflow operation
    DataFlowMultiGather dataFlowOperation = new DataFlowMultiGather(channel,
        sourceTasks, destTasks, receiver, edge);

    // intialize the operation
    dataFlowOperation.init(mergedCfg, type, instancePlan, 0);
    return dataFlowOperation;
  }

  @Override
  public DataFlowOperation keyedGather(Map<String, Object> properties, MessageType type,
                                       Set<Integer> edge,
                                       Set<Integer> sourceTasks, Set<Integer> destTasks,
                                       MultiMessageReceiver receiver,
                                       MultiMessageReceiver partialRecvr) {
    // merge with the user specified configuration, user specified will take precedence
    Config mergedCfg = Config.newBuilder().putAll(config).putAll(properties).build();

    // create the dataflow operation
    DataFlowMultiGather dataFlowOperation = new DataFlowMultiGather(channel,
        sourceTasks, destTasks, receiver, partialRecvr, edge);

    // intialize the operation
    dataFlowOperation.init(mergedCfg, type, instancePlan, 0);
    return dataFlowOperation;
  }

  @Override
  public DataFlowOperation partition(Map<String, Object> properties, MessageType type, int edge1,
                                     Set<Integer> sourceTasks, Set<Integer> destTasks,
                                     MessageReceiver finalRcvr, MessageReceiver partialRcvr) {
    // merge with the user specified configuration, user specified will take precedence
    Config mergedCfg = Config.newBuilder().putAll(config).putAll(properties).build();

    DataFlowPartition dataFlowOperation = new DataFlowPartition(channel,
        sourceTasks, destTasks, finalRcvr, partialRcvr,
        DataFlowPartition.PartitionStratergy.DIRECT);

    dataFlowOperation.init(mergedCfg, type, instancePlan, edge1);
    return dataFlowOperation;
  }

  @Override
  public DataFlowOperation partition(Map<String, Object> properties, MessageType type, int edge1,
                                     Set<Integer> sourceTasks, Set<Integer> destTasks,
                                     MessageReceiver finalRcvr, MessageReceiver partialRcvr,
                                     CompletionListener cmpListener) {
    // merge with the user specified configuration, user specified will take precedence
    Config mergedCfg = Config.newBuilder().putAll(config).putAll(properties).build();

    DataFlowPartition dataFlowOperation = new DataFlowPartition(channel,
        sourceTasks, destTasks, finalRcvr, partialRcvr,
        DataFlowPartition.PartitionStratergy.DIRECT, cmpListener);

    dataFlowOperation.init(mergedCfg, type, instancePlan, edge1);
    return dataFlowOperation;
  }

  @Override
  public DataFlowOperation partition(Map<String, Object> properties, MessageType type,
                                     MessageType keyType, int edge1,
                                     Set<Integer> sourceTasks, Set<Integer> destTasks,
                                     MessageReceiver finalRcvr, MessageReceiver partialRcvr) {
    // merge with the user specified configuration, user specified will take precedence
    Config mergedCfg = Config.newBuilder().putAll(config).putAll(properties).build();

    DataFlowPartition dataFlowOperation = new DataFlowPartition(channel,
        sourceTasks, destTasks, finalRcvr, partialRcvr,
        DataFlowPartition.PartitionStratergy.DIRECT,
        type, keyType);

    dataFlowOperation.init(mergedCfg, type, instancePlan, edge1);
    return dataFlowOperation;
  }
}
