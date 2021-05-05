package pl.stswn.tapir.model

import java.time.LocalDateTime

case class Note(
                 id: Long,
                 text: String,
                 date: LocalDateTime
               )
