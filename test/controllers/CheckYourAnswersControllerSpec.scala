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
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages._
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import viewmodels.govuk.SummaryListFluency

import scala.concurrent.Future

class CheckYourAnswersControllerSpec extends SpecBase with SummaryListFluency {
  def controller(): CheckYourAnswersController =
    new CheckYourAnswersController(
      mockUserAnswersConnectors,
      preAuthenticatedActionBuilders,
      preDataRetrievalActionImpl,
      preDataRequiredActionImpl,
      stubMessagesControllerComponents(),
      viewCheckYourAnswers,
      viewpageNotAvailable,
      mockCountryOptions
    )

  "Check Your Answers Controller" must {

    "must return Not Found and the correct view with empty user answers" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
      try running(application) {
        val request = FakeRequest(GET, controllers.subscription.routes.ContactCheckYourAnswersController.onPageLoad.url)
        val result  = route(application, request).value
        status(result) mustEqual NOT_FOUND
      } catch {
        case x: java.lang.Exception =>
          x.getMessage mustEqual "Subscription data not available"
      }
    }
    "must return OK and the correct view if an answer is provided to every question  with Secondary contact detail" in {

      val contactAnswer = emptyUserAnswers
        .set(
          SubscriptionPage,
          CheckAnswerwithSecondaryContactDetail()
        )
        .success
        .value

      val contactNfmAnswer = contactAnswer.set(NominatedFilingMemberPage, nfmCheckAnswerData()).success.value

      val contactUpeNfmAnswer = contactNfmAnswer.set(RegistrationPage, upeCheckAnswerDataWithoutPhone).success.value

      val application = applicationBuilder(userAnswers = Some(contactUpeNfmAnswer)).build()
      when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))
      running(application) {
        val request = FakeRequest(GET, controllers.routes.CheckYourAnswersController.onPageLoad.url)
        val result  = route(application, request).value
        status(result) mustEqual OK
        contentAsString(result) must include(
          "Ultimate parent"
        )
        contentAsString(result) must include(
          "Nominated filing member"
        )
        contentAsString(result) must include(
          "First contact"
        )
        contentAsString(result) must include(
          "Second contact"
        )
      }
    }

    "must return OK and the correct view if an answer is provided to every question  with no nominate filing member" in {

      val contactAnswer = emptyUserAnswers
        .set(
          SubscriptionPage,
          CheckAnswerwithSecondaryContactDetail()
        )
        .success
        .value

      val contactNfmAnswer = contactAnswer.set(NominatedFilingMemberPage, nfmCheckAnswerDataNoNominateNfm()).success.value

      val contactUpeNfmAnswer = contactNfmAnswer.set(RegistrationPage, upeCheckAnswerDataWithoutPhone).success.value

      val application = applicationBuilder(userAnswers = Some(contactUpeNfmAnswer)).build()
      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))
        val request = FakeRequest(GET, controllers.routes.CheckYourAnswersController.onPageLoad.url)
        val result  = route(application, request).value
        status(result) mustEqual OK
        contentAsString(result) must include(
          "Ultimate parent"
        )
        contentAsString(result) must include(
          "Nominated filing member"
        )
        contentAsString(result) must include(
          "First contact"
        )
        contentAsString(result) must include(
          "Second contact"
        )
      }
    }

    "must return OK and the correct view if an answer is provided to every question  without Secondary contact detail" in {

      val contactAnswer = emptyUserAnswers
        .set(
          SubscriptionPage,
          CheckAnswerwithoutSecondaryContactDetail()
        )
        .success
        .value

      val contactNfmAnswer = contactAnswer.set(NominatedFilingMemberPage, nfmCheckAnswerData()).success.value

      val contactUpeNfmAnswer = contactNfmAnswer.set(RegistrationPage, upeCheckAnswerDataWithoutPhone).success.value

      val application = applicationBuilder(userAnswers = Some(contactUpeNfmAnswer)).build()
      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))
        val request = FakeRequest(GET, controllers.routes.CheckYourAnswersController.onPageLoad.url)
        val result  = route(application, request).value
        status(result) mustEqual OK
        contentAsString(result) must include(
          "Ultimate parent"
        )
        contentAsString(result) must include(
          "Nominated filing member"
        )
        contentAsString(result) must include(
          "First contact"
        )
        contentAsString(result) must not include
          "Second contact name"
      }
    }

    "must return OK and the correct view if an answer is provided to every question and phone number also provided" in {

      val contactAnswer = emptyUserAnswers
        .set(
          SubscriptionPage,
          CheckAnswerwithSecondaryContactDetail()
        )
        .success
        .value

      val contactNfmAnswer = contactAnswer.set(NominatedFilingMemberPage, nfmCheckAnswerData()).success.value

      val contactUpeNfmAnswer = contactNfmAnswer.set(RegistrationPage, upeCheckAnswerDataWithPhone).success.value

      val application = applicationBuilder(userAnswers = Some(contactUpeNfmAnswer)).build()
      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))
        val request = FakeRequest(GET, controllers.routes.CheckYourAnswersController.onPageLoad.url)
        val result  = route(application, request).value
        status(result) mustEqual OK
        contentAsString(result) must include(
          "Ultimate parent"
        )
        contentAsString(result) must include(
          "Nominated filing member"
        )
        contentAsString(result) must include(
          "First contact"
        )
        contentAsString(result) must include(
          "Second contact"
        )
      }
    }

    "must return OK and the correct view if an answer is provided with limited company upe" in {

      val contactAnswer = emptyUserAnswers
        .set(
          SubscriptionPage,
          CheckAnswerwithSecondaryContactDetail()
        )
        .success
        .value

      val contactNfmAnswer = contactAnswer.set(NominatedFilingMemberPage, nfmCheckAnswerData()).success.value

      val contactUpeNfmAnswer = contactNfmAnswer.set(RegistrationPage, validWithIdRegDataForLimitedCompany).success.value
      val application         = applicationBuilder(userAnswers = Some(contactUpeNfmAnswer)).build()
      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))
        val request = FakeRequest(GET, controllers.routes.CheckYourAnswersController.onPageLoad.url)
        val result  = route(application, request).value
        status(result) mustEqual OK
        contentAsString(result) must include(
          "Company Registration Number"
        )
        contentAsString(result) must include(
          "Unique Taxpayer Reference"
        )
        contentAsString(result) must include(
          "First contact"
        )
        contentAsString(result) must include(
          "Further registration details"
        )
      }
    }

    "must return OK and the correct view if an answer is provided with fm registration partnership" in {

      val contactAnswer = emptyUserAnswers
        .set(
          SubscriptionPage,
          CheckAnswerwithSecondaryContactDetail()
        )
        .success
        .value

      val contactNfmAnswer = contactAnswer.set(NominatedFilingMemberPage, validWithIdFmRegistrationDataForPartnership).success.value

      val contactUpeNfmAnswer = contactNfmAnswer.set(RegistrationPage, validWithIdRegDataForLimitedCompany).success.value
      val application         = applicationBuilder(userAnswers = Some(contactUpeNfmAnswer)).build()
      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))
        val request = FakeRequest(GET, controllers.routes.CheckYourAnswersController.onPageLoad.url)
        val result  = route(application, request).value
        status(result) mustEqual OK
        contentAsString(result) must include(
          "Company Registration Number"
        )
        contentAsString(result) must include(
          "Unique Taxpayer Reference"
        )
        contentAsString(result) must include(
          "First contact"
        )
        contentAsString(result) must include(
          "Further registration details"
        )
      }
    }

    "must return OK and the correct view if an answer is provided with LLP upe" in {

      val contactAnswer = emptyUserAnswers
        .set(
          SubscriptionPage,
          CheckAnswerwithSecondaryContactDetail()
        )
        .success
        .value

      val contactNfmAnswer = contactAnswer.set(NominatedFilingMemberPage, validWithIdFmRegistrationDataForLimitedComp).success.value

      val contactUpeNfmAnswer = contactNfmAnswer.set(RegistrationPage, validWithIdRegDataForLLP).success.value
      val application         = applicationBuilder(userAnswers = Some(contactUpeNfmAnswer)).build()
      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))
        val request = FakeRequest(GET, controllers.routes.CheckYourAnswersController.onPageLoad.url)
        val result  = route(application, request).value
        status(result) mustEqual OK
        contentAsString(result) must include(
          "Company Registration Number"
        )
        contentAsString(result) must include(
          "Unique Taxpayer Reference"
        )
        contentAsString(result) must include(
          "First contact"
        )
        contentAsString(result) must include(
          "Further registration details"
        )
      }
    }

    "must return OK and the correct view if an answer is provided with limited company nfm" in {

      val contactAnswer = emptyUserAnswers
        .set(
          SubscriptionPage,
          CheckAnswerwithSecondaryContactDetail()
        )
        .success
        .value

      val contactNfmAnswer = contactAnswer.set(NominatedFilingMemberPage, validWithIdFmRegistrationDataForLimitedComp).success.value

      val contactUpeNfmAnswer = contactNfmAnswer.set(RegistrationPage, validWithIdRegDataForLimitedCompany).success.value
      val application         = applicationBuilder(userAnswers = Some(contactUpeNfmAnswer)).build()
      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))
        val request = FakeRequest(GET, controllers.routes.CheckYourAnswersController.onPageLoad.url)
        val result  = route(application, request).value
        status(result) mustEqual OK
        contentAsString(result) must include(
          "Company Registration Number"
        )
        contentAsString(result) must include(
          "Unique Taxpayer Reference"
        )
        contentAsString(result) must include(
          "First contact"
        )
        contentAsString(result) must include(
          "Further registration details"
        )
      }
    }

    "must redirect to other page if confirm and send" in {

      val contactAnswer = emptyUserAnswers
        .set(
          SubscriptionPage,
          CheckAnswerwithSecondaryContactDetail()
        )
        .success
        .value

      val contactNfmAnswer = contactAnswer.set(NominatedFilingMemberPage, validWithIdFmRegistrationDataForLimitedComp).success.value

      val contactUpeNfmAnswer = contactNfmAnswer.set(RegistrationPage, validWithIdRegDataForLimitedCompany).success.value
      val application         = applicationBuilder(userAnswers = Some(contactUpeNfmAnswer)).build()
      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))
        val request = FakeRequest(POST, controllers.routes.CheckYourAnswersController.onPageLoad.url)
        val result  = route(application, request).value
        status(result) mustEqual SEE_OTHER
      }
    }

  }
}
