package com.radovan.play.services.impl

import com.radovan.play.brokers.RoomNatsSender
import com.radovan.play.converter.TempConverter
import com.radovan.play.dto.RoomDto
import com.radovan.play.exceptions.{ExistingInstanceException, InstanceUndefinedException}
import com.radovan.play.repositories.RoomRepository
import com.radovan.play.services.{RoomCategoryService, RoomService}
import jakarta.inject.{Inject, Singleton}

@Singleton
class RoomServiceImpl extends RoomService {

  private var roomRepository: RoomRepository = _
  private var categoryService: RoomCategoryService = _
  private var tempConverter: TempConverter = _
  private var natsSender: RoomNatsSender = _

  @Inject
  private def initialize(roomRepository: RoomRepository, categoryService: RoomCategoryService,
                         tempConverter: TempConverter, natsSender: RoomNatsSender): Unit = {
    this.roomRepository = roomRepository
    this.categoryService = categoryService
    this.tempConverter = tempConverter
    this.natsSender = natsSender
  }


  override def addRoom(roomDto: RoomDto): RoomDto = {
    val category = categoryService.getCategoryById(roomDto.getRoomCategoryId())
    roomRepository.findByRoomNumber(roomDto.getRoomNumber()) match {
      case Some(value) => throw new ExistingInstanceException("This room number exists already!")
      case None =>
    }

    roomDto.setPrice(category.getPrice())
    val storedRoom = roomRepository.save(tempConverter.roomDtoToEntity(roomDto))
    tempConverter.roomEntityToDto(storedRoom)

  }

  override def getRoomById(roomId: Int): RoomDto = {
    roomRepository.findById(roomId) match {
      case Some(roomEntity) => tempConverter.roomEntityToDto(roomEntity)
      case None => throw new InstanceUndefinedException("The room has not been found!")
    }
  }

  override def deleteRoom(roomId: Int, jwtToken: String): Unit = {
    getRoomById(roomId)
    roomRepository.deleteById(roomId)
    natsSender.sendDeleteAllReservations(roomId, jwtToken)
  }

  override def listAll: Array[RoomDto] = {
    roomRepository.findAll.collect {
      case roomEntity => tempConverter.roomEntityToDto(roomEntity)
    }
  }

  override def listAllByCategoryId(categoryId: Int): Array[RoomDto] = {
    roomRepository.findAllByCategoryId(categoryId).collect {
      case roomEntity => tempConverter.roomEntityToDto(roomEntity)
    }
  }

  override def deleteAllByCategoryId(categoryId: Int, jwtToken: String): Unit = {
    listAllByCategoryId(categoryId).foreach(room => deleteRoom(room.getRoomId(), jwtToken))
  }

  override def updateRoom(roomDto: RoomDto, roomId: Int): RoomDto = {
    val category = categoryService.getCategoryById(roomDto.getRoomCategoryId())
    val currentRoom = getRoomById(roomId)
    roomRepository.findByRoomNumber(roomDto.getRoomNumber()) match {
      case Some(tempRoom) =>
        if (tempRoom.getRoomNumber() == roomDto.getRoomNumber()) {
          if (tempRoom.getRoomId() != currentRoom.getRoomId()) {
            throw new ExistingInstanceException("This room number exists already!")
          }
        }
      case None =>
    }
    roomDto.setRoomId(currentRoom.getRoomId())
    roomDto.setPrice(category.getPrice())
    val updatedRoom = roomRepository.save(tempConverter.roomDtoToEntity(roomDto))
    tempConverter.roomEntityToDto(updatedRoom)
  }
}
