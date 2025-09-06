package com.radovan.play.repositories

import com.radovan.play.entity.NoteEntity

trait NoteRepository {

  def save(noteEntity: NoteEntity):NoteEntity

  def findById(noteId:Integer):Option[NoteEntity]

  def deleteById(noteId:Integer):Unit

  def findAll:Array[NoteEntity]

  def deleteAll():Unit
}
