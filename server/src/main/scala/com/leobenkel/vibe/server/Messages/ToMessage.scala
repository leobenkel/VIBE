package com.leobenkel.vibe.server.Messages

import akka.http.scaladsl.model._
import akka.http.scaladsl.server.{RequestContext, Route}
import com.leobenkel.vibe.core.Messages.MessageStatus
import io.circe.Encoder

import scala.reflect.ClassTag

object ToMessage {
  case class RichMessage[A: ClassTag](
    operation: String,
    status:    MessageStatus,
    fieldName: String
  )(
    content: => A
  )(
    implicit val encodeA: Encoder[A]
  ) extends RichMessageSerializer[A] {
    lazy final override val getContent: A = content
    lazy final override val classTagA:  ClassTag[A] = implicitly
  }

  def makeError(
    statusCodes: StatusCode,
    message:     String,
    url:         RequestContext => String = _.request._2.path.toString
  ): Route = { request: RequestContext =>
    request.complete {
      HttpResponse.apply(
        status = statusCodes,
        headers = Nil,
        entity = HttpEntity.apply(
          ContentTypes.`application/json`,
          ToMessage.ErrorMessage(url(request))(message).toJsonString
        )
      )
    }
  }

  //    operation: String
//  )(
//    message:   String,
//    fieldName: String
//  )(
//    content: => A
//  )(
//    implicit encoder: Encoder[A]
//  ): MessageWithContent[A] = {
//    MessageWithContent[A](
//      operation,
//      MessageStatus.Failure(message),
//      fieldName
//    )(
//      content
//    )
//  }
  case class ErrorMessage(operation: String)(message: => String) extends MessageSerializer {
    lazy final override val status: MessageStatus = MessageStatus.Failure(message)
  }

  case class RichErrorMessage[A: ClassTag](
    operation: String,
    message:   String,
    fieldName: String
  )(
    content: => A
  )(
    implicit val encodeA: Encoder[A]
  ) extends RichMessageSerializer[A] {
    lazy final override val getContent: A = content
    lazy final override val status:     MessageStatus = MessageStatus.Failure(message)
    lazy final override val classTagA:  ClassTag[A] = implicitly
  }

  //  case class ErrorMessage[A: ClassTag](
  //    operation: String
  //  )(
  //    message:   String,
  //    fieldName: String
  //  )(
  //    content: => A
  //  )
}
