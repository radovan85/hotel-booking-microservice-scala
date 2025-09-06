package com.radovan.scalatra.entity

import jakarta.persistence.{Column, Entity, GeneratedValue, GenerationType, Id, Table}

import scala.beans.BeanProperty

@Entity
@Table(name = "guests")
@SerialVersionUID(1L)
class GuestEntity extends Serializable{

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  @BeanProperty var guestId:Integer = _

  @Column(name = "phone_number", nullable = false, length = 15)
  @BeanProperty var phoneNumber:String = _

  @Column(name = "id_number", nullable = false, length = 12)
  @BeanProperty var idNumber: java.lang.Long= _

  @Column(name = "user_id",nullable = false)
  @BeanProperty var userId:Integer = _

}
