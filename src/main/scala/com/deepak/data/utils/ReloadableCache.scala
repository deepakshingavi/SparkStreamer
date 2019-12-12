package com.deepak.data.utils

import com.deepak.data.dbapi.DbApi
import com.deepak.data.model._
import org.apache.log4j.Logger
import scalikejdbc._

import scala.collection.mutable


trait WithLogging {
  val logger: Logger = Logger.getRootLogger
}

trait ReloadableCache extends WithLogging {

  def dbApi = new DbApi()

  private def syncMaps[K, V](source: Map[K, V], destination: mutable.Map[K, V]): Unit = {
    destination ++= source
    destination --= destination.keys.filterNot(source.contains)
  }

  private def initializeMap[K, T](tableName: String, data: mutable.Map[K, T])(f: String => Map[K, T]): Unit = {
    logger.info(s"Initializing $tableName")
    syncMaps(f(tableName), data)
  }

  def initCustConfigs(data: mutable.Map[String, CustomerConfig])(implicit session: DBSession): Unit = {
    initializeMap(CustomerConfig.tableName, data) {
      tableName => {
        dbApi.list(tableName)
          .map((data: CustomerConfig) => data.customerId.get -> data)
          .groupBy[String](idAndData => idAndData._1)
          .mapValues(pairs => pairs.map { pair => pair._2 }.last)
      }
    }
  }

}
