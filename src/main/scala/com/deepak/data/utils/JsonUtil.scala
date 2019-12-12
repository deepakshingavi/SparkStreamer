package com.deepak.data.utils

import com.deepak.data.model.UserIdCustomerId
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.{DeserializationFeature, ObjectMapper}
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper

object JsonUtil {
  val mapper = new ObjectMapper() with ScalaObjectMapper
  mapper.registerModule(DefaultScalaModule)
  mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

  val mapKeySerializers = new SimpleModule()
  mapKeySerializers.addKeySerializer(classOf[UserIdCustomerId], new UserIdCustomerIdSerializer())
  mapKeySerializers.addKeyDeserializer(classOf[UserIdCustomerId], new UserIdCustomerIdDeserializer())

  mapper.registerModule(mapKeySerializers)

  def toJson(value: Map[Symbol, Any]): String = {
    toJson(value map { case (k, v) => k.name -> v })
  }

  def toJson(value: Any): String = {
    mapper.writeValueAsString(value)
  }

  def toMap[V](json: String)(implicit m: Manifest[V]) = fromJson[Map[String, V]](json)

  def fromJson[T](json: String)(implicit m: Manifest[T]): T = {
    mapper.readValue[T](json)
  }
}


object MarshallableImplicits {

  implicit class Unmarshallable(unMarshallMe: String) {
    def toMap: Map[String, Any] = JsonUtil.toMap(unMarshallMe)

    //def toMapOf[V]()(implicit m: Manifest[V]): Map[String,V] = JsonUtil.toMapOf[V](unMarshallMe)
    def fromJson[T]()(implicit m: Manifest[T]): T = JsonUtil.fromJson[T](unMarshallMe)
  }

  implicit class Marshallable[T](marshallMe: T) {
    def toJson: String = JsonUtil.toJson(marshallMe)
  }

}
