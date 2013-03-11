package controllers

import play.api.mvc._
import lib.SimpleLogger

object Application extends Controller {
  
  def index = Action { implicit request =>
    SimpleLogger.logRequest(request)
    Ok(views.html.index())
  }
  
}