package com.radovan.play.repositories

import com.radovan.play.entity.RoomEntity

trait RoomRepository {

  def findById(roomId:Integer):Option[RoomEntity]

  def save(roomEntity: RoomEntity):RoomEntity

  def deleteById(roomId:Integer):Unit

  def findAll:Array[RoomEntity]

  def findAllByCategoryId(categoryId:Integer):Array[RoomEntity]

  def findByRoomNumber(roomNumber:Integer):Option[RoomEntity]
}
