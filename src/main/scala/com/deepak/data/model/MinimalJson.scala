package com.deepak.data.model

import java.time.ZonedDateTime

case class MinimalJson(customerId : String, timeStamp : ZonedDateTime, logType : String, logContext : String, userId : String) {
  override def toString =
    s"""{
       | "customerId" : "${customerId}",
       |  "timeStampStr" : "${timeStamp}",
       |  "logType" : "${logType}",
       |  "logContext" : ${logContext},
       |  "userId" : "${userId}",
       |}""".stripMargin
}
