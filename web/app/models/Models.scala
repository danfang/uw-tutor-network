package models

import models.Tables._
import org.mindrot.jbcrypt.BCrypt
import play.api.Play.current
import play.api.libs.functional.syntax._
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

  /**
   * Class to represent a full user
   */
  case class UserData(firstName: Option[String], lastName: Option[String],
                     email: String, student: Boolean, tutor: Boolean,
                     verified: Boolean) {

    /**
     * Creates a Json object out of the current full user. This is used to
     * pull a user out of the database, then put it into the session.
     *
     * @return A stringified Json object representing this
     */
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

  /**
   * Companion object to get a UserData object from a Json string.
   * Used to render user fields based on the Json object stored
   * in the session.
   */
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
        (c.email, c.password, c.student, c.tutor)) +=
        ((user.email, pw, user.student, user.tutor))
    }
  }

  // Retrieve a user profile from the database.
  def getUserData(email: String): UserData = {
    DB.withSession { implicit session =>
      val res = users.filter(_.email === email).list.map(r =>
        (r.firstName, r.lastName, r.student, r.tutor, r.verified)
      ).head // FIXME: Will break if user is not defined
      UserData(res._1, res._2, email, res._3, res._4, res._5)
    }
  }
  
  // Register a given user as a tutor
  def setTutor(email: String, course: String, major: String, school: String) = {
    DB.withSession { implicit session =>
      tutors.map(t => (t.user, t.course, t.major, t.school)) +=
        (email, course, major, school)
    }
  }
  
  /***************
   * COURSE DATA *
   ************* */

  // Get all schools as maps containing school name (short and long),
  // and type (uni, hs, college).
  def getSchoolData: List[Map[String, String]] = {
    DB.withSession { implicit session =>
      (for (s <- schools) yield (s.name, s.fullName, s.`type`))
        .list.map(row =>
        Map("name" -> row._1, "fullName" -> row._2, "type" -> row._3)
      )
    }
  }

  def getMajorData(school: String) = {
    DB.withSession { implicit session =>
      TreeMap((for {
        c <- colleges
        m <- majors if m.college === c.id && m.school === school
      } yield (m.id, m.name, c.name))
        .list.map(r =>
          Map("id" -> r._1, "name" -> r._2, "college" -> r._3)
        ).groupBy(_("college")).toSeq:_*)
    }
  }

  def getCourseData(school: String, major: String) = {
    DB.withSession { implicit session =>
      (for {
        c <- courses if c.major === major && c.school === school
      } yield (c.id, c.name, c.description, c.prereqs, c.offered, c.planLink))
        .list.map(r =>
          Map("id" -> Option(r._1), "name" -> Option(r._2), "des" -> r._3,
            "pre" -> r._4, "off" -> r._5, "link" -> r._6)
        )
    }
  }
}
