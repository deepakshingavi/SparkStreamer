package com.deepak.data.model

import com.deepak.data.utils.JsonUtil
import scalikejdbc.{WrappedResultSet, _}

sealed trait FromResultSet {
  val id: Long
}

object FromResultSet {
  def apply(rs: WrappedResultSet, tableName: String): FromResultSet = {
    tableName match {
      case CustomerConfig.tableName =>
        CustomerConfig(rs)
    }
  }
}

trait HasCustomerId {
  val customerId: Option[String]
}

case class UserIdCustomerId(
                             userId: String,
                             customerId: String)

case class ConfigJson(value: String)

case class CustomerConfig(
                           id: Long,
                           customerId: Option[String],
                           configJson: CustomerSettingsMap
                         ) extends FromResultSet with HasCustomerId

case class CustomerSettingsMap(
                              transform_segment_event: String,
                              segment_write_key: String,
                              advertiser_id: String,
                              spark_segment_write_key: String,
                              recorder_list: Seq[String],
                              consumer_key: String,
                              private_key: String,
                              packagenames: Map[String, String]
                            )

object CustomerConfig extends SQLSyntaxSupport[CustomerConfig] with Serializable {
  override val tableName = "customer_config"

  def apply(rs: WrappedResultSet) = new CustomerConfig(
    rs.long("id"),
    rs.stringOpt("customer_id"),
    JsonUtil.fromJson[CustomerSettingsMap](rs.stringOpt("settings_json").getOrElse(""))
  )

}