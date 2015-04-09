package controllers

import models.Forms._
import models.Models._
import play.api.mvc._
import play.api.Logger
import controllers.Application._
import play.api.Play.current

/**
 * @author Daniel Fang <danfang@uw.edu>
 */
object Users extends Controller {

  // Login
  def login = Action { request =>
    if (request.session.get("userData").isDefined) {
      Redirect(routes.Application.getSchools)
    } else Ok(views.html.login(LoginForm))
  }

  // POST Login
  def loginSubmit = Action { implicit request =>
    LoginForm.bindFromRequest.fold(
      errForm => Ok(views.html.login(errForm)),
      user => {
        val userData = getUserData(user.email).toJsonString
        Logger.debug(userData)
        Redirect(routes.Application.getSchools).withSession(
          "userData" -> userData
        )
      }
    )
  }

  // Logout
  def logout = Action {
    Redirect(routes.Application.getSchools).withNewSession
  }

  // Register
  def register = Action { request =>
    if (request.session.get("userData").isDefined) {
      Redirect(routes.Application.getSchools)
    } else Ok(views.html.register(RegisterForm))
  }

  // POST Register
  def registerSubmit = Action { implicit request =>
    RegisterForm.bindFromRequest.fold(
      errForm => Ok(views.html.register(errForm)),
      user => {
        val result = saveUser(user)
        Ok("Confirmation: " + result.toString)
      }
    )
  }

  // POST Tutor - become a tutor
  def tutorSubmit = Action(parse.json) { implicit request =>
    val userData = getUserFromSession

    val school = (request.body \ "school").asOpt[String]
    val major = (request.body \ "major").asOpt[String]
    val course = (request.body \ "course").asOpt[String]
    val delete = (request.body \ "delete").asOpt[Boolean]

    if (!userData.isDefined || !school.isDefined ||
        !major.isDefined || !course.isDefined || !delete.isDefined)
      NotAcceptable
    else {
      setTutor(userData.get.email, school.get, major.get, course.get, delete.get)
      Ok.withSession(
        "userData" -> getUserData(userData.get.email).toJsonString
      )
    }
  }
}
