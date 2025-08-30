package com.radovan.scalatra.brokers

import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}
import com.radovan.scalatra.exceptions.ExistingInstanceException
import com.radovan.scalatra.utils.NatsUtils
import io.nats.client.Message
import jakarta.inject.{Inject, Singleton}

import java.nio.charset.StandardCharsets
import java.time.Duration
import java.util.concurrent.{CompletableFuture, TimeUnit}

@Singleton
class GuestNatsSender @Inject()(
                                 objectMapper: ObjectMapper,
                                 natsUtils: NatsUtils
                               ) {

  private val REQUEST_TIMEOUT_SECONDS = 5

  @throws[ExistingInstanceException]
  @throws[Exception]
  def retrieveCurrentUser(jwtToken: String): JsonNode = {
    val payload = objectMapper.createObjectNode()
      .put("token", jwtToken)

    try {
      val response = sendRequest("user.get", payload.toString)
      val json = objectMapper.readTree(response)

      json
    } catch {
      case e: Exception =>
        throw new RuntimeException(s"Error retrieving current user: ${e.getMessage}", e)
    }
  }

  @throws[Exception]
  def createUser(userPayload:JsonNode):Int = {
    try {
      val payloadBytes = objectMapper.writeValueAsBytes(userPayload)
      val reply = natsUtils.getConnection
        .request("user.create", payloadBytes, Duration.ofSeconds(REQUEST_TIMEOUT_SECONDS))

      val response = objectMapper.readTree(reply.getData)
      val status = if (response.has("status")) response.get("status").asInt() else 500

      if (status == 200 && response.has("id")) {
        response.get("id").asInt()
      } else if (status == 409) {
        throw new ExistingInstanceException("Email already exists.")
      } else {
        val msg = if (response.has("message")) response.get("message").asText() else "Unknown error."
        throw new Exception(s"User creation failed: $msg")
      }

    } catch {
      case e: ExistingInstanceException => throw e
      case ex: Exception =>
        throw new Exception(s"NATS user.create failed: ${ex.getMessage}", ex)
    }
  }

  @throws[Exception]
  def deleteUser(userId: Int, jwtToken: String): Unit = {
    val subject = s"user.delete.$userId"

    val payload = objectMapper.createObjectNode()
    payload.put("token", jwtToken)

    try {
      val reply = natsUtils.getConnection
        .request(subject, objectMapper.writeValueAsBytes(payload), Duration.ofSeconds(REQUEST_TIMEOUT_SECONDS))

      val response = objectMapper.readTree(reply.getData)
      val status = if (response.has("status")) response.get("status").asInt() else 500

      if (status != 200) {
        val msg = if (response.has("message")) response.get("message").asText() else "Unknown error."
        throw new Exception(s"User deletion failed: $msg")
      }

    } catch {
      case ex: Exception =>
        throw new Exception(s"NATS user.delete failed for ID $userId: ${ex.getMessage}", ex)
    }
  }

  @throws[Exception]
  def sendDeleteAllReservations(guestId: Int, jwtToken: String): Unit = {
    try {
      val payload = objectMapper.createObjectNode()
      payload.put("Authorization", jwtToken)
      val payloadBytes = objectMapper.writeValueAsBytes(payload)

      val reply = natsUtils.getConnection
        .request(s"reservations.deleteByGuestId.$guestId", payloadBytes, Duration.ofSeconds(REQUEST_TIMEOUT_SECONDS))

      if (reply == null || reply.getData == null) {
        throw new RuntimeException("No reply received from reservations.deleteByGuestId")
      }

      val response = objectMapper.readTree(reply.getData)
      val status = if (response.has("status")) response.get("status").asInt() else 500

      if (status != 200) {
        val msg = if (response.has("message")) response.get("message").asText() else "Unknown error."
        throw new RuntimeException(s"Reservations deletion failed: $msg")
      }

    } catch {
      case ex: Exception =>
        throw new RuntimeException(s"NATS reservations.deleteByGuestId failed: ${ex.getMessage}", ex)
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
