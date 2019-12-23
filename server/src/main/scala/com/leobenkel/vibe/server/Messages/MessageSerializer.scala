package com.leobenkel.vibe.server.Messages

import akka.http.scaladsl.marshalling._
import akka.http.scaladsl.model.StatusCodes
import com.leobenkel.vibe.core.Messages.Message
import io.circe.generic.auto._
import io.circe.syntax._

trait MessageSerializer extends Message with ToResponseMarshallable {
  def toJsonString: String = this.toSimpleMessage.asJson.noSpaces.toString

  override type T = String

  override def value: T = this.toJsonString

  implicit override def marshaller: ToResponseMarshaller[T] =
    Marshaller.fromToEntityMarshaller(status = StatusCodes.OK, headers = Nil)
}
