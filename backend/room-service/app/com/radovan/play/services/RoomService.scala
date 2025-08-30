package com.radovan.play.services

import com.radovan.play.dto.RoomDto

trait RoomService {

  def addRoom(roomDto: RoomDto): RoomDto

  def getRoomById(roomId: Int): RoomDto

  def listAll: Array[RoomDto]

  def listAllByCategoryId(categoryId: Int): Array[RoomDto]

  def deleteAllByCategoryId(categoryId: Int, jwtToken: String): Unit

  def updateRoom(roomDto: RoomDto, roomId: Int): RoomDto

  def deleteRoom(roomId: Int, jwtToken: String): Unit
}
