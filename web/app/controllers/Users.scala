package controllers

import models.Forms._
import models.Models._
import play.api.mvc._
import play.api.Logger
import play.api.libs.json._
import play.api.Play.current

/**
 * Controller for rendering user pages.
 *
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
        val userData = Json.toJson(getUserData(user.email)).toString()
        Logger.debug(userData)
        Redirect(routes.Application.getSchools).withSession(
          "userData" -> userData
        )
      }
    )
  }

  // Retrieves user data out of the request session.
  def getUserFromSession(implicit r: Request[Any]): Option[UserData] = {
    if (r.session.get("userData").isDefined) {
      Json.fromJson[UserData](Json.parse(r.session.get("userData").get)).asOpt
    } else None
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
        Redirect(routes.Users.login())
      }
    )
  }
}
