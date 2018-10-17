package models

import java.sql.Timestamp
import java.util.{UUID, Calendar}


case class SearchQuery(
    val query: String, 
    val id: UUID = UUID.randomUUID, 
    val  when: Timestamp = new Timestamp(Calendar.getInstance.getTime.getTime), 
    val result_count: Int = 0)

case class SearchResult(
    val entry_id: UUID,
    val entry_title: String,
    val entry_content: String,
) 