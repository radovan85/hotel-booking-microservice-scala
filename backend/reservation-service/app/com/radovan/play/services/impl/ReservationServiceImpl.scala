package com.radovan.play.services.impl

import com.fasterxml.jackson.databind.JsonNode
import com.radovan.play.brokers.ReservationNatsSender
import com.radovan.play.converter.TempConverter
import com.radovan.play.dto.{NoteDto, ReservationDto}
import com.radovan.play.exceptions.{InstanceUndefinedException, OperationNotAllowedException}
import com.radovan.play.repositories.{NoteRepository, ReservationRepository}
import com.radovan.play.services.ReservationService
import com.radovan.play.utils.{ReservationTimeForm, TimeConversionUtils}
import jakarta.inject.{Inject, Singleton}

import java.sql.Timestamp
import java.time.ZoneId
import scala.collection.mutable.ArrayBuffer
import scala.util.control.Breaks._

@Singleton
class ReservationServiceImpl extends ReservationService {

  private var reservationRepository: ReservationRepository = _
  private var noteRepository: NoteRepository = _
  private var tempConverter: TempConverter = _
  private var conversionUtils: TimeConversionUtils = _
  private var natsSender: ReservationNatsSender = _
  private val zoneId = ZoneId.of("UTC")

  @Inject
  private def initialize(reservationRepository: ReservationRepository, noteRepository: NoteRepository,
                         tempConverter: TempConverter, conversionUtils: TimeConversionUtils,
                         natsSender: ReservationNatsSender): Unit = {
    this.reservationRepository = reservationRepository
    this.noteRepository = noteRepository
    this.tempConverter = tempConverter
    this.conversionUtils = conversionUtils
    this.natsSender = natsSender
  }


  override def listAvailableBookings(timeForm: ReservationTimeForm, jwtToken: String): Array[ReservationDto] = {
    val checkInStr = s"${timeForm.getCheckInDate()} 14:00"
    val checkOutStr = s"${timeForm.getCheckOutDate()} 12:00"

    conversionUtils.isValidPeriod(checkInStr, checkOutStr)
    val reservations = new ArrayBuffer[ReservationDto]()
    val numberOfNights = conversionUtils.calculateNumberOfNights(checkInStr, checkOutStr)

    val guestNode = natsSender.retrieveCurrentGuest(jwtToken)
    val guestId = guestNode.get("guestId").asInt()

    val allCategories = natsSender.retrieveRoomCategories(jwtToken)
    val checkIn = conversionUtils.stringToTimestamp(checkInStr)
    val checkOut = conversionUtils.stringToTimestamp(checkOutStr)

    allCategories.foreach { categoryNode =>
      val categoryId = categoryNode.get("roomCategoryId").asInt()
      val pricePerNight = categoryNode.get("price").floatValue()
      val rooms = natsSender.retrieveRooms(categoryId, jwtToken)

      breakable {
        rooms.foreach { roomNode =>
          val roomId = roomNode.get("roomId").asInt()
          if (isRoomAvailable(roomId, checkIn, checkOut)) {
            val reservationDto = new ReservationDto
            reservationDto.setGuestId(guestId)
            reservationDto.setRoomId(roomId)
            reservationDto.setCheckInDateStr(timeForm.getCheckInDate())
            reservationDto.setCheckOutDateStr(timeForm.getCheckOutDate())
            reservationDto.setNumberOfNights(numberOfNights)
            reservationDto.setPrice(pricePerNight * numberOfNights)
            reservations += reservationDto
            break() // ⛔️ izlazi iz rooms.foreach čim dodaš rezervaciju
          }
        }
      }
    }

    reservations.toArray
  }


  def isRoomAvailable(roomId: Int, checkIn: Timestamp, checkOut: Timestamp): Boolean = {
    val reservations = reservationRepository.findAllByRoomId(roomId)

    !reservations.exists { r =>
      checkIn.before(r.getCheckOutDate()) && checkOut.after(r.getCheckInDate())
    }
  }


