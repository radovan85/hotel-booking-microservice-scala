package com.radovan.scalatra.services.impl

import com.radovan.scalatra.brokers.GuestNatsSender
import com.radovan.scalatra.converter.TempConverter
import com.radovan.scalatra.dto.GuestDto
import com.radovan.scalatra.exceptions.{ExistingInstanceException, InstanceUndefinedException}
import com.radovan.scalatra.repositories.GuestRepository
import com.radovan.scalatra.services.GuestService
import com.radovan.scalatra.utils.RegistrationForm
import jakarta.inject.{Inject, Singleton}

@Singleton
class GuestServiceImpl extends GuestService{

  private var guestRepository:GuestRepository = _
  private var tempConverter:TempConverter = _
  private var natsSender:GuestNatsSender = _

  @Inject
  private def initialize(guestRepository: GuestRepository,tempConverter: TempConverter,
                         natsSender: GuestNatsSender):Unit = {
    this.guestRepository = guestRepository
    this.tempConverter = tempConverter
    this.natsSender = natsSender
  }

  override def getGuestById(guestId: Int): GuestDto = {
    guestRepository.findById(guestId) match {
      case Some(guestEntity) => tempConverter.guestEntityToDto(guestEntity)
      case None => throw new InstanceUndefinedException("The guest has not been found!")
    }
  }

  override def getGuestByUserId(userId: Int): GuestDto = {
    guestRepository.findByUserId(userId) match {
      case Some(guestEntity) => tempConverter.guestEntityToDto(guestEntity)
      case None => throw new InstanceUndefinedException("The guest has not been found!")
    }
  }

  override def deleteGuest(guestId: Int, jwtToken: String): Unit = {
    val guest = getGuestById(guestId)
    guestRepository.deleteById(guestId)
    natsSender.deleteUser(guest.getUserId(), jwtToken)
    natsSender.sendDeleteAllReservations(guest.getGuestId(),jwtToken)
  }

  override def listAll: Array[GuestDto] = {
    guestRepository.findAll.collect{
      case guestEntity => tempConverter.guestEntityToDto(guestEntity)
    }
  }

  override def storeGuest(form: RegistrationForm): GuestDto = {
      val user = form.getUser()
      val guest = form.getGuest()
      guest.setUserId(natsSender.createUser(user))
      val storedGuest = guestRepository.save(tempConverter.guestDtoToEntity(guest))
      tempConverter.guestEntityToDto(storedGuest)
  }

  override def getCurrentGuest(jwtToken: String): GuestDto = {
    val currentUserNode = natsSender.retrieveCurrentUser(jwtToken)
    val userId = currentUserNode.get("id").asInt()
    getGuestByUserId(userId)
  }
}
