/*
 * Copyright 2026 HM Revenue & Customs
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

package controllers.repayments

import base.SpecBase
import connectors.SubscriptionConnector
import models.UserAnswers
import models.subscription.{AccountingPeriodV2, SubscriptionDataDisplay}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages.{PlrReferencePage, RepaymentCompletionStatus, RepaymentConfirmationPage}
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import uk.gov.hmrc.http.HeaderCarrier
import utils.DateTimeUtils.*
import views.html.repayments.RepaymentsConfirmationView

import java.time.{LocalDate, ZonedDateTime}
import scala.concurrent.{ExecutionContext, Future}

class RepaymentConfirmationControllerSpec extends SpecBase {

  "Repayment confirmation controller" must {

    "return OK and the correct view for a GET" in {
      val currentDateTimeGMT: String      = ZonedDateTime.now().toDateTimeGmtFormat
      val testUserAnswers:    UserAnswers = emptyUserAnswers
        .setOrException(RepaymentCompletionStatus, true)
        .setOrException(RepaymentConfirmationPage, currentDateTimeGMT)
        .setOrException(PlrReferencePage, testPillar2Id)

      val subscriptionData = SubscriptionDataDisplay(
        formBundleNumber = "form bundle",
        upeDetails = subscriptionDataDisplay.upeDetails,
        upeCorrespAddressDetails = subscriptionDataDisplay.upeCorrespAddressDetails,
        primaryContactDetails = subscriptionDataDisplay.primaryContactDetails,
        secondaryContactDetails = subscriptionDataDisplay.secondaryContactDetails,
        filingMemberDetails = subscriptionDataDisplay.filingMemberDetails,
        accountingPeriod = Some(
          Seq(
            AccountingPeriodV2(
              startDate = Some(LocalDate.of(2024, 1, 6)),
              endDate = Some(LocalDate.of(2025, 4, 6)),
              dueDate = Some(LocalDate.of(2024, 4, 6)),
              canAmendStartDate = Some(true),
              canAmendEndDate = Some(true)
            )
          )
        ),
        accountStatus = subscriptionDataDisplay.accountStatus
      )

      val application = applicationBuilder(userAnswers = Some(testUserAnswers))
        .overrides(bind[SubscriptionConnector].toInstance(mockSubscriptionConnector))
        .build()

      running(application) {
        when(mockSubscriptionConnector.readSubscriptionV2(any())(using any[HeaderCarrier], any[ExecutionContext]))
          .thenReturn(Future.successful(Some(subscriptionData)))

        val request = FakeRequest(GET, controllers.repayments.routes.RepaymentConfirmationController.onPageLoad().url)
        val result  = route(application, request).value
        val view    = application.injector.instanceOf[RepaymentsConfirmationView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          currentDateTimeGMT,
          testPillar2Id,
          subscriptionData.upeDetails.organisationName,
          false
        )(request, applicationConfig, messages(application)).toString
      }
    }

    "redirect to recovery page when user attempts to access the page before completing journey" in {
      val testUserAnswers = emptyUserAnswers.setOrException(RepaymentCompletionStatus, false)
      val application     = applicationBuilder(userAnswers = Some(testUserAnswers))
        .overrides(bind[SubscriptionConnector].toInstance(mockSubscriptionConnector))
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.repayments.routes.RepaymentConfirmationController.onPageLoad().url)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.JourneyRecoveryController.onPageLoad().url)
      }
    }
  }
}