  override def addReservation(reservationDto: ReservationDto, jwtToken: String): ReservationDto = {
    val checkInDate = conversionUtils.stringToTimestamp(s"${reservationDto.getCheckInDateStr()} 14:00")
    val checkOutDate = conversionUtils.stringToTimestamp(s"${reservationDto.getCheckOutDateStr()} 12:00")
    val authUser = natsSender.retrieveCurrentUser(jwtToken)
    val firstName = getSafeText(authUser, "firstName")
    val lastName = getSafeText(authUser, "lastName")
    val reservationEntity = tempConverter.reservationDtoToEntity(reservationDto)
    reservationEntity.setCreateTime(conversionUtils.getCurrentUTCTimestamp)
    reservationEntity.setUpdateTime(conversionUtils.getCurrentUTCTimestamp)
    reservationEntity.setCheckInDate(checkInDate)
    reservationEntity.setCheckOutDate(checkOutDate)
    val storedReservation = reservationRepository.save(reservationEntity)
    val note = new NoteDto
    note.setSubject("Reservation Created")
    val text = s"User $firstName $lastName has reserved room ${storedReservation.getRoomId()}. Check-in is scheduled for ${reservationDto.getCheckInDateStr()}."
    note.setText(text)
    val noteEntity = tempConverter.noteDtoToEntity(note)
    noteEntity.setCreateTime(conversionUtils.getCurrentUTCTimestamp)
    noteRepository.save(noteEntity)
    tempConverter.reservationEntityToDto(storedReservation)
  }

  override def listAll: Array[ReservationDto] = {
    reservationRepository.findAll.collect {
      case reservation => tempConverter.reservationEntityToDto(reservation)
    }
  }

  override def listAllByGuestId(guestId: Int): Array[ReservationDto] = {
    reservationRepository.findAllByGuestId(guestId).collect {
      case reservation => tempConverter.reservationEntityToDto(reservation)
    }
  }

  override def getMyReservations(jwtToken: String): Array[ReservationDto] = {
    val currentGuest = natsSender.retrieveCurrentGuest(jwtToken)
    val guestId = currentGuest.get("guestId").asInt()
    reservationRepository.findAll
      .filter(_.getGuestId() == guestId)
      .sortBy(_.getCheckInDate())
      .map(tempConverter.reservationEntityToDto)
  }

  override def cancelReservation(reservationId: Int, jwtToken: String): Unit = {
    val reservation = getReservationById(reservationId)
    val currentGuest = natsSender.retrieveCurrentGuest(jwtToken)
    val guestId = currentGuest.get("guestId").asInt()
    if (guestId != reservation.getGuestId() || !reservation.possibleCancel) {
      throw new OperationNotAllowedException("Operation not allowed!")
    }

    val authUser = natsSender.retrieveCurrentUser(jwtToken)
    val room = natsSender.retrieveRoomById(reservation.getRoomId(), jwtToken)
    val firstName = getSafeText(authUser, "firstName")
    val lastName = getSafeText(authUser, "lastName")
    val roomNumber = room.get("roomNumber").asInt()
    val noteDto = new NoteDto
    noteDto.setSubject("Reservation cancelled")
    val text = s"User $firstName $lastName has cancelled their reservation for room No $roomNumber, scheduled on ${reservation.getCheckInDateStr()}."
    noteDto.setText(text)
    val noteEntity = tempConverter.noteDtoToEntity(noteDto)
    noteEntity.setCreateTime(conversionUtils.getCurrentUTCTimestamp)
    noteRepository.save(noteEntity)
    reservationRepository.deleteById(reservationId)
  }

  override def deleteReservation(reservationId: Int, jwtToken: String): Unit = {
    val reservation = getReservationById(reservationId)
    val guestNode = natsSender.retrieveGuestById(reservation.getGuestId(),jwtToken)
    val userId = guestNode.get("userId").asInt()
    val userNode = natsSender.retrieveUserById(userId, jwtToken)
    val roomNode = natsSender.retrieveRoomById(reservation.getRoomId(), jwtToken)
    val roomNumber = roomNode.get("roomNumber").asInt()
    val firstName = getSafeText(userNode, "firstName")
    val lastName = getSafeText(userNode, "lastName")
    reservationRepository.deleteById(reservationId)
    val noteDto = new NoteDto
    noteDto.setSubject("Reservation removed")
    val text = s"The reservation for Mr./Ms. $firstName $lastName , scheduled for Room No. $roomNumber on ${reservation.getCheckInDateStr()} , has been removed by the Administrator, as it was either cancelled or no longer valid."
    noteDto.setText(text)
    val noteEntity = tempConverter.noteDtoToEntity(noteDto)
    noteEntity.setCreateTime(conversionUtils.getCurrentUTCTimestamp)
    noteRepository.save(noteEntity)

  }

  override def getReservationById(reservationId: Int): ReservationDto = {
    reservationRepository.findById(reservationId) match {
      case Some(reservation) => tempConverter.reservationEntityToDto(reservation)
      case None => throw new InstanceUndefinedException("Reservation has not been found!")
    }
  }

