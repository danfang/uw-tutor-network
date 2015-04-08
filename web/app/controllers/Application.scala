package controllers

import models.Forms._
import models.Models._
import play.api.cache.Cached
import play.api.libs.json.JsValue
import play.api.mvc._
import play.api.Play.current

object Application extends Controller {

  def index = Action { implicit request =>
      Ok(views.html.index(getUserFromSession))
  }

  def login = Action { request =>
    if (request.session.get("user").isDefined)
      Redirect(routes.Application.getSchools)
    else Ok(views.html.login(LoginForm))
  }

  def loginAttempt = Action { implicit request =>
    LoginForm.bindFromRequest.fold(
      errForm => Ok(views.html.login(errForm)),
      user => {
        Redirect(routes.Application.getSchools).withSession(
          "userData" -> getUserData(user).toJsonString
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
      errForm => Ok(views.html.register(errForm)),
      user => {
        val result = saveUser(user)
        Ok("Confirmation: " + result.toString)
      }
    )
  }

  def getSchools = Action { implicit request =>
      Ok(views.html.schools(getSchoolData, getUserFromSession))
    }

  def getMajors(school: String) = Action { implicit request =>
    val nameQuery = getFullNames(school, "")
    if (nameQuery.length > 0) {
      val metadata = Map(
        "fullName" -> nameQuery.head("school"),
        "name" -> school
      )
      Ok(views.html.majors(metadata, getMajorData(school), getUserFromSession))
    } else NotFound
  }

  def getCourses(school: String, major: String) = Action { implicit request =>
    val nameQuery = getFullNames(school, major)
    if (nameQuery.length > 0) {
      val metadata = Map(
        "schoolFull" -> nameQuery.head("school"), "school" -> school,
        "majorFull" -> nameQuery.head("major"), "major" -> major
      )
      Ok(views.html.courses(
        metadata, getCourseData(school, major), getUserFromSession)
      )
    } else NotFound
  }

  def toggleTutor = Action(parse.json) { implicit request =>
    val userData = getUserFromSession
    val courseData = (request.body \ "course").asOpt[String]
    if (!userData.isDefined || !courseData.isDefined) NotAcceptable
    else {
      val res = setTutor(userData.get.email, courseData.get)
      println(res)
      Ok("" + res)
    }
  }

  private def getUserFromSession(implicit r: Request[Any]): Option[UserData] = {
    if (r.session.get("userData").isDefined) {
      UserData.fromJsonString(r.session.get("userData").get)
    } else None
  }
}