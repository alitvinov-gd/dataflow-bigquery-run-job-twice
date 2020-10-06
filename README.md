# A dataflow job created the second time from the same template.
Seems being unable to read BigQuery.

## Error
The job can be created once, it is reading from BigQuery successfully.
When the first job is done, the second job created from the same template fails with an error:

```
2020-10-06 14:48:02.002 PDTError message from worker: java.io.IOException: Cannot start an export job since table retailsearchproject-lowes:temp_dataset_beam_bq_job_QUERY_bigquerypipelinealitvinov100621394839e5f68a_eba5287932a74fb88787362144bd56f2.temp_table_beam_bq_job_QUERY_bigquerypipelinealitvinov100621394839e5f68a_eba5287932a74fb88787362144bd56f2 does not exist org.apache.beam.sdk.io.gcp.bigquery.BigQuerySourceBase.extractFiles(BigQuerySourceBase.java:115) org.apache.beam.sdk.io.gcp.bigquery.BigQuerySourceBase.split(BigQuerySourceBase.java:148) org.apache.beam.runners.dataflow.worker.WorkerCustomSources.splitAndValidate(WorkerCustomSources.java:290) org.apache.beam.runners.dataflow.worker.WorkerCustomSources.performSplitTyped(WorkerCustomSources.java:212) org.apache.beam.runners.dataflow.worker.WorkerCustomSources.performSplitWithApiLimit(WorkerCustomSources.java:196) org.apache.beam.runners.dataflow.worker.WorkerCustomSources.performSplit(WorkerCustomSources.java:175) org.apache.beam.runners.dataflow.worker.WorkerCustomSourceOperationExecutor.execute(WorkerCustomSourceOperationExecutor.java:78) org.apache.beam.runners.dataflow.worker.BatchDataflowWorker.executeWork(BatchDataflowWorker.java:417) org.apache.beam.runners.dataflow.worker.BatchDataflowWorker.doWork(BatchDataflowWorker.java:386) org.apache.beam.runners.dataflow.worker.BatchDataflowWorker.getAndPerformWork(BatchDataflowWorker.java:311) org.apache.beam.runners.dataflow.worker.DataflowBatchWorkerHarness$WorkerThread.doWork(DataflowBatchWorkerHarness.java:140) org.apache.beam.runners.dataflow.worker.DataflowBatchWorkerHarness$WorkerThread.call(DataflowBatchWorkerHarness.java:120) org.apache.beam.runners.dataflow.worker.DataflowBatchWorkerHarness$WorkerThread.call(DataflowBatchWorkerHarness.java:107) java.util.concurrent.FutureTask.run(FutureTask.java:266) java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1149) java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:624) java.lang.Thread.run(Thread.java:748)
```
## Library version

`org.apache.beam:beam-runners-google-cloud-dataflow-java:2.24.0`
## Reproducing

- Create a BigQuery table `repeating_job_input` and populate a row:
```
insert into andrei_L.repeating_job_input
(id, group_id, dummy)
values('100', '10000', 'DUMMY1');
```
- Create a template
```
mvn clean compile  exec:java -Dexec.mainClass=org.litvinov.andrei.beam.bquery.example.BigqueryPipeline -Dexec.args="--runner=DataflowRunner --region=us-central1 --project=<your project> --stagingLocation=gs://<your bucket>/staging/ --inputTable=andrei_L.repeating_job_input  --outputFile=gs://<your bucket>/temp/repeating_job_output --tempLocation=gs://<your bucket>/temp/ --templateLocation=gs://<your bucket>/templates/example_template --workerMachineType=n1-standard-8"
```
- Create a job the first time
```
gcloud dataflow jobs run first_job --project=<your project> --region=us-central1 --gcs-location=gs://<your bucket>/templates/example_template
```
- After the job finishes successfully, check the output file
```
gsutil cat gs://<your bucket/temp/repeating_job_output-00000-of-00001
```
expecting to see:
```
1000
```
- Clean the output file
```
gsutil rm gs://<your bucket/temp/repeating_job_output-00000-of-00001
```
- Create the second job
```
gcloud dataflow jobs run second_job --project=<your project> --region=us-central1 --gcs-location=gs://<your bucket>/templates/example_template
```
- Expect the second job to fail
- Expect no output file created
```
gsutil ls gs://<your bucket/temp/repeating_job_output-00000-of-00001
CommandException: One or more URLs matched no objects
```