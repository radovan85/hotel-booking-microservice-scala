package com.radovan.spring.services.impl

import com.radovan.spring.services.MoleculerRegistrationService
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.{HttpEntity, HttpHeaders, MediaType}
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

import java.net.InetAddress
import java.util.{Map => JMap}
import scala.collection.mutable
import scala.jdk.CollectionConverters._

@Service
class MoleculerRegistrationServiceImpl extends MoleculerRegistrationService {

  private val restTemplate = new RestTemplate()

  @Value("${moleculer.registry.url:http://localhost:3000/register}")
  private val registryUrl: String = null

  @Value("${server.port:8081}")
  private val port: Int = 8081

  private val appName = "auth-service"

  @PostConstruct
  def init(): Unit = {
    registerService() // Initial registration
  }

  @Scheduled(fixedRate = 30000)
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

      val headers = new HttpHeaders()
      headers.setContentType(MediaType.APPLICATION_JSON)
      headers.setAccept(List(MediaType.APPLICATION_JSON).asJava)

      // ✅ Konvertuj registrationData u Java Map pre slanja
      val request = new HttpEntity[JMap[String, Any]](registrationData.asJava, headers)

      val response = restTemplate.postForEntity(registryUrl, request, classOf[String])

      if (response.getStatusCode.is2xxSuccessful) {
        println(s"✅ Moleculer registration successful at $registryUrl")
      } else {
        println(s"❌ Moleculer registration failed. Status: ${response.getStatusCode}")
      }

    } catch {
      case ex: Exception =>
        println(s"💥 Exception during Moleculer registration: ${ex.getMessage}")
        ex.printStackTrace()
    }
  }
}
