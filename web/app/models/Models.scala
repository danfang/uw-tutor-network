package models

import java.sql.Connection

import anorm._

/**
 *
 */
object Models {

  def getFullNames(school: String, major: String)(implicit c: Connection) = {
    if (major == "") {
      SQL(
        """
         SELECT s.full_name FROM schools as s
         JOIN colleges as c ON c.school = s.name
         WHERE s.name={school} LIMIT 1;
        """)
        .on("school" -> school)()
    } else {
      SQL(
        """
         SELECT s.full_name, c.name, m.name
         FROM schools as s
         JOIN colleges as c ON c.school = s.name
         JOIN majors as m ON m.college = c.id
         WHERE s.name={school} AND m.id={major} LIMIT 1;
        """)
        .on("school" -> school, "major" -> major)()
    }
  }

  /**
   *
   * @param school
   * @return
   */
  def getMajorData(school: String)(implicit c: Connection) = {
    SQL(
      """
            SELECT m.id, m.name, c.name
            FROM majors AS m
            JOIN colleges AS c ON m.college = c.id
            WHERE c.school={school};
      """)
      .on("school" -> school)()
      .collect({
        case Row(id: String, name: String, college: String) =>
        Map("id" -> id, "name" -> name, "college" -> college)
      })
      .groupBy(_("college"))
  }

  /**
   *
   * @param school
   * @param major
   * @return
   */
  def getCourseData(school: String, major: String)(implicit c: Connection) = {
    SQL(
      """
          SELECT c.name, c.description, c.prereqs, c.offered, c.plan_link
          FROM courses AS c
          JOIN majors AS m ON m.id = c.major
          JOIN colleges AS co ON m.college = co.id
          WHERE co.school={school} AND m.id={major};
      """)
      .on("school" -> school, "major" -> major)()
      .collect({
        case Row(name: String, desc: Option[String],
          pre: Option[String], off: Option[String],
          link: Option[String]) =>

        Map("name" -> name, "desc" -> desc.getOrElse(""),
          "pre" -> pre.getOrElse(""), "off" -> off.getOrElse(""),
          "link" -> link.getOrElse("")
        )
      })
  }
}
