package controllers

import javax.inject._

import actors.{SupervisorActor}
import akka.actor.{ActorSystem, Props}
import akka.pattern.ask
import play.api.mvc.{Action, Controller, WebSocket}
import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import dao._
/**
 * 
 */
@Singleton
class PageController @Inject()(cc: MessagesControllerComponents) extends MessagesAbstractController(cc)  {

  //val bossActor = ActorSystem().actorOf(Props[SupervisorActor])

  /**
   * Handles the site index page.
   *
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */
  def index() = Action { implicit request: MessagesRequest[AnyContent] =>
    Ok(views.html.index())
  }

  /**
   * Stub method. ABout us page
   *
   */
  def about() = index()

  def api() = index()

}

