package com.eskimi

import com.eskimi.CampaignRegistry._

//#json-formats
import spray.json.DefaultJsonProtocol

object JsonFormats {
  // import the default encoders for primitive types (Int, String, Lists etc)
  import DefaultJsonProtocol._

  // implicit val userJsonFormat = jsonFormat3(User)
  // implicit val usersJsonFormat = jsonFormat1(Users)

  // implicit val actionPerformedJsonFormat = jsonFormat1(ActionPerformed)

  implicit val bannerJsonFormat = jsonFormat4(Banner)
  implicit val targetingJsonFormat = jsonFormat1(Targeting)
  implicit val siteJsonFormat = jsonFormat2(Site)
  implicit val geoJsonFormat = jsonFormat1(Geo)
  implicit val userJsonFormat = jsonFormat2(User)
  implicit val deviceJsonFormat = jsonFormat2(Device)
  implicit val campaignJsonFormat = jsonFormat5(Campaign)
  implicit val impressionJsonFormat = jsonFormat8(Impression)
  implicit val bidRequestJsonFormat = jsonFormat5(BidRequest)
  implicit val bidResponseJsonFormat = jsonFormat5(BidResponse)
}
//#json-formats
