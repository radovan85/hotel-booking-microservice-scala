package com.radovan.scalatra.utils

import com.radovan.scalatra.services.MoleculerServiceDiscovery
import jakarta.inject.Inject

import scala.collection.concurrent.TrieMap

class ServiceUrlProvider @Inject() (moleculerServiceDiscovery: MoleculerServiceDiscovery) {

  private val cachedServiceUrls = TrieMap.empty[String, String]

  def getServiceUrl(serviceName: String): String = {
    cachedServiceUrls.getOrElseUpdate(serviceName, {
      val url = moleculerServiceDiscovery.getServiceUrl(serviceName)
      validateUrl(url, serviceName)
      url
    })
  }

  def getAuthServiceUrl: String = getServiceUrl("auth-service")

  private def validateUrl(url: String, serviceName: String): Unit = {
    if (url == null || !url.startsWith("http")) {
      throw new IllegalArgumentException(s"Invalid URL for $serviceName: $url")
    }
  }
}