package com.eskimi

import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.{ Http, server }

import scala.util.{ Failure, Success }
import scala.io.StdIn._

import api.CampaignRegistry
import akka.util.Timeout
import akka.actor.typed.{ Scheduler, ActorSystem }
import server.{
  RejectionHandler,
  RequestEntityExpectedRejection,
  Route,
  MalformedRequestContentRejection,
}
import akka.http.scaladsl.model.{ HttpResponse, StatusCodes }
import akka.http.scaladsl.server.Directives._

object BiddingAgentSystem extends App {
  val rootBehavior = Behaviors.setup[Nothing] { context =>
    val campaignRegistryActor =
      context.spawn(CampaignRegistry(), "CampaignRegistryActor")
    context.watch(campaignRegistryActor)

    implicit val timeout: Timeout =
      Timeout.create(context.system.settings.config.getDuration("my-app.routes.ask-timeout"))
    implicit val scheduler: Scheduler = context.system.scheduler

    startServer(BidRouter.bidRoutes(campaignRegistryActor))(context.system)

    Behaviors.empty
  }

  def startServer(routes: Route)(implicit system: ActorSystem[_]): Unit = {
    import system.executionContext
    val futureBinding = Http().newServerAt("localhost", sys.env("PORT").toInt).bind(routes)
    futureBinding.onComplete {
      case Success(binding) =>
        val address = binding.localAddress
        system
          .log
          .info(
            "Server online at http://{}:{}/",
            address.getHostString,
            address.getPort,
          )
      case Failure(ex) =>
        system.log.error("Failed to bind HTTP endpoint, terminating system", ex)
        system.terminate()
    }

    readLine
    futureBinding
      .flatMap(_.unbind())
      .onComplete(_ => system.terminate())
  }
  val system =
    ActorSystem[Nothing](rootBehavior, "BiddingSystem")
}
