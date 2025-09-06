package com.radovan.play.dto

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.{NotNull, Positive}

import scala.beans.BeanProperty

@SerialVersionUID(1L)
class RoomDto extends Serializable {

  @BeanProperty var roomId: Integer = _

  @NotNull
  @Positive
  @BeanProperty var roomNumber: Integer = _

  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  @BeanProperty var price: Float = _

  @NotNull
  @Positive
  @BeanProperty var roomCategoryId: Integer = _
}
