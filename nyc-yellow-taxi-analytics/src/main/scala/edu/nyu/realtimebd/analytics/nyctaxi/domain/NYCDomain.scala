package edu.nyu.realtimebd.analytics.nyctaxi.domain

object NYCDomain {
  case class NYCParams(trip_distance:Double, duration:Double, store_and_fwd_flag:String, ratecodeid:Double, 
      start_hour:Integer, start_minute:Integer,  start_second:Integer)

}