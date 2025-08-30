package com.radovan.scalatra.services

trait MoleculerServiceDiscovery {
  def getServiceUrl(serviceName: String): String
}
