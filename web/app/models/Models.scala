package models

import java.io.File

import com.typesafe.config.ConfigFactory
import models.Tables._
import org.mindrot.jbcrypt.BCrypt

import scala.slick.driver.PostgresDriver.simple._
import scala.slick.lifted.TableQuery

object Models {

  case class User(email: String, password: String, passwordConf: String,
                  student: Boolean, tutor: Boolean)

  val conf = ConfigFactory.parseFile(new File("conf/application.conf")).resolve()

  val url = conf.getString("db.default.url")
  val user = conf.getString("db.default.user")
  val pass = conf.getString("db.default.password")
  val driver = "org.postgresql.Driver"

  val db = Database.forURL(url, user = user, password = pass, driver = driver)

  val schools = TableQuery[Schools]
  val colleges = TableQuery[Colleges]
  val majors = TableQuery[Majors]
  val courses = TableQuery[Courses]
  val users = TableQuery[Users]

  def userExists(email: String): Boolean = {
    db.withSession { implicit session =>
      users.filter(_.email === email).list.length > 0
    }
  }

  def saveUser(user: User) = {
    db.withSession { implicit session =>
      val salt = BCrypt.gensalt()
      val pw = BCrypt.hashpw(user.password, salt)
      users.map(c =>
        (c.email, c.password, c.salt, c.student, c.tutor, c.verified)) +=
        ((user.email, pw, salt, Option(user.student), Option(user.tutor), Option(false)))
    }
  }

  def getSchoolData: List[Map[String, String]] = {
    db.withSession { implicit session =>
      (for (s <- schools) yield (s.name, s.fullName, s.`type`))
        .list.map(row =>
        Map("name" -> row._1, "fullName" -> row._2, "type" -> row._3)
      )
    }
  }

  def getFullNames(school: String, major: String): List[Map[String, String]] = {
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
