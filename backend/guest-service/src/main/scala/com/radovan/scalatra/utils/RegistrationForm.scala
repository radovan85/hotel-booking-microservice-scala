package com.radovan.scalatra.utils

import com.fasterxml.jackson.databind.JsonNode
import com.radovan.scalatra.dto.GuestDto
import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull

import scala.beans.BeanProperty

@SerialVersionUID(1L)
class RegistrationForm extends Serializable{

  @Valid
  @NotNull
  @BeanProperty var user:JsonNode = _

  @Valid
  @NotNull
  @BeanProperty var guest:GuestDto = _
}
