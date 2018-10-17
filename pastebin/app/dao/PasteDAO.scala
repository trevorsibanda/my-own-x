
package dao

import javax.inject.Inject

import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile
import scala.concurrent.Future
import play.api.db.slick.DatabaseConfigProvider
import play.api.db.slick.HasDatabaseConfigProvider
import slick.jdbc.JdbcProfile

import java.time.LocalDateTime
import java.sql.Date
import java.util.UUID


import models.{Paste}
import scala.concurrent.{ ExecutionContext, Future }


class PasteDAO @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] {
  import profile.api._

  private val Pastes = TableQuery[PasteTable]

  def all(): Future[Seq[Paste]] = db.run(Pastes.result)

  def find(paste_id: java.util.UUID) = db.run(Pastes.filter(_.id === paste_id).result.headOption)

  def random() = {
    val rand = SimpleFunction.nullary[Double]("rand")
    db.run(Pastes.sortBy(_ => rand).take(1).result.head)

  }

  def delete(uuid: UUID) = db.run(Pastes.filter(_.id === uuid).delete) map{ _ => ()  }

  def insert(dbcb: Paste) = {
    val query = Pastes += dbcb
    db.run(Pastes += dbcb)
  }

  def createSchema = db.run(Pastes.schema.create)

  def dropSchema = db.run(Pastes.schema.drop)

  private class PasteTable(tag: Tag) extends Table[Paste](tag, "Paste") {

    def id = column[java.util.UUID]("id", O.PrimaryKey)
    def name = column[String]("name", O.Default(java.util.UUID.randomUUID.toString), O.Length(1024*1024))
    def content = column[String]("content", O.Default(""))
    def date_created = column[Date]("date_created")

    def * = (id, name, content, date_created) <> (Paste.tupled, Paste.unapply)
  }
}
