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

package controllers.subscriptions

import base.SpecBase
import controllers.subscription.SubCheckYourAnswersController
import models.NormalMode
import org.mockito.Mockito.when
import pages._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import viewmodels.checkAnswers._
import viewmodels.govuk.SummaryListFluency
import views.html.fmview.FilingMemberCheckYourAnswersView

class SubCheckYourAnswersControllerSpec extends SpecBase with SummaryListFluency {
  def controller(): SubCheckYourAnswersController =
    new SubCheckYourAnswersController(
      preAuthenticatedActionBuilders,
      preDataRetrievalActionImpl,
      preDataRequiredActionImpl,
      stubMessagesControllerComponents(),
      viewpageNotAvailable,
      viewCheckYourAnswersSub
    )
  val subUserAnswers = emptyUserAnswers
    .set(
      SubscriptionPage,
      subCheckAnswerData()
    )
    .success
    .value

  val subCheckAnswers = Seq(
    GroupAccountingPeriodSummary.row(subUserAnswers),
    GroupAccountingPeriodStartDateSummary.row(subUserAnswers),
    GroupAccountingPeriodEndDateSummary.row(subUserAnswers)
  ).flatten

  "Subscription Check Your Answers Controller" must {

    "must return Not Found and the correct view with empty user answers" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {

        val request = FakeRequest(GET, controllers.subscription.routes.SubCheckYourAnswersController.onPageLoad.url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[FilingMemberCheckYourAnswersView]
        val list = SummaryListViewModel(Seq.empty)
        contentAsString(result) mustEqual view(list)(
          request,
          appConfig(application),
          messages(application)
        ).toString
        status(result) mustEqual NOT_FOUND
      }
    }
    "must return OK and the correct view if an answer is provided to every question " in {
      val application = applicationBuilder(userAnswers = Some(subUserAnswers)).build()
      running(application) {
        val request = FakeRequest(GET, controllers.subscription.routes.SubCheckYourAnswersController.onPageLoad.url)
        val result  = route(application, request).value
        val view    = application.injector.instanceOf[FilingMemberCheckYourAnswersView]
        val list    = SummaryListViewModel(subCheckAnswers)
        status(result) mustEqual OK
        contentAsString(result) mustEqual view(list)(
          request,
          appConfig(application),
          messages(application)
        ).toString
      }

    }

  }
}
