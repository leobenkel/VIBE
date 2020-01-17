package com.leobenkel.vibe.client.app

import io.circe.Json
import japgolly.scalajs.react.AsyncCallback
import japgolly.scalajs.react.extra.Ajax

object Config {
  lazy private val FilePath:  String = "./resources/application.json"
  lazy private val ConfigKey: String = "vibe"

  private def get(
    json: Json,
    key:  String
  ): Either[String, Json] = {
    json.\\(key).headOption match {
      case None    => Left(s"Did not find key '$key'")
      case Some(k) => Right(k)
    }
  }

  def getKey(key: String): AsyncCallback[Either[String, Json]] = {
    Ajax
      .get(FilePath)
      .setRequestContentTypeJsonUtf8
      .send
      .asAsyncCallback
      .map { xhr =>
        io.circe.parser
          .parse(xhr.responseText)
          .left.map(_.toString)
          .flatMap(get(_, ConfigKey))
          .flatMap(get(_, key))
      }
  }
}
