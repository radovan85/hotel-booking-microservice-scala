package com.radovan.play.services.impl

import com.radovan.play.converter.TempConverter
import com.radovan.play.dto.RoomCategoryDto
import com.radovan.play.exceptions.{ExistingInstanceException, InstanceUndefinedException}
import com.radovan.play.repositories.RoomCategoryRepository
import com.radovan.play.services.{RoomCategoryService, RoomService}
import jakarta.inject.{Inject, Provider, Singleton}

@Singleton
class RoomCategoryServiceImpl extends RoomCategoryService {

  private var categoryRepository: RoomCategoryRepository = _
  private var tempConverter: TempConverter = _
  private var roomServiceProvider: Provider[RoomService] = _

  @Inject
  private def initialize(categoryRepository: RoomCategoryRepository, tempConverter: TempConverter,
                         roomServiceProvider: Provider[RoomService]): Unit = {
    this.categoryRepository = categoryRepository
    this.tempConverter = tempConverter
    this.roomServiceProvider = roomServiceProvider
  }

  private def roomService = roomServiceProvider.get()


  override def addCategory(categoryDto: RoomCategoryDto): RoomCategoryDto = {
    val categoryOption = categoryRepository.findByName(categoryDto.getName())
    categoryOption match {
      case Some(value) => throw new ExistingInstanceException("This category exists already!")
      case None =>
        val storedCategory = categoryRepository.save(tempConverter.categoryDtoToEntity(categoryDto))
        tempConverter.categoryEntityToDto(storedCategory)
    }
  }

  override def getCategoryById(categoryId: Int): RoomCategoryDto = {
    categoryRepository.findById(categoryId) match {
      case Some(categoryEntity) => tempConverter.categoryEntityToDto(categoryEntity)
      case None => throw new InstanceUndefinedException("The category has not been found!")
    }
  }

  override def updateCategory(categoryDto: RoomCategoryDto, categoryId: Integer): RoomCategoryDto = {
    val currentCategory = getCategoryById(categoryId)
    val categoryOption = categoryRepository.findByName(categoryDto.getName())
    categoryOption match {
      case Some(tempCategory) =>
        if (tempCategory.getRoomCategoryId() != categoryId) throw new ExistingInstanceException("This category exists already!")
      case None =>
    }

    categoryDto.setRoomCategoryId(currentCategory.getRoomCategoryId())
    val roomsIdsOption = Option(currentCategory.getRoomsIds())
    roomsIdsOption match {
      case Some(roomsIds) => categoryDto.setRoomsIds(roomsIds)
      case None =>
    }

    val updatedCategory = categoryRepository.save(tempConverter.categoryDtoToEntity(categoryDto))
    tempConverter.categoryEntityToDto(updatedCategory)
  }

  override def deleteCategory(categoryId: Int, jwtToken: String): Unit = {
    getCategoryById(categoryId)
    roomService.listAllByCategoryId(categoryId).foreach(room => {
      roomService.deleteRoom(room.getRoomId(), jwtToken)
    })
    categoryRepository.deleteById(categoryId)
  }

  override def listAll: Array[RoomCategoryDto] = {
    categoryRepository.findAll.collect {
      case categoryEntity => tempConverter.categoryEntityToDto(categoryEntity)
    }
  }
}
