package com.deepak.data

import com.deepak.data.dbapi.{DbApi, DbSync, EventTypes}
import com.deepak.data.model.CustomerConfig
import com.deepak.data.utils.AppProperties
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import scalikejdbc.DBSession

import scala.collection.mutable.ListBuffer

@RunWith(classOf[JUnitRunner])
class DbSyncTest extends FunSuite with Fixtures {

  private val properties = mock(classOf[AppProperties])
  private val dbApi = mock(classOf[DbApi])

  implicit val session: DBSession = mock(classOf[DBSession])
  when(dbApi.list(any())(refEq(session))).thenReturn(ListBuffer.empty)

  val dbSync = new DbSync(properties, dbApi)

  test("all db listener maps are re-initialized when refresh is called") {
    val payload = s"""{"tableName": "",  "action": "${EventTypes.REFRESH}", "objectId":1}""".stripMargin
    val consumerRecord = new ConsumerRecord("", 1, 1, "", payload)
    dbSync.processEvent(consumerRecord)

    verify(dbApi, times(1)).list(anyString())(any())
    assert(dbSync.customerConfigMap.isEmpty)
  }

  test("insert or update results in value being inserted in map for the table") {
    when(dbApi.one[CustomerConfig](
      customerConfig.id,
      CustomerConfig.tableName
    )).thenReturn(customerConfig)

    assert(dbSync.customerConfigMap.isEmpty)

    var payload = s"""{"tableName": "${CustomerConfig.tableName}",  "action": "${EventTypes.INSERT}", "objectId":${customerConfig.id}}""".stripMargin
    var consumerRecord = new ConsumerRecord("", 1, 1, "", payload)

    dbSync.processEvent(consumerRecord)
    assert(dbSync.customerConfigMap(customerConfig.customerId.get) == customerConfig)

    payload = s"""{"tableName": "${CustomerConfig.tableName}",  "action": "${EventTypes.UPDATE}", "objectId":${customerConfig.id}}""".stripMargin
    consumerRecord = new ConsumerRecord("", 1, 1, "", payload)

    dbSync.customerConfigMap.clear()
    dbSync.processEvent(consumerRecord)
    assert(dbSync.customerConfigMap(customerConfig.customerId.get) == customerConfig)
  }

  test("event add in Customer config from map nullpointer") {
    dbSync.customerConfigMap.put(customerConfig.customerId.get, customerConfig)

    val payload = s"""{"tableName": "${CustomerConfig.tableName}",  "action": "${EventTypes.INSERT}", "objectId":7}""".stripMargin
    val consumerRecord = new ConsumerRecord("", 1, 1, "", payload)

    assertThrows[NullPointerException](dbSync.processEvent(consumerRecord))
  }

  test("event add in Customer config from map") {

    when(dbApi.one[CustomerConfig](
      customerConfig.id,
      CustomerConfig.tableName
    )).thenReturn(customerConfig)

    dbSync.customerConfigMap.put(customerConfig.customerId.get, customerConfig)

    val payload = s"""{"tableName": "${CustomerConfig.tableName}",  "action": "${EventTypes.INSERT}", "objectId":${customerConfig.id}}""".stripMargin
    val consumerRecord = new ConsumerRecord("", 1, 1, "", payload)

    dbSync.processEvent(consumerRecord)

    assert(dbSync.customerConfigMap(customerConfig.customerId.get) == customerConfig)
  }

  test("delete event removes value from map") {
    dbSync.customerConfigMap.put(customerConfig.customerId.get, customerConfig)

    val payload = s"""{"tableName": "${CustomerConfig.tableName}",  "action": "${EventTypes.DELETE}", "objectId":${customerConfig.id}}""".stripMargin
    val consumerRecord = new ConsumerRecord("", 1, 1, "", payload)

    dbSync.processEvent(consumerRecord)
    assert(!dbSync.customerConfigMap.contains(customerConfig.customerId.get))
  }

  test("Customer config map insertion") {
    // UserData is a special case because its ID is a value type
    //{"tableName": "customer_config",  "action": "INSERT", "objectId":7}
    var payload = s"""{"tableName": "${CustomerConfig.tableName}",  "action": "${EventTypes.INSERT}", "objectId":${customerConfig.id}}""".stripMargin
    var consumerRecord = new ConsumerRecord("", 1, 1, "", payload)

    when(dbApi.one[CustomerConfig](refEq(customerConfig.id), anyString())(refEq(session))).thenReturn(customerConfig)
    assert(dbSync.customerConfigMap.isEmpty)

    payload = s"""{"tableName": "${CustomerConfig.tableName}",  "action": "${EventTypes.INSERT}", "objectId":${customerConfig.id}}""".stripMargin
    consumerRecord = new ConsumerRecord("", 1, 1, "", payload)

    dbSync.processEvent(consumerRecord)
    assert(dbSync.customerConfigMap(userAndCustomer.customerId) == customerConfig)
  }

  test("Customer config map eviction") {
    val payload = s"""{"tableName": "${CustomerConfig.tableName}",  "action": "${EventTypes.DELETE}", "objectId":${customerConfig.id}}""".stripMargin
    val consumerRecord = new ConsumerRecord("", 1, 1, "", payload)
    dbSync.customerConfigMap.put(userAndCustomer.customerId, customerConfig)
    dbSync.processEvent(consumerRecord)
    assert(dbSync.customerConfigMap.getOrElse(customerConfig.customerId.get,None) == None)
  }

}
