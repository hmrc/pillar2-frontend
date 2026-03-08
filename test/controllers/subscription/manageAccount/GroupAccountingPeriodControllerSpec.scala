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

package controllers.subscription.manageAccount

import base.SpecBase
import connectors.SubscriptionConnector
import controllers.actions.TestAuthRetrievals.~
import forms.GroupAccountingPeriodFormProvider
import models.MneOrDomestic
import models.subscription.{AccountingPeriod, AccountingPeriodDisplay}
import navigation.AmendSubscriptionNavigator
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{verify, when}
import pages.{SubAccountingPeriodPage, SubMneOrDomesticPage}
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import uk.gov.hmrc.auth.core.AffinityGroup.Agent
import uk.gov.hmrc.auth.core.retrieve.Credentials
import uk.gov.hmrc.auth.core.{AuthConnector, User}
import uk.gov.hmrc.http.HeaderCarrier
import views.html.subscriptionview.manageAccount.GroupAccountingPeriodView

import java.time.LocalDate
import java.util.UUID
import scala.concurrent.Future

class GroupAccountingPeriodControllerSpec extends SpecBase {

  val formProvider = new GroupAccountingPeriodFormProvider()
  val startDate:    LocalDate = LocalDate.of(2023, 12, 31)
  val endDate:      LocalDate = LocalDate.of(2025, 12, 31)
  val id:           String    = UUID.randomUUID().toString
  val providerId:   String    = UUID.randomUUID().toString
  val providerType: String    = UUID.randomUUID().toString

  "GroupAccountingPeriod Controller for Organisation View Contact details" when {

    "must return OK and the correct view for a GET if no previous filled data is found" in {
      val ua = emptyUserAnswers.setOrException(SubMneOrDomesticPage, MneOrDomestic.Uk)

      val application = applicationBuilder(Some(ua)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.subscription.manageAccount.routes.GroupAccountingPeriodController.onPageLoad().url)
        val result  = route(application, request).value

        val view = application.injector.instanceOf[GroupAccountingPeriodView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider(true), isAgent = false, organisationName = None)(
          request,
          applicationConfig,
          messages(application)
        ).toString
      }
    }

