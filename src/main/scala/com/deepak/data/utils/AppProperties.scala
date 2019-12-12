package com.deepak.data.utils

import java.io.{File, FileInputStream, IOException}
import java.util.Properties

import org.slf4j.LoggerFactory

object AppProperties {

}

class AppProperties @throws[IOException]
(propsFilePath: FileInputStream) extends Serializable {
  private val logger = LoggerFactory.getLogger(classOf[AppProperties])

  private val properties: Properties = new Properties
  try properties.load(propsFilePath)
  catch {
    case e: Exception =>
      logger.error("Application path=" + new File("").getAbsolutePath, e)
      throw e
  }


  def get(key: String): String = properties.getProperty(key).trim

}
