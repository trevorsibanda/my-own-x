package models

import java.util.UUID
import java.time.LocalDateTime
import java.sql._

trait Content{
    val id: UUID
    val content: String
    val date_created: Date
}


case class Paste(val id: UUID, val name: String, val content: String, val date_created: Date = java.sql.Date.valueOf((LocalDateTime.now).toLocalDate)) extends Content{
    override def toString = {
        s"Paste($id, '$name', '<content>', $date_created)"
    }
}


