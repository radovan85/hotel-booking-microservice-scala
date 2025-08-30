package com.radovan.play.services

import com.radovan.play.dto.NoteDto

trait NoteService {

  def getNoteById(noteId:Int):NoteDto

  def deleteNote(noteId:Int):Unit

  def listAll:Array[NoteDto]

  def listAllForToday:Array[NoteDto]

  def deleteAllNotes():Unit
}
