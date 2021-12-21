package com.eskimi.api

import scala.util.Random

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import scala.collection.immutable

object CampaignRegistry {
  
  def apply(): Behavior[Query] = registry(List.empty)

  private def registry(records: List[Campaign]): Behavior[Query] =
    Behaviors.receiveMessage {
      case RegistryQuery(bidRequest, replyTo) =>
        val bidProcessor: BidProcessor = BidProcessorBuilder.fromCampaigns(records)

        replyTo ! bidProcessor.processBid(bidRequest)
        Behaviors.same
      
      case RegistryUpdate(newCampaigns) => 
        registry(records ++ newCampaigns)
    }
}
