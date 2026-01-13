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

package controllers.payments

import base.SpecBase
import controllers.actions.EnrolmentIdentifierAction.DelegatedAuthRule
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import uk.gov.hmrc.auth.core.{Enrolment, EnrolmentIdentifier}
import views.html.outstandingpayments.NoOutstandingPaymentsView

import scala.concurrent.Future

class NoOutstandingPaymentsControllerSpec extends SpecBase {

  val pillar2Id: String = "XMPLR0123456789"

  val enrolments: Set[Enrolment] = Set(
    Enrolment("HMRC-PILLAR2-ORG", List(EnrolmentIdentifier("PLRID", pillar2Id)), "Activated", Some(DelegatedAuthRule))
  )

  "NoOutstandingPaymentsController" should {
    "return OK and display the correct view for a GET" in {
      val application = applicationBuilder(enrolments = enrolments)
        .overrides(
          bind[SessionRepository].toInstance(mockSessionRepository)
        )
        .build()

      running(application) {
        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(emptyUserAnswers)))

        val request = FakeRequest(GET, controllers.payments.routes.NoOutstandingPaymentsController.onPageLoad.url)
        val result  = route(application, request).value
        val view    = application.injector.instanceOf[NoOutstandingPaymentsView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view()(
          request,
          applicationConfig,
          messages(application),
          isAgent = false
        ).toString
      }
    }

    "redirect to Journey Recovery when unable to retrieve user data" in {
      val application = applicationBuilder()
        .overrides(
          bind[SessionRepository].toInstance(mockSessionRepository)
        )
        .build()

      running(application) {
        when(mockSessionRepository.get(any())).thenReturn(Future.successful(None))

        val request = FakeRequest(GET, controllers.payments.routes.NoOutstandingPaymentsController.onPageLoad.url)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
