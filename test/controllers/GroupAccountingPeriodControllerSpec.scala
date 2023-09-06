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
import forms.GroupAccountingPeriodFormProvider
import models.fm.FilingMember
import models.subscription.Subscription
import models.{MneOrDomestic, NfmRegistrationConfirmation, NormalMode, UserAnswers}
import pages.{NominatedFilingMemberPage, SubscriptionPage}
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsFormUrlEncoded}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import utils.RowStatus
import views.html.subscriptionview.GroupAccountingPeriodView

import java.time.{LocalDate, ZoneOffset}

class GroupAccountingPeriodControllerSpec extends SpecBase {

  val formProvider = new GroupAccountingPeriodFormProvider()

  val validAnswer = LocalDate.now(ZoneOffset.UTC)

  lazy val groupAccountingPeriodRoute = controllers.subscription.routes.GroupAccountingPeriodController.onPageLoad(NormalMode).url

  override val emptyUserAnswers = UserAnswers(userAnswersId)

  def getRequest(): FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest(GET, controllers.subscription.routes.GroupAccountingPeriodController.onPageLoad(NormalMode).url)

  def postRequest(): FakeRequest[AnyContentAsFormUrlEncoded] =
    FakeRequest(POST, controllers.subscription.routes.GroupAccountingPeriodController.onPageLoad(NormalMode).url)
      .withFormUrlEncodedBody(
        "value.day"   -> validAnswer.getDayOfMonth.toString,
        "value.month" -> validAnswer.getMonthValue.toString,
        "value.year"  -> validAnswer.getYear.toString
      )

  "GroupAccountingPeriod Controller" when {

    "must return OK and the correct view for a GET" in {

      val userAnswer = UserAnswers(userAnswersId)
        .set(NominatedFilingMemberPage, FilingMember(nfmConfirmation = NfmRegistrationConfirmation.Yes, isNFMnStatus = RowStatus.Completed))
        .success
        .value
        .set(SubscriptionPage, Subscription(MneOrDomestic.Uk, subscriptionStatus = RowStatus.InProgress))
        .success
        .value

      println(userAnswer)
      val application = applicationBuilder(userAnswers = Some(userAnswer)).build()

      running(application) {

//        val request = FakeRequest(GET, controllers.subscription.routes.GroupAccountingPeriodController.onPageLoad(NormalMode).url)
        val result = route(application, getRequest).value

        val view = application.injector.instanceOf[GroupAccountingPeriodView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider(), NormalMode)(getRequest, appConfig(application), messages(application)).toString
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      val request =
        FakeRequest(POST, groupAccountingPeriodRoute)
          .withFormUrlEncodedBody(("value", "invalid value"))

      running(application) {
        val boundForm = formProvider().bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[GroupAccountingPeriodView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(request, appConfig(application), messages(application)).toString
      }
    }

  }
}
