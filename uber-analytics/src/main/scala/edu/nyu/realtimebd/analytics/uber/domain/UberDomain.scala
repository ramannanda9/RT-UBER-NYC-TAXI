package edu.nyu.realtimebd.analytics.uber.domain

object UberDomain {
     case class UberParams(duration:Integer,name:String, surgeMultiplier:Float, distance:Float,  hour:Integer, minute:Integer,  second:Integer)

}