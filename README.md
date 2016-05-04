# RT-UBER-NYC-TAXI
* This project contains the code to query *Uber API* for price information.
* This project contains the code for the *lyft API*.
* It also contains the code for *MLLIB and SPARK* to build predictive models  
* The directories contain specific code data contains data (obtained from uber and lyft)
* lyft-client contains java code to invoke LYFT API
* uber-client contains java code to invoke Uber API
* QueryAPIandStoreCSV contains the code to invoke both Uber and Lyft API in parallel using Executors and Futures
* flume contains flume script.
* lyft-analytics contains spark and mllib analytics code on lyft datasource
* uber-analytics contains spark and mllib analytics code on uber datasource
* nyc-yellow-taxi-analytics contains spark and mllib analytics code on nyc yellow taxi datasource
* data contains part of the data obtained by querying uber and lyft API
* scripts contains hive and impala queries used for filtering the data.
* The code does not contain the properties file because it contains sensitive API keys from uber and lyft. If you require those kindly let us know.
