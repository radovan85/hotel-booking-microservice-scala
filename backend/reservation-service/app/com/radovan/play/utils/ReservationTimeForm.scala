package com.radovan.play.utils

import jakarta.validation.constraints.NotEmpty

import scala.beans.BeanProperty

@SerialVersionUID(1L)
class ReservationTimeForm extends Serializable{

  @NotEmpty
  @BeanProperty var checkInDate:String = _

  @NotEmpty
  @BeanProperty var checkOutDate:String = _
}
