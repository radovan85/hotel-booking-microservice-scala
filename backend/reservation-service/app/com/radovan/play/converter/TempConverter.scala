package com.radovan.play.converter

import com.radovan.play.dto.{NoteDto, ReservationDto}
import com.radovan.play.entity.{NoteEntity, ReservationEntity}
import com.radovan.play.utils.TimeConversionUtils
import jakarta.inject.{Inject, Singleton}
import org.modelmapper.ModelMapper

@Singleton
class TempConverter {

  private var mapper:ModelMapper = _
  private var conversionUtils:TimeConversionUtils = _

  @Inject
  private def initialize(mapper: ModelMapper,conversionUtils: TimeConversionUtils):Unit = {
    this.mapper = mapper
    this.conversionUtils = conversionUtils
  }

  def reservationEntityToDto(reservationEntity: ReservationEntity):ReservationDto = {
    val returnValue = mapper.map(reservationEntity, classOf[ReservationDto])

    val checkInDateOption = Option(reservationEntity.getCheckInDate())
    checkInDateOption match {
      case Some(checkInDate) => returnValue.setCheckInDateStr(conversionUtils.timestampToString(checkInDate))
      case None =>
    }

    val checkOutDateOption = Option(reservationEntity.getCheckOutDate())
    checkOutDateOption match {
      case Some(checkOutDate) => returnValue.setCheckOutDateStr(conversionUtils.timestampToString(checkOutDate))
      case None =>
    }

    val createTimeOption = Option(reservationEntity.getCreateTime())
    createTimeOption match {
      case Some(createTime) => returnValue.setCreateTimeStr(conversionUtils.timestampToString(createTime))
      case None =>
    }

    val updateTimeOption = Option(reservationEntity.getUpdateTime())
    updateTimeOption match {
      case Some(updateTime) => returnValue.setUpdateTimeStr(conversionUtils.timestampToString(updateTime))
      case None =>
    }

    returnValue
  }

  def reservationDtoToEntity(reservationDto:ReservationDto):ReservationEntity = {
    mapper.map(reservationDto, classOf[ReservationEntity])
  }

  def noteEntityToDto(noteEntity: NoteEntity):NoteDto = {
    val returnValue = mapper.map(noteEntity, classOf[NoteDto])

    val createTimeOption = Option(noteEntity.getCreateTime())
    createTimeOption match {
      case Some(createTime) => returnValue.setCreateTimeStr(conversionUtils.timestampToString(createTime))
      case None =>
    }

    returnValue
  }

  def noteDtoToEntity(noteDto: NoteDto):NoteEntity = {
    mapper.map(noteDto, classOf[NoteEntity])
  }
}
