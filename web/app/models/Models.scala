package models

import models.Tables._
import org.mindrot.jbcrypt.BCrypt
import play.api.Play.current
import play.api.libs.functional.syntax._
import play.api.Logger
import play.api.libs.json._

import scala.collection.immutable.TreeMap
import scala.slick.driver.PostgresDriver.simple._
import scala.slick.lifted.TableQuery

object Models {

  /**
   * Class to represent an initial user account
   */
  case class User(email: String, password: String, passwordConf: String,
                  student: Boolean, tutor: Boolean)

  case class SimpleCourse(school: String, major: String, course: String, rate: Option[Int])

  implicit def courseReads: Reads[SimpleCourse] = (
    (JsPath \ "school").read[String] and
    (JsPath \ "major").read[String] and
    (JsPath \ "course").read[String] and
    (JsPath \ "rate").readNullable[Int]
  )(SimpleCourse.apply _)

  implicit def courseWrites: Writes[SimpleCourse] = (
    (JsPath \ "school").write[String] and
    (JsPath \ "major").write[String] and
    (JsPath \ "course").write[String] and
    (JsPath \ "rate").writeNullable[Int]
  )(unlift(SimpleCourse.unapply))

  /**
   * Class to represent a full user
   */
  case class UserData(firstName: Option[String], lastName: Option[String],
                      email: String, student: Boolean, tutor: Boolean,
                      verified: Boolean, tutoring: List[SimpleCourse])

  /**
   * Companion object to get a UserData object from a Json string.
   * Used to render user fields based on the Json object stored
   * in the session.
   */
  object UserData {

    def fromJsonString(str: String): Option[UserData] = {
      val res: JsResult[UserData] = Json.parse(str).validate[UserData]
      if (res.isSuccess) Option(res.get) else {
        Logger.error(res.toString)
        None
      }
    }
  }

  implicit def userDataReads: Reads[UserData] = (
    (JsPath \ "firstName").readNullable[String] and
    (JsPath \ "lastName").readNullable[String] and
    (JsPath \ "email").read[String] and
    (JsPath \ "student").read[Boolean] and
    (JsPath \ "tutor").read[Boolean] and
    (JsPath \ "verified").read[Boolean] and
    (JsPath \ "tutoring").read[List[SimpleCourse]]
  )(UserData.apply _)

  implicit def userDataWrites: Writes[UserData] = (
    (JsPath \ "firstName").writeNullable[String] and
    (JsPath \ "lastName").writeNullable[String] and
    (JsPath \ "email").write[String] and
    (JsPath \ "student").write[Boolean] and
    (JsPath \ "tutor").write[Boolean] and
    (JsPath \ "verified").write[Boolean] and
    (JsPath \ "tutoring").write[List[SimpleCourse]]
  )(unlift(UserData.unapply))

  val conf = current.configuration
  val url = conf.getString("db.default.url").get
  val user = conf.getString("db.default.user").get
  val pass = conf.getString("db.default.password").get
  val driver = "org.postgresql.Driver"

  // Our Slick Database ORM
  val DB = Database.forURL(url, user = user, password = pass, driver = driver)

  val schools = TableQuery[Schools]
  val colleges = TableQuery[Colleges]
  val majors = TableQuery[Majors]
  val courses = TableQuery[Courses]
  val users = TableQuery[Users]
  val tutors = TableQuery[Tutors]

  /*****************
   * USER ACCOUNTS *
   *************** */

  // Check if a user exists given an email.
  def userExists(email: String): Boolean = {
    DB.withSession { implicit session =>
      users.filter(_.email === email).list.length > 0
    }
  }

  // Check email and password for a user.
  def validLogin(user: User): Boolean = {
    DB.withSession { implicit session =>
      // Get the password for a user by email address.
      val res = users.filter(_.email === user.email).list.map(_.password)
      if (res.length != 1) false
      else if (BCrypt.checkpw(user.password, res.head)) true
      else false
    }
  }

  // Save a user into the database.
  def saveUser(user: User) = {
    DB.withSession { implicit session =>
      val pw = BCrypt.hashpw(user.password, BCrypt.gensalt()) // Get hash + salt
      users.map(c =>
        (c.email, c.password, c.student, c.tutor, c.verified)) +=
        ((user.email, pw, user.student, user.tutor, false))
    }
  }

  // Retrieve a user profile from the database.
  def getUserData(email: String): UserData = {
    DB.withSession { implicit session =>
      val res = users.filter(_.email === email).list.map(r =>
        (r.firstName, r.lastName, r.student, r.tutor, r.verified)
      ).head

      val tutoring = tutors.filter(_.user === email).list.map(r =>
        SimpleCourse(r.school, r.major, r.course, r.rate)
      )

      UserData(res._1, res._2, email, res._3, res._4, res._5, tutoring)
    }
  }
  
  // Register a given user as a tutor
  def setTutor(email: String, sId: String, mId: String, cId: String, delete: Boolean) = {
    DB.withSession { implicit session =>
      if (delete) {
        (for {
          t <- tutors if t.user === email && t.school === sId && t.major === mId && t.course === cId
        } yield (t)).delete
      } else {
        tutors.map(t => (t.user, t.school, t.major, t.course)) +=
          (email, sId, mId, cId)
      }
    }
  }
  
  /***************
   * COURSE DATA *
   ************* */

  // Get all schools as maps containing school name (short and long),
  // and type (uni, hs, college).
  def getSchoolData: List[Map[String, String]] = {
    DB.withSession { implicit session =>
      (for (s <- schools) yield (s.id, s.name, s.`type`))
        .list.map(row =>
        Map("id" -> row._1, "name" -> row._2, "type" -> row._3)
      )
    }
  }

  // Get all majors grouped according to their college, with college names
  // in alphabetical order. Majors are maps containing id, name, and college.
  def getMajorData(sId: String) = {
    DB.withSession { implicit session =>
      TreeMap((for {
        c <- colleges
        m <- majors if m.college === c.id && m.school === sId
      } yield (m.id, m.name, c.name))
        .list.map(r =>
          Map("id" -> r._1, "name" -> r._2, "college" -> r._3)
        ).groupBy(_("college")).toSeq:_*)
    }
  }

  // Get all courses for a given school and major as maps with optional
  // fields: id, name, desc (description), prereqs, offered,
  // and link (MyPlan link).
  def getCourseData(sId: String, mId: String) = {
    DB.withSession { implicit session =>
      (for {
        c <- courses if c.major === mId && c.school === sId
      } yield (c.id, c.name, c.description, c.prereqs, c.offered, c.planLink))
        .list.map(r =>
          Map("id" -> Option(r._1), "name" -> Option(r._2), "desc" -> r._3,
            "prereqs" -> r._4, "offered" -> r._5, "link" -> r._6)
        )
    }
  }
}
