package com.eskimi

class CampaignRepository(campaigns: Seq[Campaign]) {

  
}

object CampaignRepository {
  def withCampaigns(campaigns: Seq[Campaign]): CampaignRepository =
    new CampaignRepository(campaigns)
}
