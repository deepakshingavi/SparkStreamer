import org.apache.log4j.Logger
import org.apache.spark.sql.functions.{col, collect_list, from_json}
import org.apache.spark.sql.streaming.Trigger
import org.apache.spark.sql.types.{ArrayType, StringType, StructType}
import org.apache.spark.sql.{Dataset, _}
import org.json4s.DefaultFormats
import org.json4s.jackson.JsonMethods.parse

import scala.collection.mutable
import scala.util.Try

object JsonKafkaStreamProcessor {

  val rootLogger: Logger = Logger.getRootLogger

  //Below code works for a Kafka nested JSON event as below
  /**
   * {
   * "timestamp": "2019-06-12T23:59:59+00:00",
   * "data": {
   * "user_id": 73598
   * }
   * }
   *
   * @param args
   */
  def main(args: Array[String]): Unit = {
    val spark = Constant.getSparkSess

    //Create and load you Spark Dataframe from Kafka
    val kafkaDf: DataFrame = spark.readStream.format("kafka")
      .option("subscribe", "topic-name")
      .option("kafka.bootstrap.servers", "kafkahost:port")
      .load()

    //minimal dummy schema field which I am expecting from my json
    //replace customer_id with your mandatory field
    val schema = new StructType()
      .add("customer_id", StringType)

    //implicit format to parse JSON for json4s APIs
    implicit val formats: DefaultFormats.type = DefaultFormats


    //Spark's Out stream of the
    val outputDf = kafkaDf
      //Select kafka event as String
      .select(col("value").cast("string").as("value"),
        //Parse JSON string to `data` column
        from_json(col("value").cast("string"), schema).as("data"))
      //Select only value from data field in a column "value"
      .select("value", "data.*")
      .writeStream
      //Fail safe
      .option("checkpointLocation", "/checkpoint/dir/")

      //Stream job will be executed every 1 second
      .trigger(Trigger.ProcessingTime("1 second"))

      //Iterate every 1 second batch as a individual Dataset
      .foreachBatch((dsw: Dataset[_], batchId: Long) => {
        val recordsCount = dsw.count()
        rootLogger.info(s"""DataFrame record count="${recordsCount}" batchId=${batchId} """)

        //Grouping the dataset by Customer ID int a List
        dsw.groupBy("customer_id")
          .agg(collect_list("value").cast(ArrayType(StringType)).as("value"))
          .foreach((row: Row) => {
            //You can get the customer directly
            val customerId = row.getAs[String]("customer_id")

            //JSOn event are still List<String> so iterate 
            val data = row.getAs[mutable.WrappedArray[String]]("value")
            data
              .array
              .map(event => {
                //Parse individual JSON using json4s APIs
                val parsedJson = parse(event)
                //query JSON to extract nested values
                val timeStampStr: String = (parsedJson \ "timestamp").extract[String]
                val user_id: String = Try((parsedJson \ "user_id").extract[String]).getOrElse("no_user_")
                rootLogger.info(s"timeStampStr=${timeStampStr} user_id=${user_id}")
              })

          })
      })
    outputDf.start().awaitTermination()
  }

}
