package com.radovan.play.controllers

import com.radovan.play.utils.TokenUtils._
import com.radovan.play.dto.RoomCategoryDto
import com.radovan.play.security.{JwtSecuredAction, SecuredRequest}
import com.radovan.play.services.RoomCategoryService
import com.radovan.play.utils.{ResponsePackage, ValidatorSupport}
import flexjson.JSONDeserializer
import jakarta.inject.Inject
import org.apache.hc.core5.http.HttpStatus
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents, Result}

class RoomCategoryController @Inject()(
                                        cc: ControllerComponents,
                                        categoryService:RoomCategoryService,
                                        securedAction: JwtSecuredAction
                                      )extends AbstractController(cc) with ValidatorSupport{

  private def onlyAdmin[A](secured: SecuredRequest[A])(block: => Result): Result = {
    if (secured.roles.contains("ROLE_ADMIN")) block
    else Forbidden("Access denied: admin role required")
  }

  def getAllCategories: Action[AnyContent] = securedAction { secured =>
    new ResponsePackage(categoryService.listAll,HttpStatus.SC_OK).toResult
  }

  def getCategoryDetails(categoryId:Int):Action[AnyContent] = securedAction {secured =>
    new ResponsePackage(categoryService.getCategoryById(categoryId),HttpStatus.SC_OK).toResult
  }

  def saveCategory: Action[AnyContent] = securedAction { secured =>
    onlyAdmin(secured){
      val json = Json.stringify(secured.body.asJson.getOrElse(Json.obj()))
      val roomCategory = new JSONDeserializer[RoomCategoryDto]()
        .use(null, classOf[RoomCategoryDto])
        .deserialize(json, classOf[RoomCategoryDto])

      validateOrHalt(roomCategory)
      val storedCategory = categoryService.addCategory(roomCategory)
      new ResponsePackage(s"Room category with id ${storedCategory.getRoomCategoryId()} has been stored!",HttpStatus.SC_CREATED).toResult
    }
  }

  def updateCategory(categoryId:Int): Action[AnyContent] = securedAction { secured =>
    onlyAdmin(secured){
      val json = Json.stringify(secured.body.asJson.getOrElse(Json.obj()))
      val roomCategory = new JSONDeserializer[RoomCategoryDto]()
        .use(null, classOf[RoomCategoryDto])
        .deserialize(json, classOf[RoomCategoryDto])

      validateOrHalt(roomCategory)
      val updatedCategory = categoryService.updateCategory(roomCategory,categoryId)
      new ResponsePackage(s"Room category with id ${updatedCategory.getRoomCategoryId()} has been updated without any issues!",HttpStatus.SC_OK).toResult
    }
  }

  def deleteCategory(categoryId:Int):Action[AnyContent] = securedAction {secured =>
    onlyAdmin(secured){
      categoryService.deleteCategory(categoryId,provideToken(secured))
      new ResponsePackage(s"Room category with id $categoryId has been permanently removed!",HttpStatus.SC_OK).toResult
    }
  }
}
