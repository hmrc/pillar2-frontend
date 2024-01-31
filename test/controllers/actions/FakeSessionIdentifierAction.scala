package controllers.actions

import models.requests.SessionRequest
import play.api.mvc.{AnyContent, BodyParser, PlayBodyParsers, Request, Result}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class FakeSessionIdentifierAction extends SessionIdentifier()(ExecutionContext.Implicits.global) {

  override def refine[A](request: Request[A]): Future[Either[Result, SessionRequest[A]]] =
//      implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)
    Future.successful(Right(SessionRequest(request, "internalId")))

}
