package com.deepak.data.dbapi

import com.deepak.data.model.CustomerConfig

import scala.collection.mutable

case class CacheMap(customerConfigs: mutable.Map[String, CustomerConfig]) {

}
