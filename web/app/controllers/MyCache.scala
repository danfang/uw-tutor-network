package controllers

import java.util.concurrent.Callable

import models.Models._
import play.api.Logger
import play.cache.Cache

import scala.collection.immutable.TreeMap

/**
 * Caching system for schools, majors, and courses
 *
 * @author Daniel Fang <danfang@uw.edu>
 */
object MyCache {

  val CACHE_TIME = 24 * 60 * 60 // 24 hours
  
  def cachedSchools = {
    Cache.getOrElse("schools", { new Callable[List[Map[String, String]]] {
      def call = {
        Logger.info("Loading schools into cache.")
        getSchoolData
      }
    }}, CACHE_TIME)
  }

  def cachedMajors(school: String) = {
    Cache.getOrElse(school + ".majors", {
      new Callable[TreeMap[String, List[Map[String, String]]]] {
        def call = {
          Logger.info("Loading " + school + ".majors into cache.")
          getMajorData(school)
        }
      }
    }, CACHE_TIME)
  }

  def cachedCourses(school: String, major: String) = {
    Cache.getOrElse(school + ".majors." + major, {
      new Callable[List[Map[String, Option[String]]]] {
        def call = {
          Logger.info("Loading " + school + ".majors." + major + " into cache.")
          getCourseData(school, major)
        }
      }
    }, CACHE_TIME)
  }
}
