package com.radovan.play.brokers

import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}
import com.radovan.play.utils.NatsUtils
import io.nats.client.Message
import jakarta.inject.{Inject, Singleton}

import java.nio.charset.StandardCharsets
import java.util.concurrent.{CompletableFuture, TimeUnit}

import scala.jdk.CollectionConverters._

@Singleton
class ReservationNatsSender @Inject()(
                                       objectMapper: ObjectMapper,
                                       natsUtils: NatsUtils
                                     ){

  private val REQUEST_TIMEOUT_SECONDS = 5

  def retrieveCurrentGuest(jwtToken:String):JsonNode = {
    val payload = objectMapper.createObjectNode()
      .put("token", jwtToken)

    try {
      val response = sendRequest("guest.getCurrent", payload.toString)
      val json = objectMapper.readTree(response)

      json
    } catch {
      case e: Exception =>
        throw new RuntimeException(s"Error retrieving current guest: ${e.getMessage}", e)
    }
  }

  def retrieveRoomCategories(jwtToken: String): Array[JsonNode] = {
    val payload = objectMapper.createObjectNode()
      .put("token", jwtToken)

    val response = sendRequest(s"room.getCategories", payload.toString)
    val json = objectMapper.readTree(response)

    if (json.has("status") && json.get("status").asInt() == 500) {
      val msg = if (json.has("error")) json.get("error").asText() else "Failed to retrieve room categories"
      throw new RuntimeException(msg)
    }

    val categoriesNode = if (json.isArray) {
      json
    } else if (json.has("categories") && json.get("categories").isArray) {
      json.get("categories")
    } else {
      throw new RuntimeException("Expected array of room categories, but got: " + json.getNodeType)
    }

    categoriesNode.elements().asScala.toArray
  }


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

  def retrieveUserById(userId:Int,jwtToken: String): JsonNode = {
    val payload = objectMapper.createObjectNode()
      .put("token", jwtToken)
      .put("userId",userId)

    try {
      val response = sendRequest(s"user.getById.$userId", payload.toString)
      val json = objectMapper.readTree(response)

      json
    } catch {
      case e: Exception =>
        throw new RuntimeException(s"Error retrieving user: ${e.getMessage}", e)
    }
  }

  def retrieveGuestById(guestId:Int,jwtToken: String): JsonNode = {
    val payload = objectMapper.createObjectNode()
      .put("token", jwtToken)
      .put("guestId",guestId)

    try {
      val response = sendRequest(s"guest.getById.$guestId", payload.toString)
      val json = objectMapper.readTree(response)

      json
    } catch {
      case e: Exception =>
        throw new RuntimeException(s"Error retrieving guest: ${e.getMessage}", e)
    }
  }

  def retrieveRoomById(roomId:Int,jwtToken:String):JsonNode = {
    val payload = objectMapper.createObjectNode()
      .put("token", jwtToken)
      .put("roomId", roomId)

    try {
      val response = sendRequest(s"room.getRoom.$roomId", payload.toString)
      val json = objectMapper.readTree(response)

      json
    } catch {
      case e: Exception =>
        throw new RuntimeException(s"Error retrieving room: ${e.getMessage}", e)
    }
  }



  def retrieveRooms(categoryId: Int, jwtToken: String): Array[JsonNode] = {
    val payload = objectMapper.createObjectNode()
      .put("token", jwtToken)
      .put("categoryId", categoryId)

    val response = sendRequest(s"room.getRooms.$categoryId", payload.toString)
    val json = objectMapper.readTree(response)

    if (json.has("status") && json.get("status").asInt() == 500) {
      val msg = if (json.has("error")) json.get("error").asText() else "Failed to retrieve rooms"
      throw new RuntimeException(msg)
    }

    val roomsNode = if (json.isArray) {
      json
    } else if (json.has("rooms") && json.get("rooms").isArray) {
      json.get("rooms")
    } else {
      throw new RuntimeException("Expected array of rooms, but got: " + json.getNodeType)
    }

    roomsNode.elements().asScala.toArray
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
