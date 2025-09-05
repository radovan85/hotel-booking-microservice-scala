package com.radovan.scalatra.controllers

import com.radovan.scalatra.config.CorsHandler
import com.radovan.scalatra.metrics.MetricsSupport
import com.radovan.scalatra.services.{ApiGatewayService, PrometheusService}
import com.radovan.scalatra.utils.ResponsePackage
import jakarta.servlet.http.HttpServletRequest
import org.apache.pekko.http.scaladsl.model.StatusCodes.{BadRequest, NoContent, NotFound}
import org.scalatra.ScalatraServlet


class ApiGatewayController(
                            val apiGatewayService: ApiGatewayService,
                            val prometheusService: PrometheusService)
  extends ScalatraServlet with CorsHandler with MetricsSupport {

  get("/*") {
    handleRequest(request)
  }

  post("/*") {
    handleRequest(request)
  }

  put("/*") {
    handleRequest(request)
  }

  delete("/*") {
    handleRequest(request)
  }

  options("/*") {
    NoContent
  }


  private def handleRequest(request: HttpServletRequest): Any = {
    val path = request.getRequestURI.stripPrefix("/api/")
    val firstSegmentOpt = path.split("/").headOption

    firstSegmentOpt match {
      case Some(firstSegment) =>
        mapSegmentToService(firstSegment) match {
          case Some(serviceName) =>
            try {
              val responsePackage: ResponsePackage[String] = apiGatewayService.forwardRequest(serviceName, request)
              //responsePackage.toResponse(response)
              status = responsePackage.statusCode
              contentType = "application/json"
              responsePackage.body
            } catch {
              case e: Exception =>
                status = 502
                contentType = "text/plain"
                s"Error forwarding request to $serviceName: ${e.getMessage}"
            }
          case None =>
            status = 404
            contentType = "text/plain"
            s"Unknown service for segment: $firstSegment"
        }
      case None =>
        status = 400
        contentType = "text/plain"
        "Invalid API request format"
    }
  }


  private def mapSegmentToService(segment: String): Option[String] = {
    val serviceMappings = Map(
      "rooms" -> "room-service",
      "categories" -> "room-service",
      "auth" -> "auth-service",
      "guests" -> "guest-service",
      "reservations" -> "reservation-service",
      "notes" -> "reservation-service"

    )
    serviceMappings.get(segment)
  }
}
