package controllers

import java.sql.Connection

import models.Forms._
import models.Models._
import play.api.Play.current
import play.api.db._
import play.api.mvc._
import play.api.libs.json._

object Application extends Controller {

  def index = Action { request =>
    if (request.session.get("user").isDefined) {
      val s = request.session;
      val user = User(s.get("user").get, "", "",
        s.get("student").getOrElse("") == "",
        s.get("tutor").getOrElse("") == "")
      Ok(views.html.index("UWTN", Option(user)))
    } else Ok(views.html.index("UWTN", None))
  }

  def login = Action { request =>
    if (request.session.get("user").isDefined)
      Redirect(routes.Application.getSchools)
    else Ok(views.html.login(LoginForm))
  }

  def loginAttempt = Action { implicit request =>
    LoginForm.bindFromRequest.fold(
      form => {
        Ok(views.html.login(form))
      },
      user => {
        val userData = getUserData(user)
        print(userData)
        Redirect(routes.Application.getSchools).withSession(
          "user" -> user.email
        )
      }
    )
  }

  def logout = Action {
    Redirect(routes.Application.getSchools).withNewSession
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

  def getSchools = Action { request =>
    if (request.session.get("user").isDefined) {
      val s = request.session;
      val user = User(s.get("user").get, "", "",
        s.get("student").getOrElse("") == "",
        s.get("tutor").getOrElse("") == "")
      Ok(views.html.schools(getSchoolData, Option(user)))
    } else {
      Ok(views.html.schools(getSchoolData, None))
    }
  }

  def getMajors(school: String) = Action { request =>
    val nameQuery = getFullNames(school, "")
    if (nameQuery.length > 0) {
      val metadata = Map(
        "fullName" -> nameQuery.head("school"),
        "name" -> school
      )
      if (request.session.get("user").isDefined) {
        val s = request.session;
        val user = User(s.get("user").get, "", "",
          s.get("student").getOrElse("") == "",
          s.get("tutor").getOrElse("") == "")
        Ok(views.html.majors(metadata, getMajorData(school), Option(user)))
      } else Ok(views.html.majors(metadata, getMajorData(school), None))
    } else NotFound
  }

  def getCourses(school: String, major: String) = Action { request =>
    val nameQuery = getFullNames(school, major)
    if (nameQuery.length > 0) {
      val metadata = Map(
        "schoolFull" -> nameQuery.head("school"), "school" -> school,
        "majorFull" -> nameQuery.head("major"), "major" -> major
      )
      if (request.session.get("user").isDefined) {
        val s = request.session;
        val user = User(s.get("user").get, "", "",
          s.get("student").getOrElse("") == "",
          s.get("tutor").getOrElse("") == "")
        Ok(views.html.courses(metadata, getCourseData(school, major), Option(user)))
      } else Ok(views.html.courses(metadata, getCourseData(school, major), None))
    } else NotFound
  }
}