package com.radovan.scalatra.services

import com.radovan.scalatra.dto.GuestDto
import com.radovan.scalatra.utils.RegistrationForm

trait GuestService {

  def getGuestById(guestId:Int):GuestDto

  def getGuestByUserId(userId:Int):GuestDto

  def deleteGuest(guestId:Int,jwtToken:String):Unit

  def listAll:Array[GuestDto]

  def storeGuest(form:RegistrationForm):GuestDto

  def getCurrentGuest(jwtToken:String):GuestDto


}
