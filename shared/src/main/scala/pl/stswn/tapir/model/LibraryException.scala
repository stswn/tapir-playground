package pl.stswn.tapir.model

sealed trait LibraryException
case class NotFoundException(bookId: Long, noteId: Option[Long] = None) extends LibraryException
case object UnknownLibraryException extends LibraryException
