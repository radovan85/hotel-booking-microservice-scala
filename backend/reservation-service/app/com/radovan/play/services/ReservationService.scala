package com.radovan.play.services

import com.fasterxml.jackson.databind.JsonNode
import com.radovan.play.dto.ReservationDto
import com.radovan.play.utils.ReservationTimeForm

import java.sql.Timestamp

trait ReservationService {

  def listAvailableBookings(timeForm: ReservationTimeForm, jwtToken: String): Array[ReservationDto]

  def isRoomAvailable(roomId: Int, checkIn: Timestamp, checkOut: Timestamp): Boolean

  def addReservation(reservationDto: ReservationDto, jwtToken: String): ReservationDto

  def listAll: Array[ReservationDto]

  def listAllByGuestId(guestId: Int): Array[ReservationDto]

  def getMyReservations(jwtToken: String): Array[ReservationDto]

  def cancelReservation(reservationId: Int, jwtToken: String): Unit

  def deleteReservation(reservationId: Int, jwtToken: String): Unit

  def getReservationById(reservationId: Int): ReservationDto

  def deleteAllByGuestId(guestId: Int): Unit

  def deleteAllByRoomId(roomId: Int): Unit

  def listAllActive: Array[ReservationDto]

  def listAllExpired: Array[ReservationDto]

  def retrieveRoomAlternatives(reservationId: Int, jwtToken: String): Array[JsonNode]

  def updateReservation(reservationDto: ReservationDto, reservationId: Int, jwtToken: String): ReservationDto
}
