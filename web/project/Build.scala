import play.PlayImport._
import play.PlayScala
import sbt.Keys._
import sbt._
import com.typesafe.config._

import scala.util.Try


/**
 * Main project containing main source code depending on slick and
 * codegen project.
 *
 * @author Daniel Fang <danfang@uw.edu>
 */
object WebBuild extends Build {

  lazy val mainProject = Project(
    id = "root",
    base = file("."),
    settings = Seq(
      scalaVersion := "2.11.1",
      libraryDependencies ++= List(
        jdbc,
        anorm,
        cache,
        ws,
        "com.typesafe.slick" %% "slick" % "2.1.0",
        "com.typesafe.slick" %% "slick-codegen" % "2.1.0-RC3",
        "org.slf4j" % "slf4j-nop" % "1.6.4",
        "org.postgresql" % "postgresql" % "9.4-1201-jdbc41",
        "org.mindrot" % "jbcrypt" % "0.3m"
      ),
      version := "1.0-SNAPSHOT",
      name := "web",
      slick <<= slickCodeGenTask//, // register manual sbt command
      //sourceGenerators in Compile <+= slickCodeGenTask // automatic code generation on compile
    )
  ).enablePlugins(PlayScala)

  // code generation task
  lazy val slick = TaskKey[Seq[File]]("slick-codegen")
  lazy val slickCodeGenTask = (sourceManaged, dependencyClasspath in Compile, runner in Compile, streams) map { (dir, cp, r, s) =>

    // delete old ORM
    val outputFile = file("app/models/Tables.scala")
    Try(outputFile.delete())

    val conf = ConfigFactory.parseFile(file("conf/application.conf")).resolve()
    val url = conf.getString("db.default.url")
    val user = conf.getString("db.default.user")
    val pass = conf.getString("db.default.password")

    val outputDir = "app"
    val jdbcDriver = "org.postgresql.Driver"
    val slickDriver = "scala.slick.driver.PostgresDriver"
    val pkg = "models"

    toError(r.run("scala.slick.codegen.SourceCodeGenerator", cp.files,
      Array(slickDriver, jdbcDriver, url, outputDir, pkg, user, pass), s.log))

    Seq(file(outputFile.getAbsolutePath))
  }
}
