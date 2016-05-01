package edu.nyu.realtimebd.analytics.nyctaxi

import scala.collection.mutable.ListBuffer
import edu.nyu.realtimebd.analytics.nyctaxi.domain.NYCDomain.NYCParams

class TestAnalytics {
  def main(args:Array[String]){
    var testAnalytics=Analytics
    val testData=new ListBuffer[NYCParams]()
testData+= NYCParams(10.6,600.0,"N", 1.0, 10,2,33)
var result=testAnalytics.predictFare(testData)
result.describe().show()
  }
}