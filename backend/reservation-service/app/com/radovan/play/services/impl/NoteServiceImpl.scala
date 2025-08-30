package com.radovan.play.services.impl

import com.radovan.play.converter.TempConverter
import com.radovan.play.dto.NoteDto
import com.radovan.play.exceptions.InstanceUndefinedException
import com.radovan.play.repositories.NoteRepository
import com.radovan.play.services.NoteService
import jakarta.inject.{Inject, Singleton}

import java.sql.Timestamp
import java.time.{ZoneId, ZonedDateTime}

@Singleton
class NoteServiceImpl extends NoteService{

  private var noteRepository:NoteRepository = _
  private var tempConverter:TempConverter = _

  @Inject
  private def initialize(noteRepository: NoteRepository,tempConverter: TempConverter):Unit = {
    this.noteRepository = noteRepository
    this.tempConverter = tempConverter
  }

  override def getNoteById(noteId: Int): NoteDto = {
    noteRepository.findById(noteId) match {
      case Some(noteEntity) => tempConverter.noteEntityToDto(noteEntity)
      case None => throw new InstanceUndefinedException("The note has not been found!")
    }
  }

  override def deleteNote(noteId: Int): Unit = {
    getNoteById(noteId)
    noteRepository.deleteById(noteId)
  }

  override def listAll: Array[NoteDto] = {
    noteRepository.findAll.collect{
      case noteEntity => tempConverter.noteEntityToDto(noteEntity)
    }
  }

  override def listAllForToday: Array[NoteDto] = {
    val now = ZonedDateTime.now(ZoneId.of("UTC"))
    val startOfDayLocal = now.toLocalDate.atStartOfDay()
    val endOfDayLocal = now.toLocalDate.atTime(23, 59, 59, 999000000)

    val startOfDay = Timestamp.valueOf(startOfDayLocal)
    val endOfDay = Timestamp.valueOf(endOfDayLocal)

    noteRepository.findAll
      .filter(note =>
        note.getCreateTime().after(startOfDay) &&
          note.getCreateTime().before(endOfDay)
      )
      .map(tempConverter.noteEntityToDto)
  }


  override def deleteAllNotes(): Unit = {
    noteRepository.deleteAll()
  }
}
