package edu.nyu.realtimebd.analytics.uber

import scala.collection.mutable.ListBuffer
import edu.nyu.realtimebd.analytics.uber.domain.UberDomain.UberParams

object TestUberAnalytics {
  def main(args: Array[String]) {
    var testAnalytics = UberAnalytics
    val testData = new ListBuffer[UberParams]()
    testData += UberParams(600, "uberX", 1.2f, 10.6f, 10, 2, 33)
    var result = testAnalytics.predictFare(testData)
    result.describe().show()
  }
}