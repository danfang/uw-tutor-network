package controllers

import anorm._
import play.api._
import play.api.mvc._
import play.api.db._
import play.api.Play.current

object Application extends Controller {

  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def getMajors(schoolName: String) = Action {

    DB.withConnection { implicit c =>
      val query = SQL(
        """
          SELECT m.id, m.name, c.name
          FROM majors AS m
          JOIN colleges AS c ON m.college = c.id
          WHERE c.school={school};
        """).on("school" -> schoolName)

      Ok(views.html.majors(schoolName, query().map(row => row.asList).groupBy(list => list(2))))
    }
  }

  def getCourses(schoolName: String, majorName: String) = Action {
    DB.withConnection { implicit c =>
      val query = SQL(
        """
          SELECT c.*
          FROM courses AS c
          JOIN majors AS m ON m.id = c.major
          JOIN colleges AS co ON m.college = co.id
          WHERE co.school={school} AND m.id={major};
        """).on("school" -> schoolName, "major" -> majorName)

      Ok(views.html.courses(majorName, query().map(row => row.asList)))
    }
  }

}