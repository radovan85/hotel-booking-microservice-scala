package com.radovan.play.controllers

import com.radovan.play.dto.ReservationDto
import com.radovan.play.utils.TokenUtils._
import com.radovan.play.security.{JwtSecuredAction, SecuredRequest}
import com.radovan.play.services.ReservationService
import com.radovan.play.utils.{ReservationTimeForm, ResponsePackage, ValidatorSupport}
import flexjson.JSONDeserializer
import jakarta.inject.Inject
import org.apache.hc.core5.http.HttpStatus
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents, Result}

class ReservationController @Inject()(
                                       cc: ControllerComponents,
                                       reservationService: ReservationService,
                                       securedAction: JwtSecuredAction
                                     ) extends AbstractController(cc) with ValidatorSupport {

  private def onlyAdmin[A](secured: SecuredRequest[A])(block: => Result): Result = {
    if (secured.roles.contains("ROLE_ADMIN")) block
    else Forbidden("Access denied: admin role required")
  }

  private def onlyUser[A](secured: SecuredRequest[A])(block: => Result): Result = {
    if (secured.roles.contains("ROLE_USER")) block
    else Forbidden("Access denied: user role required")
  }

  def provideReservations: Action[AnyContent] = securedAction { secured =>
    onlyUser(secured) {
      val json = Json.stringify(secured.body.asJson.getOrElse(Json.obj()))
      val timeForm = new JSONDeserializer[ReservationTimeForm]()
        .use(null, classOf[ReservationTimeForm])
        .deserialize(json, classOf[ReservationTimeForm])

      validateOrHalt(timeForm)
      val availableReservations = reservationService.listAvailableBookings(timeForm, provideToken(secured))
      new ResponsePackage(availableReservations, HttpStatus.SC_OK).toResult
    }
  }

  def addReservation(): Action[AnyContent] = securedAction { secured =>
    onlyUser(secured) {
      val json = Json.stringify(secured.body.asJson.getOrElse(Json.obj()))
      val reservation = new JSONDeserializer[ReservationDto]()
        .use(null, classOf[ReservationDto])
        .deserialize(json, classOf[ReservationDto])

      reservationService.addReservation(reservation, provideToken(secured))
      new ResponsePackage("Your reservation has been booked successfully!", HttpStatus.SC_CREATED).toResult
    }
  }

  def provideMyReservations: Action[AnyContent] = securedAction { secured =>
    onlyUser(secured) {
      new ResponsePackage(reservationService.getMyReservations(provideToken(secured)), HttpStatus.SC_OK).toResult
    }
  }

  def getAllReservations:Action[AnyContent] = securedAction{secured =>
    onlyAdmin(secured){
      new ResponsePackage(reservationService.listAll,HttpStatus.SC_OK).toResult
    }
  }

  def getAllActiveReservations: Action[AnyContent] = securedAction { secured =>
    onlyAdmin(secured) {
      new ResponsePackage(reservationService.listAllActive, HttpStatus.SC_OK).toResult
    }
  }

  def getAllExpiredReservations: Action[AnyContent] = securedAction { secured =>
    onlyAdmin(secured) {
      new ResponsePackage(reservationService.listAllExpired, HttpStatus.SC_OK).toResult
    }
  }

  def cancelReservation(reservationId: Int): Action[AnyContent] = securedAction { secured =>
    onlyUser(secured) {
      reservationService.cancelReservation(reservationId, provideToken(secured))
      new ResponsePackage("Your reservation has been cancelled", HttpStatus.SC_OK).toResult
    }
  }

  def deleteReservation(reservationId: Int): Action[AnyContent] = securedAction { secured =>
    onlyAdmin(secured) {
      reservationService.deleteReservation(reservationId, provideToken(secured))
      new ResponsePackage(s"Reservation with id $reservationId has been cancelled!", HttpStatus.SC_OK).toResult
    }
  }

  def getReservationDetails(reservationId: Int): Action[AnyContent] = securedAction { secured =>
    onlyAdmin(secured) {
      new ResponsePackage(reservationService.getReservationById(reservationId), HttpStatus.SC_OK).toResult
    }
  }

  def findAlternativeRooms(reservationId: Int): Action[AnyContent] = securedAction { secured =>
    onlyAdmin(secured) {
      new ResponsePackage(reservationService.retrieveRoomAlternatives(reservationId, provideToken(secured)), HttpStatus.SC_OK).toResult
    }
  }

  def updateReservation(reservationId: Int): Action[AnyContent] = securedAction { secured =>
    onlyAdmin(secured) {
      val json = Json.stringify(secured.body.asJson.getOrElse(Json.obj()))
      val reservation = new JSONDeserializer[ReservationDto]()
        .use(null, classOf[ReservationDto])
        .deserialize(json, classOf[ReservationDto])

      val updatedReservation = reservationService.updateReservation(reservation, reservationId, provideToken(secured))
      new ResponsePackage(s"Reservation with id ${updatedReservation.getReservationId()} has been updated!", HttpStatus.SC_OK).toResult
    }
  }
}
