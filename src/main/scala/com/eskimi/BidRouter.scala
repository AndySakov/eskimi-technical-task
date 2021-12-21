package com.eskimi

import akka.actor.typed.{ ActorRef, Scheduler }
import akka.actor.typed.scaladsl.AskPattern._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.util.Timeout
import com.eskimi.api.JsonFormats._
import com.eskimi.api._

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import scala.concurrent.Future

object BidRouter {
  def bid(
      bidRequest: BidRequest,
      campaignRegistry: ActorRef[Query],
    )(implicit
      scheduler: Scheduler,
      timeout: Timeout,
    ): Future[Option[BidResponse]] =
    campaignRegistry.ask(RegistryQuery(bidRequest, _))

  def bidRoutes(
      campaignRegistry: ActorRef[Query]
    )(implicit
      scheduler: Scheduler,
      timeout: Timeout,
    ): Route =
    post {
      path("bid") {
        entity(as[BidRequest]) { bidRequest =>
          onSuccess(bid(bidRequest, campaignRegistry)) {
            case Some(bidResponse) => complete(bidResponse)
            case None => complete(StatusCodes.NoContent)
          }
        }
      }
    }
  def pingTest(
    )(implicit
      scheduler: Scheduler,
      timeout: Timeout,
    ): Route =
    get {
      path("ping") {
        complete("Service is up and running!")
      }
    }
}
