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

package controllers.subscription.manageAccount

import base.SpecBase
import connectors.SubscriptionConnector
import controllers.actions.*
import models.requests.IdentifierRequest
import models.subscription.{AccountingPeriodV2, SubscriptionLocalData}
import models.{InternalIssueError, MneOrDomestic}
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{verify, when}
import pages.SubAccountingPeriodPage
import play.api.Configuration
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.mvc.*
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import services.SubscriptionService
import uk.gov.hmrc.auth.core.Enrolments
import uk.gov.hmrc.http.HeaderCarrier

import java.time.LocalDate
import scala.concurrent.{ExecutionContext, Future}

class ManageGroupDetailsCheckYourAnswersControllerSpec extends SpecBase {

  private val amendablePeriod = AccountingPeriodV2(
    startDate = LocalDate.of(2025, 1, 1),
    endDate = LocalDate.of(2025, 12, 31),
    dueDate = LocalDate.of(2026, 3, 31),
    canAmendStartDate = true,
    canAmendEndDate = true
  )

  private val microPeriod = AccountingPeriodV2(
    startDate = LocalDate.of(2024, 4, 1),
    endDate = LocalDate.of(2024, 9, 30),
    dueDate = LocalDate.of(2024, 12, 31),
    canAmendStartDate = true,
    canAmendEndDate = true
  )

  private val localDataWithPeriods: SubscriptionLocalData =
    emptySubscriptionLocalData.copy(accountingPeriods = Some(Seq(amendablePeriod, microPeriod)))

  private val localDataWithoutPeriods: SubscriptionLocalData =
    emptySubscriptionLocalData.copy(accountingPeriods = None)

  private val localDataGroupNoMicro: SubscriptionLocalData =
    emptySubscriptionLocalData.copy(accountingPeriods = Some(Seq(amendablePeriod)))

  private val localDataAgentNoMicro: SubscriptionLocalData =
    emptySubscriptionLocalData.copy(accountingPeriods = Some(Seq(amendablePeriod)), organisationName = Some("ABC Intl"))

  private val localDataWithAgentInfo: SubscriptionLocalData =
    localDataWithPeriods.copy(organisationName = Some("ABC Intl"))

  private def buildApp(
    subscriptionLocalData: Option[SubscriptionLocalData],
    multiPeriodFlag:       Boolean = false,
    isAgent:               Boolean = false
  ) =
    if isAgent then buildAgentApp(subscriptionLocalData, multiPeriodFlag)
    else
      applicationBuilder(
        subscriptionLocalData = subscriptionLocalData,
        additionalData = Map("features.amendMultipleAccountingPeriods" -> multiPeriodFlag)
      ).overrides(
        bind[SessionRepository].toInstance(mockSessionRepository),
        bind[SubscriptionService].toInstance(mockSubscriptionService),
        bind[SubscriptionConnector].toInstance(mockSubscriptionConnector)
      ).build()

  private def buildAgentApp(
    subscriptionLocalData: Option[SubscriptionLocalData],
    multiPeriodFlag:       Boolean
  ) = {
    val agentAction = new IdentifierAction {
      override def refine[A](r: Request[A]): Future[Either[Result, IdentifierRequest[A]]] =
        Future.successful(Right(IdentifierRequest(r, "id", Some("groupId"), Set.empty, isAgent = true, userIdForEnrolment = "userId")))
      override def parser:                     BodyParser[AnyContent] = injectedParsers.default
      override protected def executionContext: ExecutionContext       = ExecutionContext.Implicits.global
    }
    new GuiceApplicationBuilder()
      .configure(
        Configuration.from(
          Map(
            "metrics.enabled"                         -> "false",
            "auditing.enabled"                        -> false,
            "features.grsStubEnabled"                 -> true,
            "features.amendMultipleAccountingPeriods" -> multiPeriodFlag
          )
        )
      )
      .overrides(
        bind[Enrolments].toInstance(Enrolments(Set.empty)),
        bind[DataRequiredAction].to[DataRequiredActionImpl],
        bind[IdentifierAction].to[FakeIdentifierAction],
        bind[IdentifierAction].qualifiedWith("RfmIdentifier").to[FakeIdentifierAction],
        bind[IdentifierAction].qualifiedWith("EnrolmentIdentifier").toInstance(agentAction),
        bind[IdentifierAction].qualifiedWith("ASAEnrolmentIdentifier").to[FakeIdentifierAction],
        bind[DataRetrievalAction].toInstance(new FakeDataRetrievalAction(None)),
        bind[SubscriptionDataRetrievalAction].toInstance(new FakeSubscriptionDataRetrievalAction(subscriptionLocalData)),
        bind[SessionDataRetrievalAction].toInstance(new FakeSessionDataRetrievalAction(None)),
        bind[SessionRepository].toInstance(mockSessionRepository),
        bind[SubscriptionService].toInstance(mockSubscriptionService),
        bind[SubscriptionConnector].toInstance(mockSubscriptionConnector)
      )
      .build()
  }

