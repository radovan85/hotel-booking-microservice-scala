package com.radovan.scalatra.repositories

import com.radovan.scalatra.entity.GuestEntity

trait GuestRepository {

  def findById(guestId:Integer):Option[GuestEntity]

  def findByUserId(userId:Integer):Option[GuestEntity]

  def save(guestEntity: GuestEntity):GuestEntity

  def deleteById(guestId:Integer):Unit

  def findAll:Array[GuestEntity]


}
