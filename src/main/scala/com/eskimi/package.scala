package com.eskimi

import akka.actor.typed.ActorRef

package object api {
  sealed trait Query

  final case class RegistryQuery(
      bidRequest: BidRequest,
      replyTo: ActorRef[Option[BidResponse]],
    ) extends Query
  
  final case class RegistryUpdate(newCampaigns: List[Campaign]) extends Query
  final case class Campaign(
      id: Int,
      country: String,
      targeting: Targeting,
      banners: List[Banner],
      bid: Double,
    )
  final case class Targeting(targetedSiteIds: List[String])
  final case class Banner(
      id: Int,
      src: String,
      width: Int,
      height: Int,
    )
  final case class BidRequest(
      id: String,
      imp: Option[List[Impression]],
      site: Site,
      user: Option[User],
      device: Option[Device],
    )
  final case class Impression(
      id: String,
      wmin: Option[Int],
      wmax: Option[Int],
      w: Option[Int],
      hmin: Option[Int],
      hmax: Option[Int],
      h: Option[Int],
      bidFloor: Option[Double],
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
      banner: Option[Banner],
    )

    final case class EmptyRequest()
}
