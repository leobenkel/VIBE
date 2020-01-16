package com.leobenkel.vibe.client.util

import scala.util._

object ErrorProtection {
  def apply[A](f: => A): A = {
    Try(f) match {
      case Success(value) => value
      case Failure(exception) =>
        exception.printStackTrace()
        throw exception
    }
  }
}
