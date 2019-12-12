package com.deepak.data

import com.deepak.data.model.DbEvent
import com.deepak.data.utils.JsonUtil
import org.junit.Test
import org.scalatest.junit.AssertionsForJUnit


class DbEventTest extends AssertionsForJUnit {

  @Test
  def dbEventParsingTest(): Unit = {
    var dbEventJson = """{"tableName": "customer_config", "objectId": 112233, "action": "UPDATE"}""".stripMargin
    var dbEvent = JsonUtil.fromJson[DbEvent](dbEventJson)
    var objectId = dbEvent.objectId.getOrElse(-1l)
    var tableName = dbEvent.tableName.getOrElse("")
    var action = dbEvent.action.getOrElse("")

    assert(objectId == 112233)
    assert(tableName == "customer_config")
    assert(action == "UPDATE")

    dbEventJson = """{"tableName": "customer_config",  "action": "UPDATE", "objectId":1}""".stripMargin
    dbEvent = JsonUtil.fromJson[DbEvent](dbEventJson)
    objectId = dbEvent.objectId.getOrElse(None)
    tableName = dbEvent.tableName.getOrElse("")
    action = dbEvent.action.getOrElse("")

    assert(objectId == 1)

    dbEventJson = """{"tableName": "customer_config",  "action": "DELETE"}""".stripMargin
    dbEvent = JsonUtil.fromJson[DbEvent](dbEventJson)
    objectId = dbEvent.objectId.getOrElse(None)
    tableName = dbEvent.tableName.getOrElse("")
    action = dbEvent.action.getOrElse("")

    assert(objectId == None)

  }
}
