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
import org.apache.spark.mllib.linalg.{ Vector, Vectors }
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
import org.apache.spark.sql.DataFrame
import org.apache.spark.ml.PipelineModel
import org.apache.hadoop.mapred.InvalidInputException
import org.apache.spark.ml.regression.LinearRegressionModel
import org.apache.spark.ml.tuning.CrossValidatorModel
import scala.collection.mutable.ListBuffer
import edu.nyu.realtimebd.analytics.nyctaxi.domain.NYCDomain.NYCParams
import org.apache.spark.sql.types.IntegerType

/*
 *@Author Ramandeep Singh 
 */
object Analytics {

  val sparkConf = new SparkConf().setAppName("NYC-TAXI-ANALYSIS").setMaster("local")
  val sc = new SparkContext(sparkConf)
  val sqlContext = new SQLContext(sc)
  val hiveCtxt = new HiveContext(sc)
  var df: DataFrame = _
  def initializeDataFrame(query: String): DataFrame = {
    //cache the dataframe
    if (df == null) {
      df = hiveCtxt.sql(query).na.drop().cache()
    }
    return df
  }
  def preprocessFeatures(df: DataFrame): DataFrame = {
    val stringColumns = Array("store_and_fwd_flag","ratecodeid")
    var indexModel: PipelineModel = null;
    var oneHotModel: PipelineModel = null;
    try {
      indexModel = sc.objectFile[PipelineModel]("nycyellow.model.indexModel").first()

    } catch {
      case e: InvalidInputException => println()
    }
    if (indexModel == null) {
      val stringIndexTransformer: Array[PipelineStage] = stringColumns.map(
        cname => new StringIndexer().setInputCol(cname).setOutputCol(s"${cname}_index"))
      val indexedPipeline = new Pipeline().setStages(stringIndexTransformer)
      indexModel = indexedPipeline.fit(df)
      sc.parallelize(Seq(indexModel), 1).saveAsObjectFile("nycyellow.model.indexModel")

    }

    var df_indexed = indexModel.transform(df)
    stringColumns.foreach { x => df_indexed = df_indexed.drop(x) }
    val indexedColumns = df_indexed.columns.filter(colName => colName.contains("_index"))
    val oneHotEncodedColumns = indexedColumns
    try {
      oneHotModel = sc.objectFile[PipelineModel]("nycyellow.model.onehot").first()
    } catch {
      case e: InvalidInputException => println()
    }

    if (oneHotModel == null) {
      val oneHotTransformer: Array[PipelineStage] = oneHotEncodedColumns.map { cname =>
        new OneHotEncoder().
          setInputCol(cname).setOutputCol(s"${cname}_vect")
      }
      val oneHotPipeline = new Pipeline().setStages(oneHotTransformer)
      oneHotModel = oneHotPipeline.fit(df_indexed)

      sc.parallelize(Seq(oneHotModel), 1).saveAsObjectFile("nycyellow.model.onehot")
    }

    df_indexed = oneHotModel.transform(df_indexed)
    indexedColumns.foreach { colName => df_indexed = df_indexed.drop(colName) }
    df_indexed
  }
  def buildPriceAnalysisModel(query: String) {
    initializeDataFrame(query)
    var df_indexed = preprocessFeatures(df)
     df_indexed.columns.foreach(x => println("Preprocessed Columns Model Training"+x)  )
    val df_splitData: Array[DataFrame] = df_indexed.randomSplit(Array(0.7, 0.3), 11l)
    val trainData = df_splitData(0)
    val testData = df_splitData(1)
    //drop target variable
    val testData_x = testData.drop("fare_amount")
    val testData_y = testData.select("fare_amount")
    val columnsToTransform = trainData.drop("fare_amount").columns
    //Make feature vector
    val vectorAssembler = new VectorAssembler().
      setInputCols(columnsToTransform).setOutputCol("features")
    columnsToTransform.foreach { x => println(x) }
    val trainDataTemp = vectorAssembler.transform(trainData).withColumnRenamed("fare_amount", "label")
    val testDataTemp = vectorAssembler.transform(testData_x)
    val trainDataFin = trainDataTemp.select("features", "label")
    val testDataFin = testDataTemp.select("features")
    val linearRegression = new LinearRegression()
    trainDataFin.columns.foreach(x=>println("Final Column =>"+x))
    trainDataFin.take(1)
    //Params for tuning the model.
    val paramGridMap = new ParamGridBuilder()
      .addGrid(linearRegression.maxIter, Array(10, 100, 1000))
      .addGrid(linearRegression.regParam, Array(0.1, 0.01, 0.001, 1, 10)).build()
    //5 fold cross validation         
    val cv = new CrossValidator().setEstimator(linearRegression).
      setEvaluator(new RegressionEvaluator()).setEstimatorParamMaps(paramGridMap).setNumFolds(5)
    //Fit the model
    val model = cv.fit(trainDataFin)
    val modelResult = model.transform(testDataFin)
    val predictionAndLabels = modelResult.map(r => r.getAs[Double]("prediction")).zip(testData_y.map(R => R.getAs[Double](0)))
    val regressionMetrics = new RegressionMetrics(predictionAndLabels)
    //Print the results
    println(s"R-Squared= ${regressionMetrics.r2}")
    println(s"Explained Variance=${regressionMetrics.explainedVariance}")
    println(s"MAE= ${regressionMetrics.meanAbsoluteError}")
    val lrModel = model.bestModel.asInstanceOf[LinearRegressionModel]
    println(lrModel.explainParams())
    println(lrModel.weights)
    sc.parallelize(Seq(model), 1).saveAsObjectFile("nycyellow.model")

  }
  def predictFare(list: ListBuffer[NYCParams]): DataFrame = {
    var nycModel: CrossValidatorModel = null;
    try {
      nycModel = sc.objectFile[CrossValidatorModel]("nycyellow.model").first()
    } catch {
      case e: InvalidInputException => println()
    }
    if (nycModel == null) {
      buildPriceAnalysisModel("""select 
     trip_distance,
     (cast(journey_end_time as double)-cast(journey_start_time as double)) as duration,
     store_and_fwd_flag,
     ratecodeid, 
     hour(journey_start_time) as start_hour, 
     minute(journey_start_time) as start_minute,
     second(journey_start_time) as start_second,
     fare_amount from nyc_taxi_data_limited 
     where start_latitude <> 0 and trip_distance >0 
    and journey_end_time>journey_start_time and  
    trip_distance <200 and fare_amount>1 limit 12000""")
    }
    nycModel = sc.objectFile[CrossValidatorModel]("nycyellow.model").first()
    var schema = StructType(Array(
      StructField("trip_distance", DoubleType, true),
      StructField("duration", DoubleType, true),
      StructField("store_and_fwd_flag", StringType, true),
      StructField("ratecodeid", DoubleType, true),
      StructField("start_hour", IntegerType, true),
      StructField("start_minute", IntegerType, true),
      StructField("start_second", IntegerType, true)))
    var rows: ListBuffer[Row] = new ListBuffer
    list.foreach(x => rows += Row(x.trip_distance, x.duration, x.store_and_fwd_flag, x.ratecodeid, x.start_hour, x.start_minute, x.start_second))
    val row = sc.parallelize(rows)
    var dfStructure = sqlContext.createDataFrame(row, schema)
    var preprocessed = preprocessFeatures(dfStructure)
    preprocessed.columns.foreach(x => println("Preprocessed Columns "+x)  )
    val vectorAssembler = new VectorAssembler().
      setInputCols(preprocessed.columns).setOutputCol("features")
    preprocessed = vectorAssembler.transform(preprocessed)
    var results = nycModel.transform(preprocessed.select("features"))
    results 
  }
  
}
