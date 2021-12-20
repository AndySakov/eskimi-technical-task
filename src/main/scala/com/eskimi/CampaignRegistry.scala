package com.eskimi

final case class Campaign(
    id: Int,
    country: String,
    targeting: Targeting,
    banners: List[Banner],
    bid: Double
)
final case class Targeting(targetedSiteIds: List[String])
final case class Banner(id: Int, src: String, width: Int, height: Int)
final case class BidRequest(
    id: String,
    imp: Option[List[Impression]],
    site: Site,
    user: Option[User],
    device: Option[Device]
)
final case class Impression(
    id: String,
    wmin: Option[Int],
    wmax: Option[Int],
    w: Option[Int],
    hmin: Option[Int],
    hmax: Option[Int],
    h: Option[Int],
    bidFloor: Option[Double]
)
final case class Site(id: String, domain: String)
final case class User(id: String, geo: Option[Geo])
final case class Device(id: String, geo: Option[Geo])
final case class Geo(country: Option[String])
final case class BidResponse(
    id: String,
    bidRequestId: String,
    price: Double,
    adid: Option[String],
    banner: Option[Banner]
)

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import scala.collection.immutable

object CampaignRegistry {

  sealed trait Query

  final case class RegistryQuery(
      bidRequest: BidRequest,
      replyTo: ActorRef[Option[BidResponse]]
  ) extends Query

  val activeCampaigns: Seq[Campaign] = Seq(
    Campaign(
      id = 1,
      country = "LT",
      targeting = Targeting(
        targetedSiteIds = List(
          "0006a522ce0f4bbbbaa6b3c38cafaa0f"
        ) // Use collection of your choice
      ),
      banners = List(
        Banner(
          id = 1,
          src =
            "https://business.eskimi.com/wp-content/uploads/2020/06/openGraph.jpeg",
          width = 300,
          height = 250
        )
      ),
      bid = 5d
    )
  )

  val campaignRepo: CampaignRepository =
    CampaignRepository.withCampaigns(activeCampaigns)

  def apply(): Behavior[Query] = registry(activeCampaigns)

  private def registry(campaigns: Seq[Campaign]): Behavior[Query] =
    Behaviors.receiveMessage { case RegistryQuery(bidRequest, replyTo) =>
      replyTo ! campaignRepo.matchCampaign(bidRequest)
      Behaviors.same
    }
}
