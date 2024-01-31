package controllers.actions

import controllers.actions.FakeUnauthenticatedDataRetrievalAction.mockRepository
import models.UserAnswers
import models.requests.{SessionRequest, UnauthenticatedOptionalDataRequest}
import org.mockito.MockitoSugar.mock
import repositories.UnauthenticatedDataRepository

import scala.concurrent.{ExecutionContext, Future}

class FakeUnauthenticatedDataRetrievalAction(returnedData: Option[UserAnswers])
    extends UnauthenticatedDataRetrievalAction(mockRepository)(ExecutionContext.Implicits.global) {

  override protected def transform[A](request: SessionRequest[A]): Future[UnauthenticatedOptionalDataRequest[A]] =
    Future(UnauthenticatedOptionalDataRequest(request.request, request.userId, returnedData))
}

object FakeUnauthenticatedDataRetrievalAction {
  val mockRepository: UnauthenticatedDataRepository = mock[UnauthenticatedDataRepository]
}
