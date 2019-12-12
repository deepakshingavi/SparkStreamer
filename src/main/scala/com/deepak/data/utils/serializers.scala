package com.deepak.data.utils

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.{DeserializationContext, JsonSerializer, KeyDeserializer, SerializerProvider}
import com.deepak.data.model.UserIdCustomerId

class UserIdCustomerIdSerializer extends JsonSerializer[UserIdCustomerId] {
  override def serialize(value: UserIdCustomerId, gen: JsonGenerator, serializers: SerializerProvider): Unit = {
    gen.writeFieldName(s"${value.userId}:::${value.customerId}")
  }
}

class UserIdCustomerIdDeserializer extends KeyDeserializer {
  override def deserializeKey(key: String, ctxt: DeserializationContext): AnyRef = {
    val tokens = key.split(":::")
    UserIdCustomerId(userId = tokens(0), customerId = tokens(1))
  }
}