    "must return OK and the correct view for a GET if previous data is found" in {
      val ua = emptySubscriptionLocalData.setOrException(SubMneOrDomesticPage, MneOrDomestic.Uk)

      val application = applicationBuilder(subscriptionLocalData = Some(ua)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.subscription.manageAccount.routes.GroupAccountingPeriodController.onPageLoad().url)
        val result  = route(application, request).value

        val view = application.injector.instanceOf[GroupAccountingPeriodView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          formProvider(true).fill(emptySubscriptionLocalData.subAccountingPeriod),
          isAgent = false,
          organisationName = None
        )(
          request,
          applicationConfig,
          messages(application)
        ).toString
      }
    }

    "must return OK and the correct view for a GET if page has previously been answered" in {

      val date = AccountingPeriod(startDate, endDate)
      val ua   = emptySubscriptionLocalData.setOrException(SubAccountingPeriodPage, date).setOrException(SubMneOrDomesticPage, MneOrDomestic.Uk)
      val application = applicationBuilder(subscriptionLocalData = Some(ua)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.subscription.manageAccount.routes.GroupAccountingPeriodController.onPageLoad().url)
        val result  = route(application, request).value

        val view = application.injector.instanceOf[GroupAccountingPeriodView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider(true).fill(date), isAgent = false, organisationName = None)(
          request,
          applicationConfig,
          messages(application)
        ).toString
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(subscriptionLocalData = Some(emptySubscriptionLocalData)).build()

      val request =
        FakeRequest(POST, controllers.subscription.manageAccount.routes.GroupAccountingPeriodController.onSubmit().url)
          .withFormUrlEncodedBody(("value", "invalid value"))

      running(application) {
        val boundForm = formProvider(true).bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[GroupAccountingPeriodView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, isAgent = false, organisationName = None)(
          request,
          applicationConfig,
          messages(application)
        ).toString
      }
    }

    "must update subscription data and redirect to the next page" in {
      import play.api.inject.bind

      val expectedNextPage = Call(GET, "/")
      val mockNavigator    = mock[AmendSubscriptionNavigator]
      when(mockNavigator.nextPage(any(), any())).thenReturn(expectedNextPage)
      when(mockSubscriptionConnector.save(any(), any())(using any())).thenReturn(Future.successful(Json.obj()))
      val someDate    = LocalDate.of(2024, 1, 1)
      val userAnswers = emptySubscriptionLocalData

      val expectedUserAnswers = userAnswers.setOrException(SubAccountingPeriodPage, AccountingPeriod(someDate, someDate.plusMonths(5), None))

      val application = applicationBuilder(subscriptionLocalData = Some(userAnswers))
        .overrides(
          bind[AmendSubscriptionNavigator].toInstance(mockNavigator),
          bind[SubscriptionConnector].toInstance(mockSubscriptionConnector)
        )
        .build()

      running(application) {
        val request = FakeRequest(POST, controllers.subscription.manageAccount.routes.GroupAccountingPeriodController.onSubmit().url)
          .withFormUrlEncodedBody(
            "startDate.day"   -> "1",
            "startDate.month" -> "1",
            "startDate.year"  -> "2024",
            "endDate.day"     -> "1",
            "endDate.month"   -> "6",
            "endDate.year"    -> "2024"
          )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual expectedNextPage.url
        verify(mockSubscriptionConnector).save(eqTo("id"), eqTo(Json.toJson(expectedUserAnswers)))(using any[HeaderCarrier])
        verify(mockNavigator).nextPage(SubAccountingPeriodPage, expectedUserAnswers)
      }
    }

    "must pre-fill form from DisplaySubscriptionV2Selected session and pass startDateReadOnly when canAmendStartDate is false" in {
      val selectedJson = Json
        .obj(
          "startDate"         -> "2021-09-28",
          "endDate"           -> "2022-09-27",
          "canAmendStartDate" -> false
        )
        .toString
      val sessionData = Map(
        controllers.subscription.manageAccount.ManageAccountV2SessionKeys.DisplaySubscriptionV2Selected -> selectedJson
      )
      val ua          = emptySubscriptionLocalData.setOrException(SubMneOrDomesticPage, MneOrDomestic.Uk)
      val application = applicationBuilder(subscriptionLocalData = Some(ua)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.subscription.manageAccount.routes.GroupAccountingPeriodController.onPageLoad().url)
          .withSession(sessionData.toSeq*)
        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) must include("The start date of the first accounting period cannot be changed")
      }
    }

    "must redirect to ConfirmNewAccountingPeriodController when from multi-period flow and valid form" in {
      val periodsJson = Json
        .toJson(
          Seq(
            AccountingPeriodDisplay(
              startDate = LocalDate.of(2022, 9, 28),
              endDate = LocalDate.of(2023, 9, 27),
              dueDate = LocalDate.of(2023, 10, 27),
              canAmendStartDate = true,
              canAmendEndDate = true
            )
          )
        )
        .toString
      val sessionData = Map(
        controllers.subscription.manageAccount.ManageAccountV2SessionKeys.DisplaySubscriptionV2Periods -> periodsJson
      )
      val userAnswers = emptySubscriptionLocalData.setOrException(SubMneOrDomesticPage, MneOrDomestic.Uk)
      when(mockSubscriptionConnector.save(any(), any())(using any())).thenReturn(Future.successful(Json.obj()))
      val application = applicationBuilder(subscriptionLocalData = Some(userAnswers))
        .overrides(bind[SubscriptionConnector].toInstance(mockSubscriptionConnector))
        .build()

      running(application) {
        val request = FakeRequest(POST, controllers.subscription.manageAccount.routes.GroupAccountingPeriodController.onSubmit().url)
          .withSession(sessionData.toSeq*)
          .withFormUrlEncodedBody(
            "startDate.day"   -> "1",
            "startDate.month" -> "1",
            "startDate.year"  -> "2024",
            "endDate.day"     -> "1",
            "endDate.month"   -> "12",
            "endDate.year"    -> "2024"
          )
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.subscription.manageAccount.routes.ConfirmNewAccountingPeriodController.onPageLoad().url
      }
    }

  }

  "GroupAccountingPeriod Controller for Agent View Contact details" when {

    "must return OK and the correct view for a GET if no previous data is found" in {
      val ua          = emptySubscriptionLocalData.setOrException(SubMneOrDomesticPage, MneOrDomestic.Uk)
      val application = applicationBuilder(subscriptionLocalData = Some(ua))
        .overrides(bind[AuthConnector].toInstance(mockAuthConnector))
        .build()
      when(mockAuthConnector.authorise[AgentRetrievalsType](any(), any())(using any(), any()))
        .thenReturn(
          Future.successful(
            Some(id) ~ pillar2AgentEnrolment ~ Some(Agent) ~ Some(User) ~ Some(Credentials(providerId, providerType))
          )
        )

      running(application) {
        val request = FakeRequest(
          GET,
          controllers.subscription.manageAccount.routes.GroupAccountingPeriodController.onPageLoad().url
        )
        val result = route(application, request).value
        val view   = application.injector.instanceOf[GroupAccountingPeriodView]
        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          formProvider(true).fill(emptySubscriptionLocalData.subAccountingPeriod),
          isAgent = false,
          organisationName = None
        )(
          request,
          applicationConfig,
          messages(application)
        ).toString
      }
    }

    "must return OK and the correct view for a GET if page has previously been answered" in {

      val date = AccountingPeriod(startDate, endDate)
      val ua   = emptySubscriptionLocalData.setOrException(SubAccountingPeriodPage, date).setOrException(SubMneOrDomesticPage, MneOrDomestic.Uk)
      val application = applicationBuilder(subscriptionLocalData = Some(ua))
        .overrides(bind[AuthConnector].toInstance(mockAuthConnector))
        .build()
      when(mockAuthConnector.authorise[AgentRetrievalsType](any(), any())(using any(), any()))
        .thenReturn(
          Future.successful(
            Some(id) ~ pillar2AgentEnrolment ~ Some(Agent) ~ Some(User) ~ Some(Credentials(providerId, providerType))
          )
        )

      running(application) {
        val request = FakeRequest(
          GET,
          controllers.subscription.manageAccount.routes.GroupAccountingPeriodController.onPageLoad().url
        )
        val result = route(application, request).value
        val view   = application.injector.instanceOf[GroupAccountingPeriodView]
        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider(true).fill(date), isAgent = false, organisationName = None)(
          request,
          applicationConfig,
          messages(application)
        ).toString
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(subscriptionLocalData = Some(emptySubscriptionLocalData))
        .overrides(bind[AuthConnector].toInstance(mockAuthConnector))
        .build()
      when(mockAuthConnector.authorise[AgentRetrievalsType](any(), any())(using any(), any()))
        .thenReturn(
          Future.successful(
            Some(id) ~ pillar2AgentEnrolment ~ Some(Agent) ~ Some(User) ~ Some(Credentials(providerId, providerType))
          )
        )

      running(application) {
        val request =
          FakeRequest(
            POST,
            controllers.subscription.manageAccount.routes.GroupAccountingPeriodController.onSubmit().url
          )
            .withFormUrlEncodedBody(("value", "invalid value"))
        val boundForm = formProvider(true).bind(Map("value" -> "invalid value"))
        val view      = application.injector.instanceOf[GroupAccountingPeriodView]
        val result    = route(application, request).value
        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, isAgent = false, organisationName = None)(
          request,
          applicationConfig,
          messages(application)
        ).toString
      }
    }

    "must update subscription data and redirect to the next page" in {
      import play.api.inject.bind
      val expectedNextPage = Call(GET, "/")
      val mockNavigator    = mock[AmendSubscriptionNavigator]
      when(mockNavigator.nextPage(any(), any())).thenReturn(expectedNextPage)
      when(mockSubscriptionConnector.save(any(), any())(using any())).thenReturn(Future.successful(Json.obj()))
      val someDate            = LocalDate.of(2024, 1, 1)
      val userAnswers         = emptySubscriptionLocalData
      val expectedUserAnswers = userAnswers.setOrException(SubAccountingPeriodPage, AccountingPeriod(someDate, someDate.plusMonths(5), None))
      val application         = applicationBuilder(subscriptionLocalData = Some(userAnswers))
        .overrides(
          bind[AmendSubscriptionNavigator].toInstance(mockNavigator),
          bind[SubscriptionConnector].toInstance(mockSubscriptionConnector),
          bind[AuthConnector].toInstance(mockAuthConnector)
        )
        .build()
      when(mockAuthConnector.authorise[AgentRetrievalsType](any(), any())(using any(), any()))
        .thenReturn(
          Future.successful(
            Some(id) ~ pillar2AgentEnrolment ~ Some(Agent) ~ Some(User) ~ Some(Credentials(providerId, providerType))
          )
        )

      running(application) {
        val request = FakeRequest(
          POST,
          controllers.subscription.manageAccount.routes.GroupAccountingPeriodController.onSubmit().url
        )
          .withFormUrlEncodedBody(
            "startDate.day"   -> "1",
            "startDate.month" -> "1",
            "startDate.year"  -> "2024",
            "endDate.day"     -> "1",
            "endDate.month"   -> "6",
            "endDate.year"    -> "2024"
          )
        val result = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual expectedNextPage.url
        verify(mockSubscriptionConnector).save(eqTo("id"), eqTo(Json.toJson(expectedUserAnswers)))(using any[HeaderCarrier])
        verify(mockNavigator).nextPage(SubAccountingPeriodPage, expectedUserAnswers)
      }
    }

  }
}
