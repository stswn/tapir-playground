package pl.stswn.tapir.app

import pl.stswn.tapir.endpoints.Endpoints
import pl.stswn.tapir.model.{Book, Note}
import sttp.client3._
import sttp.tapir.Endpoint
import sttp.tapir.client.sttp.SttpClientInterpreter

import scala.concurrent.{ExecutionContext, Future}

object ApiService extends Endpoints {
  private val baseUri = Some(uri"http://localhost:8080")
  private val backend = FetchBackend()

  private def send[I, O](e: Endpoint[I, _, O, Any], i: I)(implicit ec: ExecutionContext): Future[O] =
    SttpClientInterpreter.toRequestThrowErrors(e, baseUri).apply(i).send(backend).map(_.body)

  def getBooks(implicit ec: ExecutionContext): Future[List[Book]] =
    send(getBooksEndpoint, ())

  def getNotes(bookId: Long)(implicit ec: ExecutionContext): Future[List[Note]] =
    send(getNotesEndpoint, bookId)

  def addNote(bookId: Long, text: String)(implicit ec: ExecutionContext): Future[Long] =
    send(addNoteEndpoint, (bookId, text))

  def deleteNote(bookId: Long, noteId: Long)(implicit ec: ExecutionContext): Future[Unit] =
    send(deleteNoteEndpoint, (bookId, noteId))

  def addNoteAndRefresh(bookId: Long, text: String)(implicit ec: ExecutionContext): Future[List[Note]] =
    addNote(bookId, text).flatMap(_ => getNotes(bookId))

  def deleteNoteAndRefresh(bookId: Long, noteId: Long)(implicit ec: ExecutionContext): Future[List[Note]] =
    deleteNote(bookId, noteId).flatMap(_ => getNotes(bookId))
}
