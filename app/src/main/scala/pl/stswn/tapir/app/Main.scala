package pl.stswn.tapir.app

import com.raquo.laminar.api.L._
import org.scalajs.dom
import pl.stswn.tapir.model.{Book, Note}

import java.time.format.DateTimeFormatter
import scala.concurrent.ExecutionContext.Implicits.global

object Main {
  def main(args: Array[String]): Unit = {
    documentEvents.onDomContentLoaded.foreach { _ =>
      val appContainer = dom.document.querySelector("#app-content")
      render(appContainer, rootElement)
    }(unsafeWindowOwner)
  }

  private val onEnterPress = onKeyPress.filter(_.keyCode == dom.ext.KeyCode.Enter)
  private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

  def rootElement: HtmlElement = {
    val $books = EventStream.fromFuture(ApiService.getBooks)
    div(
      cls("content"),
      h1("My Library"),
      div(
        children <-- $books.split(_.id)(renderBook)
      )
    )
  }

  def renderBook(bookId: Long, initialBook: Book, $item: EventStream[Book]): HtmlElement = {
    val expandedVar = Var(false)

    div(
      cls("book"),
      h2(
        child.text <-- $item.map(_.title),
        onClick.mapTo(!expandedVar.now()) --> expandedVar.writer
      ),
      div(
        cls("tags"),
        child <-- $item.map(book => book.author.map(a => div(cls("author"), a)).getOrElse(emptyNode)),
        child <-- $item.map(book => book.country.map(c => div(cls("country"), c)).getOrElse(emptyNode)),
        child <-- $item.map(book => book.year.map(y => div(cls("year"), y)).getOrElse(emptyNode)),
        child <-- $item.map(book => book.genre.map(g => div(cls("genre"), g)).getOrElse(emptyNode))
      ),
      child <-- expandedVar.signal.map {
        case false => emptyNode
        case true => renderNotes(bookId)
      }
    )
  }

  def renderNotes(bookId: Long): HtmlElement = {
    val createNoteEventBus = new EventBus[String]
    val deleteNoteEventBus = new EventBus[Long]
    val $notes = EventStream.merge(
      EventStream.fromFuture(ApiService.getNotes(bookId)),
      createNoteEventBus.events.filter(_.nonEmpty).flatMap(text =>
        EventStream.fromFuture(ApiService.addNoteAndRefresh(bookId, text))
      ),
      deleteNoteEventBus.events.flatMap(noteId =>
        EventStream.fromFuture(ApiService.deleteNoteAndRefresh(bookId, noteId))
      )
    )

    div(
      h3("Notes"),
      div(
        cls("new-note"),
        input(
          onMountFocus,
          inContext { thisNode =>
            val commandObserver = createNoteEventBus.writer.contramap[String]{ text =>
              thisNode.ref.value = ""
              thisNode.ref.blur()
              text
            }
            List(
              onEnterPress.mapTo(thisNode.ref.value) --> commandObserver,
              onBlur.mapTo(thisNode.ref.value) --> commandObserver
            )
          }
        )
      ),
      children <-- $notes.split(_.id)(renderNote(deleteNoteEventBus))
    )
  }

  def renderNote(deleteNoteEventBus: EventBus[Long])(noteId: Long, initialNote: Note, $item: EventStream[Note]): HtmlElement = {
    div(
      cls("note"),
      div(cls("note-text"), child.text <-- $item.map(_.text)),
      div(cls("note-date"), child.text <-- $item.map(_.date.format(dateFormatter))),
      button(
        "Delete",
        onClick.mapTo(noteId) --> deleteNoteEventBus.writer
      )
    )
  }
}
