package com.radovan.play.brokers

import com.fasterxml.jackson.databind.ObjectMapper
import com.radovan.play.utils.NatsUtils
import io.nats.client.Message
import jakarta.inject.{Inject, Singleton}

import java.nio.charset.StandardCharsets
import java.time.Duration
import java.util.concurrent.{CompletableFuture, TimeUnit}

@Singleton
class RoomNatsSender @Inject()(
                                objectMapper: ObjectMapper,
                                natsUtils: NatsUtils
                              ) {

  private val REQUEST_TIMEOUT_SECONDS = 5

  @throws[Exception]
  def sendDeleteAllReservations(roomId: Int, jwtToken: String): Unit = {
    try {
      val payload = objectMapper.createObjectNode()
      payload.put("Authorization", jwtToken)
      val payloadBytes = objectMapper.writeValueAsBytes(payload)

      val reply = natsUtils.getConnection
        .request(s"reservations.deleteByRoomId.$roomId", payloadBytes, Duration.ofSeconds(REQUEST_TIMEOUT_SECONDS))

      if (reply == null || reply.getData == null) {
        throw new RuntimeException("No reply received from reservations.deleteByRoomId")
      }

      val response = objectMapper.readTree(reply.getData)
      val status = if (response.has("status")) response.get("status").asInt() else 500

      if (status != 200) {
        val msg = if (response.has("message")) response.get("message").asText() else "Unknown error."
        throw new RuntimeException(s"Reservations deletion failed: $msg")
      }

    } catch {
      case ex: Exception =>
        throw new RuntimeException(s"NATS reservations.deleteByRoomId failed: ${ex.getMessage}", ex)
    }
  }

  private def sendRequest(subject: String, payload: String): String = {
    val connection = natsUtils.getConnection
    if (connection == null) throw new RuntimeException("NATS connection is not initialized")

    try {
      val future: CompletableFuture[Message] =
        connection.request(subject, payload.getBytes(StandardCharsets.UTF_8))

      val msg = future.get(REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS)
      new String(msg.getData, StandardCharsets.UTF_8)
    } catch {
      case e: Exception =>
        throw new RuntimeException(s"NATS request failed for subject: $subject", e)
    }
  }
}
