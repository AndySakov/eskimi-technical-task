package com.eskimi.api

import spray.json.DefaultJsonProtocol._
object JsonFormats {
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
  implicit val emptyRequestJsonFormat = jsonFormat0(EmptyRequest)
}
