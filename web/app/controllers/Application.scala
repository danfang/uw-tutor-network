package controllers

import java.sql.Connection

import models.Forms._
import models.Models._
import play.api.Play.current
import play.api.db._
import play.api.mvc._

object Application extends Controller {

  def index = Action {
    Ok(views.html.index("UWTN", false))
  }

  def login = Action {
    Ok("Login")
  }

  def register = Action {
    Ok(views.html.register(RegisterForm))
  }

  def registerCreate = Action { implicit request =>
    RegisterForm.bindFromRequest.fold(
      form => {
        Ok(views.html.register(form))
      },
      user => {
        val result = saveUser(user)
        Ok("Confirmation: " + result.toString)
      }
    )
  }

  def getSchools = Action {
      Ok(views.html.schools(getSchoolData))
  }

  def getMajors(school: String) = Action {
    val nameQuery = getFullNames(school, "")
    if (nameQuery.length > 0) {
      val metadata = Map(
        "fullName" -> nameQuery.head("school"),
        "name" -> school
      )
      Ok(views.html.majors(metadata, getMajorData(school)))
    } else NotFound
  }

  def getCourses(school: String, major: String) = Action {
    DB.withConnection { implicit c: Connection =>
      val nameQuery = getFullNames(school, major)
      if (nameQuery.length > 0) {
        val metadata = Map(
          "schoolFull" -> nameQuery.head("school"), "school" -> school,
          "majorFull" -> nameQuery.head("major"), "major" -> major
        )
        Ok(views.html.courses(metadata, getCourseData(school, major)))
      } else NotFound
    }
  }
}