  override def deleteAllByGuestId(guestId: Int): Unit = {
    reservationRepository.deleteAllByGuestId(guestId)
  }

  override def deleteAllByRoomId(roomId: Int): Unit = {
    reservationRepository.deleteAllByRoomId(roomId)
  }

  override def listAllActive: Array[ReservationDto] = {
    val currentDate = conversionUtils.getCurrentUTCTimestamp.toLocalDateTime.atZone(zoneId)

    reservationRepository.findAll
      .filter { reservation =>
        reservation.getCheckOutDate()
          .toLocalDateTime
          .atZone(zoneId)
          .isAfter(currentDate)
      }
      .sortBy(_.getCheckInDate())
      .map(tempConverter.reservationEntityToDto)
  }


  override def listAllExpired: Array[ReservationDto] = {
    val currentDate = conversionUtils.getCurrentUTCTimestamp.toLocalDateTime.atZone(zoneId)

    reservationRepository.findAll
      .filter { reservation =>
        reservation.getCheckOutDate()
          .toLocalDateTime
          .atZone(zoneId)
          .isBefore(currentDate)
      }
      .map(tempConverter.reservationEntityToDto)
  }


  override def retrieveRoomAlternatives(reservationId: Int, jwtToken: String): Array[JsonNode] = {
    val returnValue = new ArrayBuffer[JsonNode]()
    val reservation = getReservationById(reservationId)
    val currentRoomNode = natsSender.retrieveRoomById(reservation.getRoomId(), jwtToken)
    val categoryId = currentRoomNode.get("roomCategoryId").asInt()
    val roomList = natsSender.retrieveRooms(categoryId, jwtToken)
    val checkIn = conversionUtils.stringToTimestamp(reservation.getCheckInDateStr())
    val checkOut = conversionUtils.stringToTimestamp(reservation.getCheckOutDateStr())
    roomList.foreach { roomNode =>
      breakable {
        val roomId = roomNode.get("roomId").asInt()
        if (roomId == reservation.getRoomId()) break() // idiomatski continue
        val available = isRoomAvailable(roomId, checkIn, checkOut)
        if (available) returnValue += roomNode
      }
    }

    returnValue.toArray
  }

  override def updateReservation(reservationDto: ReservationDto, reservationId: Int, jwtToken: String): ReservationDto = {
    val currentReservation = getReservationById(reservationId)
    val customerNode = natsSender.retrieveGuestById(currentReservation.getGuestId(),jwtToken)
    val userId = customerNode.get("userId").asInt()
    val userNode = natsSender.retrieveUserById(userId,jwtToken)
    val firstName = getSafeText(userNode, "firstName")
    val lastName = getSafeText(userNode, "lastName")
    val roomNode = natsSender.retrieveRoomById(reservationDto.getRoomId(), jwtToken)
    val roomNumber = roomNode.get("roomNumber").asInt()
    reservationDto.setReservationId(reservationId)
    val reservationEntity = tempConverter.reservationDtoToEntity(reservationDto)
    reservationEntity.setCheckInDate(conversionUtils.stringToTimestamp(currentReservation.getCheckInDateStr()))
    reservationEntity.setCheckOutDate(conversionUtils.stringToTimestamp(currentReservation.getCheckOutDateStr()))
    reservationEntity.setCreateTime(conversionUtils.stringToTimestamp(currentReservation.getCreateTimeStr()))
    reservationEntity.setUpdateTime(conversionUtils.getCurrentUTCTimestamp)
    val updatedReservation = reservationRepository.save(reservationEntity)
    val noteDto = new NoteDto
    noteDto.setSubject("Reservation updated")
    val text = s"Reservation #${reservationDto.getReservationId()} assigned to user $firstName $lastName, scheduled for ${currentReservation.getCheckInDateStr()}, has been successfully updated with a new room assignment (Room $roomNumber)."
    noteDto.setText(text)
    val noteEntity = tempConverter.noteDtoToEntity(noteDto)
    noteEntity.setCreateTime(conversionUtils.getCurrentUTCTimestamp)
    noteRepository.save(noteEntity)
    tempConverter.reservationEntityToDto(updatedReservation)

  }

  private def getSafeText(node: JsonNode, field: String): String = {
    if (node != null && node.has(field) && !node.get(field).isNull) {
      node.get(field).asText()
    } else {
      ""
    }
  }

}
