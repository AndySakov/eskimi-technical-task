package com.eskimi

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route

import scala.concurrent.Future
import com.eskimi.CampaignRegistry._
import akka.actor.typed.ActorRef
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.AskPattern._
import akka.util.Timeout
import JsonFormats._

class BidRoutes(campaignRegistry: ActorRef[Query])(implicit
    val system: ActorSystem[_]
) {
  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._

  private implicit val timeout = Timeout.create(
    system.settings.config.getDuration("my-app.routes.ask-timeout")
  )

  def bid(bidRequest: BidRequest): Future[Option[BidResponse]] =
    campaignRegistry.ask(RegistryQuery(bidRequest, _))

  val bidRoutes: Route =
    post {
      path("bid") {
        entity(as[BidRequest]) { bidRequest =>
          onSuccess(bid(bidRequest)) { response =>
            response match {
              case Some(bidResponse) => complete(bidResponse)
              case None              => complete((StatusCodes.NoContent, "{}"))
            }
          }
        }
      }
    }
}
