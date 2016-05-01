package edu.nyu.realtimebd.analytics.lyft

import edu.nyu.realtimebd.analytics.lyft.domain.LyftDomain.LyftParams
import scala.collection.mutable.ListBuffer

object TestLyftAnalytics {
  def main(args: Array[String]) {
    var testAnalytics = LyftAnalytics
    val testData = new ListBuffer[LyftParams]()
    testData += LyftParams("Lyft", 600, 10.6f, 120.0f, 10, 2, 33)
    var result = testAnalytics.predictFare(testData)
    result.describe().show()
  }
}