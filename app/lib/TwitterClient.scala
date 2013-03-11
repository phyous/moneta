package lib

import twitter4j.{Status, ResponseList, Paging, TwitterFactory}
import twitter4j.conf.ConfigurationBuilder
import scala.collection.JavaConverters._
import play.Play

object TwitterClient {
  private[this] val MAX_STATUSES:Int = 200;

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
    val paging:Paging = new Paging()
    paging.setCount(MAX_STATUSES)

    client.getUserTimeline(user, paging).asScala.toList
  }
}
