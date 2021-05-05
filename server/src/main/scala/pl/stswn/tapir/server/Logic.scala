package pl.stswn.tapir.server

import pl.stswn.tapir.model.{Book, NotFoundException, LibraryException, Note, UnknownLibraryException}
import zio.clock.Clock
import zio.stm.{STM, TMap}
import zio.{IO, UIO, ZIO, ZLayer}
import zio.macros.accessible

@accessible
object Logic {
  trait Service {
    def getBooks: IO[LibraryException, List[Book]]

    def getNotes(bookId: Long): IO[LibraryException, List[Note]]

    def saveNote(bookId: Long, text: String): IO[LibraryException, Long]

    def deleteNote(bookId: Long, noteId: Long): IO[LibraryException, Unit]
  }

  val live: ZLayer[Clock, Nothing, Logic] = {
    for {
      clock <- ZIO.service[Clock.Service]
      books <- TMap.empty[Long, (Book, List[Note])].commit
      noteSeqs <- TMap.empty[Long, Long].commit
      _ <- ZIO.foreach_(Book.sampleBooks)(book => books.put(book.id, (book, List.empty)).commit)
    } yield new Service {
      override def getBooks: UIO[List[Book]] = (for {
        booksWithNotes <- books.values
      } yield booksWithNotes.map(_._1).sortBy(_.id)).commit

      override def getNotes(bookId: Long): IO[LibraryException, List[Note]] = (for {
        bookWithNotes <- books.get(bookId).someOrFail(NotFoundException(bookId))
      } yield bookWithNotes._2).commit

      override def saveNote(bookId: Long, text: String): IO[LibraryException, Long] = for {
        date <- clock.localDateTime.orElseFail(UnknownLibraryException)
        noteId <- (for {
          prevNoteId <- noteSeqs.getOrElse(bookId, 0L)
          currNoteId = prevNoteId + 1L
          oldBook <- books.get(bookId).someOrFail(NotFoundException(bookId))
          newBook = oldBook.copy(_2 = Note(currNoteId, text, date) :: oldBook._2)
          _ <- books.put(bookId, newBook)
          _ <- noteSeqs.put(bookId, currNoteId)
        } yield currNoteId).commit
      } yield noteId

      override def deleteNote(bookId: Long, noteId: Long): IO[LibraryException, Unit] = (for {
        book <- books.get(bookId).someOrFail(NotFoundException(bookId))
        oldNotes = book._2
        _ <- STM.fromOption(oldNotes.find(_.id == noteId)).orElseFail(NotFoundException(bookId, Some(noteId)))
        newNotes = oldNotes.filterNot(_.id == noteId)
        _ <- books.put(bookId, (book._1, newNotes))
      } yield ()).commit
    }
  }.toLayer
}
