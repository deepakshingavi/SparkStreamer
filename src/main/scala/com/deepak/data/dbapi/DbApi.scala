package com.deepak.data.dbapi

import com.deepak.data.model.FromResultSet
import scalikejdbc.DBSession

import scala.collection.mutable.ListBuffer

class DbApi {
  def one[T <: FromResultSet](id: Long, tableName: String)(implicit session: DBSession): T = {
    QueryMap.getSingleObjectRetrievalQuery(tableName, id)
      .map(rs => FromResultSet.apply(rs, tableName))
      .single()
      .apply()
      .get
      .asInstanceOf[T]
  }

  def list[T](tableName: String)(implicit session: DBSession): ListBuffer[T] = {
    QueryMap.getListRetrievalQuery(tableName)
      .map(rs => FromResultSet.apply(rs, tableName))
      .list()
      .apply()
      .map(_.asInstanceOf[T])
      .to[ListBuffer]
  }
}
