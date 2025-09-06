package com.radovan.play.brokers

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.radovan.play.services.ReservationService
import com.radovan.play.utils.NatsUtils
import io.nats.client.{Dispatcher, Message, MessageHandler}
import jakarta.inject.{Inject, Singleton}

import scala.util.Try

@Singleton
class ReservationNatsListener @Inject()(
                                         objectMapper: ObjectMapper,
                                         natsUtils: NatsUtils,
                                         reservationService: ReservationService
                                       ) {

  private val connection = natsUtils.getConnection

  @Inject
  private def init(): Unit = {
    val dispatcher: Dispatcher = connection.createDispatcher()
    dispatcher.subscribe("reservations.deleteByGuestId.*", deleteReservationsByGuestId)
    dispatcher.subscribe("reservations.deleteByRoomId.*", deleteReservationsByRoomId)

  }

  private val deleteReservationsByGuestId: MessageHandler = (msg: Message) => {
    val subject = msg.getSubject
    val replyTo = msg.getReplyTo
    val guestId = extractIdFromSubject(subject, "reservations.deleteByGuestId.")

    try {
      reservationService.deleteAllByGuestId(guestId)

      val responseNode = objectMapper.createObjectNode()
      responseNode.put("status", 200)
      responseNode.put("message", s"All reservations for guest ID $guestId deleted successfully")

      publishResponse(replyTo, responseNode)
    } catch {
      case ex: Exception =>
        val errorJson = objectMapper.createObjectNode()
        errorJson.put("status", 500)
        errorJson.put("message", s"Error removing reservations: ${ex.getMessage}")
        publishResponse(msg.getReplyTo, errorJson)
    }
  }

  private val deleteReservationsByRoomId: MessageHandler = (msg: Message) => {
    try {
      val roomId = extractIdFromSubject(msg.getSubject, "reservations.deleteByRoomId.")
      val payload = objectMapper.readTree(msg.getData)
      val jwtToken = Option(payload.get("Authorization")).map(_.asText()).getOrElse("")
      reservationService.deleteAllByRoomId(roomId)

      val responseNode = objectMapper.createObjectNode()
      responseNode.put("status", 200)
      responseNode.put("message", s"All reservations for room ID $roomId deleted successfully")

      publishResponse(msg.getReplyTo, responseNode)
    } catch {
      case ex: Exception =>
        val errorJson = objectMapper.createObjectNode()
        errorJson.put("status", 500)
        errorJson.put("message", s"Error removing reservations: ${ex.getMessage}")
        publishResponse(msg.getReplyTo, errorJson)
    }
  }

  private def extractIdFromSubject(subject: String, prefix: String): Int = {
    Try(subject.replace(prefix, "").toInt).getOrElse(0)
  }

  private def publishResponse(replyTo: String, node: ObjectNode): Unit = {
    if (replyTo != null && replyTo.nonEmpty) {
      val bytes = objectMapper.writeValueAsBytes(node)
      natsUtils.getConnection.publish(replyTo, bytes)
    }
  }
}
