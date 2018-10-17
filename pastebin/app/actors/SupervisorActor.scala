package actors

import akka.actor.{Actor, ActorSystem, PoisonPill, Props}
//import com.typesafe.scalalogging.Logger
import play.api.Logger
import actors._
import models._

/**
 * This is the supervisor actor that will spawn the worker actors at each client connection.
 * Anytime that the worker actors send their updates, this guy communicates the change to the
 * messenger actor.
 */
class SupervisorActor extends Actor {

  val searchActor = ActorSystem().actorOf(Props[SearchActor])

  override def receive: Receive = {
      case PasteCreated(paste: Paste, access: PasteAccess) => {
        Logger.info(s"Created ${access.visibility} paste with ${access.lifetime} visibility: $paste")
        if(access.visibility == Visibility.Public())
          searchActor ! IndexItem(paste)
        else
          Logger.info(s"Not indexing paste $paste because it is not public")
        searchActor ! PasteSearch("content")  
      }
      case PasteDestroyed(paste: Paste, access: PasteAccess) => {
        Logger.info(s"Deleted $paste")
      }
      case PasteAccessed(paste: Paste, access: PasteAccess) => {
        Logger.debug(s"Access paste $paste $access")
      }
      case PasteSearch(query: String) => {
        searchActor ! PasteSearch(query)
      }
      
  }
}