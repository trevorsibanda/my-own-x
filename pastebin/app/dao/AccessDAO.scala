package dao

import javax.inject.Inject
import java.sql.Date
import java.util.UUID

import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile
import scala.concurrent.Future
import play.api.db.slick.DatabaseConfigProvider
import play.api.db.slick.HasDatabaseConfigProvider
import slick.jdbc.JdbcProfile

import models._
import scala.concurrent.{ExecutionContext, Future}

class AccessDAO @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] {
  import profile.api._

  private val Accesss = TableQuery[AccessTable]

  def all(): Future[Seq[PasteAccessRow]] = db.run(Accesss.result)

  def insert(t: PasteAccessRow): Future[Unit] = {
      db.run(Accesss += t).map { _ => () }
  }

  def find(id: java.util.UUID): Future[PasteAccessRow] = {
      db.run(Accesss.filter(_.id === id).result.head)
  }

  def delete(uuid: UUID) = db.run(Accesss.filter(_.id === uuid).delete) map{ _ => ()  }

  def createSchema = db.run(Accesss.schema.create)

  def dropSchema = db.run(Accesss.schema.drop)

  private class AccessTable(tag: Tag) extends Table[PasteAccessRow](tag, "Access") {

    def id = column[java.util.UUID]("id", O.PrimaryKey)
    def deletion_key = column[java.util.UUID]("deletion_key")
    def lifetime = column[Int]("lifetime", O.Default(30))
    def visibility = column[String]("visibility")
    def creation_date = column[Date]("creation_date")
    
    def * = (id, visibility, lifetime, deletion_key, creation_date).mapTo[PasteAccessRow]  //<> (PasteAccessRow.tupled, PasteAccessRow.unapply)
  }
}
