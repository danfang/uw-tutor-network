package models

import models.Models._
import play.api.data.Form
import play.api.data.Forms._

/**
 * @author Daniel Fang <danfang@uw.edu>
 */
object Forms {

  def RegisterForm = Form(
    mapping(
      "email" -> email.verifying("Email taken.", !userExists(_)),
      "password" -> nonEmptyText(6, 25),
      "password confirmation" -> nonEmptyText(6, 25),
      "student" -> optional(checked("Student")),
      "tutor" -> optional(checked("Tutor"))
    ) ((e, p, p2, s, t) => {
        User(e, p, p2, s.getOrElse(false), t.getOrElse(false))
      })
      (u => {
        Option(u.email, "", "", Option(u.student), Option(u.tutor))
      })
      verifying("Passwords do not match,", u => u.password == u.passwordConf)
  )

  def LoginForm = Form(
    mapping(
      "email" -> email,
      "password" -> nonEmptyText(6, 25)
    ) ((e, p) => {
      User(e, p, "", false, false)
    }) (u => Option(u.email, u.password))
    verifying("Incorrect email/password.", validLogin(_))
  )
}
