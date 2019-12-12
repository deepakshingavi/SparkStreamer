package com.deepak.data.model

case class DbEvent(tableName: Option[String], objectId: Option[java.lang.Long], action: Option[String])
