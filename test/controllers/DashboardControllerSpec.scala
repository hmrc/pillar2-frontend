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
import models.registration.RegistrationInfo
import models.subscription.ReadSubscriptionRequestParameters
import models.{MandatoryInformationMissingError, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages.{UpeRegInformationPage, upeNameRegistrationPage}
import play.api.libs.json.Json
import play.api.mvc.ControllerHelpers.TODO.executionContext
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.Html
import uk.gov.hmrc.auth.core.{Enrolment, EnrolmentIdentifier, Enrolments}
import uk.gov.hmrc.http.HeaderCarrier
import views.html.DashboardView

import java.time.LocalDate
import scala.concurrent.Future
class DashboardControllerSpec extends SpecBase {

  val mockDashboardView                   = mock[DashboardView]
  val stubbedMessagesControllerComponents = stubMessagesControllerComponents()

  val controller = new DashboardController(
    preDataRetrievalActionImpl,
    preAuthenticatedActionBuilders,
    preDataRequiredActionImpl,
    mockReadSubscriptionService,
    stubbedMessagesControllerComponents,
    mockDashboardView
  )(executionContext, appConfig)

  "Dashboard Controller" should {
//    "return OK and the correct view for a GET" in {
//
//      val userAnswers = UserAnswers(
//        id = "some-user-id",
//        data = Json.obj(
//          upeNameRegistrationPage.toString -> Json.toJson("Test Organisation"),
//          UpeRegInformationPage.toString   -> Json.toJson(RegistrationInfo("crn", "utr", "safeId", Some(LocalDate.now()), Some(true)))
//        )
//      )
//      when(mockReadSubscriptionService.readSubscription(any[ReadSubscriptionRequestParameters])(any[HeaderCarrier]))
//        .thenReturn(Future.successful(Right(userAnswers)))
//
//      val expectedHtmlContent = "<div>Test Organisation</div>"
//      when(
//        mockDashboardView.apply(any[String], any[String], any[String])(any(), any(), any())
//      ).thenReturn(Html(expectedHtmlContent))
//
//      val result = controller.onPageLoad(FakeRequest(GET, "/dashboard"))
//
//      status(result) mustBe OK
//      contentAsString(result) must include("Test Organisation")
//
//    }

    "return OK and the correct view for a GET" in {
      val plrId = "some-plr-id"
      val userAnswers = UserAnswers(
        id = "some-user-id",
        data = Json.obj(
          upeNameRegistrationPage.toString -> Json.toJson("Test Organisation"),
          UpeRegInformationPage.toString   -> Json.toJson(RegistrationInfo("crn", "utr", "safeId", Some(LocalDate.now()), Some(true)))
        )
      )

      // Mock enrolments to include PLRID
      val enrolments = Enrolments(Set(Enrolment("HMRC-PILLAR2-ORG", Seq(EnrolmentIdentifier("PLRID", plrId)), "Activated")))

      // Mock ReadSubscriptionService
      when(mockReadSubscriptionService.readSubscription(any[ReadSubscriptionRequestParameters])(any[HeaderCarrier]))
        .thenReturn(Future.successful(Right(userAnswers)))

      // Adjust the expected HTML content based on the updated userAnswers
      val expectedHtmlContent = "<div>Test Organisation</div>"
      when(mockDashboardView.apply(any[String], any[String], any[String])(any(), any(), any()))
        .thenReturn(Html(expectedHtmlContent))

      // Create a FakeRequest with the mocked enrolments
      val fakeRequest = FakeRequest(GET, "/dashboard").withEnrolments(enrolments)

      val result = controller.onPageLoad(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) must include("Test Organisation")
    }

    "return InternalServerError when the readSubscription service fails" in {
      when(mockReadSubscriptionService.readSubscription(any[ReadSubscriptionRequestParameters])(any[HeaderCarrier]))
        .thenReturn(Future.successful(Left(MandatoryInformationMissingError("Some error"))))

      val result = controller.onPageLoad(FakeRequest(GET, "/"))

      status(result) mustBe INTERNAL_SERVER_ERROR
      contentAsString(result) must include("Subscription not found in user answers")
    }

    "return OK but with default values when user answers are incomplete" in {
      val userAnswers = UserAnswers(
        id = "some-user-id",
        data = Json.obj()
      )
      when(mockReadSubscriptionService.readSubscription(any[ReadSubscriptionRequestParameters])(any[HeaderCarrier]))
        .thenReturn(Future.successful(Right(userAnswers)))

      val result = controller.onPageLoad(FakeRequest(GET, "/"))

      status(result) mustBe OK
    }

  }
}
