package controllers.api.v1

import javax.inject._

import actors.{SupervisorActor}
import akka.actor.{ActorSystem, Props}
import akka.pattern.ask
import play.api.mvc.{Action, Controller, WebSocket}
import play.api._
import play.api.mvc._
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
class ContentController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {

  val bossActor = ActorSystem().actorOf(Props[SupervisorActor])

  /**
   * Handles the site index page.
   *
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */
  def view(paste_id: String) = TODO
  /**
   * Stub method. ABout us page
   *
   */
  def create() = TODO

  def delete(paste_id: String) = TODO

}

