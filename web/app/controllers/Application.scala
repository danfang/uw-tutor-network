package controllers

import controllers.DbCache._
import models.Models._
import play.api.libs.json._
import play.api.mvc._
import play.api.Play.current

object Application extends Controller {

  // Splash page
  def index = Action { implicit request =>
      Ok(views.html.index(getUserFromSession))
  }

  def getSchools = Action { implicit request =>
    val schoolData = cachedSchools
    Ok(views.html.schools(schoolData, getUserFromSession))
  }

  def getMajors(sId: String) = Action { implicit request =>
    val schoolData = cachedSchools.find(_("id") == sId)
    if (schoolData.isEmpty) NotFound("Could not find school " + sId)
    else Ok(views.html.majors(schoolData.get, cachedMajors(sId), getUserFromSession))
  }

  def getCourses(sId: String, mId: String) = Action { implicit request =>
    val schoolData = cachedSchools.find(_("id") == sId)
    if (schoolData.isEmpty) NotFound("Could not find school " + sId)
    else {

      val majorData = cachedMajors(sId).values.flatten.find(_("id") == mId)
      if (majorData.isEmpty) NotFound("Could not find major " + mId + " at " + sId)
      else {
        Ok(views.html.courses(
          schoolData.get, majorData.get, getUserFromSession)
        )
      }
    }
  }

  def getCoursesJson(sId: String, mId: String) = Action { implicit request =>
    val schoolData = cachedSchools.find(_("id") == sId)
    if (schoolData.isEmpty) NotFound("Could not find school " + sId)
    else {

      val majorData = cachedMajors(sId).values.flatten.find(_("id") == mId)
      if (majorData.isEmpty) NotFound("Could not find major " + mId + " at " + sId)
      else {

        val user = getUserFromSession
        val json = Json.obj(
          "tutoring" -> { if (user.isDefined) Json.toJson(user.get.tutoring) else JsNull },
          "courses" -> cachedCourses(sId, mId)
        )
        Ok(json)
      }
    }
  }

  def getUserFromSession(implicit r: Request[Any]): Option[UserData] = {
    if (r.session.get("userData").isDefined) {
      UserData.fromJsonString(r.session.get("userData").get)
    } else None
  }
}