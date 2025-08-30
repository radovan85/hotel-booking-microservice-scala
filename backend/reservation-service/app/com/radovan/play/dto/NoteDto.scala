package com.radovan.play.dto

import scala.beans.BeanProperty

@SerialVersionUID(1L)
class NoteDto extends Serializable {

  @BeanProperty var noteId: Integer = _
  @BeanProperty var subject: String = _
  @BeanProperty var text: String = _
  @BeanProperty var createTimeStr: String = _
}
