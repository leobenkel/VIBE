package com.leobenkel.vibe.client.routes.pages

import com.leobenkel.vibe.client.app.AppState
import com.leobenkel.vibe.client.routes.Framework.RouteEngine.{BackendCore, StateCore}
import com.leobenkel.vibe.client.routes.Framework.{RouteEngine, RouteTrait}
import com.leobenkel.vibe.client.util.{ErrorProtection, Log}
import com.leobenkel.vibe.core.Messages.{ContentS, MessageWithContentForJson}
import com.leobenkel.vibe.core.Schemas.Traits.{SchemaBase, TableRef}
import com.leobenkel.vibe.core.Utils.SchemaTypes.TABLE_NAME
import io.circe._
import japgolly.scalajs.react.CtorType.ChildArg
import japgolly.scalajs.react._
import japgolly.scalajs.react.extra.Ajax
import japgolly.scalajs.react.vdom.html_<^._
import org.scalajs.dom
import org.scalajs.dom.raw.HTMLButtonElement
import typingsJapgolly.semanticDashUiDashReact.components._
import typingsJapgolly.semanticDashUiDashReact.distCommonjsElementsButtonButtonMod.ButtonProps
import ujson.Value.InvalidData

import scala.scalajs.js
import scala.util._

/**
  * A "page" in the application, in this same directory you'd put all of the other application "pages".
  * These are not html pages per se, since we're dealing with a single page com.leobenkel.vibe.client.app. But it's useful to treat
  * each of these as pages internally.
  */
trait ListPageForTable[PK, T <: SchemaBase[PK]] extends RouteTrait {
  case class State(
    objects:             Seq[T] = Seq.empty,
    override val errors: Option[String] = None
  ) extends StateCore

  case class Backend(override val $ : BackendScope[_, State]) extends BackendCore[State] {
    final override def init(state: State): Callback = Callback.empty

    lazy private val decoderContent: Decoder[ContentS[T]] = (c: HCursor) =>
      ErrorProtection {
        Log.debug(s"Content: ${c.keys}")
        for {
          length <- c.downField("length").as[Int]
          _ = Log.debug(s"Length: $length")
          itemsJ <- c.downField("items").as[Seq[Json]]
          _ = Log.debug(s"i: $itemsJ")
          items = itemsJ.map(_.as[T](decoderT).right.get)
        } yield {
          Log.debug(s"Items: $items")
          ContentS(items = items, length = length)
        }
      }

    lazy private val decoderMessage: Decoder[ReturnType] = (c: HCursor) =>
      ErrorProtection {
        Log.debug(s"MessageKeys: ${c.keys}")
        for {
          operation    <- c.downField("operation").as[String]
          success      <- c.downField("success").as[Boolean]
          errorMessage <- c.downField("errorMessage").as[Option[String]]
          items        <- c.downField(tableName).as[ContentS[T]](decoderContent)
        } yield {
          MessageWithContentForJson(
            operation = operation,
            success = success,
            errorMessage = errorMessage,
            content = items
          )
        }
      }

    final override def refreshImpl(
      host:  String,
      state: State
    ): AsyncCallback[CallbackTo[Unit]] =
      Ajax
        .get(s"http://$host/api/$tableName/all")
        .setRequestContentTypeJsonUtf8
        .send
        .asAsyncCallback
        .map { xhr =>
          try {
            Log.debug(s"Raw: ${xhr.responseText}")
            val output = io.circe.parser.decode[ReturnType](xhr.responseText)(decoderMessage)
            Log.debug(s"Output: $output")
            $.modState(_.copy(objects = output.right.get.content.items))
          } catch {
            case e: InvalidData =>
              dom.console.error(e.msg + ":" + e.data)
              throw e
          }
        }

    private def onAddNewObject(
      event: ReactMouseEventFrom[HTMLButtonElement],
      data:  ButtonProps
    ): Callback =
      // TODO: Redirect to insert form
      Callback.alert(
        "Clicked on 'Add New object'... did you expect something else? hey, " +
          "I can't write everything for you!"
      )

    final override def renderImpl(
      appState: AppState,
      state:    State
    ): VdomElement = {
      <.div(
        Table()(
          TableHeader()(
            TableRow()(
              getHeaderColumns.toVdomArray { n =>
                TableHeaderCell(key = n.name)(VdomNode.cast(n.name))
              }
            )
          ),
          TableBody()(
            state.objects.toVdomArray { obj =>
              TableRow(key = obj.id.toString)(
                getTableValues(obj).zipWithIndex.toVdomArray {
                  case (r, idx) =>
                    TableCell(key = s"${obj.id.toString}-$idx")(r)
                }
              )
            }
          )
        ),
        Button(onClick = onAddNewObject)("Add new object")
      )
    }

    lazy final override val owner: RouteTrait = selfRoute

    final override def setError(
      state:    State,
      newError: String
    ): State = {
      state.copy(errors = Some(newError))
    }
  }

  lazy final override val engine: RouteEngine.Engine[_] = new RouteEngine[State, Backend] {
    lazy final override protected val owner:     RouteTrait = selfRoute
    lazy final override protected val initState: State = State()

    override protected def initBackend(initState: BackendScope[Unit, State]): Backend =
      Backend(initState)
  }.asInstanceOf[RouteEngine.Engine[_]]

  protected type ReturnType = MessageWithContentForJson[ContentS[T]]

  protected def getTableRef: TableRef[PK, T]

  lazy private val tableName:   TABLE_NAME = getTableRef.getTableName
  lazy final override val name: TABLE_NAME = tableName
  lazy final override val url:  TABLE_NAME = name

  lazy final protected val getHeaderColumns: Array[Symbol] =
    ErrorProtection(getTableRef.getHeaderColumns)

  final protected def getTableValues(obj: T): Array[ChildArg] = ErrorProtection {
    getTableRef.getTableValues(obj).map {
      case a: Long =>
        Try {
          // TODO: Improve the display of dates here.
          val date = new js.Date(a.toDouble)
          s"${date.toLocaleDateString()} ${date.toLocaleTimeString()}"
        } match {
          case Success(value) => VdomNode.cast(value)
          case Failure(exception) =>
            exception.printStackTrace()
            VdomNode.cast(s"Failed: ${exception.toString}")
        }
      case a: Boolean => VdomNode.cast(a.toString)
      case a => VdomNode.cast(a)
    }
  }

  type DecodingType = T
  protected def decoderT: Decoder[DecodingType]

}

object ListPageForTable {
  type SchemaAllPage[A] = ListPageForTable[A, SchemaBase[A]]
}
