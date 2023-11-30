/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package controllers

import base.SpecBase
import config.FrontendAppConfig
import controllers.actions.AuthenticatedIdentifierAction
import generators.ModelGenerators
import models.SubscriptionCreateError
import models.requests.IdentifierRequest
import models.subscription.ReadSubscriptionRequestParameters
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.mvc.ControllerHelpers.TODO.executionContext
import play.api.mvc._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core.{AuthConnector, Enrolment, EnrolmentIdentifier}
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
class CustomIdentifierAction @Inject() (
  authConnector: AuthConnector,
  config:        FrontendAppConfig,
  parser:        BodyParsers.Default,
  enrolmentsSet: Set[Enrolment]
)(implicit ec:   ExecutionContext)
    extends AuthenticatedIdentifierAction(authConnector, config, parser) {

  override def refine[A](request: Request[A]): Future[Either[Result, IdentifierRequest[A]]] = {

    val identifierRequest = IdentifierRequest(request, "some-user-id", enrolmentsSet)

    Future.successful(Right(identifierRequest))
  }
}

class DashboardControllerSpec extends SpecBase with ModelGenerators {

  val stubbedMessagesControllerComponents = stubMessagesControllerComponents()
  val bodyParsers                         = new BodyParsers.Default

  "Dashboard Controller" should {

    "Dashboard Controller" should {

      "return OK and the correct view for a GET" in {

        val enrolmentsSet: Set[Enrolment] = Set(
          Enrolment(
            key = "HMRC-PILLAR2-ORG",
            identifiers = Seq(
              EnrolmentIdentifier("PLRID", "12345678"),
              EnrolmentIdentifier("UTR", "ABC12345")
            ),
            state = "activated"
          ),
          Enrolment(
            key = "HMRC-VAT-ORG",
            identifiers = Seq(
              EnrolmentIdentifier("VRN", "987654321"),
              EnrolmentIdentifier("UTR", "DEF67890")
            ),
            state = "activated"
          )
        )

        val customIdentifierAction = new CustomIdentifierAction(
          mockAuthConnector,
          mockFrontendAppConfig,
          bodyParsers,
          enrolmentsSet
        )

        when(mockAuthConnector.authorise[Unit](any, any)(any, any)).thenReturn(Future.successful(()))

        val request: Request[AnyContent] = FakeRequest(GET, controllers.routes.DashboardController.onPageLoad.url)

        val result: Future[Result] = customIdentifierAction.invokeBlock(
          request,
          { _: IdentifierRequest[AnyContent] =>
            Future.successful(Ok)
          }
        )

        status(result) mustEqual OK
      }

      "return Internal Server Error for a GET when there's an error retrieving subscription" in {
        val enrolmentsSet: Set[Enrolment] = Set(
          Enrolment(
            key = "HMRC-PILLAR2-ORG",
            identifiers = Seq(
              EnrolmentIdentifier("PLRID", "12345678"),
              EnrolmentIdentifier("UTR", "ABC12345")
            ),
            state = "activated"
          ),
          Enrolment(
            key = "HMRC-VAT-ORG",
            identifiers = Seq(
              EnrolmentIdentifier("VRN", "987654321"),
              EnrolmentIdentifier("UTR", "DEF67890")
            ),
            state = "activated"
          )
        )

        val customIdentifierAction = new CustomIdentifierAction(
          mockAuthConnector,
          mockFrontendAppConfig,
          bodyParsers,
          enrolmentsSet
        )

        when(mockAuthConnector.authorise[Unit](any, any)(any, any)).thenReturn(Future.successful(()))

        when(mockReadSubscriptionService.readSubscription(any[ReadSubscriptionRequestParameters])(any[HeaderCarrier]))
          .thenReturn(Future.successful(Left(SubscriptionCreateError)))

        val request: Request[AnyContent] = FakeRequest(GET, controllers.routes.DashboardController.onPageLoad.url)

        val result: Future[Result] = customIdentifierAction.invokeBlock(
          request,
          { _: IdentifierRequest[AnyContent] =>
            Future.successful(InternalServerError)
          }
        )

        status(result) mustEqual INTERNAL_SERVER_ERROR
      }

      "return SEE_OTHER and the correct view for a GET when PLR reference is missing" in {
        val enrolmentsSet: Set[Enrolment] = Set(
          Enrolment(
            key = "HMRC-PILLAR2-van",
            identifiers = Seq(
            ),
            state = "activated"
          )
        )

        val customIdentifierAction = new CustomIdentifierAction(
          mockAuthConnector,
          mockFrontendAppConfig,
          bodyParsers,
          enrolmentsSet
        )

        val dashboardController = new DashboardController(
          mockUserAnswersConnectors,
          mockDataRetrievalAction,
          customIdentifierAction,
          mockDataRequiredAction,
          mockReadSubscriptionService,
          mockControllerComponents,
          mockDashboardView
        )(executionContext, appConfig)

        when(mockAuthConnector.authorise[Unit](any, any)(any, any)).thenReturn(Future.successful(()))

        when(mockReadSubscriptionService.readSubscription(any[ReadSubscriptionRequestParameters])(any[HeaderCarrier]))
          .thenReturn(Future.successful(Left(SubscriptionCreateError)))

        val request: Request[AnyContent] = FakeRequest(GET, controllers.routes.DashboardController.onPageLoad.url)

        val result: Future[Result] = customIdentifierAction.invokeBlock(
          request,
          { _: IdentifierRequest[AnyContent] => Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())) }
        )

        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.JourneyRecoveryController.onPageLoad().url)
      }
    }

  }

}
