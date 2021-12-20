package com.eskimi

class CampaignRepository(campaigns: Seq[Campaign]) {

  def matchCampaign(bidRequest: BidRequest): Option[BidResponse] =
    None
}

object CampaignRepository {
  def withCampaigns(campaigns: Seq[Campaign]): CampaignRepository =
    new CampaignRepository(campaigns)
}
