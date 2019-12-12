package com.deepak.data

import org.apache.kafka.clients.consumer.ConsumerRecord
import com.deepak.data.model._

trait Fixtures {
  val userId = "u1"
  val customerId = "t1"
  val userAndCustomer: UserIdCustomerId = UserIdCustomerId(userId, customerId)

  val tenantConfigJson: CustomerSettingsMap = CustomerSettingsMap("tse", "swk", "advid", "spswk", List.empty, "ck", "pk", Map.empty)
  val customerConfig: CustomerConfig = CustomerConfig(1L, Some(customerId), tenantConfigJson)

  val consumerRecord = new ConsumerRecord("", 1, 1, "foo", "bar")

}
