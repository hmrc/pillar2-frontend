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

package controllers.pdf

import base.SpecBase
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages.{PlrReferencePage, UpeNameRegistrationPage}
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.mvc.Http.RequestHeader
import repositories.SessionRepository
import services.FopService
import utils.ViewHelpers
import views.xml.pdf.ConfirmationPdf

import java.time.LocalDate
import scala.concurrent.Future

class PrintPdfControllerSpec extends SpecBase {
  "Print Pdf Controller" should {

    "return OK and the correct PDF for a GET" in {
      val testCompanyName    = "testName"
      val testPlr2Reference  = "XMPLR0012345674"
      val ua = emptyUserAnswers
        .setOrException(UpeNameRegistrationPage, testCompanyName)
        .setOrException(PlrReferencePage, testPlr2Reference)
      val mockSessionRepository = mock[SessionRepository]

      val mockFopService = mock[FopService]
      val fakePdfContent = Array[Byte](1, 2, 3)

      when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(ua)))
      when(mockFopService.render(any())).thenReturn(Future.successful(fakePdfContent))

      val application = applicationBuilder(userAnswers = Some(ua))
        .overrides(
          inject.bind[SessionRepository].toInstance(mockSessionRepository),
          inject.bind[FopService].toInstance(mockFopService),
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.pdf.routes.PrintPdfController.onDownload.url)

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsBytes(result) mustEqual fakePdfContent
        headers(result).get(CONTENT_DISPOSITION) mustBe Some("attachment; filename=Pillar 2 Registration Confirmation.pdf")
      }
    }

    "redirect to the journey recovery page in the case of an error" in {
      val ua = emptyUserAnswers
      val mockSessionRepository = mock[SessionRepository]

      val mockFopService = mock[FopService]
      val fakePdfContent = Array[Byte](1, 2, 3)

      when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(ua)))
      when(mockFopService.render(any())).thenReturn(Future.successful(fakePdfContent))

      val application = applicationBuilder(userAnswers = Some(ua))
        .overrides(
          inject.bind[SessionRepository].toInstance(mockSessionRepository),
          inject.bind[FopService].toInstance(mockFopService),
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.pdf.routes.PrintPdfController.onDownload.url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
