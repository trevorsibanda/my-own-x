package controllers

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
