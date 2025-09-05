package com.radovan.scalatra.controllers

import com.radovan.scalatra.utils.TokenUtils._
import com.fasterxml.jackson.databind.ObjectMapper
import com.radovan.scalatra.metrics.MetricsSupport
import com.radovan.scalatra.security.{CorsHandler, SecuritySupport}
import com.radovan.scalatra.services.{GuestService, PrometheusService}
import com.radovan.scalatra.utils.{RegistrationForm, ResponsePackage, ValidatorSupport}
import jakarta.inject.Inject
import org.apache.hc.core5.http.HttpStatus
import org.scalatra.ScalatraServlet

class GuestController @Inject()(
                                 val guestService: GuestService,
                                 val objectMapper: ObjectMapper,
                                 val prometheusService: PrometheusService
                               ) extends ScalatraServlet
  with ValidatorSupport
  with CorsHandler
  with SecuritySupport
  with MetricsSupport
  with ErrorsController {


  get("/") {
    secured(Set("ROLE_ADMIN")) {
      new ResponsePackage(guestService.listAll, HttpStatus.SC_OK).toResponse(response)
    }
  }

  delete("/:id") {
    secured(Set("ROLE_ADMIN")) {
      val guestId = params("id").toInt
      guestService.deleteGuest(guestId, provideToken(request))
      new ResponsePackage(s"Guest with id $guestId has been permanently removed!", HttpStatus.SC_OK).toResponse(response)
    }
  }

  get("/:id") {
    secured(Set("ROLE_ADMIN")) {
      val guestId = params("id").toInt
      new ResponsePackage(guestService.getGuestById(guestId), HttpStatus.SC_OK).toResponse(response)
    }
  }

  post("/register") {
    val guestForm = objectMapper.readValue(request.body, classOf[RegistrationForm])
    validateOrHalt(guestForm)
    guestService.storeGuest(guestForm)
    new ResponsePackage(s"You have been registered successfully!", HttpStatus.SC_CREATED).toResponse(response)
  }

  get("/me") {
    secured(Set("ROLE_USER")) {
      new ResponsePackage(guestService.getCurrentGuest(provideToken(request)), HttpStatus.SC_OK).toResponse(response)
    }
  }

}
