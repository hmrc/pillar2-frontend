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
import models.MneOrDomestic
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages.pdf.{PdfRegistrationDatePage, PdfRegistrationTimeStampPage}
import pages.{SubMneOrDomesticPage, UpeNameRegistrationPage}
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import uk.gov.hmrc.auth.core.{Enrolment, EnrolmentIdentifier}
import views.html.registrationview.RegistrationConfirmationView

import scala.concurrent.Future

class RegistrationConfirmationControllerSpec extends SpecBase {

  "RegistrationConfirmation Controller" when {
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
      val testPlr2Id      = "12345678"
      val testCompanyName = "Test Limited"
      val testTimeStamp   = "11:45am (GMT)"
      val testDate        = "17 January 2025"

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers), enrolments)
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

      running(application) {
        val request = FakeRequest(GET, routes.RegistrationConfirmationController.onPageLoad.url)
        when(mockSessionRepository.get(any()))
          .thenReturn(
            Future.successful(
              Some(
                emptyUserAnswers
                  .setOrException(PdfRegistrationDatePage, testDate)
                  .setOrException(PdfRegistrationTimeStampPage, testTimeStamp)
                  .setOrException(UpeNameRegistrationPage, testCompanyName)
                  .setOrException(SubMneOrDomesticPage, MneOrDomestic.Uk)
              )
            )
          )

        val result = route(application, request).value
        val view   = application.injector.instanceOf[RegistrationConfirmationView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(testPlr2Id, testCompanyName, testDate, testTimeStamp, MneOrDomestic.Uk)(
          request,
          applicationConfig,
          messages(application)
        ).toString
      }
    }

    "must return OK and the correct view with content equal to 'Domestic Top-up Tax and Multinational Top-up Tax' for a GET" in {
      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers), enrolments)
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

      val testCompanyName = "Test Limited"
      val testPlr2ID      = "12345678"
      val testTimeStamp   = "11:45am (GMT)"
      val testDate        = "17 January 2025"

      running(application) {
        val request = FakeRequest(GET, routes.RegistrationConfirmationController.onPageLoad.url)

        when(mockSessionRepository.get(any()))
          .thenReturn(
            Future.successful(
              Some(
                emptyUserAnswers
                  .setOrException(PdfRegistrationDatePage, testDate)
                  .setOrException(PdfRegistrationTimeStampPage, testTimeStamp)
                  .setOrException(UpeNameRegistrationPage, testCompanyName)
                  .setOrException(SubMneOrDomesticPage, MneOrDomestic.UkAndOther)
              )
            )
          )
        val result = route(application, request).value
        val view   = application.injector.instanceOf[RegistrationConfirmationView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(testPlr2ID, testCompanyName, testDate, testTimeStamp, MneOrDomestic.UkAndOther)(
          request,
          applicationConfig,
          messages(application)
        ).toString
      }
    }

    "redirect to journey recover if no pillar 2 reference or data found in session repository" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
      running(application) {
        val request = FakeRequest(GET, routes.RegistrationConfirmationController.onPageLoad.url)
        val result  = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
