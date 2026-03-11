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
import forms.NewAccountingPeriodFormProvider
import generators.Generators
import models.NormalMode
import models.subscription.AccountingPeriod
import pages.NewAccountingPeriodPage
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.subscriptionview.manageAccount.NewAccountingPeriodView

import java.time.LocalDate

class NewAccountingPeriodControllerSpec extends SpecBase with Generators {

  val formProvider = new NewAccountingPeriodFormProvider()
  val startDate:    LocalDate = LocalDate.of(2023, 12, 31)
  val endDate:      LocalDate = LocalDate.of(2025, 12, 31)
  val plrReference: String    = "XMPLR0123456789"

  "NewAccountingPeriod Controller for an organisation" must {

    "return OK and the correct view for a GET if no previous filled data is found" in {
      val application = applicationBuilder(subscriptionLocalData = Some(emptySubscriptionLocalData)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.subscription.manageAccount.routes.NewAccountingPeriodController.onPageLoad(NormalMode).url)
        val result  = route(application, request).value

        val view = application.injector.instanceOf[NewAccountingPeriodView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          formProvider(None, None),
          isAgent = false,
          organisationName = None,
          plrReference = plrReference,
          mode = NormalMode
        )(
          request,
          applicationConfig,
          messages(application)
        ).toString
      }
    }

    "return OK and the correct view for a GET when a previous answer exists" in {
      val accountingPeriod = AccountingPeriod(startDate, endDate)
      val ua               = emptyUserAnswers
        .set(NewAccountingPeriodPage, accountingPeriod)
        .success
        .value

      val application = applicationBuilder(subscriptionLocalData = Some(emptySubscriptionLocalData), userAnswers = Some(ua)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.subscription.manageAccount.routes.NewAccountingPeriodController.onPageLoad(NormalMode).url)
        val result  = route(application, request).value

        val view = application.injector.instanceOf[NewAccountingPeriodView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          formProvider(None, None),
          isAgent = false,
          organisationName = None,
          plrReference = plrReference,
          mode = NormalMode
        )(
          request,
          applicationConfig,
          messages(application)
        ).toString
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {
      val application = applicationBuilder(subscriptionLocalData = Some(emptySubscriptionLocalData)).build()

      val request =
        FakeRequest(POST, controllers.subscription.manageAccount.routes.NewAccountingPeriodController.onSubmit(NormalMode).url)
          .withFormUrlEncodedBody(("value", "invalid value"))

      running(application) {
        val boundForm = formProvider(None, None).bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[NewAccountingPeriodView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, isAgent = false, organisationName = None, plrReference = plrReference, mode = NormalMode)(
          request,
          applicationConfig,
          messages(application)
        ).toString
      }
    }
  }
}
