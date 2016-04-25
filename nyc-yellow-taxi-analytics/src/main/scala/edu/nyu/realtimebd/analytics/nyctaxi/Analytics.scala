package edu.nyu.realtimebd.analytics.nyctaxi
import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.sql.hive.HiveContext
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.DataFrame
import org.apache.spark.ml.PipelineStage
import org.apache.spark.ml.feature.StringIndexer
import org.apache.spark.ml.Pipeline
import org.apache.spark.ml.feature.OneHotEncoder
import org.apache.spark.mllib.regression.LinearRegressionWithSGD
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.linalg.{Vector, Vectors}
import org.apache.spark.sql.Row;
import org.apache.spark.ml.tuning.CrossValidator
import org.apache.spark.ml.classification.LogisticRegression
import org.apache.spark.ml.regression.LinearRegression
import org.apache.spark.ml.evaluation.BinaryClassificationEvaluator
import org.apache.spark.mllib.evaluation.RegressionMetrics
import org.apache.spark.ml.tuning.ParamGridBuilder
import org.apache.spark.ml.feature.VectorAssembler
import org.apache.spark.sql.SQLContext
import org.apache.spark.sql.types.StructField
import org.apache.spark.sql.types.StringType
import org.apache.spark.sql.types.DoubleType
import org.apache.spark.sql.types.StructType
import org.apache.spark.ml.evaluation.RegressionEvaluator
import akka.dispatch.Foreach

/*
 *@Author Ramandeep Singh 
 */
object Analytics {
  
  val sparkConf=new SparkConf().setAppName("NYC-TAXI-ANALYSIS").setMaster("local")
  val sc=new SparkContext(sparkConf)
    val hiveCtxt=new HiveContext(sc)
  var df:DataFrame=_
  def initializeDataFrame(query:String){
    //cache the dataframe
    if(df==null){
      df=hiveCtxt.sql(query).na.drop().cache()
    }
    return df
  }
  
  def buildPriceAnalysisModel(query:String){
   if(df==null){
     initializeDataFrame(query)
   }
   // String columns need to be encoded to indexes these are by occurence count
   val stringColumns= Array("store_and_fwd_flag")
   val stringIndexTransformer:Array[PipelineStage]=stringColumns.map(
       cname=>new StringIndexer().setInputCol(cname).setOutputCol(s"${cname}_index")
   )
   val indexedPipeline=new Pipeline().setStages(stringIndexTransformer)
   val indexModel=indexedPipeline.fit(df)
   //drop string columns
   var df_indexed=indexModel.transform(df).drop("store_and_fwd_flag")
   val indexedColumns=df_indexed.columns.filter(colName=> colName.contains("_index"))
   val oneHotEncodedColumns=indexedColumns:+"ratecodeid"
   //do one hot encoding of now integer features, which just represent categories
   val oneHotTransformer:Array[PipelineStage]=oneHotEncodedColumns.map { cname=> new OneHotEncoder().
     setInputCol(cname).setOutputCol(s"${cname}_vect")
     }
   val oneHotPipeline=new Pipeline().setStages(oneHotTransformer)
   val oneHotModel= oneHotPipeline.fit(df_indexed)
   df_indexed=oneHotModel.transform(df_indexed).drop("ratecodeid")
   indexedColumns.foreach { colName => df_indexed=df_indexed.drop(colName) }
   val df_splitData:Array[DataFrame]= df_indexed.randomSplit(Array(0.7,0.3),11l)
   val trainData=df_splitData(0)
   val testData=df_splitData(1)
   //drop target variable
    val testData_x=testData.drop("fare_amount")
    val testData_y=testData.select("fare_amount")
    val columnsToTransform=trainData.drop("fare_amount").columns
    //Make feature vector
    val vectorAssembler= new VectorAssembler().
        setInputCols(columnsToTransform).setOutputCol("features")
    columnsToTransform.foreach { x => println(x) }
    val trainDataTemp=vectorAssembler.transform(trainData).withColumnRenamed("fare_amount", "label")    
    val testDataTemp=vectorAssembler.transform(testData_x)
    val trainDataFin=trainDataTemp.select("features","label")
    val testDataFin=testDataTemp.select("features")
    val linearRegression=new LinearRegression() 
    //Params for tuning the model.
    val paramGridMap = new ParamGridBuilder()
             .addGrid(linearRegression.maxIter, Array(10, 100, 1000))
             .addGrid(linearRegression.regParam, Array(0.1, 0.01,0.001,1,10)).build()
    //5 fold cross validation         
    val cv=new CrossValidator().setEstimator(linearRegression).
    setEvaluator(new RegressionEvaluator()).setEstimatorParamMaps(paramGridMap).setNumFolds(5)
    //Fit the model
    val model=cv.fit(trainDataFin)
    val modelResult= model.transform(testDataFin)
    model.bestModel.params.foreach{ x => print(x.name ," ",x.toString())}
    val predictionAndLabels = modelResult.map(r=>r.getAs[Double]("prediction")).zip(testData_y.map(R=>R.getAs[Double](0)))
    val regressionMetrics=new RegressionMetrics(predictionAndLabels) 
    //Print the results
    println(s"R-Squared= ${regressionMetrics.r2}")
    println(s"Explained Variance=${regressionMetrics.explainedVariance}")
    println(s"MAE= ${regressionMetrics.meanAbsoluteError}")
    
    //TODO save and reuse the model
    
  }
  
  def main(args: Array[String]){
    buildPriceAnalysisModel("""select start_latitude, start_longitude,end_latitude,end_longitude,
     fare_amount, trip_distance, hour(journey_start_time) as start_hour, hour(journey_end_time) as end_hour,minute(journey_start_time) as start_time,
     minute(journey_end_time) as end_time, store_and_fwd_flag,ratecodeid from default.nyc_taxi_data_limited 
     where start_latitude <> 0 and trip_distance <>0 and trip_distance <200 and fare_amount>1 limit 12000""");
    
  }
  
}
