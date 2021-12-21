package com.eskimi.api

import scala.util.Random
import Generator.randomID

object BidResponseBuilder {
  private def fromCampaign(
      camp: Campaign
    )(implicit
      bidRequest: BidRequest
    ): Option[BidResponse] = {
    val bid = camp.bid
    val acceptsBid = (bidFloor: Option[Double]) => bidFloor.exists(bid >= _)
    val randomBanner = (banners: List[Banner]) => Random.shuffle(banners).headOption
    bidRequest.imp match {
      case Some(impList) =>
        val validImpression =
          Random.shuffle(impList.filter(v => acceptsBid(v.bidFloor))).head

        validImpression.bidFloor match {
          case Some(bidFloor) =>
            Some(
              BidResponse(
                id = "randomID",
                bidRequestId = bidRequest.id,
                price = bidFloor,
                adid = Some(s"${camp.id}"),
                banner = randomBanner(camp.banners),
              )
            )

          case None => None
        }
      case None => None
    }
  }
  def fromMatchingCampaignList(
      campaigns: List[Campaign]
    )(implicit
      bidRequest: BidRequest
    ): Option[BidResponse] =
    campaigns match {
      // In case we get just one matching campaign
      case x: List[Campaign] if x.length == 1 =>
        fromCampaign(x.head)
      
      // In case we get more than one matching campaign
      case x: List[Campaign] if x.nonEmpty =>
        val randCamp = campaigns(Random.nextInt(x.length - 1))
        fromCampaign(randCamp)
      
      // Every other possibility
      case _ => None
    }
}
