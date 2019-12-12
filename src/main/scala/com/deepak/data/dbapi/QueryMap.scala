package com.deepak.data.dbapi

import com.deepak.data.model.CustomerConfig
import scalikejdbc._

object QueryMap {
  def getSingleObjectRetrievalQuery(tableName: String, id: Long): SQL[Nothing, NoExtractor] = {
    val unescapedTableName = SQLSyntax.createUnsafely(tableName)
    tableName match {
      case CustomerConfig.tableName =>
        sql"SELECT * FROM $unescapedTableName WHERE id=${id}"
    }
  }

  def getListRetrievalQuery(tableName: String): SQL[Nothing, NoExtractor] = {
    val unescapedTableName = SQLSyntax.createUnsafely(tableName)
    tableName match {
      case CustomerConfig.tableName =>
        sql"select * from $unescapedTableName"
    }
  }
}
