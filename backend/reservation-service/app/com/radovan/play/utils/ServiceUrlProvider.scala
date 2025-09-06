package com.radovan.play.utils

import com.radovan.play.services.MoleculerServiceDiscovery
import jakarta.inject.{Inject, Singleton}

import java.util.concurrent.ConcurrentHashMap
import scala.collection.concurrent.{Map => ConcurrentMap}
import scala.jdk.CollectionConverters._

@Singleton
class ServiceUrlProvider @Inject()(
                                    moleculerServiceDiscovery: MoleculerServiceDiscovery
                                  ) {

  private val cachedServiceUrls: ConcurrentMap[String, String] =
    new ConcurrentHashMap[String, String]().asScala

  def getServiceUrl(serviceName: String): String = {
    cachedServiceUrls.getOrElseUpdate(serviceName, {
      try {
        val serviceUrl = moleculerServiceDiscovery.getServiceUrl(serviceName)
        validateUrl(serviceUrl, serviceName)
        serviceUrl
      } catch {
        case e: RuntimeException =>
          System.err.println(s"Failed to retrieve service URL for: $serviceName - ${e.getMessage}")
          throw e
      }
    })
  }

  def getAuthServiceUrl: String = getServiceUrl("auth-service")

  private def validateUrl(url: String, serviceName: String): Unit = {
    if (url == null || !url.startsWith("http"))
      throw new IllegalArgumentException(s"Invalid URL for $serviceName: $url")
  }
}