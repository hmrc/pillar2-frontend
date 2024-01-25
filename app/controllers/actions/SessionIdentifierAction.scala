package controllers.actions

import controllers.routes
import models.requests.SessionRequest
import play.api.mvc.Results.Redirect
import play.api.mvc.{ActionFunction, ActionRefiner, Request, Result}
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import utils.FutureConverter._
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SessionIdentifierAction @Inject()()(implicit val executionContext: ExecutionContext)
  extends ActionRefiner[Request, SessionRequest] with ActionFunction[Request, SessionRequest] {

  override def refine[A](request: Request[A]): Future[Either[Result, SessionRequest[A]]] = {

    val hc = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    hc.sessionId
      .map(session => Right(SessionRequest(request, session.value)).toFuture)
      .getOrElse(Left(Redirect(routes.JourneyRecoveryController.onPageLoad())).toFuture)
  }
}