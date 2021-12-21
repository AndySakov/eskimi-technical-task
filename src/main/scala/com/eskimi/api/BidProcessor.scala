package com.eskimi.api

class BidProcessor(campaigns: List[Campaign]) {
  private def countryFilter(
      camp: Campaign
    )(implicit
      bidRequest: BidRequest
    ): Boolean = {
    val campaignCountry = camp.country

    // Extract the device country from the bid request
    val deviceCountry = for {
      device <- bidRequest.device
      geo <- device.geo
      country <- geo.country
    } yield country

    // Extract the user country from the bid request
    val userCountry = for {
      user <- bidRequest.user
      geo <- user.geo
      country <- geo.country
    } yield country

    // Do some validation
    deviceCountry match {
      case Some(country) if country.equalsIgnoreCase(campaignCountry) => true
      case None =>
        userCountry match {
          case Some(country) if country.equalsIgnoreCase(campaignCountry) =>
            true
          case _ => false
        }

      case _ => false

    }
  }

  private def bidFloorFilter(
      camp: Campaign
    )(implicit
      bidRequest: BidRequest
    ): Boolean = {
    val bid = camp.bid
    val acceptsBid = (bidFloor: Option[Double]) => bidFloor.exists(bid >= _)

    // Run the validation only if there are impressions to filter
    bidRequest.imp.exists(x => x.exists(v => acceptsBid(v.bidFloor)))
  }

  private def targetingFilter(
      camp: Campaign
    )(implicit
      bidRequest: BidRequest
    ): Boolean =
      // Run the validation only if there are targets to filter
    camp.targeting.targetedSiteIds.exists(_ == bidRequest.site.id)

  private def bannerFilter(
      camp: Campaign
    )(implicit
      bidRequest: BidRequest
    ): Boolean = {
    val banners = camp.banners
    val isValidBanner = (banner: Banner) => {
      val width = banner.width
      val height = banner.height

      val checkWidth = (imp: Impression) => {
        // Cases where w is provided
        val validWidth = imp.w match {
          case Some(w) => w == width
          case None =>
            false
        }

        // Cases where either wmin or wmax or both are provided
        val validMinMaxValues = (imp.wmin, imp.wmax) match {
          case (Some(wmin), Some(wmax)) => width >= wmin && width <= wmax
          case (Some(wmin), None) => width >= wmin
          case (None, Some(wmax)) => width <= wmax
          case _ => false
        }

        // It has to fulfil either one of these conditions
        validWidth || validMinMaxValues
      }

      val checkHeight = (imp: Impression) => {
        val validHeight = imp.h match {
          case Some(h) => h == height
          case None =>
            false
        }

        val validMinMaxValues = (imp.hmin, imp.hmax) match {
          case (Some(hmin), Some(hmax)) => height >= hmin && height <= hmax
          case (Some(hmin), None) => height >= hmin
          case (None, Some(hmax)) => height <= hmax
          case _ => false
        }

        validHeight || validMinMaxValues
      }

      val hasRightBanner = (impression: Impression) =>
        checkWidth(impression) && checkHeight(impression)

      bidRequest.imp.exists(x => x.filter(hasRightBanner(_)).nonEmpty)
    }
    banners.exists(isValidBanner)
  }

  def processBid(implicit bidRequest: BidRequest): Option[BidResponse] = {
    val stepOne = campaigns
      .filter(targetingFilter)
    val stepTwo = stepOne
      .filter(countryFilter)
    val stepThree = stepTwo
      .filter(bidFloorFilter)
    val matchingCampaignList = stepThree
      .filter(bannerFilter)
    BidResponseBuilder.fromMatchingCampaignList(matchingCampaignList)
  }
}

object BidProcessorBuilder {
  def fromCampaigns(campaigns: List[Campaign]): BidProcessor =
    new BidProcessor(campaigns)
}
