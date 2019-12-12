package com.deepak.data.processor

import java.io.FileInputStream
import java.time.ZonedDateTime
import java.time.format.{DateTimeFormatter, DateTimeFormatterBuilder}
import java.util.UUID.randomUUID

import com.deepak.data.dbapi.{CacheMap, DbApi, DbSync}
import com.deepak.data.model.MinimalJson
import com.deepak.data.utils.AppProperties
import com.deepak.data.utils.Constant._
import org.apache.log4j.{Level, Logger}
import org.apache.spark.sql.functions.{col, collect_list, from_json}
import org.apache.spark.sql.streaming.{DataStreamReader, Trigger}
import org.apache.spark.sql.types.{ArrayType, StringType, StructType}
import org.apache.spark.sql.{Row, SparkSession}
import org.json4s.DefaultFormats
import org.json4s.jackson.JsonMethods.parse
import scalikejdbc.{AutoSession, DBSession}

import scala.collection.mutable
import scala.util.Try

object StreamProcessor {

  def checkpointDir: String = java.nio.file.Files.createTempDirectory(this.getClass.getSimpleName).toUri.toString

  val rootLogger: Logger = Logger.getRootLogger

  val tsParser: DateTimeFormatter = new DateTimeFormatterBuilder()
    .parseCaseInsensitive
    .append(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
    .appendOptional(new DateTimeFormatterBuilder().appendPattern(".SSSSSS").toFormatter())
    .appendOffsetId
    .toFormatter()

  def main(args: Array[String]): Unit = {

    val argsMap: Map[String, String] = collection.immutable.HashMap(
      PROPS_PATH -> args(0)
    )
    val propertyFileName = argsMap(PROPS_PATH)

    var dbSync: DbSync = null

    try {
      val spark: SparkSession = SparkSession
        .builder()
        .config("spark.sql.streaming.checkpointLocation", checkpointDir)
        .getOrCreate()

      val props = new AppProperties(new FileInputStream(propertyFileName))
      implicit val session: DBSession = AutoSession
      dbSync = new DbSync(props, new DbApi())(session)

      val jobName: String = spark.conf.get(STREAMING_JOB_NAME)
      val customerConfigMap = dbSync.customerConfigMap
      new Thread(dbSync).start()
      val cacheMap = CacheMap(customerConfigMap)

      Logger.getLogger("org.apache.spark").setLevel(Level.WARN)
      Logger.getLogger("org.spark-project").setLevel(Level.WARN)
      Logger.getLogger("org.apache.kafka").setLevel(Level.WARN)
      Logger.getLogger("com.deepak.data").setLevel(Level.INFO)

      val startingOffsets = spark.conf.get(START_OFFSET, "")
      val kafkaDfTemp: DataStreamReader = spark.readStream.format("kafka")
        .option("subscribe", props.get(TOPIC_NAME))
        .option("kafka.bootstrap.servers", props.get(KAFKA_BROKERS))
      val kafkaDf = {
        if (!startingOffsets.isEmpty) kafkaDfTemp.option("startingOffsets", startingOffsets) //Use -1 for latest, -2 for earliest
        else kafkaDfTemp
        }.load()

      val schema = new StructType()
        .add("customerId", StringType)

      val outputDf = kafkaDf
        .select(col("value").cast("string").as("value"), from_json(col("value").cast("string"), schema).as("data"))
        .select("value", "data.*")
        .writeStream
        .option("checkpointLocation", spark.conf.get("spark.sql.streaming.checkpointLocation"))
        .trigger(Trigger.ProcessingTime(props.get(TRIGGER_BATCH_DURATION)))
        .foreachBatch((dsw, batchId) => {

          val recordsCount = dsw.count()

          rootLogger.info(s"""DataFrame record count="${recordsCount}" batchId=${batchId} """)

          dsw
            .groupBy("customerId")
            .agg(collect_list("value").cast(ArrayType(StringType)).as("value"))
            .foreach((row: Row) => {
              processRow(jobName, cacheMap, batchId, row)
            })
        })
      outputDf.start().awaitTermination()
    } catch {
      case e: Exception => rootLogger.error("Exception in Main method", e)
    }
    finally {
      if (dbSync != null) {
        dbSync.turnOff = true
      }
    }
  }


  def processRow(
                  jobName: String,
                  cacheMap: CacheMap,
                  batchId: Long,
                  row: Row
                ): Unit = {
    val batch_uuid: String = s"_${randomUUID().toString}"
    val customerId = row.getAs[String]("customerId")
    val data = row.getAs[mutable.WrappedArray[String]]("value")

    rootLogger.info(s""" start batch_uuid=${batch_uuid} batchId=${batchId} customerId="${customerId}" size="${data.size}" """)

    if (cacheMap.customerConfigs.contains(customerId)) {
      val batch = data
        .array
        .flatMap(event => processBatch(event, cacheMap, customerId, batch_uuid))
        .toList
      batch.foreach(log => {
        rootLogger.error(log)
      })
    }
  }

  def processBatch(data: String, cacheMap: CacheMap, customerId: String, batch_uuid: String): Option[String] = {

    var segmentJsonStr: Option[String] = None

    try {
      val parsedJson = parse(data)
      implicit val formats: DefaultFormats.type = DefaultFormats
      val timeStampStr: String = (parsedJson \ "timestamp").extract[String]
      val timestamp = ZonedDateTime.parse(timeStampStr, tsParser)
      val logType: String = (parsedJson \ "type").extract[String]
      val logContext: String = Try((parsedJson \ "context").extract[String]).getOrElse("{}")
      val userId: String = Try((parsedJson \ "userId").extract[String]).getOrElse("NA")
      segmentJsonStr = Option(MinimalJson(customerId, timestamp, logType, logContext, userId).toString)

    } catch {
      case ex: Exception => rootLogger.error(s"Exception while processing uuid=${batch_uuid} data=", ex)
        throw ex
    }
    segmentJsonStr
  }

}
