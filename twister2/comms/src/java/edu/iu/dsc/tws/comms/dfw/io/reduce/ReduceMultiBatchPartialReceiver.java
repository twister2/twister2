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
package edu.iu.dsc.tws.comms.dfw.io.reduce;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.iu.dsc.tws.common.config.Config;
import edu.iu.dsc.tws.comms.api.DataFlowOperation;
import edu.iu.dsc.tws.comms.api.MultiMessageReceiver;
import edu.iu.dsc.tws.comms.api.ReduceFunction;

public class ReduceMultiBatchPartialReceiver implements MultiMessageReceiver {
  private ReduceFunction reduceFunction;

  private Map<Integer, ReduceBatchPartialReceiver> receiverMap = new HashMap<>();

  public ReduceMultiBatchPartialReceiver(ReduceFunction reduceFn) {
    this.reduceFunction = reduceFn;
  }

  @Override
  public void init(Config cfg, DataFlowOperation op,
                   Map<Integer, Map<Integer, List<Integer>>> expectedIds) {
    for (Map.Entry<Integer, Map<Integer, List<Integer>>> e : expectedIds.entrySet()) {
      ReduceBatchPartialReceiver finalReceiver =
          new ReduceBatchPartialReceiver(e.getKey(), reduceFunction);
      receiverMap.put(e.getKey(), finalReceiver);
      finalReceiver.init(cfg, op, e.getValue());
    }
  }

  @Override
  public boolean onMessage(int source, int path, int target, int flags, Object object) {
    ReduceBatchPartialReceiver finalReceiver = receiverMap.get(path);
    return finalReceiver.onMessage(source, path, target, flags, object);
  }

  @Override
  public void progress() {
    for (Map.Entry<Integer, ReduceBatchPartialReceiver> e : receiverMap.entrySet()) {
      e.getValue().progress();
    }
  }
}
