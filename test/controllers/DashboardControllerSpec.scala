/*
 * Copyright 2024 HM Revenue & Customs
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
import connectors.UserAnswersConnectors
import controllers.actions.AuthenticatedIdentifierAction
import generators.ModelGenerators
import models.subscription.{AccountStatus, DashboardInfo, ReadSubscriptionRequestParameters}
import models.{BadRequestError, DuplicateSubmissionError, InternalServerError_, NotFoundError, SubscriptionCreateError, UnprocessableEntityError, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import pages.{fmDashboardPage, subAccountStatusPage}
import play.api.libs.json.{JsValue, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.api.{Configuration, inject}
import services.ReadSubscriptionService
import uk.gov.hmrc.auth.core.{Enrolment, EnrolmentIdentifier}
import uk.gov.hmrc.http.HeaderCarrier

import java.time.LocalDate
import scala.concurrent.Future

class DashboardControllerSpec extends SpecBase with ModelGenerators {

  val enrolmentsSet: Set[Enrolment] = Set(
    Enrolment(
      key = "HMRC-PILLAR2-ORG",
      identifiers = Seq(
        EnrolmentIdentifier("PLRID", "12345678"),
        EnrolmentIdentifier("UTR", "ABC12345")
      ),
      state = "activated"
    )
  )
  val validJsValue: JsValue = Json.parse("""{ "someField": "12345678" }""")
  val actionBuilders = preAuthenticatedEnrolmentActionBuilders(Some(enrolmentsSet))

  val dashBoardUserAnswers = emptyUserAnswers
    .setOrException(fmDashboardPage, DashboardInfo("org name", LocalDate.of(2025, 12, 31)))
    .setOrException(subAccountStatusPage, AccountStatus(inactive = true))

  val noDashBoardUserAnswers = emptyUserAnswers
    .setOrException(subAccountStatusPage, AccountStatus(inactive = true))

  "Dashboard Controller" should {

    "return OK and the correct view for a GET" in {
      when(mockUserAnswersConnectors.getUserAnswer(any[String])(any[HeaderCarrier]))
        .thenReturn(Future.successful(Some(dashBoardUserAnswers)))
      when(mockReadSubscriptionService.readSubscription(any[ReadSubscriptionRequestParameters])(any[HeaderCarrier]))
        .thenReturn(Future.successful(Right(validJsValue)))

      val application = applicationBuilder(userAnswers = Some(dashBoardUserAnswers))
        .overrides(
          inject.bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors),
          inject.bind[AuthenticatedIdentifierAction].toInstance(actionBuilders),
          inject.bind[ReadSubscriptionService].toInstance(mockReadSubscriptionService)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.routes.DashboardController.onPageLoad.url)
          .withSession("plrId" -> "12345678", "id" -> "12345678")
        val result = route(application, request).value

        status(result) mustEqual OK

        verify(mockReadSubscriptionService).readSubscription(any[ReadSubscriptionRequestParameters])(any[HeaderCarrier])

        verify(mockUserAnswersConnectors).getUserAnswer(any[String])(any[HeaderCarrier])

      }
    }

    "return Internal Server Error for a GET when there's an error retrieving subscription" in {

      when(mockReadSubscriptionService.readSubscription(any[ReadSubscriptionRequestParameters])(any[HeaderCarrier]))
        .thenReturn(Future.successful(Left(InternalServerError_)))

      val testConfig = Configuration("features.showErrorScreens" -> false)

      val application = applicationBuilder(userAnswers = Some(dashBoardUserAnswers))
        .configure(testConfig)
        .overrides(
          inject.bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors),
          inject.bind[AuthenticatedIdentifierAction].toInstance(actionBuilders),
          inject.bind[ReadSubscriptionService].toInstance(mockReadSubscriptionService)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.routes.DashboardController.onPageLoad.url)
          .withSession("plrId" -> "12345678", "id" -> "12345678")
        val result = route(application, request).value

        status(result) mustEqual INTERNAL_SERVER_ERROR
      }
    }

    "return SEE_OTHER and the correct view for a GET when PLR reference is missing" in {

      val application = applicationBuilder(userAnswers = Some(dashBoardUserAnswers))
        .overrides(
          inject.bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors),
          inject.bind[AuthenticatedIdentifierAction].toInstance(actionBuilders),
          inject.bind[ReadSubscriptionService].toInstance(mockReadSubscriptionService)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.routes.DashboardController.onPageLoad.url)
          .withSession("id" -> "12345678")
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result) mustBe Some(controllers.routes.JourneyRecoveryController.onPageLoad().url)
      }
    }

    "return BadRequestError for a GET when there's an error retrieving subscription" in {
      when(mockReadSubscriptionService.readSubscription(any[ReadSubscriptionRequestParameters])(any[HeaderCarrier]))
        .thenReturn(Future.successful(Left(BadRequestError)))

      val testConfig = Configuration("features.showErrorScreens" -> true)

      val application = applicationBuilder(userAnswers = Some(dashBoardUserAnswers))
        .configure(testConfig)
        .overrides(
          inject.bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors),
          inject.bind[AuthenticatedIdentifierAction].toInstance(actionBuilders),
          inject.bind[ReadSubscriptionService].toInstance(mockReadSubscriptionService)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.routes.DashboardController.onPageLoad.url)
          .withSession("plrId" -> "12345678", "id" -> "12345678")

        val result = route(application, request).value

        redirectLocation(result) mustBe Some(controllers.routes.ViewAmendSubscriptionFailedController.onPageLoad.url)

      }
    }

    "return NotFoundError for a GET when there's an error retrieving subscription" in {
      when(mockReadSubscriptionService.readSubscription(any[ReadSubscriptionRequestParameters])(any[HeaderCarrier]))
        .thenReturn(Future.successful(Left(NotFoundError)))

      val testConfig = Configuration("features.showErrorScreens" -> true)

      val application = applicationBuilder(userAnswers = Some(dashBoardUserAnswers))
        .configure(testConfig)
        .overrides(
          inject.bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors),
          inject.bind[AuthenticatedIdentifierAction].toInstance(actionBuilders),
          inject.bind[ReadSubscriptionService].toInstance(mockReadSubscriptionService)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.routes.DashboardController.onPageLoad.url)
          .withSession("plrId" -> "12345678", "id" -> "12345678")

        val result = route(application, request).value

        redirectLocation(result) mustBe Some(controllers.routes.ViewAmendSubscriptionFailedController.onPageLoad.url)

      }
    }

    "return DuplicateSubmissionError for a GET when there's an error retrieving subscription" in {
      when(mockReadSubscriptionService.readSubscription(any[ReadSubscriptionRequestParameters])(any[HeaderCarrier]))
        .thenReturn(Future.successful(Left(DuplicateSubmissionError)))

      val testConfig = Configuration("features.showErrorScreens" -> true)

      val application = applicationBuilder(userAnswers = Some(dashBoardUserAnswers))
        .configure(testConfig)
        .overrides(
          inject.bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors),
          inject.bind[AuthenticatedIdentifierAction].toInstance(actionBuilders),
          inject.bind[ReadSubscriptionService].toInstance(mockReadSubscriptionService)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.routes.DashboardController.onPageLoad.url)
          .withSession("plrId" -> "12345678", "id" -> "12345678")

        val result = route(application, request).value

        redirectLocation(result) mustBe Some(controllers.routes.ViewAmendSubscriptionFailedController.onPageLoad.url)

      }
    }

    "return UnprocessableEntityError for a GET when there's an error retrieving subscription" in {
      when(mockReadSubscriptionService.readSubscription(any[ReadSubscriptionRequestParameters])(any[HeaderCarrier]))
        .thenReturn(Future.successful(Left(UnprocessableEntityError)))

      val testConfig = Configuration("features.showErrorScreens" -> true)

      val application = applicationBuilder(userAnswers = Some(dashBoardUserAnswers))
        .configure(testConfig)
        .overrides(
          inject.bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors),
          inject.bind[AuthenticatedIdentifierAction].toInstance(actionBuilders),
          inject.bind[ReadSubscriptionService].toInstance(mockReadSubscriptionService)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.routes.DashboardController.onPageLoad.url)
          .withSession("plrId" -> "12345678", "id" -> "12345678")

        val result = route(application, request).value

        redirectLocation(result) mustBe Some(controllers.routes.ViewAmendSubscriptionFailedController.onPageLoad.url)

      }
    }

    "return InternalServerError_ for a GET when there's an error retrieving subscription when showErrorScreens is true" in {
      when(mockReadSubscriptionService.readSubscription(any[ReadSubscriptionRequestParameters])(any[HeaderCarrier]))
        .thenReturn(Future.successful(Left(InternalServerError_)))

      val testConfig = Configuration("features.showErrorScreens" -> true)

      val application = applicationBuilder(userAnswers = Some(dashBoardUserAnswers))
        .configure(testConfig)
        .overrides(
          inject.bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors),
          inject.bind[AuthenticatedIdentifierAction].toInstance(actionBuilders),
          inject.bind[ReadSubscriptionService].toInstance(mockReadSubscriptionService)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.routes.DashboardController.onPageLoad.url)
          .withSession("plrId" -> "12345678", "id" -> "12345678")

        val result = route(application, request).value

        redirectLocation(result) mustBe Some(controllers.routes.ViewAmendSubscriptionFailedController.onPageLoad.url)

      }
    }

    "return SubscriptionCreateError for a GET when there's an error retrieving subscription when showErrorScreens is true" in {
      when(mockReadSubscriptionService.readSubscription(any[ReadSubscriptionRequestParameters])(any[HeaderCarrier]))
        .thenReturn(Future.successful(Left(SubscriptionCreateError)))

      val testConfig = Configuration("features.showErrorScreens" -> true)

      val application = applicationBuilder(userAnswers = Some(dashBoardUserAnswers))
        .configure(testConfig)
        .overrides(
          inject.bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors),
          inject.bind[AuthenticatedIdentifierAction].toInstance(actionBuilders),
          inject.bind[ReadSubscriptionService].toInstance(mockReadSubscriptionService)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.routes.DashboardController.onPageLoad.url)
          .withSession("plrId" -> "12345678", "id" -> "12345678")

        val result = route(application, request).value

        redirectLocation(result) mustBe Some(controllers.routes.ViewAmendSubscriptionFailedController.onPageLoad.url)

      }
    }

    "redirect to JourneyRecoveryController when plrReference is None" in {
      when(mockReadSubscriptionService.readSubscription(any[ReadSubscriptionRequestParameters])(any[HeaderCarrier]))
        .thenReturn(Future.successful(Right(Json.obj())))

      val testConfig = Configuration("features.showErrorScreens" -> false)

      val application = applicationBuilder(userAnswers = Some(dashBoardUserAnswers))
        .configure(testConfig)
        .overrides(
          inject.bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors),
          inject.bind[AuthenticatedIdentifierAction].toInstance(actionBuilders),
          inject.bind[ReadSubscriptionService].toInstance(mockReadSubscriptionService)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.routes.DashboardController.onPageLoad.url)
          .withSession("nonExistentPlrId" -> "12345678")

        val result = route(application, request).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.JourneyRecoveryController.onPageLoad().url)

      }
    }

    "redirect to JourneyRecoveryController when getUserAnswer returns None" in {

      when(mockReadSubscriptionService.readSubscription(any[ReadSubscriptionRequestParameters])(any[HeaderCarrier]))
        .thenReturn(Future.successful(Right(Json.obj())))

      when(mockUserAnswersConnectors.getUserAnswer(any[String])(any[HeaderCarrier]))
        .thenReturn(Future.successful(None))

      val testConfig = Configuration("features.showErrorScreens" -> false)

      val application = applicationBuilder(userAnswers = None)
        .configure(testConfig)
        .overrides(
          inject.bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors),
          inject.bind[AuthenticatedIdentifierAction].toInstance(actionBuilders),
          inject.bind[ReadSubscriptionService].toInstance(mockReadSubscriptionService)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.routes.DashboardController.onPageLoad.url)
          .withSession("plrId" -> "12345678", "id" -> "12345678")
        val result = route(application, request).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.JourneyRecoveryController.onPageLoad().url)

      }
    }

    "redirect to JourneyRecoveryController when fmDashboardPage is not present in userAnswers" in {
      when(mockReadSubscriptionService.readSubscription(any[ReadSubscriptionRequestParameters])(any[HeaderCarrier]))
        .thenReturn(Future.successful(Right(Json.obj())))

      val userAnswersWithoutFmDashboardPage = UserAnswers("12345678").remove(fmDashboardPage).getOrElse(UserAnswers("12345678"))

      when(mockUserAnswersConnectors.getUserAnswer(any[String])(any[HeaderCarrier]))
        .thenReturn(Future.successful(Some(userAnswersWithoutFmDashboardPage)))

      val application = applicationBuilder(userAnswers = Some(userAnswersWithoutFmDashboardPage))
        .overrides(
          inject.bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors),
          inject.bind[ReadSubscriptionService].toInstance(mockReadSubscriptionService)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.routes.DashboardController.onPageLoad.url)
          .withSession("plrId" -> "12345678", "id" -> "12345678")
        val result = route(application, request).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.JourneyRecoveryController.onPageLoad().url)
      }
    }

  }
}
