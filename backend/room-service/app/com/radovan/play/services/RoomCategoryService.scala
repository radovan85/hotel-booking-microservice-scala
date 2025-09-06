package com.radovan.play.services

import com.radovan.play.dto.RoomCategoryDto

trait RoomCategoryService {

  def addCategory(categoryDto: RoomCategoryDto): RoomCategoryDto

  def getCategoryById(categoryId: Int): RoomCategoryDto

  def updateCategory(categoryDto: RoomCategoryDto, categoryId: Integer): RoomCategoryDto

  def deleteCategory(categoryId: Int, jwtToken: String): Unit

  def listAll: Array[RoomCategoryDto]
}
