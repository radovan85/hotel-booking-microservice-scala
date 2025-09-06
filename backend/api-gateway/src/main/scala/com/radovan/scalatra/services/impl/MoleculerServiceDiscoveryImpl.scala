package com.radovan.scalatra.services.impl

import com.radovan.scalatra.services.MoleculerServiceDiscovery
import flexjson.JSONDeserializer
import jakarta.inject.Inject
import org.apache.hc.client5.http.classic.methods.HttpGet
import org.apache.hc.client5.http.impl.classic.HttpClients
import org.apache.hc.core5.http.ClassicHttpResponse
import org.apache.hc.core5.http.io.HttpClientResponseHandler

import java.util

class MoleculerServiceDiscoveryImpl @Inject() () extends MoleculerServiceDiscovery {

  private val client = HttpClients.createDefault()

  private val MOLECULER_DISCOVERY_URL = "http://registry-service:3400/services"

  override def getServiceUrl(serviceName: String): String = {
    val request = new HttpGet(MOLECULER_DISCOVERY_URL)
    request.addHeader("Accept", "application/json")

    val responseHandler = new HttpClientResponseHandler[String] {
      override def handleResponse(response: ClassicHttpResponse): String = {
        val statusCode = response.getCode
        val entity = response.getEntity
        val body = if (entity != null) {
          try {
            org.apache.hc.core5.http.io.entity.EntityUtils.toString(entity, "UTF-8")
          } catch {
            case e: Exception =>
              println(s"❌ Failed to read response body: ${e.getMessage}")
              ""
          }
        } else ""

        if (statusCode >= 200 && statusCode < 300) {
          if (body == null || body.trim.isEmpty) {
            throw new RuntimeException("Moleculer registry did not respond properly!")
          }

          val jsonData = new JSONDeserializer[Object]().deserialize(body)

          val servicesList = jsonData.asInstanceOf[util.List[_]]
          val it = servicesList.iterator()

          while (it.hasNext) {
            val service = it.next().asInstanceOf[java.util.Map[String, Object]]

            val name = Option(service.get("name")).map(_.toString).getOrElse("")
            val endpoint = Option(service.get("endpoint")).map(_.toString).getOrElse("")

            if (name.equalsIgnoreCase(serviceName) && endpoint.nonEmpty) {
              return endpoint
            }
          }

          throw new RuntimeException(s"Service not found: $serviceName")
        } else {
          throw new RuntimeException(s"Failed to fetch service list from Moleculer registry. Status: $statusCode, body: $body")
        }
      }
    }

    client.execute(request, responseHandler)
  }
}