  "onPageLoad" when {

    "no subscription cache is present" must {
      "redirect to Journey Recovery" in {
        val application = buildApp(subscriptionLocalData = None)
        running(application) {
          val request = FakeRequest(GET, routes.ManageGroupDetailsCheckYourAnswersController.onPageLoad().url)
          val result  = route(application, request).value
          status(result) mustEqual SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.routes.JourneyRecoveryController.onPageLoad().url)
        }
      }
    }

    "subscription cache present and sessionRepository returns None" must {
      "redirect to Journey Recovery" in {
        val application = buildApp(subscriptionLocalData = Some(emptySubscriptionLocalData))
        running(application) {
          when(mockSessionRepository.get(any())).thenReturn(Future.successful(None))
          val request = FakeRequest(GET, routes.ManageGroupDetailsCheckYourAnswersController.onPageLoad().url)
          val result  = route(application, request).value
          status(result) mustEqual SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.routes.JourneyRecoveryController.onPageLoad().url)
        }
      }
    }

    "feature flag is false" must {
      "render the single-period CYA view" in {
        val application = buildApp(subscriptionLocalData = Some(emptySubscriptionLocalData), multiPeriodFlag = false)
        running(application) {
          when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(emptyUserAnswers)))
          val request = FakeRequest(GET, routes.ManageGroupDetailsCheckYourAnswersController.onPageLoad().url)
          val result  = route(application, request).value
          status(result) mustEqual OK
          contentAsString(result) must include("Group details")
          contentAsString(result) must not include "Accounting periods"
        }
      }
    }

    "feature flag is true, group user, no micro periods" must {
      "render multi-period view with a single period card and a Change link" in {
        val application = buildApp(subscriptionLocalData = Some(localDataGroupNoMicro), multiPeriodFlag = true)
        running(application) {
          when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(emptyUserAnswers)))
          when(mockSubscriptionService.readSubscriptionV2AndSave(any(), any())(using any()))
            .thenReturn(Future.successful(localDataGroupNoMicro))
          val request = FakeRequest(GET, routes.ManageGroupDetailsCheckYourAnswersController.onPageLoad().url)
          val result  = route(application, request).value
          val body    = contentAsString(result)
          status(result) mustEqual OK
          body must include("Accounting periods")
          body must include("Current period")
          body must not include "Previous period"
          body must include("select-period/0")
        }
      }
    }

    "feature flag is true and accountingPeriods already cached" must {
      "render the multi-period view" in {
        val application = buildApp(subscriptionLocalData = Some(localDataWithPeriods), multiPeriodFlag = true)
        running(application) {
          when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(emptyUserAnswers)))
          when(mockSubscriptionService.readSubscriptionV2AndSave(any(), any())(using any()))
            .thenReturn(Future.successful(localDataWithPeriods))
          val request = FakeRequest(GET, routes.ManageGroupDetailsCheckYourAnswersController.onPageLoad().url)
          val result  = route(application, request).value
          status(result) mustEqual OK
          contentAsString(result) must include("Accounting periods")
          contentAsString(result) must include("Current period")
          contentAsString(result) must include("Previous period")
        }
      }

    }

    "feature flag is true and accountingPeriods not yet cached" must {
      "call readSubscriptionV2AndSave and render multi-period view" in {
        val application = buildApp(subscriptionLocalData = Some(localDataWithoutPeriods), multiPeriodFlag = true)
        running(application) {
          when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(emptyUserAnswers)))
          when(mockSubscriptionService.readSubscriptionV2AndSave(any(), any())(using any()))
            .thenReturn(Future.successful(localDataWithPeriods))
          val request = FakeRequest(GET, routes.ManageGroupDetailsCheckYourAnswersController.onPageLoad().url)
          val result  = route(application, request).value
          status(result) mustEqual OK
          contentAsString(result) must include("Accounting periods")
        }
      }

      "fall back to single-period view when V2 service fails" in {
        val application = buildApp(subscriptionLocalData = Some(localDataWithoutPeriods), multiPeriodFlag = true)
        running(application) {
          when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(emptyUserAnswers)))
          when(mockSubscriptionService.readSubscriptionV2AndSave(any(), any())(using any()))
            .thenReturn(Future.failed(InternalIssueError))
          val request = FakeRequest(GET, routes.ManageGroupDetailsCheckYourAnswersController.onPageLoad().url)
          val result  = route(application, request).value
          status(result) mustEqual OK
          contentAsString(result) must include("Group details")
          contentAsString(result) must not include "Accounting periods"
        }
      }
    }

    "feature flag is true and V2 returns no periods" must {
      "render empty state message" in {
        val application = buildApp(subscriptionLocalData = Some(localDataWithoutPeriods), multiPeriodFlag = true)
        running(application) {
          when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(emptyUserAnswers)))
          when(mockSubscriptionService.readSubscriptionV2AndSave(any(), any())(using any()))
            .thenReturn(Future.successful(localDataWithoutPeriods))
          val request = FakeRequest(GET, routes.ManageGroupDetailsCheckYourAnswersController.onPageLoad().url)
          val result  = route(application, request).value
          status(result) mustEqual OK
          contentAsString(result) must include("There are no accounting periods available to amend")
          contentAsString(result) must not include "select-period/"
        }
      }
    }

    "agent user" when {

      "feature flag is true and no micro periods" must {
        "render multi-period view with agent section header" in {
          val application = buildApp(subscriptionLocalData = Some(localDataAgentNoMicro), multiPeriodFlag = true, isAgent = true)
          running(application) {
            when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(emptyUserAnswers)))
            when(mockSubscriptionService.readSubscriptionV2AndSave(any(), any())(using any()))
              .thenReturn(Future.successful(localDataAgentNoMicro))
            val request = FakeRequest(GET, routes.ManageGroupDetailsCheckYourAnswersController.onPageLoad().url)
            val result  = route(application, request).value
            val body    = contentAsString(result)
            status(result) mustEqual OK
            body must include("Accounting periods")
            body must include("Current period")
            body must not include "Previous period"
            body must include("ABC Intl")
          }
        }
      }

      "feature flag is true and micro period present" must {
        "render multi-period view with both period cards and agent section header" in {
          val application = buildApp(subscriptionLocalData = Some(localDataWithAgentInfo), multiPeriodFlag = true, isAgent = true)
          running(application) {
            when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(emptyUserAnswers)))
            when(mockSubscriptionService.readSubscriptionV2AndSave(any(), any())(using any()))
              .thenReturn(Future.successful(localDataWithAgentInfo))
            val request = FakeRequest(GET, routes.ManageGroupDetailsCheckYourAnswersController.onPageLoad().url)
            val result  = route(application, request).value
            val body    = contentAsString(result)
            status(result) mustEqual OK
            body must include("Accounting periods")
            body must include("Current period")
            body must include("Previous period")
            body must include("ABC Intl")
          }
        }
      }

      "feature flag is false" must {
        "render single-period CYA view" in {
          val application =
            buildApp(subscriptionLocalData = Some(localDataWithAgentInfo.copy(accountingPeriods = None)), multiPeriodFlag = false, isAgent = true)
          running(application) {
            when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(emptyUserAnswers)))
            val request = FakeRequest(GET, routes.ManageGroupDetailsCheckYourAnswersController.onPageLoad().url)
            val result  = route(application, request).value
            val body    = contentAsString(result)
            status(result) mustEqual OK
            body must include("Group details")
            body must not include "Accounting periods"
          }
        }
      }
    }

    "location rendering" must {
      "show UK and non-UK location text when feature flag is on" in {
        val ukAndOtherData = localDataWithPeriods.copy(subMneOrDomestic = MneOrDomestic.UkAndOther)
        val application    = buildApp(subscriptionLocalData = Some(ukAndOtherData), multiPeriodFlag = true)
        running(application) {
          when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(emptyUserAnswers)))
          when(mockSubscriptionService.readSubscriptionV2AndSave(any(), any())(using any()))
            .thenReturn(Future.successful(ukAndOtherData))
          val request = FakeRequest(GET, routes.ManageGroupDetailsCheckYourAnswersController.onPageLoad().url)
          val result  = route(application, request).value
          status(result) mustEqual OK
          contentAsString(result) must include("In the UK and outside the UK")
        }
      }

      "show UK and non-UK location text when feature flag is off" in {
        val ukAndOtherData = emptySubscriptionLocalData.copy(subMneOrDomestic = MneOrDomestic.UkAndOther)
        val application    = buildApp(subscriptionLocalData = Some(ukAndOtherData), multiPeriodFlag = false)
        running(application) {
          when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(emptyUserAnswers)))
          val request = FakeRequest(GET, routes.ManageGroupDetailsCheckYourAnswersController.onPageLoad().url)
          val result  = route(application, request).value
          status(result) mustEqual OK
          contentAsString(result) must include("In the UK and outside the UK")
        }
      }

      "show UK only location text when feature flag is on" in {
        val application = buildApp(subscriptionLocalData = Some(localDataWithPeriods), multiPeriodFlag = true)
        running(application) {
          when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(emptyUserAnswers)))
          when(mockSubscriptionService.readSubscriptionV2AndSave(any(), any())(using any()))
            .thenReturn(Future.successful(localDataWithPeriods))
          val request = FakeRequest(GET, routes.ManageGroupDetailsCheckYourAnswersController.onPageLoad().url)
          val result  = route(application, request).value
          status(result) mustEqual OK
          contentAsString(result) must include("Only in the UK")
        }
      }

      "show UK only location text when feature flag is off" in {
        val application = buildApp(subscriptionLocalData = Some(emptySubscriptionLocalData), multiPeriodFlag = false)
        running(application) {
          when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(emptyUserAnswers)))
          val request = FakeRequest(GET, routes.ManageGroupDetailsCheckYourAnswersController.onPageLoad().url)
          val result  = route(application, request).value
          status(result) mustEqual OK
          contentAsString(result) must include("Only in the UK")
        }
      }
    }

  }

  "selectPeriod" when {

    "a valid index is provided" must {
      "save the selected period to cache and redirect to GroupAccountingPeriodController" in {
        val application = buildApp(subscriptionLocalData = Some(localDataWithPeriods), multiPeriodFlag = true)
        running(application) {
          val expectedUpdated = localDataWithPeriods
            .setOrException(SubAccountingPeriodPage, amendablePeriod.toAccountingPeriod)
          when(mockSubscriptionConnector.save(eqTo("id"), any())(using any()))
            .thenReturn(Future.successful(Json.toJson(expectedUpdated)))

          val request = FakeRequest(GET, routes.ManageGroupDetailsCheckYourAnswersController.selectPeriod(0).url)
          val result  = route(application, request).value
          status(result) mustEqual SEE_OTHER
          redirectLocation(result) mustBe Some(
            controllers.subscription.manageAccount.routes.GroupAccountingPeriodController.onPageLoad().url
          )
          verify(mockSubscriptionConnector).save(eqTo("id"), any())(using any[HeaderCarrier])
        }
      }
    }

    "an out-of-range index is provided" must {
      "redirect to Journey Recovery" in {
        val application = buildApp(subscriptionLocalData = Some(localDataWithPeriods), multiPeriodFlag = true)
        running(application) {
          val request = FakeRequest(GET, routes.ManageGroupDetailsCheckYourAnswersController.selectPeriod(99).url)
          val result  = route(application, request).value
          status(result) mustEqual SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.routes.JourneyRecoveryController.onPageLoad().url)
        }
      }
    }

    "accountingPeriods is empty" must {
      "redirect to Journey Recovery" in {
        val application = buildApp(subscriptionLocalData = Some(emptySubscriptionLocalData), multiPeriodFlag = true)
        running(application) {
          val request = FakeRequest(GET, routes.ManageGroupDetailsCheckYourAnswersController.selectPeriod(0).url)
          val result  = route(application, request).value
          status(result) mustEqual SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.routes.JourneyRecoveryController.onPageLoad().url)
        }
      }
    }
  }
}
