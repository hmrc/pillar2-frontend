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

package controllers.subscription

import base.SpecBase
import connectors.UserAnswersConnectors
import forms.ContactNameComplianceFormProvider
import models.subscription.AccountingPeriod
import models.{MneOrDomestic, NormalMode}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages.{SubAccountingPeriodPage, SubMneOrDomesticPage, SubPrimaryContactNamePage}
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.subscriptionview.ContactNameComplianceView

import java.time.LocalDate
import scala.concurrent.Future

class ContactNameComplianceControllerSpec extends SpecBase {

  val formProvider = new ContactNameComplianceFormProvider()

  "Contact Name Compliance controller" when {

    "must return OK and the correct view for a GET when no previous data is found" in {
      val ua = emptyUserAnswers
        .setOrException(SubMneOrDomesticPage, MneOrDomestic.Uk)
        .setOrException(SubAccountingPeriodPage, AccountingPeriod(LocalDate.now(), LocalDate.now()))
      val application = applicationBuilder(userAnswers = Some(ua)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.subscription.routes.ContactNameComplianceController.onPageLoad(NormalMode).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ContactNameComplianceView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider(), NormalMode)(request, appConfig(), messages(application)).toString
      }
    }

    "must return OK and the correct view for a GET when previous data is found" in {

      val ua = emptyUserAnswers
        .setOrException(SubPrimaryContactNamePage, "name")
        .setOrException(SubMneOrDomesticPage, MneOrDomestic.Uk)
        .setOrException(SubAccountingPeriodPage, AccountingPeriod(LocalDate.now(), LocalDate.now()))
      val application = applicationBuilder(userAnswers = Some(ua)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.subscription.routes.ContactNameComplianceController.onPageLoad(NormalMode).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ContactNameComplianceView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider().fill("name"), NormalMode)(
          request,
          appConfig(),
          messages(application)
        ).toString
      }
    }

    "redirect to bookmark page if previous page not answered" in {
      val application = applicationBuilder(userAnswers = None).build()
      running(application) {
        val request = FakeRequest(GET, controllers.subscription.routes.ContactNameComplianceController.onPageLoad(NormalMode).url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.JourneyRecoveryController.onPageLoad().url)
      }
    }
    "must redirect to next page when valid data is submitted" in {
      val ua = emptyUserAnswers
        .set(SubPrimaryContactNamePage, "TestName")
        .success
        .value

      val application = applicationBuilder(userAnswers = Some(ua))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .build()
      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))
        val request =
          FakeRequest(POST, controllers.subscription.routes.ContactNameComplianceController.onSubmit(NormalMode).url)
            .withFormUrlEncodedBody(
              ("value", "name")
            )
        val result = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.subscription.routes.ContactEmailAddressController.onPageLoad(NormalMode).url
      }
    }
    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val stringInput = randomStringGenerator(161)
        val request =
          FakeRequest(POST, controllers.subscription.routes.ContactNameComplianceController.onPageLoad(NormalMode).url)
            .withFormUrlEncodedBody(("value", stringInput))

        val boundForm = formProvider().bind(Map("value" -> stringInput))
        val view      = application.injector.instanceOf[ContactNameComplianceView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(request, appConfig(), messages(application)).toString
      }
    }

  }
}
