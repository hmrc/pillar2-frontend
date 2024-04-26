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

package controllers.rfm

import base.SpecBase
import models.MneOrDomestic
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages.SubMneOrDomesticPage
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import repositories.SessionRepository
import uk.gov.hmrc.auth.core.{Enrolment, EnrolmentIdentifier}
import utils.ViewHelpers
import views.html.rfm.RfmConfirmationView

import scala.concurrent.Future

class rfmConfirmationControllerSpec extends SpecBase {
  val dateHelper = new ViewHelpers()
  "RfmConfirmation Controller" when {
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
    "must return OK and the correct view with content equal to 'Domestic Top-up Tax' for a GET" in {

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers), enrolments)
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

      running(application) {
        val request = FakeRequest(GET, controllers.rfm.routes.RfmConfirmationController.onPageLoad.url)
        when(mockSessionRepository.get(any()))
          .thenReturn(Future.successful(Some(emptyUserAnswers.setOrException(SubMneOrDomesticPage, MneOrDomestic.Uk))))
        val result      = route(application, request).value
        val currentDate = HtmlFormat.escape(dateHelper.formatDateGDS(java.time.LocalDate.now))
        val view        = application.injector.instanceOf[RfmConfirmationView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view("12345678", currentDate.toString())(
          request,
          appConfig(application),
          messages(application)
        ).toString
      }
    }

    "must return OK and the correct view for a GET - rfm feature false" in {

      val ua = emptyUserAnswers
      val application = applicationBuilder(userAnswers = Some(ua))
        .configure(
          Seq(
            "features.rfmAccessEnabled" -> false
          ): _*
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.rfm.routes.RfmConfirmationController.onPageLoad.url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.routes.UnderConstructionController.onPageLoad.url
      }
    }

    "must return OK and the correct view with content equal to 'Domestic Top-up Tax and Multinational Top-up Tax' for a GET" in {
      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers), enrolments)
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

      running(application) {
        val request = FakeRequest(GET, controllers.rfm.routes.RfmConfirmationController.onPageLoad.url)
        when(mockSessionRepository.get(any()))
          .thenReturn(Future.successful(Some(emptyUserAnswers)))
        val result      = route(application, request).value
        val currentDate = HtmlFormat.escape(dateHelper.formatDateGDS(java.time.LocalDate.now))
        val view        = application.injector.instanceOf[RfmConfirmationView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view("12345678", currentDate.toString())(
          request,
          appConfig(application),
          messages(application)
        ).toString
      }
    }

//    "redirect to journey recover if no pillar 2 reference or data found in session repository" in {
//      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
//      running(application) {
//        val request = FakeRequest(GET, controllers.rfm.routes.RfmConfirmationController.onPageLoad.url)
//        val result  = route(application, request).value
//        status(result) mustEqual SEE_OTHER
//        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
//      }
//    }
  }
}
