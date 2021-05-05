package pl.stswn.tapir.server

import org.http4s.HttpRoutes
import pl.stswn.tapir.endpoints.Endpoints
import sttp.tapir.server.http4s.ztapir.ZHttp4sServerInterpreter
import sttp.tapir.ztapir._
import zio.RIO
import zio.clock.Clock
import zio.interop.catz._
import cats.syntax.all._
import sttp.tapir.swagger.http4s.SwaggerHttp4s

object Routes extends Endpoints {

  private val docsYaml = {
    import sttp.tapir.docs.openapi.OpenAPIDocsInterpreter
    import sttp.tapir.openapi.circe.yaml._
    OpenAPIDocsInterpreter.toOpenAPI(
      List(
        getBooksEndpoint, getNotesEndpoint, addNoteEndpoint, deleteNoteEndpoint
      ),
      "Library", "1.0"
    ).toYaml
  }

  val routes: HttpRoutes[RIO[Logic with Clock, *]] =
    ZHttp4sServerInterpreter.from(
      List(
        getBooksEndpoint.zServerLogic(_ => Logic.getBooks),
        getNotesEndpoint.zServerLogic(Logic.getNotes),
        addNoteEndpoint.zServerLogic{ case (bookId, text) => Logic.saveNote(bookId, text)},
        deleteNoteEndpoint.zServerLogic{ case (bookId, noteId) => Logic.deleteNote(bookId, noteId)}
      )
    ).toRoutes <+>
      new SwaggerHttp4s(docsYaml).routes
}
