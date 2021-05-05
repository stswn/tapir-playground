package pl.stswn.tapir.endpoints

import io.circe.generic.auto._
import pl.stswn.tapir.model.{Book, LibraryException, NotFoundException, Note, UnknownLibraryException}
import sttp.model.StatusCode
import sttp.tapir._
import sttp.tapir.generic.auto._
import sttp.tapir.json.circe._

trait Endpoints {
  private val booksEndpoint = endpoint
    .in("api" / "books")
    .errorOut(
      oneOf[LibraryException](
        oneOfMapping(StatusCode.NotFound, jsonBody[NotFoundException].description("Indicates that the object does not exist")),
        oneOfMapping(StatusCode.InternalServerError, emptyOutputAs(UnknownLibraryException))
      )
    )

  val getBooksEndpoint: Endpoint[Unit, LibraryException, List[Book], Any] =
    booksEndpoint
      .get
      .out(jsonBody[List[Book]].description("List of all books in the library"))
      .description("Get all books")

  private val notesEndpoint =
    booksEndpoint
      .in(path[Long].name("Book ID").description("Book identifier"))
      .in("notes")

  val getNotesEndpoint: Endpoint[Long, LibraryException, List[Note], Any] =
    notesEndpoint
      .get
      .out(jsonBody[List[Note]].description("List of book notes"))
      .description("Get all notes for the specified book")

  val addNoteEndpoint: Endpoint[(Long, String), LibraryException, Long, Any] =
    notesEndpoint
      .in(stringBody.validate(Validator.MinLength(3)).description("Note text content"))
      .post
      .out(jsonBody[Long].description("New note ID"))
      .description("Create new note for the specified book")

  val deleteNoteEndpoint: Endpoint[(Long, Long), LibraryException, Unit, Any] =
    notesEndpoint
      .in(path[Long].name("Note ID").description("Note identifier"))
      .delete
      .description("Delete specified note")
}
