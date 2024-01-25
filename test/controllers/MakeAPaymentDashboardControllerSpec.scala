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

package controllers

import base.SpecBase
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core.{Enrolment, EnrolmentIdentifier}
import views.html.MakeAPaymentDashboardView

class MakeAPaymentDashboardControllerSpec extends SpecBase {

  "Payment Dashboard Controller" should {
    "return OK and the correct view for a GET" in {
      val enrolments: Set[Enrolment] = Set(
        Enrolment(
          key = "HMRC-PILLAR2-ORG",
          identifiers = Seq(
            EnrolmentIdentifier("PLRID", "12345678"),
            EnrolmentIdentifier("UTR", "ABC12345")
          ),
          state = "activated"
        )
      )
      val application = applicationBuilder(userAnswers = None, enrolments)
        .overrides(
        )
        .build()
      running(application) {

        val request =
          FakeRequest(GET, controllers.routes.MakeAPaymentDashboardController.onPageLoad.url)

        val result = route(application, request).value
        val view   = application.injector.instanceOf[MakeAPaymentDashboardView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view("12345678")(
          request,
          appConfig(application),
          messages(application)
        ).toString
      }

    }

    "redirect to general error screen page for a GET when PLR reference is missing" in {
      val enrolmentsSet: Set[Enrolment] = Set(
        Enrolment(
          key = "HMRC-PILLAR2-van",
          identifiers = Seq(
          ),
          state = "activated"
        )
      )

    }
  }

}
