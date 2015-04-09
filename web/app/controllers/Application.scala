package controllers

import controllers.DbCache._
import controllers.Users._
import play.api.mvc._
import play.api.Play.current

/**
 * Controller for rendering main pages.
 */
object Application extends Controller {

  // Splash page
  def index = Action { implicit request =>
      Ok(views.html.index(getUserFromSession))
  }

  // Render a list of schools
  def getSchools = Action { implicit request =>
    val schoolData = cachedSchools
    Ok(views.html.schools(schoolData, getUserFromSession))
  }

  // Render a list of majors grouped by college.
  def getMajors(sId: String) = Action { implicit request =>
    val schoolData = cachedSchools.find(_("id") == sId)
    if (schoolData.isEmpty) NotFound("Could not find school " + sId)
    else Ok(views.html.majors(schoolData.get, cachedMajors(sId), getUserFromSession))
  }

  // Render a basic course UI for a given school and major.
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
}