package com.radovan.play.services.impl

import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}
import com.radovan.play.services.MoleculerServiceDiscovery
import jakarta.inject.{Inject, Singleton}
import play.libs.ws.WSClient

import scala.jdk.CollectionConverters._

@Singleton
class MoleculerServiceDiscoveryImpl @Inject() (
                                             wsClient: WSClient,
                                             objectMapper: ObjectMapper
                                           ) extends MoleculerServiceDiscovery {

  private val MOLECULER_DISCOVERY_URL = "http://registry-service:3400/services"


  override def getServiceUrl(serviceName: String): String = {

    val responseJson: JsonNode = wsClient.url(MOLECULER_DISCOVERY_URL)
      .addHeader("Accept", "application/json")
      .get()
      .toCompletableFuture
      .join()
      .asJson()

    if (responseJson == null || !responseJson.isArray || responseJson.isEmpty)
      throw new RuntimeException("No services found in Moleculer registry")

    val services = responseJson.elements().asScala.toSeq

    services.foreach { service =>
      val name = Option(service.get("name")).map(_.asText()).getOrElse("N/A")
      val endpoint = Option(service.get("endpoint")).map(_.asText()).getOrElse("N/A")
    }

    val endpointOpt: Option[String] = services
      .find(s => Option(s.get("name")).exists(_.asText().equalsIgnoreCase(serviceName)))
      .flatMap(s => Option(s.get("endpoint")).map(_.asText()))

    endpointOpt.getOrElse {
      throw new RuntimeException(s"Service '$serviceName' not found in Moleculer registry")
    }
  }





}
