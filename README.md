# RT-UBER-NYC-TAXI
#### What's New (06/04/2018)
* All the API's i.e. UBER, LYFT and QueryAPIandStoreCSV have been rewritten to use RXJava2 constructs ``Flowable`` ``Single`` with retry support and ``delaying`` of stream errors.
* Requests are scheduled on ``Schedulers.io()``.
* No messy ``Executors`` or ``Callback`` hell.

#### General Information
* This project contains the code to query *Uber API* for price information.
* This project contains the code for the *lyft API*.
* It also contains the code for *MLLIB and SPARK* to build predictive models  
* The directories contain specific code data contains data (obtained from uber and lyft)
* lyft-client contains java code to invoke LYFT API. It has support for rate limiting.
* uber-client contains java code to invoke Uber API. It has support for rate limiting.
* QueryAPIandStoreCSV contains the code to invoke both Uber and Lyft API in parallel
* flume contains flume script.
* lyft-analytics contains spark and mllib analytics code on lyft datasource
* uber-analytics contains spark and mllib analytics code on uber datasource
* nyc-yellow-taxi-analytics contains spark and mllib analytics code on nyc yellow taxi datasource
* data contains part of the data obtained by querying uber and lyft API
* scripts contains hive and impala queries used for filtering the data.


#### Running the code

* To test uber and lyft api's you can run the main class in lyft-client(LyftClientUtilTest.java) or uber-client(UberClientUtil.java). These jars are not runnable.
* Remember you still have to add the values for property keys in resources folder of each client.
* The main code that invokes the above api to gather data is under ``RT-UBER-NYC-TAXI/QueryAPIAndStoreCSV/build/libs/QueryAPIAndStoreCSV1-0.jar``. You can run this code. You need to specify a sample data file located in the data folder. for ex ``java -jar QueryAPIAndStoreCSV-1.0.jar -d data -f query-data.csv``
* You can then create tables in hive using the ``db_scripts.sql``.
* Although flume is not required you can use it. For flume just run the flume agent. ``flume-ng agent --conf conf -f flume.conf -n flume-hive-ingest``
* Again running spark code for any of the 3 modules requires that you create hive tables and load and store data. The scripts directory contains the scripts. After you are done with that run the Test*.class in each project. Initially the model will be trained using that data and stored, from next call onwards the execution will be fast as stored model will be used to do predictions.
