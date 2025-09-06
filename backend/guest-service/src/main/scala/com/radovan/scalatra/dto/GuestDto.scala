package com.radovan.scalatra.dto

import jakarta.validation.constraints.{Max, Min, NotEmpty, Size}

import scala.beans.BeanProperty

@SerialVersionUID(1L)
class GuestDto extends Serializable{

  @BeanProperty var guestId:Integer = _

  @NotEmpty
  @Size(min=9,max=15)
  @BeanProperty var phoneNumber:String = _

  @Min(100000L)      // najmanje 6 cifara
  @Max(999999999999L) // najviše 12 cifara
  @BeanProperty var idNumber:java.lang.Long = _

  @BeanProperty var userId:Integer = _
}
