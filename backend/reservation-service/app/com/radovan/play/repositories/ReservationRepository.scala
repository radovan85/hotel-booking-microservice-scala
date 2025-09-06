package com.radovan.play.repositories

import com.radovan.play.entity.ReservationEntity

trait ReservationRepository {

  def findAllByRoomId(roomId:Integer):Array[ReservationEntity]

  def save(reservationEntity: ReservationEntity):ReservationEntity

  def findAll:Array[ReservationEntity]

  def findAllByGuestId(guestId:Integer):Array[ReservationEntity]

  def deleteById(reservationId:Integer):Unit

  def findById(reservationId:Integer):Option[ReservationEntity]

  def deleteAllByGuestId(guestId:Integer):Unit

  def deleteAllByRoomId(roomId:Integer):Unit
}
