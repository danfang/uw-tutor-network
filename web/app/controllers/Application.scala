package controllers

import java.sql.Connection

import play.api._
import play.api.mvc._
import play.api.db._
import models.Models._
import play.api.Play.current

object Application extends Controller {

  def index = Action {
    Ok(views.html.index("UWTN", false))
  }

  def login = Action {
    Ok(views.html.register())
  }

  def register = Action {
    Ok(views.html.register())
  }

  def getSchools = Action {
    DB.withConnection { implicit c: Connection =>
      val schools = getSchoolData
      Ok(views.html.schools(schools))
    }
  }

  def getMajors(school: String) = Action {
    DB.withConnection { implicit c: Connection =>
      val nameQuery = getFullNames(school, "")
      if (nameQuery.length > 0) {
        val metadata = Map(
          "fullName" -> nameQuery.head[String]("schools.full_name"),
          "name" -> school
        )
        Ok(views.html.majors(metadata, getMajorData(school)))
      } else NotFound
    }
  }

  def getCourses(school: String, major: String) = Action {
    DB.withConnection { implicit c: Connection =>
      val nameQuery = getFullNames(school, major)
      if (nameQuery.length > 0) {
        val metadata = Map(
          "schoolFull" -> nameQuery.head[String]("schools.full_name"),
          "school" -> school,
          "majorFull" -> nameQuery.head[String]("majors.name"),
          "major" -> major
        )
        Ok(views.html.courses(metadata, getCourseData(school, major)))
      } else NotFound
    }
  }

}