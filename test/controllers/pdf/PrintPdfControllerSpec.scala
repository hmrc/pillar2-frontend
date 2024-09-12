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
import models.UkOrAbroadBankAccount.UkBankAccount
import models.UserAnswers
import models.repayments.BankAccountDetails
import models.rfm.CorporatePosition
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.EitherValues
import org.scalatestplus.mockito.MockitoSugar
import pages.pdf.{PdfRegistrationDatePage, PdfRegistrationTimeStampPage}
import pages._
import play.api.http.HeaderNames
import play.api.inject
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import services.FopService

import scala.concurrent.Future

class PrintPdfControllerSpec extends SpecBase with EitherValues with MockitoSugar {

  "rfm" must {

    val answers: UserAnswers = emptyUserAnswers
      .set(RfmCorporatePositionPage, CorporatePosition.NewNfm)
      .success
      .value
      .set(RfmUkBasedPage, false)
      .success
      .value
      .set(RfmNameRegistrationPage, "first last")
      .success
      .value
      .set(RfmRegisteredAddressPage, nonUkAddress)
      .success
      .value
      .set(RfmPrimaryContactNamePage, "primary name")
      .success
      .value
      .set(RfmPrimaryContactEmailPage, "primary@test.com")
      .success
      .value
      .set(RfmContactByTelephonePage, true)
      .success
      .value
      .set(RfmCapturePrimaryTelephonePage, "0191 123456789")
      .success
      .value
      .set(RfmAddSecondaryContactPage, true)
      .success
      .value
      .set(RfmSecondaryContactNamePage, "secondary name")
      .success
      .value
      .set(RfmSecondaryEmailPage, "secondary@test.com")
      .success
      .value
      .set(RfmSecondaryPhonePreferencePage, true)
      .success
      .value
      .set(RfmSecondaryCapturePhonePage, "0191 987654321")
      .success
      .value
      .set(RfmContactAddressPage, nonUkAddress)
      .success
      .value

    val sessionRepositoryUserAnswers: UserAnswers = emptyUserAnswers
      .set(PlrReferencePage, "someID")
      .success
      .value

    "onDownloadRfmAnswers" should {

      "return OK and the correct view" in {
        val mockFopService = mock[FopService]
        val application = applicationBuilder(userAnswers = Some(answers))
          .overrides(
            bind[FopService].toInstance(mockFopService)
          )
          .build()
        when(mockFopService.render(any())).thenReturn(Future.successful("hello".getBytes))
        running(application) {
          val request = FakeRequest(GET, controllers.pdf.routes.PrintPdfController.onDownloadRfmAnswers.url)
          val result  = route(application, request).value
          status(result) mustEqual OK
          contentAsString(result) mustEqual "hello"
          header(HeaderNames.CONTENT_DISPOSITION, result).value mustEqual "attachment; filename=replace-filing-member-answers.pdf"
        }
      }

      "redirect to the journey recovery controller when the user answers are incomplete" in {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
        running(application) {
          val request = FakeRequest(GET, controllers.pdf.routes.PrintPdfController.onDownloadRfmAnswers.url)
          val result  = route(application, request).value
          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.rfm.routes.RfmJourneyRecoveryController.onPageLoad.url
        }
      }

    }

    "onDownloadRfmConfirmation" should {

      "return OK and the correct view" in {
        val mockFopService = mock[FopService]
        val application = applicationBuilder(userAnswers = Some(sessionRepositoryUserAnswers))
          .overrides(
            bind[FopService].toInstance(mockFopService),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()
        when(mockFopService.render(any())).thenReturn(Future.successful("hello".getBytes))
        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(sessionRepositoryUserAnswers)))
        running(application) {
          val request = FakeRequest(GET, controllers.pdf.routes.PrintPdfController.onDownloadRfmConfirmation.url)
          val result  = route(application, request).value
          status(result) mustEqual OK
          contentAsString(result) mustEqual "hello"
          header(HeaderNames.CONTENT_DISPOSITION, result).value mustEqual "attachment; filename=replace-filing-member-confirmation.pdf"
        }
      }

      "redirect to the journey recovery controller when the user answers are incomplete" in {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
        running(application) {
          val request = FakeRequest(GET, controllers.pdf.routes.PrintPdfController.onDownloadRfmConfirmation.url)
          val result  = route(application, request).value
          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.rfm.routes.RfmJourneyRecoveryController.onPageLoad.url
        }
      }

    }

  }

  "repayment" must {

    val amount: BigDecimal = BigDecimal(100.99)
    val ukBankAccountDetails: BankAccountDetails = BankAccountDetails(
      bankName = "Barclays",
      nameOnBankAccount = "Epic Adventure Inc",
      sortCode = "206705",
      accountNumber = "86473611"
    )

    val answers: UserAnswers = UserAnswers("id")
      .set(RepaymentsRefundAmountPage, amount)
      .success
      .value
      .set(ReasonForRequestingRefundPage, "The reason for refund")
      .success
      .value
      .set(UkOrAbroadBankAccountPage, UkBankAccount)
      .success
      .value
      .set(BankAccountDetailsPage, ukBankAccountDetails)
      .success
      .value
      .set(RepaymentsContactNamePage, "contact name")
      .success
      .value
      .set(RepaymentsContactEmailPage, "contact@test.com")
      .success
      .value
      .set(RepaymentsContactByTelephonePage, true)
      .success
      .value
      .set(RepaymentsTelephoneDetailsPage, "0191 123456789")
      .success
      .value

    "onDownloadRepaymentAnswers" should {

      "return OK and the correct view" in {
        val mockFopService = mock[FopService]
        val application = applicationBuilder(userAnswers = Some(answers))
          .overrides(
            bind[FopService].toInstance(mockFopService)
          )
          .build()
        when(mockFopService.render(any())).thenReturn(Future.successful("hello".getBytes))
        running(application) {
          val request = FakeRequest(GET, controllers.pdf.routes.PrintPdfController.onDownloadRepaymentAnswers.url)
          val result  = route(application, request).value
          status(result) mustEqual OK
          contentAsString(result) mustEqual "hello"
          header(HeaderNames.CONTENT_DISPOSITION, result).value mustEqual "attachment; filename=repayment-answers.pdf"
        }
      }

      "redirect to the journey recovery controller when the user answers are incomplete" in {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
        running(application) {
          val request = FakeRequest(GET, controllers.pdf.routes.PrintPdfController.onDownloadRepaymentAnswers.url)
          val result  = route(application, request).value
          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }

    }

    "onDownloadRepaymentConfirmation" should {

      "return OK and the correct view" in {
        val mockFopService = mock[FopService]
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[FopService].toInstance(mockFopService)
          )
          .build()
        when(mockFopService.render(any())).thenReturn(Future.successful("hello".getBytes))
        running(application) {
          val request = FakeRequest(GET, controllers.pdf.routes.PrintPdfController.onDownloadRepaymentConfirmation.url)
          val result  = route(application, request).value
          status(result) mustEqual OK
          contentAsString(result) mustEqual "hello"
          header(HeaderNames.CONTENT_DISPOSITION, result).value mustEqual "attachment; filename=repayment-confirmation.pdf"
        }
      }

    }

  }

  class PrintPdfControllerSpec extends SpecBase {
    "Print Pdf Controller" should {

      "return OK and the correct PDF for a GET" in {
        val testCompanyName   = "testName"
        val testPlr2Reference = "XMPLR0012345674"
        val testTimeStamp     = "11:45am (GMT)"
        val testDate          = "17 January 2025"

        val ua = emptyUserAnswers
          .setOrException(PdfRegistrationDatePage, testDate)
          .setOrException(PdfRegistrationTimeStampPage, testTimeStamp)
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
            inject.bind[FopService].toInstance(mockFopService)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, controllers.pdf.routes.PrintPdfController.printRegistrationConfirmation.url)

          val result = route(application, request).value

          status(result) mustEqual OK
          contentAsBytes(result) mustEqual fakePdfContent
          headers(result).get(CONTENT_DISPOSITION) mustBe Some("attachment; filename=Pillar 2 Registration Confirmation.pdf")
        }
      }

      "redirect to the journey recovery page in the case of an error" in {
        val ua                    = emptyUserAnswers
        val mockSessionRepository = mock[SessionRepository]

        val mockFopService = mock[FopService]
        val fakePdfContent = Array[Byte](1, 2, 3)

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(ua)))
        when(mockFopService.render(any())).thenReturn(Future.successful(fakePdfContent))

        val application = applicationBuilder(userAnswers = Some(ua))
          .overrides(
            inject.bind[SessionRepository].toInstance(mockSessionRepository),
            inject.bind[FopService].toInstance(mockFopService)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, controllers.pdf.routes.PrintPdfController.printRegistrationConfirmation.url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }
    }
  }
}
