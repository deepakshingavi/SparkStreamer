package com.deepak.data.dbapi

import java.time.Duration
import java.util.Properties

import com.deepak.data.model._
import com.deepak.data.utils.Constant._
import com.deepak.data.utils.{AppProperties, JsonUtil, ReloadableCache}
import org.apache.kafka.clients.consumer.{ConsumerRecord, KafkaConsumer}
import scalikejdbc._

import scala.collection.JavaConversions._
import scala.collection.mutable

object EventTypes {
  val INSERT = "INSERT"
  val DELETE = "DELETE"
  val UPDATE = "UPDATE"
  val REFRESH = "REFRESH"
}

class DbSync(apps: AppProperties, override val dbApi: DbApi)(implicit session: DBSession)
  extends Runnable
    with ReloadableCache {
  var turnOff = false
  val topicName: String = apps.get(DB_LISTENER_TOPIC_NAME)

  ConnectionPool.singleton(
    apps.get(DB_CONN_URL),
    apps.get(DB_CONN_USER_NAME),
    apps.get(DB_CONN_PASSWORD)
  )

  def initializeCache(): Unit = {
    initCustConfigs(this.customerConfigMap)
  }

  var customerConfigMap: mutable.Map[String, CustomerConfig] = mutable.Map.empty

  initializeCache()

  def run(): Unit = {
    dbEventListener()
  }

  def dbEventListener() {
    val consumer = new KafkaConsumer[String, String](initializeKafkaProperties)
    consumer.subscribe(java.util.Arrays.asList(topicName))

    while (true) {
      val record = consumer.poll(Duration.ofMillis(1000))
      for (data <- record.iterator()) {
        try {
          logger.info(data.value())
          processEvent(data)
        } catch {
          case ex: NullPointerException =>
            logger.error(s"Object not found!!! json=${data.value()}", ex)
          case ex: Exception =>
            logger.error("Error while parsing JSON.", ex)
        }
      }
    }
  }

  def processEvent(data: ConsumerRecord[String, String]): Unit = {
    val dbEvent = JsonUtil.fromJson[DbEvent](data.value())
    val objectId : java.lang.Long = dbEvent.objectId.getOrElse(-1l)
    val tableName = dbEvent.tableName.getOrElse("")
    val action = dbEvent.action.getOrElse("")
    if (objectId == -1 && !"*".equals(tableName)) {
      logger.error("ObjectId can't be empty only for All table cache refresh `*`.")
    } else if (objectId != -1) {
      tableName match {
        case "*" if action == EventTypes.REFRESH =>
          logger.info(s"${EventTypes.REFRESH} detected: re-loading cache")
          initializeCache()
          logger.info(s"cache re-loading done!!!")

        case CustomerConfig.tableName =>
          action match {
            case EventTypes.INSERT | EventTypes.UPDATE =>
              logger.info(s"received ${CustomerConfig.tableName} $action")
              val customerConfig = dbApi.one[CustomerConfig](objectId, tableName)
              customerConfigMap.put(customerConfig.customerId.get, customerConfig)

            case EventTypes.DELETE =>
              logger.info(s"received ${CustomerConfig.tableName} $action")
              customerConfigMap.retain((_, v) => v.id != objectId)
          }

        case _ =>
          logger.error(s"Invalid table Database event. ${data.value}")
      }
    }
  }

  private def initializeKafkaProperties = {
    val props = new Properties()
    props.put("bootstrap.servers", apps.get(KAFKA_BROKERS))
    props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
    props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
    props.put("group.id", "db-event-listener")
    props
  }
}