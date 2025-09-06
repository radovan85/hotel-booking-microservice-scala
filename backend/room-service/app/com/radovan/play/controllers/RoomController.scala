package com.radovan.play.controllers

import com.radovan.play.dto.RoomDto
import com.radovan.play.utils.TokenUtils._
import com.radovan.play.security.{JwtSecuredAction, SecuredRequest}
import com.radovan.play.services.RoomService
import com.radovan.play.utils.{ResponsePackage, ValidatorSupport}
import flexjson.JSONDeserializer
import jakarta.inject.Inject
import org.apache.hc.core5.http.HttpStatus
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents, Result}

class RoomController @Inject()(
                                cc: ControllerComponents,
                                roomService: RoomService,
                                securedAction: JwtSecuredAction
                              ) extends AbstractController(cc) with ValidatorSupport {


  private def onlyAdmin[A](secured: SecuredRequest[A])(block: => Result): Result = {
    if (secured.roles.contains("ROLE_ADMIN")) block
    else Forbidden("Access denied: admin role required")
  }

  def getAllRooms: Action[AnyContent] = securedAction { secured =>
    new ResponsePackage(roomService.listAll, HttpStatus.SC_OK).toResult
  }

  def getRoomDetails(roomId: Int): Action[AnyContent] = securedAction { secured =>
    new ResponsePackage(roomService.getRoomById(roomId), HttpStatus.SC_OK).toResult
  }

  def saveRoom: Action[AnyContent] = securedAction { secured =>
    onlyAdmin(secured) {
      val json = Json.stringify(secured.body.asJson.getOrElse(Json.obj()))
      val room = new JSONDeserializer[RoomDto]()
        .use(null, classOf[RoomDto])
        .deserialize(json, classOf[RoomDto])

      validateOrHalt(room)
      val storedRoom = roomService.addRoom(room)
      new ResponsePackage(s"Room with id ${storedRoom.getRoomId()} has been stored!", HttpStatus.SC_CREATED).toResult
    }
  }

  def updateRoom(roomId: Int): Action[AnyContent] = securedAction { secured =>
    onlyAdmin(secured) {
      val json = Json.stringify(secured.body.asJson.getOrElse(Json.obj()))
      val room = new JSONDeserializer[RoomDto]()
        .use(null, classOf[RoomDto])
        .deserialize(json, classOf[RoomDto])

      validateOrHalt(room)
      val updatedRoom = roomService.updateRoom(room, roomId)
      new ResponsePackage(s"Room with id ${updatedRoom.getRoomId()} has been updated without any issues!", HttpStatus.SC_OK).toResult
    }
  }

  def deleteRoom(roomId: Int): Action[AnyContent] = securedAction { secured =>
    onlyAdmin(secured) {
      roomService.deleteRoom(roomId,provideToken(secured))
      new ResponsePackage(s"The room with id $roomId has been permanently removed!", HttpStatus.SC_OK).toResult
    }
  }


}
