package com.eskimi

import akka.actor.Scheduler
import akka.actor.testkit.typed.scaladsl.TestProbe
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.AskPattern._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.http.scaladsl.model.{ StatusCodes, HttpMethod }
import akka.util.Timeout
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import scala.concurrent.duration._
import akka.http.scaladsl.server.{ Route, MethodRejection, MalformedRequestContentRejection }
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.DefaultJsonProtocol
import akka.http.scaladsl.model._
import com.eskimi.api._
import com.eskimi.api.JsonFormats._
import scala.concurrent.Await
import scala.language.postfixOps
import spray.json.DeserializationException
import akka.actor.typed.scaladsl.adapter._

class BidRouterSpec
    extends AnyWordSpec
       with Matchers
       with ScalatestRouteTest
       with SprayJsonSupport
       with DefaultJsonProtocol {
  implicit val typedSystem: ActorSystem[Nothing] = system.toTyped
  implicit val timeout: Timeout = Timeout(500.milliseconds)
  implicit val scheduler: Scheduler = system.scheduler

  "The ping route" should {
    val probe: TestProbe[String] = TestProbe[String]()
    val route = BidRouter.pingTest

    "return a response for GET requests to the `ping` path" in {
      Get("/ping") ~> route ~> check {
        responseAs[String] shouldEqual "Service is up and running!"
      }
    }

    "leave GET requests to other paths unhandled" in {
      Get("/") ~> route ~> check {
        handled shouldBe false
      }
    }
  }

  "The bid route" should {
    val probe: TestProbe[Query] = TestProbe[Query]()
    val route = BidRouter.bidRoutes(probe.ref)
    val activeCampaigns = List(
      Campaign(
        id = 1,
        country = "LT",
        targeting = Targeting(
          targetedSiteIds = List("0006a522ce0f4bbbbaa6b3c38cafaa0f")
        ),
        banners = List(
          Banner(
            id = 1,
            src = "https://business.eskimi.com/wp-content/uploads/2020/06/openGraph.jpeg",
            width = 300,
            height = 250,
          )
        ),
        bid = 5d,
      )
    )

    val demoBidRequest = BidRequest(
      "SGu1Jpq1IO",
      Some(
        List(
          Impression(
            "1",
            Some(50),
            Some(300),
            Some(300),
            Some(100),
            Some(300),
            Some(250),
            Some(3.12123),
          )
        )
      ),
      Site("0006a522ce0f4bbbbaa6b3c38cafaa0f", "fake.tld"),
      Some(User("USARIO1", Some(Geo(Some("LT"))))),
      Some(Device("440579f4b408831516ebd02f6e1c31b4", Some(Geo(Some("LT"))))),
    )

    "reject GET requests to the `bid` path" in {
      Get("/bid") ~> route ~> check {
        assert {
          rejection match {
            case x: MethodRejection => true
            case _ => false
          }
        }

        handled shouldBe false
      }
    }

    "reject requests with no data with a MalformedRequestContentRejection" in {
      val emptyBody = Await.result(Marshal(EmptyRequest()).to[MessageEntity], 1 second)
      Post("/bid").withEntity(emptyBody) ~> route ~> check {
        assert {
          rejection match {
            case x: MalformedRequestContentRejection => true
            case _ => false
          }
        }

        handled shouldBe false
      }
    }

    "return a valid bid response" in {
      val body = Await.result(Marshal(demoBidRequest).to[MessageEntity], 1 second)
      val test = Post("/bid").withEntity(body) ~> route
      val ping = probe.expectMessageType[RegistryQuery]

      ping.replyTo ! BidProcessorBuilder.fromCampaigns(activeCampaigns).processBid(demoBidRequest)

      test ~> check {
        status shouldBe StatusCodes.OK

        responseAs[BidResponse] shouldBe BidResponse(
          id = "randomID",
          bidRequestId = "SGu1Jpq1IO",
          price = 3.12123,
          adid = Some("1"),
          banner = Some(
            Banner(
              id = 1,
              src = "https://business.eskimi.com/wp-content/uploads/2020/06/openGraph.jpeg",
              width = 300,
              height = 250,
            )
          ),
        )
      }
    }

    "return no content to requests to an empty registry" in {
      val body = Await.result(Marshal(demoBidRequest).to[MessageEntity], 1 second)
      val test = Post("/bid").withEntity(body) ~> route
      val ping = probe.expectMessageType[RegistryQuery]

      ping.replyTo ! BidProcessorBuilder
        .fromCampaigns(List.empty[Campaign])
        .processBid(demoBidRequest)

      test ~> check {
        status shouldBe StatusCodes.NoContent
        responseAs[String] shouldBe ""
      }
    }

    "return empty content for umatched bid requests" in {
      val unmatchedBidRequest = demoBidRequest.copy(site = Site("0006a522ce0f4bbbbaa6b3c38cafaa6u", "google.com"))
      val body = Await.result(Marshal(unmatchedBidRequest).to[MessageEntity], 1 second)
      val test = Post("/bid").withEntity(body) ~> route
      val ping = probe.expectMessageType[RegistryQuery]

      ping.replyTo ! BidProcessorBuilder.fromCampaigns(activeCampaigns).processBid(unmatchedBidRequest)

      test ~> check {
        status shouldBe StatusCodes.NoContent
        responseAs[String] shouldBe ""
      }
    }
  }
}
