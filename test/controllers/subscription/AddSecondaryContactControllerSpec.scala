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
import forms.AddSecondaryContactFormProvider
import models.fm.FilingMember
import models.subscription.Subscription
import models.{MneOrDomestic, NormalMode, UserAnswers}
import pages.{NominatedFilingMemberPage, SubscriptionPage}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import utils.RowStatus
import views.html.subscriptionview.AddSecondaryContactView

class AddSecondaryContactControllerSpec extends SpecBase {

  val formProvider = new AddSecondaryContactFormProvider()

  "AddSecondaryContact Controller" when {

    "must return OK and the correct view for a GET" in {
      val userAnswers = UserAnswers(userAnswersId)
        .set(NominatedFilingMemberPage, FilingMember(nfmConfirmation = true, isNFMnStatus = RowStatus.Completed))
        .success
        .value
        .set(SubscriptionPage, Subscription(MneOrDomestic.Uk, RowStatus.InProgress))
        .success
        .value
      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.subscription.routes.AddSecondaryContactController.onPageLoad(NormalMode).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[AddSecondaryContactView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider(), NormalMode)(request, appConfig(application), messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(userAnswersId)
        .set(SubscriptionPage, Subscription(MneOrDomestic.Uk, RowStatus.InProgress, useContactPrimary = Some(true)))
        .success
        .value
        .set(NominatedFilingMemberPage, FilingMember(nfmConfirmation = true, isNFMnStatus = RowStatus.Completed))
        .success
        .value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.subscription.routes.AddSecondaryContactController.onPageLoad(NormalMode).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[AddSecondaryContactView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider().fill(true), NormalMode)(
          request,
          appConfig(application),
          messages(application)
        ).toString
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, controllers.subscription.routes.AddSecondaryContactController.onPageLoad(NormalMode).url)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = formProvider().bind(Map("value" -> ""))

        val view = application.injector.instanceOf[AddSecondaryContactView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(request, appConfig(application), messages(application)).toString
      }
    }

  }
}
