package com.radovan.play.brokers

import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}
import com.fasterxml.jackson.databind.node.{ArrayNode, ObjectNode}
import com.radovan.play.services.{RoomCategoryService, RoomService}
import com.radovan.play.utils.NatsUtils
import io.nats.client.{Dispatcher, MessageHandler}
import jakarta.inject.{Inject, Singleton}

import java.time.Instant
import scala.util.Try

@Singleton
class RoomNatsListener @Inject()(
                                  objectMapper: ObjectMapper,
                                  natsUtils: NatsUtils,
                                  categoryService: RoomCategoryService,
                                  roomService: RoomService
                                ) {

  private val connection = natsUtils.getConnection

  @Inject
  private def init(): Unit = {
    val dispatcher: Dispatcher = connection.createDispatcher()
    dispatcher.subscribe("room.getCategories", getCategories)
    dispatcher.subscribe("room.getRooms.*", getRooms)
    dispatcher.subscribe("room.getRoom.*", onGetRoom)
  }

  private val getCategories: MessageHandler = msg => {
    try {
      val payload = objectMapper.readTree(msg.getData)
      val jwtToken = Option(payload.get("Authorization")).map(_.asText()).getOrElse("")

      val categories = categoryService.listAll

      val categoriesArrayNode: ArrayNode = objectMapper.createArrayNode()
      categories.foreach { categoryDto =>
        val jsonNode: JsonNode = objectMapper.valueToTree(categoryDto)
        categoriesArrayNode.add(jsonNode)
      }

      val responseJson: ObjectNode = objectMapper.createObjectNode()
      responseJson.set("categories", categoriesArrayNode)
      responseJson.put("status", 200)
      responseJson.put("timestamp", Instant.now().toString)

      publishResponse(msg.getReplyTo, responseJson)

    } catch {
      case ex: Exception =>
        val errorJson = objectMapper.createObjectNode()
        errorJson.put("status", 500)
        errorJson.put("message", s"Error retrieving categories: ${ex.getMessage}")
        publishResponse(msg.getReplyTo, errorJson)
    }
  }

  private val getRooms: MessageHandler = msg => {
    try {
      val categoryId = extractIdFromSubject(msg.getSubject, "room.getRooms.")
      val payload = objectMapper.readTree(msg.getData)
      val jwtToken = Option(payload.get("Authorization")).map(_.asText()).getOrElse("")

      val rooms = roomService.listAllByCategoryId(categoryId)

      val roomsArrayNode: ArrayNode = objectMapper.createArrayNode()
      rooms.foreach { roomDto =>
        val jsonNode: JsonNode = objectMapper.valueToTree(roomDto)
        roomsArrayNode.add(jsonNode)
      }

      val responseJson: ObjectNode = objectMapper.createObjectNode()
      responseJson.set("rooms", roomsArrayNode)
      responseJson.put("status", 200)
      responseJson.put("categoryId", categoryId)
      responseJson.put("timestamp", Instant.now().toString)

      publishResponse(msg.getReplyTo, responseJson)

    } catch {
      case ex: Exception =>
        val errorJson = objectMapper.createObjectNode()
        errorJson.put("status", 500)
        errorJson.put("message", s"Error retrieving rooms: ${ex.getMessage}")
        publishResponse(msg.getReplyTo, errorJson)
    }
  }

  private val onGetRoom: MessageHandler = msg => {
    try {
      val payload: JsonNode = objectMapper.readTree(msg.getData)
      val roomId = extractIdFromSubject(msg.getSubject, "room.getRoom.")
      val jwtToken = Option(payload.get("Authorization")).map(_.asText()).getOrElse("")

      val roomDto = roomService.getRoomById(roomId)

      val roomTemplate = objectMapper.writeValueAsBytes(roomDto)

      val replyTo = Option(msg.getReplyTo).getOrElse("room.response")
      connection.publish(replyTo, objectMapper.writeValueAsBytes(roomDto))

    } catch {
      case ex: Exception =>
        val errorNode: ObjectNode = objectMapper.createObjectNode()
        errorNode.put("status", 500)
        errorNode.put("error", s"Failed to retrieve room: ${ex.getMessage}")
        val replyTo = Option(msg.getReplyTo).getOrElse("room.response")
        connection.publish(replyTo, objectMapper.writeValueAsBytes(errorNode))

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
