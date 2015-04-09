package controllers

import java.util.concurrent.Callable

import models.Models._
import play.api.Logger
import play.cache.Cache

/**
 * Caching system for schools, majors, and courses
 *
 * @author Daniel Fang <danfang@uw.edu>
 */
object DbCache {

  val CACHE_TIME = 24 * 60 * 60 // 24 hours

  implicit def funToCallable[A](f: () => A) = new Callable[A]() {
    def call() = f()
  }

  def cachedSchools = {
    Cache.getOrElse("schools", () => {
      Logger.info("Loading schools into cache.")
      getSchoolData
    }, CACHE_TIME)
  }

  def cachedMajors(school: String) = {
    Cache.getOrElse(school + ".majors", () => {
      Logger.info("Loading " + school + ".majors into cache.")
      getMajorData(school)
    }, CACHE_TIME)
  }

  def cachedCourses(school: String, major: String) = {
    Cache.getOrElse(school + ".majors." + major, () => {
      Logger.info("Loading " + school + ".majors." + major + " into cache.")
      getCourseData(school, major)
    }, CACHE_TIME)
  }

}
