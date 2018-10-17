package controllers

import java.time.LocalDateTime
import java.util.UUID

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
import scala.util.{Try, Success, Failure}

import dao._
import models._
import actors._
/**
 * 
 */
@Singleton
class ContentController @Inject()(pasteDao: PasteDAO, accessDao: AccessDAO, searchDao: SearchDAO, cc: MessagesControllerComponents) extends MessagesAbstractController(cc) {

  val bossActor = ActorSystem().actorOf(Props[SupervisorActor])

  def install = Action.async{ implicit request: MessagesRequest[AnyContent] =>
    pasteDao.createSchema flatMap{ _ => 
      accessDao.createSchema flatMap{ _ => 
        searchDao.createSchema map{ _ => 
          Ok("Created schema and elastic index")
         }
        }
    }
    
  
  }

  def view(paste_id: String) = Action.async { implicit request: MessagesRequest[AnyContent] =>
    Try(java.util.UUID.fromString(paste_id)) match{
      case Success(uuid) => pasteDao.find(uuid) flatMap{
        case Some(paste) => accessDao.find(uuid) map{
          case ac_key => Ok(views.html.view(Some(paste), PasteAccess.fromRow(ac_key), request.flash.get("show_access_details").getOrElse("xx").isEmpty ))
        }
        case None => Future( NotFound(views.html.page404(s"Oh no, failed to find paste $uuid")))
      }
      case Failure(err) => Future( NotFound(views.html.page404("This page never existed")) )
    }
    
  }

  def search() = Action{ implicit request: MessagesRequest[AnyContent] => 
    val query: String = request.getQueryString("query") match{
      case Some(s) => s
      case None => ""
    }
    if(query.length < 3){
      Ok(views.html.search(query, Seq()))
    }else{
      searchDao.log_search(query) map{ _ => ()}
      val results = searchDao.query(query)
      Ok(views.html.search(query, results))
    }
  
  }

  def random() = Action.async { implicit request: MessagesRequest[AnyContent] =>
    Try(pasteDao.random()) match{
      case Success(query) => query map{ paste => 
        Redirect(routes.ContentController.view(paste.id.toString))
      }
      case Failure(err) => {
        Logger.warn(s"Failed to fetch random paste: $err ")
        Future( InternalServerError(views.html.page500("Something went wrong")))
      }
    } 
  }

  /**
   * Stub method. ABout us page
   *
   */
  case class NewPaste(val title: String, val content: String, val lifetime: Int, val visibility: String) 
  val createForm: Form[NewPaste] =  Form(
    mapping(
      "title" -> nonEmptyText,
      "content" -> nonEmptyText,
      "lifetime" -> number,
      "visibility" -> nonEmptyText
    )(NewPaste.apply)(NewPaste.unapply) verifying("Failed form constraints!", fields => fields match{
      case formData => ! (Seq(
          (formData.title.length >= 3 && formData.title.length <= 255 ),
          (formData.content.length >= 3 && formData.content.length <= 1024*1024),
          Seq("public", "private").contains(formData.visibility), 
          Seq(1, 3, 7, 14, 30, 90, 120, -1).contains(formData.lifetime),
        ).contains(false)) 
    })
  )

  def create() = Action.async{ implicit request: MessagesRequest[AnyContent] => 
    val success = { data: NewPaste =>
      Logger.debug(s"Received post data $data")
      val paste: Paste = new Paste(java.util.UUID.randomUUID, data.title, data.content, java.sql.Date.valueOf((LocalDateTime.now).toLocalDate))
      val access: PasteAccess = PasteAccess.fromRow( PasteAccessRow(paste.id, data.visibility, data.lifetime, java.util.UUID.randomUUID, paste.date_created))
      pasteDao.insert(paste) flatMap{
      case _ => {
          accessDao.insert(access.toRow) map{ _ => 
            if(access.visibility == Visibility.Public())
              searchDao.index_item(paste) 
            else
              Logger.info(s"Not indexing paste $paste because it is not public")
            bossActor ! PasteCreated(paste, access)  
            Redirect(routes.ContentController.view(paste.id.toString)).flashing("show_access_details" -> "", "message" -> "Successfully created new paste")
          }
      }
    }  
    }

    val failure = { erroredForm: Form[NewPaste] =>
      Logger.debug(s"Received form content with errors $erroredForm")
      Future( BadRequest("Your form failed validation. Please try again after correcting errors"))
    }
    
    val result: Form[NewPaste] = createForm.bindFromRequest
    result.fold(failure, success)	
  }

  def delete(paste_id: String) = Action.async{ implicit request: MessagesRequest[AnyContent] =>
      val deletion_key: String = request.getQueryString("deletion_key") match{
        case Some(s) => s
        case None => ""
      }    
      Try( (UUID.fromString(paste_id), UUID.fromString(deletion_key) )) match{
        case Success( (uuid, delete_key)) => accessDao.find(uuid) flatMap{ access => 
              ( access.deletion_key == delete_key) match{
                case true => {
                  Logger.debug("Deleted paste $access")
                  pasteDao.delete(uuid) flatMap{ _ =>
                    accessDao.delete(uuid) map{ _ =>
                      searchDao.remove_item_from_index(uuid)
                      bossActor ! PasteDestroyed(Paste(uuid,"",""), access.toPasteAccess)
                      Redirect(routes.PageController.index()).flashing("message" -> "Successfully deleted paste")
                    }
                  }
                }
                case false => {
                   Future( NotFound("Cannot delete non existant paste")) 
                }
              }
          }
          case Failure(_) => Future( InternalServerError("Invalid paste or deletion_key UUID"))
        }
  }
}

