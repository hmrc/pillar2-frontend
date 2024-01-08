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
import connectors.UserAnswersConnectors
import models.fm.{FilingMember, FilingMemberNonUKData}
import models.grs.{EntityType, GrsRegistrationResult, RegistrationStatus}
import models.registration._
import models.subscription.{AccountingPeriod, SubscriptionResponse}
import models.{MandatoryInformationMissingError, MneOrDomestic, NonUKAddress, UKAddress}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages._
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.{RegisterWithoutIdService, SubscriptionService, TaxEnrolmentService}
import uk.gov.hmrc.http.HttpResponse
import utils.{Pillar2SessionKeys, RowStatus}
import viewmodels.govuk.SummaryListFluency

import java.time.LocalDate
import scala.concurrent.Future

class CheckYourAnswersControllerSpec extends SpecBase with SummaryListFluency {

  private val response = SubscriptionResponse(
    plrReference = "XE1111123456789",
    formBundleNumber = "12345678",
    processingDate = LocalDate.now().atStartOfDay()
  )

  private val date = LocalDate.now()
  private val nonUkAddress = NonUKAddress(
    addressLine1 = "1 drive",
    addressLine2 = None,
    addressLine3 = "la la land",
    addressLine4 = None,
    postalCode = None,
    countryCode = "AB"
  )
  private val ukAddress = UKAddress(
    addressLine1 = "1 drive",
    addressLine2 = None,
    addressLine3 = "la la land",
    addressLine4 = None,
    postalCode = "m19hgs",
    countryCode = "AB"
  )
  private val grsResponse = GrsResponse(
    Some(
      IncorporatedEntityRegistrationData(
        companyProfile = CompanyProfile(
          companyName = "ABC Limited",
          companyNumber = "1234",
          dateOfIncorporation = date,
          unsanitisedCHROAddress = IncorporatedEntityAddress(address_line_1 = Some("line 1"), None, None, None, None, None, None, None)
        ),
        ctutr = "1234567890",
        identifiersMatch = true,
        businessVerification = None,
        registration = GrsRegistrationResult(
          registrationStatus = RegistrationStatus.Registered,
          registeredBusinessPartnerId = Some("XB0000000000001"),
          failures = None
        )
      )
    )
  )
  private val regData = RegistrationInfo(crn = "123", utr = "345", safeId = "567", registrationDate = None, filingMember = None)
  private val defaultUserAnswer = emptyUserAnswers
    .setOrException(upeRegisteredInUKPage, true)
    .setOrException(UpeRegInformationPage, regData)
    .setOrException(GrsUpeStatusPage, RowStatus.Completed)
    .setOrException(subRegisteredAddressPage, nonUkAddress)
    .setOrException(NominateFilingMemberPage, false)
    .setOrException(subMneOrDomesticPage, MneOrDomestic.Uk)
    .setOrException(subAccountingPeriodPage, AccountingPeriod(date, date))
    .setOrException(upeEntityTypePage, EntityType.UkLimitedCompany)
    .setOrException(upeGRSResponsePage, grsResponse)
  private val nfmNoID = emptyUserAnswers
    .setOrException(NominateFilingMemberPage, true)
    .setOrException(fmRegisteredInUKPage, false)
    .setOrException(fmNameRegistrationPage, "name")
    .setOrException(fmRegisteredAddressPage, nonUkAddress)
    .setOrException(fmContactNamePage, "contactName")
    .setOrException(fmContactEmailPage, "some@email.com")
    .setOrException(fmPhonePreferencePage, true)
    .setOrException(fmCapturePhonePage, "12312321")
  private val nfmId = emptyUserAnswers
    .setOrException(NominateFilingMemberPage, true)
    .setOrException(fmRegisteredInUKPage, true)
    .setOrException(fmEntityTypePage, EntityType.UkLimitedCompany)
    .setOrException(fmGRSResponsePage, grsResponse)
  private val upNoID = emptyUserAnswers
    .setOrException(upeNameRegistrationPage, "name")
    .setOrException(upeRegisteredInUKPage, false)
    .setOrException(upeRegisteredAddressPage, ukAddress)
    .setOrException(upeContactNamePage, "contactName")
    .setOrException(upeContactEmailPage, "some@email.com")
    .setOrException(upePhonePreferencePage, true)
    .setOrException(upeCapturePhonePage, "12312321")
  private val upId = emptyUserAnswers
    .setOrException(NominateFilingMemberPage, true)
    .setOrException(upeRegisteredInUKPage, true)
    .setOrException(fmEntityTypePage, EntityType.UkLimitedCompany)
    .setOrException(fmGRSResponsePage, grsResponse)

