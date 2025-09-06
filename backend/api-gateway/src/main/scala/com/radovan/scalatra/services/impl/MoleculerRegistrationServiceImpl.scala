package com.radovan.scalatra.services.impl

import com.radovan.scalatra.config.ApacheHttpClientSync
import com.radovan.scalatra.services.MoleculerRegistrationService
import jakarta.inject.{Inject, Singleton}
import org.apache.pekko.actor.{ActorSystem, Cancellable}
import org.apache.pekko.stream.Materializer

import java.net.InetAddress
import flexjson.JSONSerializer

import scala.collection.mutable
import scala.concurrent.ExecutionContext
import scala.jdk.CollectionConverters._

@Singleton
class MoleculerRegistrationServiceImpl @Inject() (
                                                   system: ActorSystem,
                                                   mat: Materializer
                                                 ) extends MoleculerRegistrationService {

  implicit val ec: ExecutionContext = system.dispatcher

  private val moleculerRegistryUrl = "http://registry-service:3400/register"
  private val appName = "api-gateway"
  private val port = 8090

  private val scheduler: Cancellable = system.scheduler.scheduleAtFixedRate(
    initialDelay = scala.concurrent.duration.Duration.Zero,
    interval = scala.concurrent.duration.Duration(30, "seconds")
  )(() => registerService())

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

      val jsonString = new JSONSerializer()
        .exclude("*.class")
        .deepSerialize(registrationData.asJava)

      val headers = Map(
        "Content-Type" -> "application/json",
        "Accept" -> "application/json"
      )

      val (statusCode, responseBody) = ApacheHttpClientSync.post(
        moleculerRegistryUrl,
        jsonString,
        headers
      )

      if (statusCode >= 200 && statusCode < 300) {
        println(s"✅ Moleculer registration successful at $moleculerRegistryUrl")
      } else {
        println(s"❌ Moleculer registration failed. Status: $statusCode, Body: $responseBody")
      }

    } catch {
      case ex: Exception =>
        println(s"💥 Exception during Moleculer registration: ${ex.getMessage}")
        ex.printStackTrace()
    }
  }
}
