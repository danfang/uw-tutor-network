package models
// AUTO-GENERATED Slick data model
/** Stand-alone Slick data model for immediate use */
object Tables extends {
  val profile = scala.slick.driver.PostgresDriver
} with Tables

/** Slick data model trait for extension, choice of backend or usage in the cake pattern. (Make sure to initialize this late.) */
trait Tables {
  val profile: scala.slick.driver.JdbcProfile
  import profile.simple._
  import scala.slick.model.ForeignKeyAction
  // NOTE: GetResult mappers for plain SQL are only generated for tables where Slick knows how to map the types of all columns.
  import scala.slick.jdbc.{GetResult => GR}
  
  /** DDL for all tables. Call .create to execute. */
  lazy val ddl = Colleges.ddl ++ Courses.ddl ++ Majors.ddl ++ Schools.ddl ++ Tutors.ddl ++ Users.ddl
  
  /** Entity class storing rows of table Colleges
   *  @param id Database column id DBType(varchar), Length(2147483647,true)
   *  @param name Database column name DBType(varchar), Length(2147483647,true)
   *  @param school Database column school DBType(varchar), Length(2147483647,true) */
  case class CollegesRow(id: String, name: String, school: String)
  /** GetResult implicit for fetching CollegesRow objects using plain SQL queries */
  implicit def GetResultCollegesRow(implicit e0: GR[String]): GR[CollegesRow] = GR{
    prs => import prs._
    CollegesRow.tupled((<<[String], <<[String], <<[String]))
  }
  /** Table description of table colleges. Objects of this class serve as prototypes for rows in queries. */
  class Colleges(_tableTag: Tag) extends Table[CollegesRow](_tableTag, "colleges") {
    def * = (id, name, school) <> (CollegesRow.tupled, CollegesRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (id.?, name.?, school.?).shaped.<>({r=>import r._; _1.map(_=> CollegesRow.tupled((_1.get, _2.get, _3.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))
    
    /** Database column id DBType(varchar), Length(2147483647,true) */
    val id: Column[String] = column[String]("id", O.Length(2147483647,varying=true))
    /** Database column name DBType(varchar), Length(2147483647,true) */
    val name: Column[String] = column[String]("name", O.Length(2147483647,varying=true))
    /** Database column school DBType(varchar), Length(2147483647,true) */
    val school: Column[String] = column[String]("school", O.Length(2147483647,varying=true))
    
    /** Primary key of Colleges (database name colleges_pkey) */
    val pk = primaryKey("colleges_pkey", (id, school))
    
    /** Foreign key referencing Schools (database name colleges_school_fkey) */
    lazy val schoolsFk = foreignKey("colleges_school_fkey", school, Schools)(r => r.name, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
  }
  /** Collection-like TableQuery object for table Colleges */
  lazy val Colleges = new TableQuery(tag => new Colleges(tag))
  
  /** Entity class storing rows of table Courses
   *  @param id Database column id DBType(varchar), Length(2147483647,true)
   *  @param major Database column major DBType(varchar), Length(2147483647,true)
   *  @param college Database column college DBType(varchar), Length(2147483647,true)
   *  @param school Database column school DBType(varchar), Length(2147483647,true)
   *  @param name Database column name DBType(varchar), Length(2147483647,true)
   *  @param description Database column description DBType(text), Length(2147483647,true), Default(None)
   *  @param offered Database column offered DBType(varchar), Length(2147483647,true), Default(None)
   *  @param planLink Database column plan_link DBType(varchar), Length(2147483647,true), Default(None)
   *  @param prereqs Database column prereqs DBType(varchar), Length(2147483647,true), Default(None) */
  case class CoursesRow(id: String, major: String, college: String, school: String, name: String, description: Option[String] = None, offered: Option[String] = None, planLink: Option[String] = None, prereqs: Option[String] = None)
  /** GetResult implicit for fetching CoursesRow objects using plain SQL queries */
  implicit def GetResultCoursesRow(implicit e0: GR[String], e1: GR[Option[String]]): GR[CoursesRow] = GR{
    prs => import prs._
    CoursesRow.tupled((<<[String], <<[String], <<[String], <<[String], <<[String], <<?[String], <<?[String], <<?[String], <<?[String]))
  }
  /** Table description of table courses. Objects of this class serve as prototypes for rows in queries. */
  class Courses(_tableTag: Tag) extends Table[CoursesRow](_tableTag, "courses") {
    def * = (id, major, college, school, name, description, offered, planLink, prereqs) <> (CoursesRow.tupled, CoursesRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (id.?, major.?, college.?, school.?, name.?, description, offered, planLink, prereqs).shaped.<>({r=>import r._; _1.map(_=> CoursesRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6, _7, _8, _9)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))
    
    /** Database column id DBType(varchar), Length(2147483647,true) */
    val id: Column[String] = column[String]("id", O.Length(2147483647,varying=true))
    /** Database column major DBType(varchar), Length(2147483647,true) */
    val major: Column[String] = column[String]("major", O.Length(2147483647,varying=true))
    /** Database column college DBType(varchar), Length(2147483647,true) */
    val college: Column[String] = column[String]("college", O.Length(2147483647,varying=true))
    /** Database column school DBType(varchar), Length(2147483647,true) */
    val school: Column[String] = column[String]("school", O.Length(2147483647,varying=true))
    /** Database column name DBType(varchar), Length(2147483647,true) */
    val name: Column[String] = column[String]("name", O.Length(2147483647,varying=true))
    /** Database column description DBType(text), Length(2147483647,true), Default(None) */
    val description: Column[Option[String]] = column[Option[String]]("description", O.Length(2147483647,varying=true), O.Default(None))
    /** Database column offered DBType(varchar), Length(2147483647,true), Default(None) */
    val offered: Column[Option[String]] = column[Option[String]]("offered", O.Length(2147483647,varying=true), O.Default(None))
    /** Database column plan_link DBType(varchar), Length(2147483647,true), Default(None) */
    val planLink: Column[Option[String]] = column[Option[String]]("plan_link", O.Length(2147483647,varying=true), O.Default(None))
    /** Database column prereqs DBType(varchar), Length(2147483647,true), Default(None) */
    val prereqs: Column[Option[String]] = column[Option[String]]("prereqs", O.Length(2147483647,varying=true), O.Default(None))
    
    /** Primary key of Courses (database name courses_pkey) */
    val pk = primaryKey("courses_pkey", (id, major, college, school))
    
    /** Foreign key referencing Majors (database name courses_major_fkey) */
    lazy val majorsFk = foreignKey("courses_major_fkey", (major, college, school), Majors)(r => (r.id, r.college, r.school), onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
  }
  /** Collection-like TableQuery object for table Courses */
  lazy val Courses = new TableQuery(tag => new Courses(tag))
  
  /** Entity class storing rows of table Majors
   *  @param id Database column id DBType(varchar), Length(2147483647,true)
   *  @param college Database column college DBType(varchar), Length(2147483647,true)
   *  @param school Database column school DBType(varchar), Length(2147483647,true)
   *  @param name Database column name DBType(varchar), Length(2147483647,true)
   *  @param link Database column link DBType(varchar), Length(2147483647,true), Default(None) */
  case class MajorsRow(id: String, college: String, school: String, name: String, link: Option[String] = None)
  /** GetResult implicit for fetching MajorsRow objects using plain SQL queries */
  implicit def GetResultMajorsRow(implicit e0: GR[String], e1: GR[Option[String]]): GR[MajorsRow] = GR{
    prs => import prs._
    MajorsRow.tupled((<<[String], <<[String], <<[String], <<[String], <<?[String]))
  }
  /** Table description of table majors. Objects of this class serve as prototypes for rows in queries. */
  class Majors(_tableTag: Tag) extends Table[MajorsRow](_tableTag, "majors") {
    def * = (id, college, school, name, link) <> (MajorsRow.tupled, MajorsRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (id.?, college.?, school.?, name.?, link).shaped.<>({r=>import r._; _1.map(_=> MajorsRow.tupled((_1.get, _2.get, _3.get, _4.get, _5)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))
    
    /** Database column id DBType(varchar), Length(2147483647,true) */
    val id: Column[String] = column[String]("id", O.Length(2147483647,varying=true))
    /** Database column college DBType(varchar), Length(2147483647,true) */
    val college: Column[String] = column[String]("college", O.Length(2147483647,varying=true))
    /** Database column school DBType(varchar), Length(2147483647,true) */
    val school: Column[String] = column[String]("school", O.Length(2147483647,varying=true))
    /** Database column name DBType(varchar), Length(2147483647,true) */
    val name: Column[String] = column[String]("name", O.Length(2147483647,varying=true))
    /** Database column link DBType(varchar), Length(2147483647,true), Default(None) */
    val link: Column[Option[String]] = column[Option[String]]("link", O.Length(2147483647,varying=true), O.Default(None))
    
    /** Primary key of Majors (database name majors_pkey) */
    val pk = primaryKey("majors_pkey", (id, college, school))
    
    /** Foreign key referencing Colleges (database name majors_college_fkey) */
    lazy val collegesFk = foreignKey("majors_college_fkey", (college, school), Colleges)(r => (r.id, r.school), onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
  }
  /** Collection-like TableQuery object for table Majors */
  lazy val Majors = new TableQuery(tag => new Majors(tag))
  
  /** Entity class storing rows of table Schools
   *  @param name Database column name DBType(varchar), PrimaryKey, Length(2147483647,true)
   *  @param fullName Database column full_name DBType(varchar), Length(2147483647,true)
   *  @param `type` Database column type DBType(varchar), Length(2147483647,true) */
  case class SchoolsRow(name: String, fullName: String, `type`: String)
  /** GetResult implicit for fetching SchoolsRow objects using plain SQL queries */
  implicit def GetResultSchoolsRow(implicit e0: GR[String]): GR[SchoolsRow] = GR{
    prs => import prs._
    SchoolsRow.tupled((<<[String], <<[String], <<[String]))
  }
  /** Table description of table schools. Objects of this class serve as prototypes for rows in queries.
   *  NOTE: The following names collided with Scala keywords and were escaped: type */
  class Schools(_tableTag: Tag) extends Table[SchoolsRow](_tableTag, "schools") {
    def * = (name, fullName, `type`) <> (SchoolsRow.tupled, SchoolsRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (name.?, fullName.?, `type`.?).shaped.<>({r=>import r._; _1.map(_=> SchoolsRow.tupled((_1.get, _2.get, _3.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))
    
    /** Database column name DBType(varchar), PrimaryKey, Length(2147483647,true) */
    val name: Column[String] = column[String]("name", O.PrimaryKey, O.Length(2147483647,varying=true))
    /** Database column full_name DBType(varchar), Length(2147483647,true) */
    val fullName: Column[String] = column[String]("full_name", O.Length(2147483647,varying=true))
    /** Database column type DBType(varchar), Length(2147483647,true)
     *  NOTE: The name was escaped because it collided with a Scala keyword. */
    val `type`: Column[String] = column[String]("type", O.Length(2147483647,varying=true))
    
    /** Uniqueness Index over (fullName) (database name schools_full_name_key) */
    val index1 = index("schools_full_name_key", fullName, unique=true)
  }
  /** Collection-like TableQuery object for table Schools */
  lazy val Schools = new TableQuery(tag => new Schools(tag))
  
  /** Entity class storing rows of table Tutors
   *  @param user Database column user DBType(varchar), Length(2147483647,true)
   *  @param course Database column course DBType(varchar), Length(2147483647,true)
   *  @param major Database column major DBType(varchar), Length(2147483647,true)
   *  @param college Database column college DBType(varchar), Length(2147483647,true)
   *  @param school Database column school DBType(varchar), Length(2147483647,true)
   *  @param rate Database column rate DBType(int4), Default(None) */
  case class TutorsRow(user: String, course: String, major: String, college: String, school: String, rate: Option[Int] = None)
  /** GetResult implicit for fetching TutorsRow objects using plain SQL queries */
  implicit def GetResultTutorsRow(implicit e0: GR[String], e1: GR[Option[Int]]): GR[TutorsRow] = GR{
    prs => import prs._
    TutorsRow.tupled((<<[String], <<[String], <<[String], <<[String], <<[String], <<?[Int]))
  }
  /** Table description of table tutors. Objects of this class serve as prototypes for rows in queries. */
  class Tutors(_tableTag: Tag) extends Table[TutorsRow](_tableTag, "tutors") {
    def * = (user, course, major, college, school, rate) <> (TutorsRow.tupled, TutorsRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (user.?, course.?, major.?, college.?, school.?, rate).shaped.<>({r=>import r._; _1.map(_=> TutorsRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))
    
    /** Database column user DBType(varchar), Length(2147483647,true) */
    val user: Column[String] = column[String]("user", O.Length(2147483647,varying=true))
    /** Database column course DBType(varchar), Length(2147483647,true) */
    val course: Column[String] = column[String]("course", O.Length(2147483647,varying=true))
    /** Database column major DBType(varchar), Length(2147483647,true) */
    val major: Column[String] = column[String]("major", O.Length(2147483647,varying=true))
    /** Database column college DBType(varchar), Length(2147483647,true) */
    val college: Column[String] = column[String]("college", O.Length(2147483647,varying=true))
    /** Database column school DBType(varchar), Length(2147483647,true) */
    val school: Column[String] = column[String]("school", O.Length(2147483647,varying=true))
    /** Database column rate DBType(int4), Default(None) */
    val rate: Column[Option[Int]] = column[Option[Int]]("rate", O.Default(None))
    
    /** Primary key of Tutors (database name tutors_pkey) */
    val pk = primaryKey("tutors_pkey", (user, course, major, college, school))
    
    /** Foreign key referencing Courses (database name tutors_course_fkey) */
    lazy val coursesFk = foreignKey("tutors_course_fkey", (course, major, college, school), Courses)(r => (r.id, r.major, r.college, r.school), onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing Users (database name tutors_user_fkey) */
    lazy val usersFk = foreignKey("tutors_user_fkey", user, Users)(r => r.email, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
  }
  /** Collection-like TableQuery object for table Tutors */
  lazy val Tutors = new TableQuery(tag => new Tutors(tag))
  
  /** Entity class storing rows of table Users
   *  @param firstName Database column first_name DBType(varchar), Length(2147483647,true), Default(None)
   *  @param lastName Database column last_name DBType(varchar), Length(2147483647,true), Default(None)
   *  @param email Database column email DBType(varchar), PrimaryKey, Length(2147483647,true)
   *  @param password Database column password DBType(varchar), Length(2147483647,true)
   *  @param verified Database column verified DBType(bool)
   *  @param student Database column student DBType(bool)
   *  @param tutor Database column tutor DBType(bool)
   *  @param rate Database column rate DBType(int4), Default(None)
   *  @param about Database column about DBType(varchar), Length(500,true), Default(None) */
  case class UsersRow(firstName: Option[String] = None, lastName: Option[String] = None, email: String, password: String, verified: Boolean, student: Boolean, tutor: Boolean, rate: Option[Int] = None, about: Option[String] = None)
  /** GetResult implicit for fetching UsersRow objects using plain SQL queries */
  implicit def GetResultUsersRow(implicit e0: GR[Option[String]], e1: GR[String], e2: GR[Boolean], e3: GR[Option[Int]]): GR[UsersRow] = GR{
    prs => import prs._
    UsersRow.tupled((<<?[String], <<?[String], <<[String], <<[String], <<[Boolean], <<[Boolean], <<[Boolean], <<?[Int], <<?[String]))
  }
  /** Table description of table users. Objects of this class serve as prototypes for rows in queries. */
  class Users(_tableTag: Tag) extends Table[UsersRow](_tableTag, "users") {
    def * = (firstName, lastName, email, password, verified, student, tutor, rate, about) <> (UsersRow.tupled, UsersRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (firstName, lastName, email.?, password.?, verified.?, student.?, tutor.?, rate, about).shaped.<>({r=>import r._; _3.map(_=> UsersRow.tupled((_1, _2, _3.get, _4.get, _5.get, _6.get, _7.get, _8, _9)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))
    
    /** Database column first_name DBType(varchar), Length(2147483647,true), Default(None) */
    val firstName: Column[Option[String]] = column[Option[String]]("first_name", O.Length(2147483647,varying=true), O.Default(None))
    /** Database column last_name DBType(varchar), Length(2147483647,true), Default(None) */
    val lastName: Column[Option[String]] = column[Option[String]]("last_name", O.Length(2147483647,varying=true), O.Default(None))
    /** Database column email DBType(varchar), PrimaryKey, Length(2147483647,true) */
    val email: Column[String] = column[String]("email", O.PrimaryKey, O.Length(2147483647,varying=true))
    /** Database column password DBType(varchar), Length(2147483647,true) */
    val password: Column[String] = column[String]("password", O.Length(2147483647,varying=true))
    /** Database column verified DBType(bool) */
    val verified: Column[Boolean] = column[Boolean]("verified")
    /** Database column student DBType(bool) */
    val student: Column[Boolean] = column[Boolean]("student")
    /** Database column tutor DBType(bool) */
    val tutor: Column[Boolean] = column[Boolean]("tutor")
    /** Database column rate DBType(int4), Default(None) */
    val rate: Column[Option[Int]] = column[Option[Int]]("rate", O.Default(None))
    /** Database column about DBType(varchar), Length(500,true), Default(None) */
    val about: Column[Option[String]] = column[Option[String]]("about", O.Length(500,varying=true), O.Default(None))
  }
  /** Collection-like TableQuery object for table Users */
  lazy val Users = new TableQuery(tag => new Users(tag))
}