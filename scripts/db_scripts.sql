#tail -n +2 yellow_tripdata_2015-01.csv> yellow_tripdata_2015-01_header_less.csv

create external table nyc_taxi_data(
  VendorID int, tpep_pickup_datetime string, tpep_dropoff_datetime string, passenger_count int,trip_distance double,pickup_longitude double,pickup_latitude double,RateCodeID int,store_and_fwd_flag string ,dropoff_longitude double,dropoff_latitude double,payment_type int,fare_amount double,extra double,mta_tax double,tip_amount double,tolls_amount double,improvement_surcharge double,total_amount double)
  row format delimited fields terminated by ','
  location '/user/cloudera/projectData'

compute stats;


#casting avoids bad data
select VendorID, tpep_pickup_datetime, from (
select *,
row_number() over (partition by trunc(cast(tpep_pickup_datetime as timestamp), 'HH') order by trunc(cast(tpep_pickup_datetime as timestamp), 'HH') desc )
as rownumb from nyc_taxi_data
) as q where rownumb<10;

#new table to store subset of records.
create table nyc_taxi_data_limited(
 VendorID int, journey_start_time string, journey_end_time string, passenger_count int,trip_distance double,start_longitude double,start_latitude double,RateCodeID int,store_and_fwd_flag string ,end_longitude double,end_latitude double,payment_type int,fare_amount double,extra double,mta_tax double,tip_amount double,tolls_amount double,improvement_surcharge double,total_amount double

)

#Top-N Subquery selects first 500 records per hour for a day
insert into nyc_taxi_data_limited  select VendorID, tpep_pickup_datetime , tpep_dropoff_datetime , passenger_count ,trip_distance ,pickup_longitude ,pickup_latitude,RateCodeID ,store_and_fwd_flag  ,dropoff_longitude ,dropoff_latitude ,payment_type ,fare_amount ,extra,mta_tax ,tip_amount,tolls_amount,improvement_surcharge,total_amount from ( select *,
row_number() over (partition by trunc(cast(tpep_pickup_datetime as timestamp), 'HH') order by trunc(cast(tpep_pickup_datetime as timestamp), 'HH') desc)
as rownumb from nyc_taxi_data where cast(tpep_pickup_datetime as timestamp) between cast('2015-01-01 00:00:00' as timestamp) and cast('2015-01-02 00:00:00' as timestamp)
) as q  where rownumb<500;

#export the new table to query the uber and lyft API
impala-shell -B -o data_limited_new.csv --output_delimiter="," --print_header -q "select * from nyc_taxi_data_limited order by cast(journey_start_time as timestamp)";


create external table lyft (ridetype string, displayname string, currency string, minCostCent int, maxCostCent int, journeyduration int,
distanceMiles float, primeTimePercentage string,currentDate timestamp)
row format delimited fields terminated by ','
location '/user/cloudera/lyftdata'


create external table uber (localizeddisplayname string, highestimate float, lowestimate float, minimumfare float, duration int, displayname string,
  productid string,surge_multiplier float,currencyCode string, distance float, currentDate timestamp)
  row format delimited fields terminated by ','
  location '/user/cloudera/uberdata'
