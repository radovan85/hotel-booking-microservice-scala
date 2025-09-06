package com.radovan.play.dto

import jakarta.validation.constraints.{Max, Min, NotEmpty, NotNull, Size}

import scala.beans.BeanProperty

@SerialVersionUID(1L)
class RoomCategoryDto extends Serializable {

  @BeanProperty var roomCategoryId: Integer = _

  @NotEmpty
  @Size(min = 2, max = 30)
  @BeanProperty var name: String = _

  @NotNull
  @Min(5)
  @BeanProperty var price: Float = _

  @NotNull
  @Min(0)
  @Max(1)
  @BeanProperty var wifi: Short = _

  @NotNull
  @Min(0)
  @Max(1)
  @BeanProperty var wc: Short = _

  @NotNull
  @Min(0)
  @Max(1)
  @BeanProperty var tv: Short = _

  @NotNull
  @Min(0)
  @Max(1)
  @BeanProperty var bar: Short = _

  @BeanProperty var roomsIds: Array[Integer] = _
}
