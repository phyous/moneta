package lib

import twitter4j.{Status, ResponseList, Paging, TwitterFactory}
import twitter4j.conf.ConfigurationBuilder
import scala.collection.JavaConverters._
import scala.concurrent.{future, blocking, Future, Await}
import play.api.libs.concurrent.Execution.Implicits._
import scala.concurrent.duration._
import play.Play

object TwitterClient {
  private[this] val MAX_STATUSES:Int = 200;
  private[this] val USER_TIMELINE_PARALLELISM:Int = 1;

  val client = {
    val ConsumerKey = Play.application().configuration().getString("twitter.ConsumerKey")
    val ConsumerSecret = Play.application().configuration().getString("twitter.ConsumerSecret")
    val AccessToken = Play.application().configuration().getString("twitter.AccessToken")
    val AccessTokenSecret = Play.application().configuration().getString("twitter.AccessTokenSecret")

    val cb = new ConfigurationBuilder()
    cb.setDebugEnabled(true)
      .setOAuthConsumerKey(ConsumerKey)
      .setOAuthConsumerSecret(ConsumerSecret)
      .setOAuthAccessToken(AccessToken)
      .setOAuthAccessTokenSecret(AccessTokenSecret)
    val tf = new TwitterFactory(cb.build())
    tf.getInstance()
  }

  def getUserTimeline(user:String):List[Status] = {
    val pages:List[Paging] = (1 to USER_TIMELINE_PARALLELISM).foldLeft(List[Paging]()) {
      (list, page) =>
        val paging:Paging = new Paging()
        paging.setCount(MAX_STATUSES)
        paging.setPage(page)
        paging :: list
    }

    val statusFutures: List[Future[List[Status]]] =  pages.map {
      page => future { blocking {client.getUserTimeline(user, page).asScala.toList}}
    }

    val futStatuses: Future[List[List[Status]]] = Future.sequence(statusFutures)
    Await.result(futStatuses, 2 seconds).foldLeft(List[Status]()){
      (fullList, statuses) => fullList ::: statuses
    }
  }
}
