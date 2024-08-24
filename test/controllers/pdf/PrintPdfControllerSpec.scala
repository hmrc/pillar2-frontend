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
import models.UserAnswers
import models.rfm.{CorporatePosition, RfmJourneyModel}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.EitherValues
import org.scalatestplus.mockito.MockitoSugar
import pages.{RfmAddSecondaryContactPage, RfmCapturePrimaryTelephonePage, RfmContactAddressPage, RfmContactByTelephonePage, RfmCorporatePositionPage, RfmNameRegistrationPage, RfmPrimaryContactEmailPage, RfmPrimaryContactNamePage, RfmRegisteredAddressPage, RfmSecondaryCapturePhonePage, RfmSecondaryContactNamePage, RfmSecondaryEmailPage, RfmSecondaryPhonePreferencePage, RfmUkBasedPage}
import play.api.http.HeaderNames
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.FopService

import scala.concurrent.Future

class PrintPdfControllerSpec extends SpecBase with EitherValues with MockitoSugar {

  val answers: UserAnswers = emptyUserAnswers
    .set(RfmCorporatePositionPage, CorporatePosition.NewNfm).success.value
    .set(RfmUkBasedPage, false).success.value
    .set(RfmNameRegistrationPage, "first last").success.value
    .set(RfmRegisteredAddressPage, nonUkAddress).success.value
    .set(RfmPrimaryContactNamePage, "primary name").success.value
    .set(RfmPrimaryContactEmailPage, "primary@test.com").success.value
    .set(RfmContactByTelephonePage, true).success.value
    .set(RfmCapturePrimaryTelephonePage, "0191 123456789").success.value
    .set(RfmAddSecondaryContactPage, true).success.value
    .set(RfmSecondaryContactNamePage, "secondary name").success.value
    .set(RfmSecondaryEmailPage, "secondary@test.com").success.value
    .set(RfmSecondaryPhonePreferencePage, true).success.value
    .set(RfmSecondaryCapturePhonePage, "0191 987654321").success.value
    .set(RfmContactAddressPage, nonUkAddress).success.value

  val model: RfmJourneyModel = RfmJourneyModel.from(answers).right.value


  "onDownloadRfm" must {

    "return OK and the correct view" in {
      val mockFopService = mock[FopService]
      val application = applicationBuilder(userAnswers = Some(answers))
        .overrides(
          bind[FopService].toInstance(mockFopService)
        )
        .build()
      when(mockFopService.render(any())).thenReturn(Future.successful("hello".getBytes))
      running(application) {
        val request = FakeRequest(GET, controllers.pdf.routes.PrintPdfController.onDownloadRfm.url)
        val result = route(application, request).value
        status(result) mustEqual OK
        contentAsString(result) mustEqual "hello"
        header(HeaderNames.CONTENT_DISPOSITION, result).value mustEqual "attachment; filename=replace-filing-member-check-your-answers.pdf"
      }
    }

    "redirect to the journey recovery controller when the user answers are incomplete" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
      running(application) {
        val request = FakeRequest(GET, controllers.pdf.routes.PrintPdfController.onDownloadRfm.url)
        val result = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.rfm.routes.RfmJourneyRecoveryController.onPageLoad.url
      }
    }
  }
}
