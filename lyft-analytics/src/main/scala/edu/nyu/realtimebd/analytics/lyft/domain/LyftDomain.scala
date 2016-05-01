package edu.nyu.realtimebd.analytics.lyft.domain

object LyftDomain {
       case class LyftParams(name:String, duration:Integer, distance:Float,primetime:Float,  hour:Integer, minute:Integer,  second:Integer)

}