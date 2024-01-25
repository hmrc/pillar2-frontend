package models.requests

import play.api.mvc.{Request, WrappedRequest}

case class SessionRequest[A](request: Request[A], userId: String) extends WrappedRequest[A](request)
