package com.radovan.play.entity

import jakarta.persistence.{Column, Entity, GeneratedValue, GenerationType, Id, Table}

import java.sql.Timestamp
import scala.beans.BeanProperty

@Entity
@Table(name="notes")
@SerialVersionUID(1L)
class NoteEntity extends Serializable{

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "note_id")
  @BeanProperty var noteId:Integer = _

  @Column(nullable = false, length = 40)
  @BeanProperty var subject:String = _

  @Column(nullable = false, length = 255)
  @BeanProperty var text:String = _

  @Column(name = "create_time", nullable = false)
  @BeanProperty var createTime:Timestamp = _
}
