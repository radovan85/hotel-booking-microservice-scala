package com.radovan.scalatra.brokers

import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}
import com.radovan.scalatra.services.GuestService
import com.radovan.scalatra.utils.NatsUtils
import io.nats.client.{Dispatcher, MessageHandler}
import jakarta.inject.{Inject, Singleton}

import scala.util.Try

@Singleton
class GuestNatsListener @Inject()(
                                 objectMapper:ObjectMapper,
                                 natsUtils: NatsUtils,
                                 guestService: GuestService
                                 ) {

  private val connection = natsUtils.getConnection

  @Inject
  private def init(): Unit = {
    val dispatcher: Dispatcher = connection.createDispatcher()
    dispatcher.subscribe("guest.getCurrent", onGetCurrentGuest)
    dispatcher.subscribe("guest.getById.*", onGetGuestById)
  }

  private val onGetCurrentGuest: MessageHandler = msg => {
    try {
      val payload: JsonNode = objectMapper.readTree(msg.getData)
      val jwtToken = Option(payload.get("token")).map(_.asText()).getOrElse {
        throw new RuntimeException("Missing token in guest.getCurrent payload")
      }

      val guestUserNode = guestService.getCurrentGuest(jwtToken)

      val replyTo = Option(msg.getReplyTo).getOrElse("guest.response")
      connection.publish(replyTo, objectMapper.writeValueAsBytes(guestUserNode))

    } catch {
      case ex: Exception =>
        val errorNode: ObjectNode = objectMapper.createObjectNode()
        errorNode.put("status", 500)
        errorNode.put("error", s"Failed to retrieve current guest: ${ex.getMessage}")
        val replyTo = Option(msg.getReplyTo).getOrElse("guest.response")
        connection.publish(replyTo, objectMapper.writeValueAsBytes(errorNode))

    }
  }

  private val onGetGuestById: MessageHandler = msg => {
    try {
      val payload: JsonNode = objectMapper.readTree(msg.getData)
      val guestId = extractIdFromSubject(msg.getSubject, "guest.getById.")
      val jwtToken = Option(payload.get("token")).map(_.asText()).getOrElse {
        throw new RuntimeException("Missing token in guest.getById payload")
      }

      val guestDto = guestService.getGuestById(guestId)

      val replyTo = Option(msg.getReplyTo).getOrElse("guest.response")
      connection.publish(replyTo, objectMapper.writeValueAsBytes(guestDto))

    } catch {
      case ex: Exception =>
        val errorNode: ObjectNode = objectMapper.createObjectNode()
        errorNode.put("status", 500)
        errorNode.put("error", s"Failed to retrieve guest: ${ex.getMessage}")
        val replyTo = Option(msg.getReplyTo).getOrElse("guest.response")
        connection.publish(replyTo, objectMapper.writeValueAsBytes(errorNode))

    }
  }

  private def extractIdFromSubject(subject: String, prefix: String): Int = {
    Try(subject.replace(prefix, "").toInt).getOrElse(0)
  }


}
