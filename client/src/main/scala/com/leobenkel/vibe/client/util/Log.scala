package com.leobenkel.vibe.client.util

import com.leobenkel.vibe.client.app.Config

object Log {
  // TODO: hook that somewhere in the deployment, or application.json configuration
  val WithDebug: Boolean = false

  sealed trait LogLevel
  case object INFO extends LogLevel
  case object DEBUG extends LogLevel
  case object ERROR extends LogLevel

  def info(msg:  => String): Unit = printToConsole(INFO, msg)
  def debug(msg: => String): Unit = printToConsole(DEBUG, msg)
  def error(msg: => String): Unit = printToConsole(ERROR, msg)

  private def printToConsole(
    level: LogLevel,
    msg:   => String
  ): Unit = level match {
    case INFO  => System.out.println(s"[$level] $msg")
    case DEBUG => if (WithDebug) System.out.println(s"[$level] $msg")
    case ERROR => System.err.println(s"[$level] $msg")
  }
}
