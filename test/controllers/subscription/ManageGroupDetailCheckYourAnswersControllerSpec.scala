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

package controllers.subscription

import base.SpecBase
import connectors.UserAnswersConnectors
import models.subscription.{AccountingPeriod, AmendSubscriptionRequestParameters, DashboardInfo}
import models.{ApiError, MneOrDomestic, NonUKAddress, SubscriptionCreateError, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages._
import play.api.inject
import play.api.inject.bind
import play.api.libs.json.{JsValue, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.AmendSubscriptionService
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import viewmodels.govuk.SummaryListFluency

import java.time.LocalDate
import scala.concurrent.Future

class ManageGroupDetailCheckYourAnswersControllerSpec extends SpecBase with SummaryListFluency {

  "Subscription Check Your Answers Controller" must {

    val startDate = LocalDate.of(2023, 12, 31)
    val endDate   = LocalDate.of(2025, 12, 31)
    val date      = AccountingPeriod(startDate, endDate)

    val amendSubUserAnswers = emptyUserAnswers
      .setOrException(UpeRegisteredInUKPage, true)
      .setOrException(UpeNameRegistrationPage, "International Organisation Inc.")
      .setOrException(SubPrimaryContactNamePage, "Name")
      .setOrException(SubPrimaryEmailPage, "email@email.com")
      .setOrException(SubPrimaryPhonePreferencePage, true)
      .setOrException(SubPrimaryCapturePhonePage, "123456789")
      .setOrException(SubAddSecondaryContactPage, true)
      .setOrException(SubSecondaryContactNamePage, "second contact name")
      .setOrException(SubSecondaryEmailPage, "second@email.com")
      .setOrException(subSecondaryPhonePreferencePage, true)
      .setOrException(SubSecondaryCapturePhonePage, "123456789")
      .setOrException(SubRegisteredAddressPage, NonUKAddress("this", None, "over", None, None, countryCode = "AR"))
      .setOrException(SubMneOrDomesticPage, MneOrDomestic.Uk)
      .setOrException(SubAccountingPeriodPage, date)
      .setOrException(FmDashboardPage, DashboardInfo("org name", LocalDate.of(2025, 12, 31)))
      .setOrException(NominateFilingMemberPage, false)

    "return OK and the correct view if an answer is provided to every question " in {
      val userAnswer = UserAnswers(userAnswersId)
        .set(SubMneOrDomesticPage, MneOrDomestic.Uk)
        .success
        .value
        .set(SubAccountingPeriodPage, date)
        .success
        .value
      val application = applicationBuilder(userAnswers = Some(userAnswer)).build()
      running(application) {
        val request = FakeRequest(GET, controllers.subscription.manageAccount.routes.ManageGroupDetailsCheckYourAnswersController.onPageLoad.url)
        val result  = route(application, request).value
        status(result) mustEqual OK
        contentAsString(result) must include("Check your answer")
        contentAsString(result) must include("Where does the group operate?")
      }
    }

    "return OK and the correct view if an answer is provided to every question when UkAndOther  option is selected  " in {
      val userAnswer = UserAnswers(userAnswersId)
        .set(SubMneOrDomesticPage, MneOrDomestic.UkAndOther)
        .success
        .value
        .set(SubAccountingPeriodPage, date)
        .success
        .value
      val application = applicationBuilder(userAnswers = Some(userAnswer)).build()
      running(application) {
        val request = FakeRequest(GET, controllers.subscription.manageAccount.routes.ManageGroupDetailsCheckYourAnswersController.onPageLoad.url)
        val result  = route(application, request).value
        status(result) mustEqual OK
        contentAsString(result) must include("Check your answer")
        contentAsString(result) must include("Where does the group operate?")
      }
    }

    "trigger amend subscription API if all data is available for group accounting period" in {
      val application = applicationBuilder(userAnswers = Some(amendSubUserAnswers))
        .overrides(inject.bind[AmendSubscriptionService].toInstance(mockAmendSubscriptionService))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .build()
      running(application) {
        val mockHttpResponse = HttpResponse(OK, "")
        when(mockUserAnswersConnectors.remove(any())(any())).thenReturn(Future.successful(mockHttpResponse))
        val mockResponse = Json.parse("""{"success":{"processingDate":"2022-01-31T09:26:17Z","formBundleNumber":"119000004320"}}""")
        when(mockAmendSubscriptionService.amendSubscription(AmendSubscriptionRequestParameters(any()))(any()))
          .thenReturn(Future.successful(Right(mockResponse)))

        val request = FakeRequest(POST, controllers.subscription.manageAccount.routes.ManageGroupDetailsCheckYourAnswersController.onSubmit.url)
        val result  = route(application, request).value
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some("/report-pillar2-top-up-taxes/pillar2-top-up-tax-home")
      }
    }

    "redirect to journey recovery if no data if no data is found for group accounting period" in {
      val mockAmendSubscriptionService = mock[AmendSubscriptionService]
      val application = applicationBuilder(userAnswers = None)
        .overrides(inject.bind[AmendSubscriptionService].toInstance(mockAmendSubscriptionService))
        .build()

      val mockResponse: Either[ApiError, JsValue] = Left(SubscriptionCreateError)
      when(mockAmendSubscriptionService.amendSubscription(any[AmendSubscriptionRequestParameters])(any[HeaderCarrier]))
        .thenReturn(Future.successful(mockResponse))

      val request = FakeRequest(POST, controllers.subscription.manageAccount.routes.ManageGroupDetailsCheckYourAnswersController.onSubmit.url)
      val result  = route(application, request).value

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.ViewAmendSubscriptionFailedController.onPageLoad.url)
    }

  }
}
