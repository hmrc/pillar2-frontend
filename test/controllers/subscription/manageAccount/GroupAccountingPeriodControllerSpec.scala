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
import models.subscription.AccountingPeriod
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
          formProvider(true).fill(emptySubscriptionLocalData.subAccountingPeriod.get),
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
          formProvider(true).fill(emptySubscriptionLocalData.subAccountingPeriod.get),
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

    "must update the matching entry in accountingPeriods when subAccountingPeriod dates match" in {
      import models.subscription.DisplayAccountingPeriod
      import play.api.inject.bind

      val originalStart = LocalDate.of(2024, 1, 1)
      val originalEnd   = LocalDate.of(2024, 12, 31)
      val newStart      = LocalDate.of(2024, 2, 1)
      val newEnd        = LocalDate.of(2024, 11, 30)

      val period1 = DisplayAccountingPeriod(originalStart, originalEnd, LocalDate.of(2025, 3, 31), canAmendStartDate = true, canAmendEndDate = true)
      val period2 = DisplayAccountingPeriod(
        LocalDate.of(2023, 1, 1),
        LocalDate.of(2023, 12, 31),
        LocalDate.of(2024, 3, 31),
        canAmendStartDate = true,
        canAmendEndDate = true
      )

      val localDataWithOldPeriod = emptySubscriptionLocalData
        .copy(accountingPeriods = Some(Seq(period1, period2)))
        .setOrException(SubAccountingPeriodPage, AccountingPeriod(originalStart, originalEnd))

      val expectedUpdated = localDataWithOldPeriod
        .setOrException(SubAccountingPeriodPage, AccountingPeriod(newStart, newEnd))
        .copy(accountingPeriods = Some(Seq(period1.copy(startDate = newStart, endDate = newEnd), period2)))

      val expectedNextPage = Call(GET, "/next")
      val mockNavigator    = mock[AmendSubscriptionNavigator]
      when(mockNavigator.nextPage(any(), any())).thenReturn(expectedNextPage)
      when(mockSubscriptionConnector.save(any(), any())(using any())).thenReturn(Future.successful(Json.obj()))

      val application = applicationBuilder(subscriptionLocalData = Some(localDataWithOldPeriod))
        .overrides(
          bind[AmendSubscriptionNavigator].toInstance(mockNavigator),
          bind[SubscriptionConnector].toInstance(mockSubscriptionConnector)
        )
        .build()

      running(application) {
        val request = FakeRequest(POST, controllers.subscription.manageAccount.routes.GroupAccountingPeriodController.onSubmit().url)
          .withFormUrlEncodedBody(
            "startDate.day"   -> "1",
            "startDate.month" -> "2",
            "startDate.year"  -> "2024",
            "endDate.day"     -> "30",
            "endDate.month"   -> "11",
            "endDate.year"    -> "2024"
          )
        val result = route(application, request).value
        status(result) mustEqual SEE_OTHER
        verify(mockSubscriptionConnector).save(eqTo("id"), eqTo(Json.toJson(expectedUpdated)))(using any[HeaderCarrier])
      }
    }

    "must not modify accountingPeriods when no subAccountingPeriod is set" in {
      import play.api.inject.bind

      val newStart      = LocalDate.of(2024, 2, 1)
      val newEnd        = LocalDate.of(2024, 11, 30)
      val expectedNext  = Call(GET, "/next")
      val mockNavigator = mock[AmendSubscriptionNavigator]
      when(mockNavigator.nextPage(any(), any())).thenReturn(expectedNext)
      when(mockSubscriptionConnector.save(any(), any())(using any())).thenReturn(Future.successful(Json.obj()))

      val expectedSaved = emptySubscriptionLocalData.setOrException(SubAccountingPeriodPage, AccountingPeriod(newStart, newEnd))

      val application = applicationBuilder(subscriptionLocalData = Some(emptySubscriptionLocalData))
        .overrides(
          bind[AmendSubscriptionNavigator].toInstance(mockNavigator),
          bind[SubscriptionConnector].toInstance(mockSubscriptionConnector)
        )
        .build()

      running(application) {
        val request = FakeRequest(POST, controllers.subscription.manageAccount.routes.GroupAccountingPeriodController.onSubmit().url)
          .withFormUrlEncodedBody(
            "startDate.day"   -> "1",
            "startDate.month" -> "2",
            "startDate.year"  -> "2024",
            "endDate.day"     -> "30",
            "endDate.month"   -> "11",
            "endDate.year"    -> "2024"
          )
        val result = route(application, request).value
        status(result) mustEqual SEE_OTHER
        verify(mockSubscriptionConnector).save(eqTo("id"), eqTo(Json.toJson(expectedSaved)))(using any[HeaderCarrier])
      }
    }

  }
}
