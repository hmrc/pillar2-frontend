package controllers.actions

import models.requests.{SessionRequest, UnauthenticatedOptionalDataRequest}
import play.api.mvc.ActionTransformer
import repositories.UnauthenticatedDataRepository

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class UnauthenticatedDataRetrievalAction @Inject()(val sessionRepository: UnauthenticatedDataRepository)
                                                  (implicit val executionContext: ExecutionContext)
  extends ActionTransformer[SessionRequest, UnauthenticatedOptionalDataRequest] {

  override protected def transform[A](request: SessionRequest[A]): Future[UnauthenticatedOptionalDataRequest[A]] = {

    sessionRepository.get(request.userId).map {
      UnauthenticatedOptionalDataRequest(request.request, request.userId, _)
    }
  }
}