  private val subData = emptyUserAnswers
    .setOrException(subPrimaryContactNamePage, "name")
    .setOrException(subPrimaryEmailPage, "email@hello.com")
    .setOrException(subPrimaryPhonePreferencePage, true)
    .setOrException(subPrimaryCapturePhonePage, "123213")
    .setOrException(subSecondaryContactNamePage, "name")
    .setOrException(subSecondaryEmailPage, "email@hello.com")
    .setOrException(subSecondaryPhonePreferencePage, true)
    .setOrException(subSecondaryCapturePhonePage, "123213")
  private val filingMember =
    FilingMember(
      isNfmRegisteredInUK = false,
      withoutIdRegData = Some(
        FilingMemberNonUKData(
          registeredFmName = "Nfm name ",
          contactName = "Ashley Smith",
          emailAddress = "test@test.com",
          phonePreference = true,
          telephone = Some("122223444"),
          registeredFmAddress = nonUkAddress
        )
      )
    )

  private val sampleRegistrationInfo = RegistrationInfo(
    crn = "CRN123456",
    utr = "UTR654321",
    safeId = "SAFEID789012",
    registrationDate = None,
    filingMember = None
  )

  private val registration = Registration(
    isUPERegisteredInUK = false,
    withoutIdRegData = Some(
      WithoutIdRegData(
        upeNameRegistration = "Paddington",
        upeContactName = "Paddington ltd",
        contactUpeByTelephone = false,
        emailAddress = "example@gmail.com",
        upeRegisteredAddress = UKAddress(
          addressLine1 = "1",
          addressLine2 = Some("2"),
          addressLine3 = "3",
          addressLine4 = Some("4"),
          postalCode = "5",
          countryCode = "GB"
        )
      )
    ),
    registrationInfo = Some(sampleRegistrationInfo)
  )

