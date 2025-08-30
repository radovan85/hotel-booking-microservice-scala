package com.radovan.play.controllers

import com.radovan.play.security.{JwtSecuredAction, SecuredRequest}
import com.radovan.play.services.NoteService
import com.radovan.play.utils.ResponsePackage
import jakarta.inject.Inject
import org.apache.hc.core5.http.HttpStatus
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents, Result}

class NoteController @Inject()(
                                cc: ControllerComponents,
                                noteService: NoteService,
                                securedAction: JwtSecuredAction
                              ) extends AbstractController(cc) {

  private def onlyAdmin[A](secured: SecuredRequest[A])(block: => Result): Result = {
    if (secured.roles.contains("ROLE_ADMIN")) block
    else Forbidden("Access denied: admin role required")
  }

  def getNoteDetails(noteId: Int): Action[AnyContent] = securedAction { secured =>
    onlyAdmin(secured) {
      new ResponsePackage(noteService.getNoteById(noteId), HttpStatus.SC_OK).toResult
    }
  }

  def deleteNote(noteId: Int): Action[AnyContent] = securedAction { secured =>
    onlyAdmin(secured) {
      noteService.deleteNote(noteId)
      new ResponsePackage(s"Note with id $noteId has been removed!", HttpStatus.SC_OK).toResult
    }
  }

  def removeAllNotes(): Action[AnyContent] = securedAction { secured =>
    onlyAdmin(secured) {
      noteService.deleteAllNotes()
      new ResponsePackage(s"All notes have been removed!", HttpStatus.SC_OK).toResult
    }
  }

  def getAllNotes: Action[AnyContent] = securedAction { secured =>
    onlyAdmin(secured) {
      new ResponsePackage(noteService.listAll, HttpStatus.SC_OK).toResult
    }
  }

  def getTodaysNotes: Action[AnyContent] = securedAction { secured =>
    onlyAdmin(secured) {
      new ResponsePackage(noteService.listAllForToday, HttpStatus.SC_OK).toResult
    }
  }

}
