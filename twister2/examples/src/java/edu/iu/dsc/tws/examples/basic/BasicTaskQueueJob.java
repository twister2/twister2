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
package edu.iu.dsc.tws.examples.basic;

//import java.util.HashMap;
//
//import edu.iu.dsc.tws.api.JobConfig;
//import edu.iu.dsc.tws.api.Twister2Submitter;
//import edu.iu.dsc.tws.api.basic.job.BasicJob;
//import edu.iu.dsc.tws.common.config.Config;
//import edu.iu.dsc.tws.examples.SimpleTaskQueue;
//import edu.iu.dsc.tws.rsched.core.ResourceAllocator;
//import edu.iu.dsc.tws.rsched.spi.resource.ResourceContainer;
//
///**
// * Created by vibhatha on 2/13/18.
// */
//public final class BasicTaskQueueJob {
//  private BasicTaskQueueJob() {
//
//  }
//
//  public static void main(String[] args) {
//    // first load the configurations from command line and config files
//
//    Config config = ResourceAllocator.loadConfig(new HashMap<>());
//
//    // build JobConfig
//    JobConfig jobConfig = new JobConfig();
//
//    // build the job
//    BasicJob basicJob = BasicJob.newBuilder()
//        .setName("basic-taskqueue")
//        .setContainerClass(SimpleTaskQueue.class.getName())
//        .setRequestResource(new ResourceContainer(2, 1024), 2)
//        .setConfig(jobConfig)
//        .build();
//
//    // now submit the job
//    Twister2Submitter.submitContainerJob(basicJob, config);
//  }
//
//}
