package controllers

import controllers.Users._
import controllers.DbCache._
import models.Models._
import play.api.libs.json._
import play.api.mvc._
import play.api.Logger
/**
 * Controller to output raw Json.
 *
 * @author Daniel Fang <danfang@uw.edu>
 */
object API extends Controller {

  def getCourses(sId: String, mId: String) = Action { implicit request =>
    val schoolData = cachedSchools.find(_("id") == sId)
    if (schoolData.isEmpty) NotFound("Could not find school " + sId)
    else {

      val majorData = cachedMajors(sId).values.flatten.find(_("id") == mId)
      if (majorData.isEmpty) NotFound("Could not find major " + mId + " at " + sId)
      else {

        val user = getUserFromSession
        Ok(Json.obj(
          "tutoring" -> { if (user.isDefined) Json.toJson(user.get.tutoring) else JsNull },
          "courses" -> cachedCourses(sId, mId)
        ))
      }
    }
  }

  def getTutors(sId: String, mId: String) = Action { implicit request =>
    val tutors = getTutorData(sId, mId)
    Ok(Json.toJson(tutors))
  }

  // POST Tutor - become a tutor
  def tutorSubmit = Action(parse.json) { implicit request =>
    val userData = getUserFromSession
    val course = Json.fromJson[SimpleCourse](request.body).asOpt
    val delete = (request.body \ "delete").asOpt[Boolean]
    Logger.debug(course.toString + ", delete: " + delete.getOrElse(false))

    if (userData.isEmpty || course.isEmpty || delete.isEmpty)
      NotAcceptable
    else {
      Logger.debug(setTutor(userData.get.email, course.get.school,
        course.get.major, course.get.course, delete.get).toString
      )

      if (!delete.get) userData.get.tutoring += course.get
      else userData.get.tutoring -= course.get

      Logger.debug(userData.toString)
      Ok.withSession("userData" -> Json.toJson(userData).toString())
    }
  }

}
