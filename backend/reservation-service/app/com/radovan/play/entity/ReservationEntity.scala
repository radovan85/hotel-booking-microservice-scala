package com.radovan.play.entity

import jakarta.persistence.{Column, Entity, GeneratedValue, GenerationType, Id, Table}

import java.sql.Timestamp
import scala.beans.BeanProperty

@Entity
@Table(name = "reservations")
@SerialVersionUID(1L)
class ReservationEntity extends Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "reservation_id")
  @BeanProperty var reservationId: Integer = _

  @Column(name = "room_id", nullable = false)
  @BeanProperty var roomId: Integer = _

  @Column(name = "guest_id", nullable = false)
  @BeanProperty var guestId: Integer = _

  @Column(name = "check_in", nullable = false)
  @BeanProperty var checkInDate: Timestamp = _

  @Column(name = "check_out", nullable = false)
  @BeanProperty var checkOutDate: Timestamp = _

  @Column(name = "create_time", nullable = false)
  @BeanProperty var createTime: Timestamp = _

  @Column(name = "update_time", nullable = false)
  @BeanProperty var updateTime: Timestamp = _

  @Column(nullable = false)
  @BeanProperty var price: Float = _

  @Column(name = "num_of_nights", nullable = false)
  @BeanProperty var numberOfNights: Integer = _

}
