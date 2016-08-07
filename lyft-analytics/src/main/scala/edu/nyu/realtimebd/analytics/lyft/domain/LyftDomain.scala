package edu.nyu.realtimebd.analytics.lyft.domain

/**
 * This class contains the definition for case class that is used in passing values to analytics class for prediction
 */
object LyftDomain {
  case class LyftParams(name: String, duration: Integer, distance: Float, primetime: Float, hour: Integer, minute: Integer, second: Integer)

}