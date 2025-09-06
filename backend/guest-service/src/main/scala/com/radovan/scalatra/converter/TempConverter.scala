package com.radovan.scalatra.converter

import com.radovan.scalatra.dto.GuestDto
import com.radovan.scalatra.entity.GuestEntity
import jakarta.inject.{Inject, Singleton}
import org.modelmapper.ModelMapper

@Singleton
class TempConverter {

  private var mapper:ModelMapper = _

  @Inject
  private def initialize(mapper: ModelMapper):Unit = {
    this.mapper = mapper
  }

  def guestEntityToDto(guestEntity: GuestEntity):GuestDto = {
    mapper.map(guestEntity, classOf[GuestDto])
  }

  def guestDtoToEntity(guestDto: GuestDto):GuestEntity = {
    mapper.map(guestDto, classOf[GuestEntity])
  }
 }
