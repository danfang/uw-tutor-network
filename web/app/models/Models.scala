package models

import java.io.File
import java.sql.Connection
import Tables._
import anorm._
import com.typesafe.config.ConfigFactory
import controllers.Application.User
import slick.driver.PostgresDriver.simple._

import scala.slick.lifted.TableQuery

object Models {

  val conf = ConfigFactory.parseFile(new File("conf/application.conf")).resolve()
  val url = conf.getString("db.default.url")
  val user = conf.getString("db.default.user")
  val pass = conf.getString("db.default.password")

  val db = Database.forURL(url, user = user, password = pass, driver = "org.postgresql.Driver")

  val schools = TableQuery[Schools]
  val colleges = TableQuery[Colleges]
  val majors = TableQuery[Majors]
  val courses = TableQuery[Courses]
  val users = TableQuery[Users]

  def getSchoolData: List[Map[String, String]] = {
    val query = for (s <- schools) yield (s.name, s.fullName, s.`type`)
    db.withSession { implicit session =>
      query.list.map(row =>
        Map("name" -> row._1, "fullName" -> row._2, "type" -> row._3)
      )
    }
  }

  def getFullNames(school: String, major: String) = {
    db.withSession { implicit session =>

      if (major == "") {
        schools.filter(_.name === school).take(1).list.map(row =>
          Map("school" -> row.fullName)
        )
      } else {
        (for {
          s <- schools if s.name === school
          c <- colleges if c.school === s.name
          m <- majors if m.college === c.id && m.id === major
        } yield (s.fullName, m.name))
          .take(1).list.map(r =>
            Map("school" -> r._1, "major" -> r._2)
          )
      }
    }
  }

  def userExists(user: User): Boolean = {
    db.withSession { implicit session =>
      var result = users.filter(_.email === user.email).list
      print(result.length > 0)
      result.length > 0
    }
  }

  def saveUser(user: User) = {
    db.withSession { implicit session =>
      users.map(c => (c.email, c.password, c.salt, c.student, c.tutor)) +=
        (user.email, user.password, "asdf", Option(user.student), Option(user.tutor))
    }
  }

  def getMajorData(school: String) = {
    db.withSession { implicit session =>
      (for {
        c <- colleges if c.school === school
        m <- majors if m.college === c.id
      } yield (m.id, m.name, c.name))
        .list.map(r =>
          Map("id" -> r._1, "name" -> r._2, "college" -> r._3)
        ).groupBy(_("college"))
    }
  }

  def getCourseData(school: String, major: String) = {
    db.withSession { implicit session =>
      (for {
        co <- colleges if co.school === school
        m <- majors if m.id === major && m.college === co.id
        c <- courses if c.major === m.id
      } yield (c.id, c.name, c.description, c.prereqs, c.offered, c.planLink))
        .list.map(r =>
          Map("id" -> r._1, "name" -> r._2, "desc" -> r._3.getOrElse(""),
            "pre" -> r._4.getOrElse(""), "off" -> r._5.getOrElse(""),
            "link" -> r._6.getOrElse(""))
        )
    }
  }
}
