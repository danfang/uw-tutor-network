package models

import java.io.File

import com.typesafe.config.ConfigFactory
import models.Tables._
import org.mindrot.jbcrypt.BCrypt
import play.api.libs.json._
import play.api.libs.functional.syntax._

import scala.slick.driver.PostgresDriver.simple._
import scala.slick.lifted.TableQuery

object Models {

  case class User(email: String, password: String, passwordConf: String,
                  student: Boolean, tutor: Boolean)

  case class UserData(firstName: Option[String], lastName: Option[String],
                     email: String, student: Boolean, tutor: Boolean,
                     verified: Boolean) {

    def toJsonString: String = {
      Json.stringify(Json.obj(
          "email" -> JsString(email),
          "student" -> JsBoolean(student),
          "tutor" -> JsBoolean(tutor),
          "verified" -> JsBoolean(verified),
          "firstName" -> {
            if (firstName.isDefined) JsString(firstName.get) else JsNull
          },
          "lastName" -> {
            if (lastName.isDefined) JsString(lastName.get) else JsNull
          }
      ))
    }
  }

  object UserData {
    def fromJsonString(str: String): Option[UserData] = {

      implicit val reads: Reads[UserData] = (
        (JsPath \ "firstName").readNullable[String] and
          (JsPath \ "lastName").readNullable[String] and
          (JsPath \ "email").read[String] and
          (JsPath \ "student").read[Boolean] and
          (JsPath \ "tutor").read[Boolean] and
          (JsPath \ "verified").read[Boolean]
        )(UserData.apply _)

      val res: JsResult[UserData] = Json.parse(str).validate[UserData]
      if (res.isSuccess) Option(res.get) else {
        print(res)
        None
      }
    }
  }

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
  val tutors = TableQuery[Tutors]

  def userExists(email: String): Boolean = {
    db.withSession { implicit session =>
      users.filter(_.email === email).list.length > 0
    }
  }

  def validLogin(user: User): Boolean = {
    db.withSession { implicit session =>
      val result = users.filter(_.email === user.email).take(1).list.map(
        row => row.password
      )
      if (result.length != 1) false
      else if (BCrypt.checkpw(user.password, result.head)) true
      else false
    }
  }

  def saveUser(user: User) = {
    db.withSession { implicit session =>
      val salt = BCrypt.gensalt()
      val pw = BCrypt.hashpw(user.password, salt)
      users.map(c =>
        (c.email, c.password, c.student, c.tutor, c.verified)) +=
        ((user.email, pw, user.student, user.tutor, false))
    }
  }

  def getUserData(user: User): UserData = {
    db.withSession { implicit session =>
      val opts = users.filter(_.email === user.email).take(1).list.map(r =>
        (r.firstName, r.lastName, r.student, r.tutor, r.verified)
      ).head
      UserData(opts._1, opts._2, user.email, opts._3, opts._4, opts._5)
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
          m <- majors if m.school === s.name && m.id === major
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
        c <- colleges
        m <- majors if m.college === c.id && m.school === school
      } yield (m.id, m.name, c.name))
        .list.map(r =>
          Map("id" -> r._1, "name" -> r._2, "college" -> r._3)
        ).groupBy(_("college"))
    }
  }

  def getCourseData(school: String, major: String) = {
    db.withSession { implicit session =>
      (for {
        c <- courses if c.major === major && c.school === school
      } yield (c.id, c.name, c.description, c.prereqs, c.offered, c.planLink))
        .list.map(r =>
          Map("id" -> Option(r._1), "name" -> Option(r._2), "des" -> r._3,
            "pre" -> r._4, "off" -> r._5, "link" -> r._6)
        )
    }
  }

  def setTutor(email: String, course: String) = {
    db.withSession { implicit session =>
      tutors.map(t => (t.user, t.course)) += (email, course)
    }
  }
}
