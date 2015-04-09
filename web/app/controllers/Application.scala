package controllers

import controllers.DbCache._
import models.Forms._
import models.Models._
import play.api.mvc._

object Application extends Controller {

  def index = Action { implicit request =>
      Ok(views.html.index(getUserFromSession))
  }

  def login = Action { request =>
    if (request.session.get("userData").isDefined) {
      Redirect(routes.Application.getSchools)
    } else Ok(views.html.login(LoginForm))
  }

  def loginSubmit = Action { implicit request =>
    LoginForm.bindFromRequest.fold(
      errForm => Ok(views.html.login(errForm)),
      user => {
        Redirect(routes.Application.getSchools).withSession(
          "userData" -> getUserData(user.email).toJsonString
        )
      }
    )
  }

  def logout = Action {
    Redirect(routes.Application.getSchools).withNewSession
  }

  def register = Action { request =>
    if (request.session.get("userData").isDefined) {
      Redirect(routes.Application.getSchools)
    } else Ok(views.html.register(RegisterForm))
  }

  def registerSubmit = Action { implicit request =>
    RegisterForm.bindFromRequest.fold(
      errForm => Ok(views.html.register(errForm)),
      user => {
        val result = saveUser(user)
        Ok("Confirmation: " + result.toString)
      }
    )
  }

  def getSchools = Action { implicit request =>
    val schools = cachedSchools
    Ok(views.html.schools(schools, getUserFromSession))
  }

  def getMajors(school: String) = Action { implicit request =>
    val schoolData = cachedSchools.find(_("name") == school)
    if (schoolData.isEmpty) NotFound("Could not find school " + school)
    else Ok(views.html.majors(schoolData.get, cachedMajors(school), getUserFromSession))
  }

  def getCourses(school: String, major: String) = Action { implicit request =>
    val schoolData = cachedSchools.find(_("name") == school)
    if (schoolData.isEmpty) NotFound("Could not find school " + school)
    else {

      val majorData = cachedMajors(school).values.flatten.find(_("id") == major)
      if (majorData.isEmpty) NotFound("Could not find major " + major + " at " + school)
      else {
        Ok(views.html.courses(
          schoolData.get, majorData.get, cachedCourses(school, major),
          getUserFromSession)
        )
      }
    }
  }

  def tutorSubmit = Action(parse.json) { implicit request =>
    val userData = getUserFromSession
    val courseData = (request.body \ "course").asOpt[String]
    if (!userData.isDefined || !courseData.isDefined) NotAcceptable
    else {
      val res = setTutor(userData.get.email, courseData.get, "", "")
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