/*
 * Copyright 2023 HM Revenue & Customs
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
import models.subscription.{SubscriptionRequestParameters, SubscriptionResponse}
import models.{NonUKAddress, UKAddress}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages._
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import viewmodels.govuk.SummaryListFluency

import java.time.LocalDate
import scala.concurrent.Future

class CheckYourAnswersControllerSpec extends SpecBase with SummaryListFluency {
  def controller(): CheckYourAnswersController =
    new CheckYourAnswersController(
      mockMessagesApi,
      mockIdentifierAction,
      mockDataRetrievalAction,
      mockDataRequiredAction,
      mockRegisterWithoutIdService,
      mockSubscriptionService,
      mockUserAnswersConnectors,
      mockTaxEnrolmentService,
      mockControllerComponents,
      mockCheckYourAnswersView,
      mockCountryOptions
    )

  val date = LocalDate.now()
  val nonUkAddress = NonUKAddress(
    addressLine1 = "1 drive",
    addressLine2 = None,
    addressLine3 = "la la land",
    addressLine4 = None,
    postalCode = None,
    countryCode = "AB"
  )
  val ukAddress = UKAddress(
    addressLine1 = "1 drive",
    addressLine2 = None,
    addressLine3 = "la la land",
    addressLine4 = None,
    postalCode = "m19hgs",
    countryCode = "AB"
  )
  val grsResponse = GrsResponse(
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

  val validSubscriptionCreateParameter = SubscriptionRequestParameters("id", "regSafeId", Some("fmSafeId"))
  val validSubscriptionSuccessResponse =
    SubscriptionResponse(
      plrReference = "XMPLR0012345678",
      formBundleNumber = "119000004320",
      processingDate = LocalDate.parse("2023-09-22").atStartOfDay()
    )
  val nfmNoID = emptyUserAnswers
    .set(NominateFilingMemberPage, true)
    .success
    .value
    .set(fmRegisteredInUKPage, false)
    .success
    .value
    .set(fmNameRegistrationPage, "name")
    .success
    .value
    .set(fmRegisteredAddressPage, nonUkAddress)
    .success
    .value
    .set(fmContactNamePage, "contactName")
    .success
    .value
    .set(fmContactEmailPage, "some@email.com")
    .success
    .value
    .set(fmPhonePreferencePage, true)
    .success
    .value
    .set(fmCapturePhonePage, "12312321")
    .success
    .value
  val nfmId = emptyUserAnswers
    .set(NominateFilingMemberPage, true)
    .success
    .value
    .set(fmRegisteredInUKPage, true)
    .success
    .value
    .set(fmEntityTypePage, EntityType.UkLimitedCompany)
    .success
    .value
    .set(fmGRSResponsePage, grsResponse)
    .success
    .value
  val upNoID = emptyUserAnswers
    .set(upeNameRegistrationPage, "name")
    .success
    .value
    .set(upeRegisteredInUKPage, false)
    .success
    .value
    .set(upeRegisteredAddressPage, ukAddress)
    .success
    .value
    .set(upeContactNamePage, "contactName")
    .success
    .value
    .set(upeContactEmailPage, "some@email.com")
    .success
    .value
    .set(upePhonePreferencePage, true)
    .success
    .value
    .set(upeCapturePhonePage, "12312321")
    .success
    .value
  val upId = emptyUserAnswers
    .set(NominateFilingMemberPage, true)
    .success
    .value
    .set(upeRegisteredInUKPage, true)
    .success
    .value
    .set(fmEntityTypePage, EntityType.UkLimitedCompany)
    .success
    .value
    .set(fmGRSResponsePage, grsResponse)
    .success
    .value

  val subData = emptyUserAnswers
    .set(subPrimaryContactNamePage, "name")
    .success
    .value
    .set(subPrimaryEmailPage, "email@hello.com")
    .success
    .value
    .set(subPrimaryPhonePreferencePage, true)
    .success
    .value
    .set(subPrimaryCapturePhonePage, "123213")
    .success
    .value
    .set(subSecondaryContactNamePage, "name")
    .success
    .value
    .set(subSecondaryEmailPage, "email@hello.com")
    .success
    .value
    .set(subSecondaryPhonePreferencePage, true)
    .success
    .value
    .set(subSecondaryCapturePhonePage, "123213")
    .success
    .value
  val filingMember =
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
  val sampleRegistrationInfo = RegistrationInfo(
    crn = "CRN123456",
    utr = "UTR654321",
    safeId = "SAFEID789012"
  )

  val registration = Registration(
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

    "must return OK and the correct view if an answer is provided to every contact detail question" in {
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

    "must return OK and the correct view if an answer is provided to every ultimate parent question" in {
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
    "must return OK and the correct view if an answer is provided to every Filing member question" in {

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

    "must return OK and the correct view if an answer is provided with limited company upe" in {

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

    "must return OK and the correct view if an answer is provided with limited company nfm" in {

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

    "must trigger create subscription API if nfm and upe data is found" in {
      val regData = RegistrationInfo(crn = "123", utr = "345", safeId = "567")
      val userAnswer = emptyUserAnswers
        .set(upeRegisteredInUKPage, true)
        .success
        .value
        .set(UpeRegInformationPage, regData)
        .success
        .value
        .set(NominateFilingMemberPage, false)
        .success
        .value
      val application = applicationBuilder(userAnswers = Some(userAnswer)).build()
      running(application) {
        val request = FakeRequest(POST, controllers.routes.CheckYourAnswersController.onSubmit.url)
        val result  = route(application, request).value
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some("/report-pillar2-top-up-taxes/display-error")
      }
    }

    "must redirect to journey recovery if no data is for either upe or nfm found" in {

      val application = applicationBuilder(userAnswers = None).build()
      running(application) {
        val request = FakeRequest(POST, controllers.routes.CheckYourAnswersController.onSubmit.url)
        val result  = route(application, request).value
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.JourneyRecoveryController.onPageLoad().url)
      }
    }

  }
}
