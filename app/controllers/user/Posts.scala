package controllers.user

import play.api._
import libs.json._
import libs.json.JsString
import play.api.mvc._

import views._

import models._
import lib.TwitterClient
import twitter4j.{TwitterException}

object Posts extends Controller {

  def index(id: String) = Action {
    try {
      val status = getStatuses(id)
      Ok(status).as("application/json")
    } catch {
      case te: TwitterException =>
        Logger.error("%s".format(te.toString))
        if (te.getStatusCode == 404) {
          NotFound( "User %s not found. Try again with a valid user.".format(id))
        } else if (te.getStatusCode == 401) {
          Unauthorized("%s's posts are protected! Try again with another user.".format(id))
        } else {
          InternalServerError("Something went horribly wrong. Don't worry, I'm on it.")
        }
      case e: EmptyUserException =>
        NotFound( "User %s has no tweets! Please try another user.".format(id))
    }
  }

  private[this] def getStatuses(id: String):JsObject = {
    val statuses = TwitterClient.getUserTimeline(id)

    if (statuses.size == 0){
      throw new EmptyUserException();
    }

    val dateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd")
    val groupedDates = statuses.groupBy {
      case i => dateFormat.format(i.getCreatedAt)
    }

    val dateCount = groupedDates.foldLeft(Json.arr()) {
      case (json, (date, statuses)) =>
        val sampleStatusText = statuses.head.getText
        val sampleStatusId = statuses.head.getId
        json.append( Json.obj("date" -> JsString(date),
          "count" -> statuses.length,
          "sampleText" -> JsString(sampleStatusText),
          "sampleId" -> JsString(sampleStatusId.toString))
      )
    }

    val user = statuses.head.getUser
    val userStatuses = Json.obj(
      "profileImageURL" -> JsString(user.getBiggerProfileImageURL),
      "statusesCount" -> user.getStatusesCount,
      "userScreenName" -> user.getScreenName,
      "userName" -> user.getName,
      "postData" -> dateCount
    )
    userStatuses
  }

  class EmptyUserException extends Exception {}


}
