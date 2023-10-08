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

package controllers.subscription

import base.SpecBase
import org.mockito.Mockito.when
import pages._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import viewmodels.checkAnswers._
import viewmodels.govuk.SummaryListFluency
import views.html.subscriptionview.ContactCheckYourAnswersView

class ContactCheckYourAnswersControllerSpec extends SpecBase with SummaryListFluency {
  def controller(): ContactCheckYourAnswersController =
    new ContactCheckYourAnswersController(
      preAuthenticatedActionBuilders,
      preDataRetrievalActionImpl,
      preDataRequiredActionImpl,
      stubMessagesControllerComponents(),
      viewpageNotAvailable,
      viewContactCheckYourAnswers,
      mockCountryOptions
    )
  val completeUserAnswer = emptyUserAnswers
    .set(
      SubscriptionPage,
      ContactCheckAnswerWithSecondaryContactData()
    )
    .success
    .value

  val completeUserAnswerWithoutSecondaryPhone = emptyUserAnswers
    .set(
      SubscriptionPage,
      ContactCheckAnswerSecondaryWithoutPhone()
    )
    .success
    .value

  val noSecondContactUserAnswers = emptyUserAnswers
    .set(
      SubscriptionPage,
      primaryContactCheckAnswerData()
    )
    .success
    .value

  "Contact Check Your Answers Controller" must {

    "must return Not Found and the correct view with empty user answers" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
      running(application) {
        val request = FakeRequest(GET, controllers.subscription.routes.ContactCheckYourAnswersController.onPageLoad.url)
        val result  = route(application, request).value
        status(result) mustEqual NOT_FOUND
      }
    }
    "must return OK and the correct view if an answer is provided to every question " in {
      val application = applicationBuilder(userAnswers = Some(completeUserAnswer)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.subscription.routes.ContactCheckYourAnswersController.onPageLoad.url)
        val result  = route(application, request).value
        status(result) mustEqual OK
        contentAsString(result) must include(
          "Contact details"
        )
        contentAsString(result) must include(
          "Second contact"
        )
        contentAsString(result) must include(
          "Contact address"
        )
      }
    }

    "must return OK and the correct view if an answer is provided without secondary phone" in {
      val application = applicationBuilder(userAnswers = Some(completeUserAnswerWithoutSecondaryPhone)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.subscription.routes.ContactCheckYourAnswersController.onPageLoad.url)
        val result  = route(application, request).value
        status(result) mustEqual OK
        contentAsString(result) must include(
          "Contact details"
        )
        contentAsString(result) must include(
          "Second contact"
        )
        contentAsString(result) must include(
          "Contact address"
        )
        contentAsString(result) must not include
          "Second contact telephone number"
      }
    }
    "must return OK and the correct view if only primary Contact and address answer is provided to  question " in {
      val application = applicationBuilder(userAnswers = Some(noSecondContactUserAnswers)).build()
      running(application) {
        val request = FakeRequest(GET, controllers.subscription.routes.ContactCheckYourAnswersController.onPageLoad.url)
        val result  = route(application, request).value
        status(result) mustEqual OK
        contentAsString(result) must include(
          "Contact details"
        )
        contentAsString(result) must not include
          "Secondary Contact"
        contentAsString(result) must include(
          "Contact address"
        )
      }
    }

  }
}
