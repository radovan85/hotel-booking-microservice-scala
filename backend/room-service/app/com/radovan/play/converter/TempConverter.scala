package com.radovan.play.converter

import com.radovan.play.dto.{RoomCategoryDto, RoomDto}
import com.radovan.play.entity.{RoomCategoryEntity, RoomEntity}
import com.radovan.play.repositories.{RoomCategoryRepository, RoomRepository}
import jakarta.inject.{Inject, Singleton}
import org.modelmapper.ModelMapper

import scala.collection.mutable.ArrayBuffer
import scala.jdk.CollectionConverters._

@Singleton
class TempConverter {

  private var mapper:ModelMapper = _
  private var categoryRepository:RoomCategoryRepository = _
  private var roomRepository:RoomRepository = _

  @Inject
  private def initialize(mapper: ModelMapper,categoryRepository: RoomCategoryRepository,
                         roomRepository: RoomRepository):Unit = {
    this.mapper = mapper
    this.categoryRepository = categoryRepository
    this.roomRepository = roomRepository
  }

  def categoryEntityToDto(categoryEntity: RoomCategoryEntity):RoomCategoryDto = {
    val returnValue = mapper.map(categoryEntity,classOf[RoomCategoryDto])
    val wcOption = Option(categoryEntity.getWc())
    wcOption match {
      case Some(wc) => returnValue.setWc(wc.asInstanceOf[Short])
      case None =>
    }

    val wifiOption = Option(categoryEntity.getWifi())
    wifiOption match {
      case Some(wifi) => returnValue.setWifi(wifi.asInstanceOf[Short])
      case None =>
    }

    val tvOption = Option(categoryEntity.getTv())
    tvOption match {
      case Some(tv) => returnValue.setTv(tv.asInstanceOf[Short])
      case None =>
    }

    val barOption = Option(categoryEntity.getBar())
    barOption match {
      case Some(bar) => returnValue.setBar(bar.asInstanceOf[Short])
      case None =>
    }

    val roomsOption = Option(categoryEntity.getRooms().asScala)
    val roomsIds = new ArrayBuffer[Integer]()
    roomsOption match {
      case Some(rooms) =>
        rooms.foreach(roomEntity => roomsIds += roomEntity.getRoomId())
      case None =>
    }

    returnValue.setRoomsIds(roomsIds.toArray)
    returnValue
  }

  def categoryDtoToEntity(categoryDto: RoomCategoryDto):RoomCategoryEntity = {
    val returnValue = mapper.map(categoryDto, classOf[RoomCategoryEntity])

    val wcOption = Option(categoryDto.getWc())
    wcOption match {
      case Some(wc) => returnValue.setWc(wc.asInstanceOf[Byte])
      case None =>
    }

    val wifiOption = Option(categoryDto.getWifi())
    wifiOption match {
      case Some(wifi) => returnValue.setWifi(wifi.asInstanceOf[Byte])
      case None =>
    }

    val tvOption = Option(categoryDto.getTv())
    tvOption match {
      case Some(tv) => returnValue.setTv(tv.asInstanceOf[Byte])
      case None =>
    }

    val barOption = Option(categoryDto.getBar())
    barOption match {
      case Some(bar) => returnValue.setBar(bar.asInstanceOf[Byte])
      case None =>
    }

    val roomsIdsOption = Option(categoryDto.getRoomsIds())
    val rooms = new ArrayBuffer[RoomEntity]()
    roomsIdsOption match {
      case Some(roomsIds) =>
        roomsIds.foreach(roomId => {
          roomRepository.findById(roomId) match {
            case Some(roomEntity) => rooms += roomEntity
            case None =>
          }
        })
      case None =>
    }

    returnValue.setRooms(rooms.asJava)
    returnValue
  }

  def roomEntityToDto(roomEntity: RoomEntity):RoomDto = {
    val returnValue = mapper.map(roomEntity,classOf[RoomDto])
    val categoryOption = Option(roomEntity.getRoomCategory())
    categoryOption match {
      case Some(categoryEntity) => returnValue.setRoomCategoryId(categoryEntity.getRoomCategoryId())
      case None =>
    }

    returnValue
  }

  def roomDtoToEntity(roomDto: RoomDto):RoomEntity = {
    val returnValue = mapper.map(roomDto,classOf[RoomEntity])
    val categoryIdOption = Option(roomDto.getRoomCategoryId())
    categoryIdOption match {
      case Some(categoryId) =>
        categoryRepository.findById(categoryId) match {
          case Some(categoryEntity) => returnValue.setRoomCategory(categoryEntity)
          case None =>
        }
      case None =>
    }

    returnValue
  }
}
