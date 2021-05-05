package pl.stswn.tapir.server.akka

import akka.http.scaladsl.server.Route
import akka.stream.scaladsl.{Flow, Sink, Source}
import io.circe.generic.auto._
import io.circe.syntax._
import pl.stswn.tapir.endpoints.Endpoints
import pl.stswn.tapir.model.Book
import sttp.capabilities.WebSockets
import sttp.capabilities.akka.AkkaStreams
import sttp.model.sse.ServerSentEvent
import sttp.tapir.{endpoint, _}
import sttp.tapir.generic.auto._
import sttp.tapir.json.circe._
import sttp.tapir.server.akkahttp.{AkkaHttpServerInterpreter, serverSentEventsBody}

import scala.concurrent.Future

object Routes extends Endpoints {

  val getBooksRoute: Route = AkkaHttpServerInterpreter.toRoute(getBooksEndpoint) { _ =>
    Future.successful(Right(Book.sampleBooks))
    // Future.successful(Left(UnknownLibraryException)) -> type checked business logic error
    // Future.failed(new RuntimeException("Aaaah")) -> unchecked exception resulting in 500 response
  }

  // ---- Streaming ----

  // Web Sockets
  case class Incoming(message: String)
  val wsGetBooksEndpoint:
    Endpoint[Unit, Unit, Flow[Incoming, Book, Any], AkkaStreams with WebSockets] =
    endpoint
      .in("api" / "books")
      .get
      .out(webSocketBody[Incoming, CodecFormat.Json, Book, CodecFormat.Json](AkkaStreams))

  val wsRoute: Route = AkkaHttpServerInterpreter.toRoute(wsGetBooksEndpoint){ _ =>
    Future.successful(Right(
      Flow.fromSinkAndSource(
        Sink.ignore,
        Source.repeat(
          Book(0, "Fifty Shades of Grey", Some("E.L. James"), None, None, None)
        ).take(50)
      )
    ))
  }

  // SSE
  val sseBooksEndpoint: Endpoint[Unit, Unit, Source[ServerSentEvent, Any], AkkaStreams] =
    endpoint
      .in("api" / "books")
      .get
      .out(serverSentEventsBody)

  val sseRoute: Route = AkkaHttpServerInterpreter.toRoute(sseBooksEndpoint){ _ =>
    Future.successful(Right(
      Source.repeat(
        Book(0, "Fifty Shades of Grey", Some("E.L. James"), None, None, None)
      ).take(50)
        .map(book => ServerSentEvent(data = book.asJson.asString))
    ))
  }
}
