package controllers.actions

import controllers.routes
import models.requests.{UnauthenticatedDataRequest, UnauthenticatedOptionalDataRequest}
import play.api.mvc.{ActionRefiner, Result}
import play.api.mvc.Results.Redirect
import utils.FutureConverter._

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class UnauthenticatedDataRequiredAction @Inject()(implicit val executionContext: ExecutionContext)
  extends ActionRefiner[UnauthenticatedOptionalDataRequest, UnauthenticatedDataRequest] {

  override protected def refine[A](request: UnauthenticatedOptionalDataRequest[A]): Future[Either[Result, UnauthenticatedDataRequest[A]]] = {

    request.userAnswers match {
      case None =>
        Left(Redirect(routes.JourneyRecoveryController.onPageLoad())).toFuture
      case Some(data) =>
        Right(UnauthenticatedDataRequest(request.request, request.userId, data)).toFuture
    }
  }
}