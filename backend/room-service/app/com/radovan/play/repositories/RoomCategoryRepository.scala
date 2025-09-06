package com.radovan.play.repositories

import com.radovan.play.entity.RoomCategoryEntity

trait RoomCategoryRepository {

  def findById(categoryId:Integer):Option[RoomCategoryEntity]

  def findByName(name:String):Option[RoomCategoryEntity]

  def save(categoryEntity: RoomCategoryEntity):RoomCategoryEntity

  def findAll:Array[RoomCategoryEntity]

  def deleteById(categoryId:Integer):Unit
}
