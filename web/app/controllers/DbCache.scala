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

  def cachedMajors(sId: String) = {
    Cache.getOrElse(sId + ".majors", () => {
      Logger.info("Loading " + sId + ".majors into cache.")
      getMajorData(sId)
    }, CACHE_TIME)
  }

  def cachedCourses(sId: String, mId: String) = {
    Cache.getOrElse(sId + ".majors." + mId, () => {
      Logger.info("Loading " + sId + ".majors." + mId + " into cache.")
      getCourseData(sId, mId)
    }, CACHE_TIME)
  }

}
