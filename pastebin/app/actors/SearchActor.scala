package actors

import java.util.UUID
import akka.actor.Actor
import actors._
import models._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._
import play.api.Logger


class SearchActor extends Actor {

  
  
  override def receive: Receive = {
    case PasteSearch(q: String) => {
      Logger.info(s"""Search for item, query string: "$q" """)
      
    }
    case IndexItem(paste: Paste) => {
      Logger.info(s"Indexing item $paste")
      //Logger.debug(index(paste).toString)
    }
    case RemoveIndexedItem(paste: Paste) => {
      Logger.info(s"Removing indexed paste $paste")
    }
  }

  

  


}
