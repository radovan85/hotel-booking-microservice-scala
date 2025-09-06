package com.radovan.play.services.impl

import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}
import com.radovan.play.services.MoleculerRegistrationService
import jakarta.inject.{Inject, Singleton}
import org.apache.pekko.actor.ActorSystem
import play.libs.ws.{WSClient, WSResponse}

import java.net.InetAddress
import scala.collection.mutable
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.jdk.CollectionConverters._

@Singleton
class MoleculerRegistrationServiceImpl @Inject() (
                                                   wsClient: WSClient,
                                                   objectMapper: ObjectMapper,
                                                   actorSystem: ActorSystem,
                                                   executionContext: ExecutionContext
                                                 ) extends MoleculerRegistrationService {


  private val moleculerRegistryUrl = "http://registry-service:3400/register"
  private val appName = "reservation-service"
  private val port = 9000

  actorSystem.scheduler.scheduleAtFixedRate(
    initialDelay = 0.seconds,
    interval = 30.seconds
  )(() => registerService())(executionContext)

  override def registerService(): Unit = {
    try {
      val ipAddr = InetAddress.getLocalHost.getHostAddress

      val metadata = mutable.LinkedHashMap[String, Any]()
      metadata.put("team", "antifraud")
      metadata.put("ip", ipAddr)
      metadata.put("healthCheck", s"http://$ipAddr:$port/api/health")

      val registrationData = mutable.LinkedHashMap[String, Any]()
      registrationData.put("name", appName)
      registrationData.put("version", "1.0")
      registrationData.put("endpoint", s"http://$ipAddr:$port")
      registrationData.put("metadata", metadata.asJava)

      val jsonPayload: JsonNode = objectMapper.valueToTree(registrationData.asJava)

      wsClient.url(moleculerRegistryUrl)
        .addHeader("Content-Type", "application/json")
        .post(jsonPayload)
        .thenAccept(handleResponse)


    } catch {
      case ex: Exception =>
        println(s"💥 Exception during Moleculer registration: ${ex.getMessage}")
        ex.printStackTrace()
    }
  }

  private def handleResponse(response: WSResponse): Unit = {
    println(s"Moleculer server response status: ${response.getStatus}")
    if (response.getStatus == 204 || response.getStatus == 200) {
      println("Service registered successfully!")
    } else {
      System.err.println(s"Failed to register service with Moleculer: ${response.getStatusText}")
      System.err.println(s"Response body: ${response.getBody}")
    }
  }
}
