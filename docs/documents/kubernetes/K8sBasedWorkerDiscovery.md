# Kubernetes Master Based Worker Discovery
Ahmet Uyar

We developed a worker discoverer that watches the pods in Kubernetes clusters by listening to 
Kubernetes master. It provides the following services to workers in a Twister2 job: 
* Unique ID assignment to workers
* Worker address discovery 
* Getting the IP addresses after the worker pods started running

The worker discoverer class is: 
* [edu.iu.dsc.tws.rsched.schedulers.k8s.worker.WorkerDiscoverer](../../../twister2/resource-scheduler/src/java/edu/iu/dsc/tws/rsched/schedulers/k8s/worker/WorkerDiscoverer.java)

It implements interface: 
* [edu.iu.dsc.tws.common.discovery.IWorkerDiscoverer](../../../twister2/common/src/java/edu/iu/dsc/tws/common/discovery/IWorkerDiscoverer.java)

## Unique ID Assignment to Workers 
Worker IDs in a Twister2 job start from 0 and increase sequentially without any gaps in between. 

Each pod in Twister2 jobs has a unique name with a unique integer extension. 
When there are n pods in a job (StatefulSet), each pod name is composed of <jobname>-<index>. 
Index starts from zero and increases sequentially. Kubernetes master assigns these pod indexes.
Each container is also assigned a name that is similar to the pod names. 
Container names are in the form of <twister2-container>-<index>. 
Again the index starts from zero and increases sequentially in each pod. 
While pod indexes are assigned by Kubernetes master, we assign the container name indexes 
when we create the StatefulSets. 
 
We construct unique worker IDs by using pod name and container name indexes. 
The worker ID generated by the formula: 

    workerID = (podIndex x ContainersPerPod) + containerIndex
    
## Worker IP and Port Numbers
Each worker needs to have a unique <IP>:<Port> combination. Currently each worker has one TCP port. 
More ports can be added later on as needed. 

Each worker uses the IP addresses of the pod it is running in as its IP address. 
IP address of each pod can be either get by using localhost or 
sent to the containers as an environment variable using downward API call, 
when submitting the job. 

The port number of each non-MPI enabled worker is calculated as the basePort + containerIndex. 
When there are multiple containers in a pod, each container has an increasing index starting from 0. 

The port number of MPI enabled workers are calculate as the basePort + MPI ranking. 

### Watching Workers to Start
Each worker watches pod running events and when a pod reaches to “Running” phase, 
they assume that all workers in that pod started running. 
This is not %100 accurate. It is a close assumption.  
A pod may have started but some workers in that may have not started yet or stalled at some point.
This worker discoverer does not distinguish pod Running and worker starts.
 
Job Master based worker discoverer provides a more precise discovery method. 
In that case, each worker reports its status. Therefore, it is more accurate. 
The advantage of this one is that it works in the absence of the Job Master also.  
