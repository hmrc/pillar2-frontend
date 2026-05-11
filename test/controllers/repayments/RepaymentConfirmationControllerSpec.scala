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
import models.subscription.{AccountingPeriodV2, SubscriptionDataV2}
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

  "Repayment confirmation controller" when {

    "must return OK and the correct view for a GET" when {

      "amendMultipleAccountingPeriods is disabled (v1)" in {
        val currentDateTimeGMT: String      = ZonedDateTime.now().toDateTimeGmtFormat
        val testUserAnswers:    UserAnswers = emptyUserAnswers
          .setOrException(RepaymentCompletionStatus, true)
          .setOrException(RepaymentConfirmationPage, currentDateTimeGMT)
          .setOrException(PlrReferencePage, PlrReference)

        val application = applicationBuilder(
          userAnswers = Some(testUserAnswers),
          additionalData = Map("features.amendMultipleAccountingPeriods" -> false)
        ).overrides(bind[SubscriptionConnector].toInstance(mockSubscriptionConnector))
          .build()

        running(application) {
          when(mockSubscriptionConnector.readSubscription(any())(using any[HeaderCarrier], any[ExecutionContext]))
            .thenReturn(Future.successful(Some(subscriptionData)))

          val request = FakeRequest(GET, controllers.repayments.routes.RepaymentConfirmationController.onPageLoad().url)
          val result  = route(application, request).value
          val view    = application.injector.instanceOf[RepaymentsConfirmationView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(
            currentDateTimeGMT,
            PlrReference,
            subscriptionData.upeDetails.organisationName,
            false
          )(request, applicationConfig, messages(application)).toString
        }
      }

      "amendMultipleAccountingPeriods is enabled (v2)" in {
        val currentDateTimeGMT: String      = ZonedDateTime.now().toDateTimeGmtFormat
        val testUserAnswers:    UserAnswers = emptyUserAnswers
          .setOrException(RepaymentCompletionStatus, true)
          .setOrException(RepaymentConfirmationPage, currentDateTimeGMT)
          .setOrException(PlrReferencePage, PlrReference)

        val v2Data = SubscriptionDataV2(
          formBundleNumber = "form bundle",
          upeDetails = subscriptionData.upeDetails,
          upeCorrespAddressDetails = subscriptionData.upeCorrespAddressDetails,
          primaryContactDetails = subscriptionData.primaryContactDetails,
          secondaryContactDetails = subscriptionData.secondaryContactDetails,
          filingMemberDetails = subscriptionData.filingMemberDetails,
          accountingPeriod = Seq(
            AccountingPeriodV2(
              startDate = LocalDate.of(2024, 1, 6),
              endDate = LocalDate.of(2025, 4, 6),
              dueDate = LocalDate.of(2024, 4, 6),
              canAmendStartDate = true,
              canAmendEndDate = true
            )
          ),
          accountStatus = subscriptionData.accountStatus
        )

        val application = applicationBuilder(
          userAnswers = Some(testUserAnswers),
          additionalData = Map("features.amendMultipleAccountingPeriods" -> true)
        ).overrides(bind[SubscriptionConnector].toInstance(mockSubscriptionConnector))
          .build()

        running(application) {
          when(mockSubscriptionConnector.readSubscriptionV2(any())(using any[HeaderCarrier], any[ExecutionContext]))
            .thenReturn(Future.successful(Some(v2Data)))

          val request = FakeRequest(GET, controllers.repayments.routes.RepaymentConfirmationController.onPageLoad().url)
          val result  = route(application, request).value
          val view    = application.injector.instanceOf[RepaymentsConfirmationView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(
            currentDateTimeGMT,
            PlrReference,
            v2Data.upeDetails.organisationName,
            false
          )(request, applicationConfig, messages(application)).toString
        }
      }
    }

    "must redirect to recovery page when the user attempts to access the page before completing journey" in {
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
