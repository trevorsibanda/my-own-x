package dao

import javax.inject.Inject
import java.sql.Timestamp

import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile
import scala.concurrent.Future
import play.api.db.slick.DatabaseConfigProvider
import play.api.db.slick.HasDatabaseConfigProvider

import play.api.Logger

import com.sksamuel.elastic4s.RefreshPolicy
import com.sksamuel.elastic4s.RefreshPolicy
import com.sksamuel.elastic4s.http.{ElasticClient, ElasticProperties}
import com.sksamuel.elastic4s.http.Response
import com.sksamuel.elastic4s.http.search.SearchResponse
//import com.sksamuel.elastic4s.embedded.LocalNode
import com.sksamuel.elastic4s.http.search.SearchResponse
//import com.sksamuel.elastic4s.http.search.{RequestFailure, RequestSuccess}


import models.{SearchResult, SearchQuery, Paste}
import scala.concurrent.{ ExecutionContext, Future }


class SearchDAO @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] {
  import profile.api._
  import com.sksamuel.elastic4s.http.ElasticDsl._
  
  lazy val client = ElasticClient(ElasticProperties("http://localhost:9200"))

  def createSchema = {
    elastic_create_indices
    db.run(SearchTable.schema.create)
  }

  def elastic_create_indices = {
    client.execute{
      createIndex("pasty").mappings(
        mapping("pastes").fields(
          textField("id"),
          textField("title"),
          textField("content")
        )
      )
    }.await
  }

  def elastic_index(paste: Paste) = client.execute{
    indexInto("pasty" / "pastes").fields("id" -> paste.id.toString, "title" -> paste.name, "content" -> paste.content).refresh(RefreshPolicy.Immediate)
  }.await

  def elastic_query(q: String) = client.execute{
    search("pasty") matchQuery("content", q)
  }.await

  

  private val SearchTable = TableQuery[SearchTable]

  def all(): Future[Seq[SearchQuery]] = db.run(SearchTable.result)

  def find(id: java.util.UUID): Future[Option[SearchQuery]] = db.run(SearchTable.filter(_.id === id).result.headOption)

  def query(q: String): Seq[SearchResult] = {
    val results: Response[SearchResponse] = elastic_query(q)
    val l = results.result.hits.hits.toList map{item => 
      val mapped = item.sourceAsMap
      println(mapped)
      new SearchResult(java.util.UUID.fromString(mapped("id").asInstanceOf[String]), mapped.getOrElse("title", "<no-title>").asInstanceOf[String], mapped("content").asInstanceOf[String].take(255))
    }
    l
  }

  def log_search(q: String, num_results: Int = 0) = {
    val entry = new SearchQuery(q, result_count = num_results)
    insert(entry)
  }

  def index_item(paste: Paste) = {
    elastic_index(paste)
  }

  def insert(i: SearchQuery) = db.run(SearchTable += i)

  def delete(id: java.util.UUID) = db.run(SearchTable.filter(_.id === id).delete)

  def remove_item_from_index(uuid: java.util.UUID) = {
    
  }

  def dropSchema = db.run(SearchTable.schema.drop)

  private class SearchTable(tag: Tag) extends Table[SearchQuery](tag, "SearchQuery") {

    def id = column[java.util.UUID]("id", O.PrimaryKey)
    def query = column[String]("query", O.Length(255))
    def when = column[Timestamp]("evt_time")
    def result_count = column[Int]("result_count", O.Default(0))

    def * = (query, id, when, result_count) <> (SearchQuery.tupled, SearchQuery.unapply)
  }
}
