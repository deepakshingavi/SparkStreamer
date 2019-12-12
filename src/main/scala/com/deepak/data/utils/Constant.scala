package com.deepak.data.utils

object Constant {
  //  val SEGMENT_KEY_TENANT_MAP : String = "segment.key.mapping"

  val PUSH_GATEWAY: String = "prometheus.pushgateway"

//  val HTTP_SERVER_PORT: String = "prometheus.http.port"

  val STREAMING_JOB_NAME: String = "spark.app.name"

  val KAFKA_BROKERS: String = "kafka.brokers"

  val TOPIC_NAME = "kafka.input.topic"

  val BEGIN_DATE: String = "BEGIN_DATE"
  val PROPS_PATH = "PROPS_PATH"

  val S3_SOURCE_ACCESS_KEY = "s3.source.access.key"
  val S3_SOURCE_SECRET_KEY = "s3.source.secret.key"


  val SEGMENT_HOST = "segment.http.host"
  val SEGMENT_TRACK_URL = "segment.http.url.track"
  val SEGMENT_IDENTIFY_URL = "segment.http.url.identify"
  val SEGMENT_BATCH_URL = "segment.http.url.batch"
  val SEGMENT_BATCH_SIZE = "segment.http.batch.size"

  val TENANT_RELATED_DATA = "tenant-related-data"
  val EVENT_DATA_FILTERS = "event-data-field-filters"
  val EVENT_TYPE_FIELD_FILTERS = "event-type-field-filters"
  val DB_CONN_URL = "db.conn.url"
  val DB_CONN_USER_NAME = "db.conn.user"
  val DB_CONN_PASSWORD = "db.conn.pwd"

  val PROMETHEUS_HOST = "prometheus.https.host"
  val PROMETHEUS_USER = "prometheus.https.user"
  val PROMETHEUS_PASSWORD = "prometheus.https.pwd"

  val MA_USER_ACTIVITY = "MA.USER_ACTIVITY"
  val SA_OBJECT_CHANGED = "SA.OBJECT_CHANGED"
  val MA_SESSION_CREATION = "MA.SESSION_CREATION"
  val SIGN_UP_DRIVERS_LICENSE_ENTERED = "SIGN_UP_DRIVERS_LICENSE_ENTERED"
  val CUSTOMER_FAILED_BACKGROUND_CHECKS = "CUSTOMER_FAILED_BACKGROUND_CHECKS"
  val CUSTOMER_APPROVED = "CUSTOMER_APPROVED"
  val SESSION_CREATION = "SESSION_CREATION"

  val CHANGE_NAME = 1
  val DO_NOTHING = 2
  val SET_VALUES = 3

  val IS_APPROVE_TO_DRIVE = "is_approved_to_drive"

  val USER_AGREEMENT_CONSENT_DECISIONS = "USER-AGREEMENTS-CONSENT-DECISIONS"
  val USER_AGREEMENT_POLICY_DOCUMENT_AGREEMENTS = "USER-AGREEMENTS-POLICY-DOCUMENT-AGREEMENTS"
  val TRIGGER_BATCH_DURATION = "streaming.job.trigger"

  val PROMOTIONS_ACTIVITY = "PROMOTIONS_ACTIVITY"
  val PAYMENT_LIFECYCLE = "PAYMENT_LIFECYCLE"

  val APP_SESSION_STARTED = "APP_SESSION_STARTED"

  val PROMO_PAYMENT_EVENT_NAMES = Seq(PROMOTIONS_ACTIVITY,PAYMENT_LIFECYCLE)

  val DB_LISTENER_TOPIC_NAME = "db.listener.topic.name"

  val START_OFFSET = "spark.startingOffsets"

  val SOURCE_HAS_NO_NAME = "SOURCE_HAS_NO_NAME"

}
