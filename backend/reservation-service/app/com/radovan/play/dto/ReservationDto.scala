package com.radovan.play.dto

import java.time.{LocalDateTime, ZoneId, ZonedDateTime}
import java.time.format.DateTimeFormatter
import scala.beans.BeanProperty

@SerialVersionUID(1L)
class ReservationDto extends Serializable {

  @BeanProperty var reservationId: Integer = _
  @BeanProperty var roomId: Integer = _
  @BeanProperty var guestId: Integer = _
  @BeanProperty var checkInDateStr: String = _
  @BeanProperty var checkOutDateStr: String = _
  @BeanProperty var createTimeStr: String = _
  @BeanProperty var updateTimeStr: String = _
  @BeanProperty var price: Float = _
  @BeanProperty var numberOfNights: Integer = _

  def possibleCancel: Boolean = {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
    val currentTimeUTC = ZonedDateTime.now(ZoneId.of("UTC"))
    val cancelDeadline = currentTimeUTC.plusDays(1).toLocalDateTime

    val checkInDateTime = LocalDateTime
      .parse(checkInDateStr, formatter)
      .atZone(ZoneId.of("UTC"))
      .toLocalDateTime

    cancelDeadline.isBefore(checkInDateTime)
  }

}
