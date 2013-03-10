package controllers.user

import play.api._
import libs.json._
import libs.json.JsString
import play.api.mvc._

import views._

import models._
import lib.TwitterClient
import twitter4j.{Status, ResponseList}
import scala.collection.JavaConverters._

object Posts extends Controller {

  def index(id: String) = Action {
    val statuses = TwitterClient.getUserTimeline(id)
    val statusList = statuses.asScala.toList

    val dateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd")
    val groupedDates = statusList.groupBy {
      case i => dateFormat.format(i.getCreatedAt)
    }

    val dateCount = groupedDates.foldLeft(Json.arr()) {
      case (json, (date, statuses)) => json.append(
        Json.obj("date" -> JsString(date),
          "count" -> statuses.length)
      )
    }

    Ok(dateCount).as("application/json")
  }
}
