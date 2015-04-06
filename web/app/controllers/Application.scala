package controllers

import java.sql.Connection

import play.api._
import play.api.mvc._
import play.api.db._
import play.api.data.Forms._
import play.api.data._
import models.Models._
import play.api.Play.current

object Application extends Controller {

  case class User(email: String, password: String, passwordConf: String, student: Boolean, tutor: Boolean)

  def index = Action {
    Ok(views.html.index("UWTN", false))
  }

  def login = Action {
    Ok("Login")
  }

  val registerForm = Form(
    mapping(
      "email" -> email,
      "password" -> nonEmptyText(6, 25),
      "password confirmation" -> nonEmptyText(6, 25),
      "student" -> checked("Student"),
      "tutor" -> checked("Tutor")
    ) (User.apply)
      (u => Option(u.email, "", "", u.student, u.tutor))
      verifying("Passwords do not match,", u => u.password == u.passwordConf)
      verifying("Email taken.", u => !userExists(u))
  )

  def register = Action {
    Ok(views.html.register(registerForm))
  }

  def registerCreate = Action { implicit request =>
    registerForm.bindFromRequest.fold(
      form => {
        Ok(views.html.register(form))
      },
      user => {
        val result = saveUser(user)
        Ok("Confirmation: " + user.toString)
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