  "Check Your Answers Controller" must {

    "return OK and the correct view if an answer is provided to every contact detail question" in {
      val application = applicationBuilder(userAnswers = Some(subData))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .build()

      when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))
      running(application) {
        val request = FakeRequest(GET, controllers.routes.CheckYourAnswersController.onPageLoad.url)
        val result  = route(application, request).value
        status(result) mustEqual OK
        contentAsString(result) must include(
          "First contact"
        )
        contentAsString(result) must include(
          "Second contact"
        )
      }
    }

    "return OK and the correct view if an answer is provided to every ultimate parent question" in {
      val application = applicationBuilder(userAnswers = Some(upNoID))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .build()

      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))
        val request = FakeRequest(GET, controllers.routes.CheckYourAnswersController.onPageLoad.url)
        val result  = route(application, request).value
        status(result) mustEqual OK
        contentAsString(result) must include(
          "Ultimate parent"
        )
      }
    }
    "return OK and the correct view if an answer is provided to every Filing member question" in {

      val application = applicationBuilder(userAnswers = Some(nfmNoID))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .build()
      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))
        val request = FakeRequest(GET, controllers.routes.CheckYourAnswersController.onPageLoad.url)
        val result  = route(application, request).value
        status(result) mustEqual OK

        contentAsString(result) must include(
          "Nominated filing member"
        )

      }
    }

    "return OK and the correct view if an answer is provided with limited company upe" in {

      val application = applicationBuilder(userAnswers = Some(upId))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .build()
      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))
        val request = FakeRequest(GET, controllers.routes.CheckYourAnswersController.onPageLoad.url)
        val result  = route(application, request).value
        status(result) mustEqual OK
        contentAsString(result) must include(
          "Company Registration Number"
        )
        contentAsString(result) must include(
          "Unique Taxpayer Reference"
        )
        contentAsString(result) must include(
          "First contact"
        )
        contentAsString(result) must include(
          "Further registration details"
        )
      }
    }

    "return OK and the correct view if an answer is provided with limited company nfm" in {

      val application = applicationBuilder(userAnswers = Some(nfmId))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .build()
      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))
        val request = FakeRequest(GET, controllers.routes.CheckYourAnswersController.onPageLoad.url)
        val result  = route(application, request).value
        status(result) mustEqual OK
        contentAsString(result) must include(
          "Company Registration Number"
        )
        contentAsString(result) must include(
          "Unique Taxpayer Reference"
        )
        contentAsString(result) must include(
          "First contact"
        )
        contentAsString(result) must include(
          "Further registration details"
        )
      }
    }

    "trigger create subscription API if nfm and upe data is found" in {

      val userAnswer = defaultUserAnswer
        .setOrException(subPrimaryPhonePreferencePage, false)
        .setOrException(subAddSecondaryContactPage, false)
      val mockHttpResponse = HttpResponse(OK, "")
      val application = applicationBuilder(userAnswers = Some(userAnswer))
        .overrides(
          bind[SubscriptionService].toInstance(mockSubscriptionService),
          bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors),
          bind[TaxEnrolmentService].toInstance(mockTaxEnrolmentService)
        )
        .build()
      running(application) {
        when(mockSubscriptionService.checkAndCreateSubscription(any(), any(), any())(any(), any())).thenReturn(Future.successful(Right(response)))
        when(mockUserAnswersConnectors.remove(any())(any())).thenReturn(Future.successful(mockHttpResponse))
        when(mockTaxEnrolmentService.checkAndCreateEnrolment(any())(any(), any())).thenReturn(Future.successful(Right(OK)))

        val request = FakeRequest(POST, controllers.routes.CheckYourAnswersController.onSubmit.url)
        val result  = route(application, request).value
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.RegistrationConfirmationController.onPageLoad.url)
      }
    }

    "redirect to journey recovery if no data is for either upe or nfm found" in {

      val application = applicationBuilder(userAnswers = None).build()
      running(application) {
        val request = FakeRequest(POST, controllers.routes.CheckYourAnswersController.onSubmit.url)
        val result  = route(application, request).value
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.subscription.routes.InprogressTaskListController.onPageLoad.url)
      }
    }

    "redirected to cannot return after subscription error page if the user has already subscribed with a pillar 2 reference" in {
      val application = applicationBuilder(None).build()
      running(application) {
        val request = FakeRequest(GET, controllers.routes.CheckYourAnswersController.onPageLoad.url).withSession(Pillar2SessionKeys.plrId -> "")
        val result  = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.CannotReturnAfterSubscriptionController.onPageLoad.url
      }
    }
    "user is redirected to error page" should {
      "they miss mandatory information filing member" in {

        val ua = emptyUserAnswers
          .setOrException(UpeRegInformationPage, RegistrationInfo(crn = "123", utr = "456", safeId = "UpeSafeID", None, None))
          .setOrException(FmSafeIDPage, "fmSafeID")
          .setOrException(upeRegisteredInUKPage, true)
          .setOrException(fmRegisteredInUKPage, true)
          .setOrException(NominateFilingMemberPage, true)

        val application = applicationBuilder(Some(ua))
          .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
          .overrides(bind[RegisterWithoutIdService].toInstance(mockRegisterWithoutIdService))
          .build()

        running(application) {
          val request = FakeRequest(POST, controllers.routes.CheckYourAnswersController.onSubmit.url)
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.subscription.routes.InprogressTaskListController.onPageLoad.url

        }
      }

      "they miss mandatory information for upe" in {
        val ua = emptyUserAnswers
          .setOrException(UpeRegInformationPage, RegistrationInfo(crn = "123", utr = "456", safeId = "UpeSafeID", None, None))
          .setOrException(upeRegisteredInUKPage, true)
          .setOrException(NominateFilingMemberPage, false)
        val application = applicationBuilder(Some(ua))
          .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
          .overrides(bind[RegisterWithoutIdService].toInstance(mockRegisterWithoutIdService))
          .build()

        running(application) {
          when(mockRegisterWithoutIdService.sendUpeRegistrationWithoutId(any(), any())(any(), any()))
            .thenReturn(Future.successful(Left(MandatoryInformationMissingError("wrong"))))
          val request = FakeRequest(POST, controllers.routes.CheckYourAnswersController.onSubmit.url)
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.subscription.routes.InprogressTaskListController.onPageLoad.url
        }
      }

      "chosen yes for nominating primary phone but not provided one" in {
        val regData = RegistrationInfo(crn = "123", utr = "345", safeId = "567", registrationDate = None, filingMember = None)
        val userAnswer = defaultUserAnswer
          .setOrException(subPrimaryPhonePreferencePage, true)
          .setOrException(subAddSecondaryContactPage, false)

        val application = applicationBuilder(userAnswers = Some(userAnswer))
          .overrides(
            bind[SubscriptionService].toInstance(mockSubscriptionService),
            bind[TaxEnrolmentService].toInstance(mockTaxEnrolmentService)
          )
          .build()
        running(application) {
          when(mockSubscriptionService.checkAndCreateSubscription(any(), any(), any())(any(), any())).thenReturn(Future.successful(Right(response)))
          when(mockTaxEnrolmentService.checkAndCreateEnrolment(any())(any(), any())).thenReturn(Future.successful(Right(OK)))

          val request = FakeRequest(POST, controllers.routes.CheckYourAnswersController.onSubmit.url)
          val result  = route(application, request).value
          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.subscription.routes.InprogressTaskListController.onPageLoad.url)
        }
      }
      "chosen yes for nominating secondary phone but not provided one" in {
        val regData = RegistrationInfo(crn = "123", utr = "345", safeId = "567", registrationDate = None, filingMember = None)
        val userAnswer = defaultUserAnswer
          .setOrException(subPrimaryPhonePreferencePage, false)
          .setOrException(subAddSecondaryContactPage, true)
          .setOrException(subSecondaryPhonePreferencePage, true)

        val application = applicationBuilder(userAnswers = Some(userAnswer))
          .overrides(
            bind[SubscriptionService].toInstance(mockSubscriptionService),
            bind[TaxEnrolmentService].toInstance(mockTaxEnrolmentService)
          )
          .build()
        running(application) {
          when(mockSubscriptionService.checkAndCreateSubscription(any(), any(), any())(any(), any())).thenReturn(Future.successful(Right(response)))
          when(mockTaxEnrolmentService.checkAndCreateEnrolment(any())(any(), any())).thenReturn(Future.successful(Right(OK)))

          val request = FakeRequest(POST, controllers.routes.CheckYourAnswersController.onSubmit.url)
          val result  = route(application, request).value
          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.subscription.routes.InprogressTaskListController.onPageLoad.url)
        }
      }

    }

  }
}
