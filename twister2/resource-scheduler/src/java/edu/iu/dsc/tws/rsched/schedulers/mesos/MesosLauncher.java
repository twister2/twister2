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

package edu.iu.dsc.tws.rsched.schedulers.mesos;

import java.util.logging.Logger;

import org.apache.mesos.MesosSchedulerDriver;
import org.apache.mesos.Protos;
import org.apache.mesos.Scheduler;

import edu.iu.dsc.tws.common.config.Config;
import edu.iu.dsc.tws.proto.system.job.JobAPI;
import edu.iu.dsc.tws.rsched.spi.resource.RequestedResources;
import edu.iu.dsc.tws.rsched.spi.scheduler.ILauncher;


/**
 * Launch a topology to mesos cluster
 */
public class MesosLauncher implements ILauncher {



  //private String frameworkName;
  private MesosController controller;
  //private String executorName = "MesosWorker";
  //private String workerClass;
  //private static String command;
  //private static String jobURI;
  private MesosSchedulerDriver driver;




  private static final Logger LOG = Logger.getLogger(MesosLauncher.class.getName());
  private Config config;

  @Override
  public void initialize(Config mconfig) {
    this.config = mconfig;
    //frameworkName = MesosContext.MesosFrameworkName(config);
    this.controller = new MesosController(config);
    //jobURI = MesosContext.jobURI(config);
    //workerClass = MesosContext.mesosWorkerClass(config);


    /*HashMap<String, String> envs = new HashMap<String, String>();
    envs.put("jobname","deneme"); */



    //jobURI = MesosContext.jobURI(config);
    //System.out.println("=============Command:" + command);

  }

  @Override
  public void close() {

  }

  @Override
  public boolean terminateJob(String jobName) {
    //Protos.Status status = driver.stop();
    boolean status;
    if (driver == null) {
      status = true;
      System.out.println("Job already terminated!");
    } else {
      status = driver.stop() == Protos.Status.DRIVER_STOPPED ? false : true;
    }
    return status;
  }

  @Override
  public boolean launch(RequestedResources resourceRequest, JobAPI.Job job) {

    runFramework(MesosContext.getMesosMasterUri(config), job.getJobName());

    return false;
  }

  private void runFramework(String mesosMaster, String jobName) {
    //Protos.Credential.Builder credentialbuilder = Protos.Credential.newBuilder()
      //  .setPrincipal("principal1");
    //ByteString byteString = ByteString.copyFrom("secret1".getBytes());
    //credentialbuilder.setSecret(byteString);
    //String pass = System.getenv("MESOS_EXAMPLE_SECRET");
   // System.out.println("pass is : " + pass);
    Scheduler scheduler = new MesosScheduler(controller, config, jobName);
    driver = new MesosSchedulerDriver(scheduler, controller.getFrameworkInfo(),
        mesosMaster);
    int status = driver.run() == Protos.Status.DRIVER_STOPPED ? 0 : 1;

    //driver.stop();
    //System.exit(status);
  }

}
