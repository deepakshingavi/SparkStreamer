package com.deepak.data.utils

import spray.json.{DefaultJsonProtocol, JsArray, JsFalse, JsNull, JsNumber, JsObject, JsString, JsTrue, JsValue, JsonFormat}

object M2eeJsonProtocol extends DefaultJsonProtocol {

  implicit object AnyJsonFormat extends JsonFormat[Any] {
    def write(x: Any): JsValue = x match {
      case n: Int => JsNumber(n)
      case s: String => JsString(s)
      case b: Boolean if b => JsTrue
      case b: Boolean if !b => JsFalse
    }
    def read(value: JsValue) = value match {
      case JsNull => null
      case JsNumber(n) => n.getClass.getName match {
        case "scala.math.BigDecimal" => n.bigDecimal
        case _ => n.intValue()
      }
      case JsString(s) => s
      case JsTrue => true
      case JsFalse => false
      case JsArray(elements) => elements.map(read)
      case JsObject(fields) => fields.toMap
    }
  }
}
