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
package edu.iu.dsc.tws.comms.dfw.io.gather;

import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.iu.dsc.tws.common.config.Config;
import edu.iu.dsc.tws.comms.api.BatchReceiver;
import edu.iu.dsc.tws.comms.api.DataFlowOperation;

public class StreamingFinalGatherReceiver extends StreamingPartialGatherReceiver {
  private static final Logger LOG = Logger.getLogger(StreamingFinalGatherReceiver.class.getName());

  private BatchReceiver receiver;

  public StreamingFinalGatherReceiver(BatchReceiver receiver) {
    this.receiver = receiver;
  }

  @Override
  public void init(Config cfg, DataFlowOperation op, Map<Integer, List<Integer>> expectedIds) {
    super.init(cfg, op, expectedIds);
    receiver.init(cfg, op, expectedIds);
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  @Override
  protected boolean handleMessage(int task, Object message, int flags, int dest) {
    if (message instanceof List) {
      receiver.receive(task, ((List<Object>) message).iterator());
    } else {
      LOG.log(Level.WARNING, "Messages should be in list format");
      return false;
    }
    return true;
  }
}
