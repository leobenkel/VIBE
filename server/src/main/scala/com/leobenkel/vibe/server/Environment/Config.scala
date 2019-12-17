package com.leobenkel.vibe.server.Environment

import better.files.File
import com.typesafe.config.ConfigFactory

trait Config {
  def config: Config.Service
}

object Config {

  trait Service {
    protected def configKey: String
    protected def config:    com.typesafe.config.Config
    def getLocalString(key: String): String = config.getString(s"$configKey.$key")
    def getLocalInt(key:    String): Int = config.getInt(s"$configKey.$key")
  }

  trait ConfigLive extends Config.Service {
    lazy final val configKey: String = "vibe"
    lazy final val config: com.typesafe.config.Config = {
      val confFileName =
        System.getProperty("application.conf", "./src/main/resources/application.conf")
      val confFile = File(confFileName)
      val config = ConfigFactory
        .parseFile(confFile.toJava)
        .withFallback(ConfigFactory.load())
      config
    }

  }

  trait Live extends Config {
    override def config: Service = new ConfigLive {}
  }
}
