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
    SubscriptionResponse(plrReference = "XMPLR0012345678", formBundleNumber = "119000004320", processingDate = LocalDate.parse("2023-09-22"))
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
        contentAsString(result) must not include
          "First contact"
        contentAsString(result) must not include
          "Second contact name"
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
//    "must redirect to other page if confirm and send" in {
//
//
//      val application = applicationBuilder(userAnswers = Some(contactUpeNfmAnswer))
//        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
//        .overrides(
//          bind[RegistrationConnector].toInstance(mockRegistrationConnector)
//        )
//        .overrides(
//          bind[RegisterWithoutIdService].toInstance(mockRegisterWithoutIdService)
//        )
//        .build()
//      running(application) {
//        when(mockRegisterWithoutIdService.sendUpeRegistrationWithoutId(anyString, any[UserAnswers])(any[HeaderCarrier], any[ExecutionContext]))
//          .thenReturn(Future.successful(Right(SafeId("XE1111123456789"))))
//        when(mockRegisterWithoutIdService.sendFmRegistrationWithoutId(anyString, any[UserAnswers])(any[HeaderCarrier], any[ExecutionContext]))
//          .thenReturn(Future.successful(Right(SafeId("XE1111123456789"))))
//        val response = Future.successful(Right(Some(SafeId("XE1111123456789"))))
//        when(mockRegistrationConnector.upeRegisterationWithoutID(any(), any())(any(), any())).thenReturn(response)
//        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))
//        val request = FakeRequest(POST, controllers.routes.CheckYourAnswersController.onPageLoad.url)
//        val result = route(application, request).value
//        status(result) mustEqual SEE_OTHER
//      }
//    }
    //
    //    "createRegistrationAndSubscription" must {
    //
    //      implicit val hc:          HeaderCarrier           = HeaderCarrier()
    //      implicit val ec:          ExecutionContext        = ExecutionContext.global
    //      implicit val dataRequest: DataRequest[AnyContent] = DataRequest(FakeRequest(), "sessionId", emptyUserAnswers)
    //
    //      "handle scenario where RegistrationInfo is present but FilingMember SafeId is absent" when {
    //
    //        "filingMember has nfmConfirmation as true" in {
    //
    //          when(mockRegisterWithoutIdService.sendUpeRegistrationWithoutId(anyString, any[UserAnswers])(any[HeaderCarrier], any[ExecutionContext]))
    //            .thenReturn(Future.successful(Right(SafeId("mockedSafeId"))))
    //
    //          when(
    //            mockSubscriptionService
    //              .checkAndCreateSubscription(any[String], any[String], any[Option[String]])(any[HeaderCarrier], any[ExecutionContext])
    //          )
    //            .thenReturn(Future.successful(Right(validSubscriptionSuccessResponse)))
    //
    //          when(mockRegisterWithoutIdService.sendFmRegistrationWithoutId(anyString, any[UserAnswers])(any[HeaderCarrier], any[ExecutionContext]))
    //            .thenReturn(Future.successful(Right(SafeId("XMPLR0012345678"))))
    //
    //          when(mockTaxEnrolmentService.checkAndCreateEnrolment(any[EnrolmentInfo]())(any[HeaderCarrier], any[ExecutionContext]))
    //            .thenReturn(Future.successful(Right(200)))
    //
    //          val traitUnderTest = new RegisterAndSubscribe {
    //            override val registerWithoutIdService = mockRegisterWithoutIdService
    //            override val subscriptionService      = mockSubscriptionService
    //            override val userAnswersConnectors    = mockUserAnswersConnectors
    //            override val taxEnrolmentService      = mockTaxEnrolmentService
    //          }
    //
    //          val result = traitUnderTest.createRegistrationAndSubscription(registration, filingMember)(
    //            HeaderCarrier(),
    //            ExecutionContext.global,
    //            dataRequest
    //          )
    //
    //          status(result) shouldBe SEE_OTHER
    //
    //        }
    //
    //        "filingMember has nfmConfirmation as false" in {
    //
    //          when(mockRegisterWithoutIdService.sendUpeRegistrationWithoutId(anyString, any[UserAnswers])(any[HeaderCarrier], any[ExecutionContext]))
    //            .thenReturn(Future.successful(Right(SafeId("mockedSafeId"))))
    //
    //          when(
    //            mockSubscriptionService
    //              .checkAndCreateSubscription(any[String], any[String], any[Option[String]])(any[HeaderCarrier], any[ExecutionContext])
    //          )
    //            .thenReturn(Future.successful(Right(validSubscriptionSuccessResponse)))
    //
    //          when(mockRegisterWithoutIdService.sendFmRegistrationWithoutId(anyString, any[UserAnswers])(any[HeaderCarrier], any[ExecutionContext]))
    //            .thenReturn(Future.successful(Right(SafeId("XMPLR0012345678"))))
    //
    //          val successfulEnrolmentResponse = NO_CONTENT
    //          when(mockTaxEnrolmentService.checkAndCreateEnrolment(any[EnrolmentInfo])(any[HeaderCarrier], any[ExecutionContext]))
    //            .thenReturn(Future.successful(Right(successfulEnrolmentResponse)))
    //
    //          val traitUnderTest = new RegisterAndSubscribe {
    //            override val registerWithoutIdService = mockRegisterWithoutIdService
    //            override val subscriptionService      = mockSubscriptionService
    //            override val userAnswersConnectors    = mockUserAnswersConnectors
    //            override val taxEnrolmentService      = mockTaxEnrolmentService
    //          }
    //
    //          val sampleRegistrationInfo = RegistrationInfo(
    //            crn = "CRN123456",
    //            utr = "UTR654321",
    //            safeId = "SAFEID789012"
    //          )
    //
    //
    //          val result = traitUnderTest.createRegistrationAndSubscription(registration, filingMember)(
    //            HeaderCarrier(),
    //            ExecutionContext.global,
    //            dataRequest
    //          )
    //
    //          status(result) shouldBe SEE_OTHER
    //          redirectLocation(result) shouldBe Some(
    //            routes.RegistrationConfirmationController.onPageLoad.url
    //          )
    //
    //        }
    //      }
    //
    //      "handle scenario where RegistrationInfo is present, FilingMember SafeId is absent, and nfmConfirmation is true" in {
    //
    //        when(mockRegisterWithoutIdService.sendUpeRegistrationWithoutId(anyString, any[UserAnswers])(any[HeaderCarrier], any[ExecutionContext]))
    //          .thenReturn(Future.successful(Right(SafeId("mockedSafeId"))))
    //
    //        when(
    //          mockSubscriptionService
    //            .checkAndCreateSubscription(any[String], any[String], any[Option[String]])(any[HeaderCarrier], any[ExecutionContext])
    //        )
    //          .thenReturn(Future.successful(Right(validSubscriptionSuccessResponse)))
    //
    //        when(mockRegisterWithoutIdService.sendFmRegistrationWithoutId(anyString, any[UserAnswers])(any[HeaderCarrier], any[ExecutionContext]))
    //          .thenReturn(Future.successful(Right(SafeId("XMPLR0012345678"))))
    //
    //        when(mockTaxEnrolmentService.checkAndCreateEnrolment(any[EnrolmentInfo]())(any[HeaderCarrier], any[ExecutionContext]))
    //          .thenReturn(Future.successful(Right(200)))
    //
    //        val traitUnderTest = new RegisterAndSubscribe {
    //          override val registerWithoutIdService = mockRegisterWithoutIdService
    //          override val subscriptionService      = mockSubscriptionService
    //          override val userAnswersConnectors    = mockUserAnswersConnectors
    //          override val taxEnrolmentService      = mockTaxEnrolmentService
    //        }
    //
    //
    //        val result = traitUnderTest.createRegistrationAndSubscription(registration, filingMember)(
    //          HeaderCarrier(),
    //          ExecutionContext.global,
    //          dataRequest
    //        )
    //
    //        status(result) shouldBe SEE_OTHER
    //        redirectLocation(result) shouldBe Some(
    //          routes.RegistrationConfirmationController.onPageLoad.url
    //        )
    //
    //      }
    //
    //      "handle scenario where RegistrationInfo is absent but FilingMember SafeId is present" in {
    //
    //        when(mockRegisterWithoutIdService.sendUpeRegistrationWithoutId(anyString, any[UserAnswers])(any[HeaderCarrier], any[ExecutionContext]))
    //          .thenReturn(Future.successful(Right(SafeId("mockedSafeId"))))
    //
    //        when(
    //          mockSubscriptionService
    //            .checkAndCreateSubscription(any[String], any[String], any[Option[String]])(any[HeaderCarrier], any[ExecutionContext])
    //        )
    //          .thenReturn(Future.successful(Right(validSubscriptionSuccessResponse)))
    //
    //        when(mockTaxEnrolmentService.checkAndCreateEnrolment(any[EnrolmentInfo]())(any[HeaderCarrier], any[ExecutionContext]))
    //          .thenReturn(Future.successful(Right(200)))
    //
    //        val traitUnderTest = new RegisterAndSubscribe {
    //          override val registerWithoutIdService = mockRegisterWithoutIdService
    //          override val subscriptionService      = mockSubscriptionService
    //          override val userAnswersConnectors    = mockUserAnswersConnectors
    //          override val taxEnrolmentService      = mockTaxEnrolmentService
    //        }
    //
    //        val registration = Registration(
    //          isUPERegisteredInUK = false,
    //          withoutIdRegData = Some(
    //            WithoutIdRegData(
    //              upeNameRegistration = "Paddington",
    //              upeContactName = "Paddington ltd",
    //              contactUpeByTelephone = false,
    //              emailAddress = "example@gmail.com",
    //              upeRegisteredAddress =
    //                UKAddress(
    //                  addressLine1 = "1",
    //                  addressLine2 = Some("2"),
    //                  addressLine3 = "3",
    //                  addressLine4 = Some("4"),
    //                  postalCode = "5",
    //                  countryCode = "GB"
    //                )
    //            )
    //          ),
    //          registrationInfo = None
    //        )
    //
    //        val filingMember =
    //          FilingMember(
    //            nfmConfirmation = false,
    //            isNfmRegisteredInUK = Some(false),
    //            isNFMnStatus = RowStatus.InProgress,
    //            withoutIdRegData = Some(
    //              WithoutIdNfmData(
    //                registeredFmName = "Nfm name ",
    //                fmContactName = Some("Ashley Smith"),
    //                fmEmailAddress = Some("test@test.com"),
    //                contactNfmByTelephone = Some(true),
    //                telephoneNumber = Some("122223444"),
    //                registeredFmAddress = Some(
    //                  NfmRegisteredAddress(
    //                    addressLine1 = "1",
    //                    addressLine2 = Some("2"),
    //                    addressLine3 = "3",
    //                    addressLine4 = Some("4"),
    //                    postalCode = Some("5"),
    //                    countryCode = "GB"
    //                  )
    //                )
    //              )
    //            ),
    //            safeId = Some("1234")
    //          )
    //
    //        val result = traitUnderTest.createRegistrationAndSubscription(registration, filingMember)(
    //          HeaderCarrier(),
    //          ExecutionContext.global,
    //          dataRequest
    //        )
    //
    //        status(result) shouldBe SEE_OTHER
    //        redirectLocation(result) shouldBe Some(
    //          routes.RegistrationConfirmationController.onPageLoad.url
    //        )
    //
    //      }
    //
    //      "handle scenario where RegistrationInfo is present but FilingMember SafeId is absent and nfmConfirmation is true" in {
    //
    //        when(mockRegisterWithoutIdService.sendFmRegistrationWithoutId(anyString, any[UserAnswers])(any[HeaderCarrier], any[ExecutionContext]))
    //          .thenReturn(Future.successful(Right(SafeId("XMPLR0012345678"))))
    //
    //        when(mockTaxEnrolmentService.checkAndCreateEnrolment(any[EnrolmentInfo]())(any[HeaderCarrier], any[ExecutionContext]))
    //          .thenReturn(Future.successful(Right(200)))
    //
    //        // 3. Call the method
    //        val result = controller.createRegistrationAndSubscription(mockRegistration, mockFilingMember)
    //
    //        // 4. Verify the expected outcome
    //        status(result)           shouldBe SEE_OTHER // or whatever your expected status code is
    //        redirectLocation(result) shouldBe Some(routes.UnderConstructionController.onPageLoad.url)
    //
    //      }
    //
    //      "redirect to RegistrationConfirmationController when registration info and FM Safe ID are provided" in {
    //        when(mockAuthConnector.authorise[Option[String]](any(), any[Retrieval[Option[String]]]())(any[HeaderCarrier], any[ExecutionContext]))
    //          .thenReturn(Future.successful(Some(userAnswersId)))
    //        when(mockAuthConnector.authorise[Enrolments](any(), any[Retrieval[Enrolments]]())(any[HeaderCarrier], any[ExecutionContext]))
    //          .thenReturn(Future.successful(Enrolments(Set())))
    //        when(
    //          mockAuthConnector
    //            .authorise[Option[AffinityGroup]](any(), any[Retrieval[Option[AffinityGroup]]]())(any[HeaderCarrier], any[ExecutionContext])
    //        )
    //          .thenReturn(Future.successful(Some(Individual)))
    //        when(
    //          mockAuthConnector
    //            .authorise[Option[CredentialRole]](any(), any[Retrieval[Option[CredentialRole]]]())(any[HeaderCarrier], any[ExecutionContext])
    //        )
    //          .thenReturn(Future.successful(Some(Assistant)))
    //
    //        when(mockUserAnswersConnectors.getUserAnswer(userAnswersId)).thenReturn(Future.successful(Some(emptyUserAnswers)))
    //        when(
    //          mockAuthConnector
    //            .authorise[Option[AffinityGroup]](any(), any[Retrieval[Option[AffinityGroup]]]())(any[HeaderCarrier], any[ExecutionContext])
    //        )
    //          .thenReturn(Future.successful(Some(Individual)))
    //        when(
    //          mockAuthConnector
    //            .authorise[Option[CredentialRole]](any(), any[Retrieval[Option[CredentialRole]]]())(any[HeaderCarrier], any[ExecutionContext])
    //        )
    //          .thenReturn(Future.successful(Some(Assistant)))
    //
    //        when(
    //          mockSubscriptionService
    //            .checkAndCreateSubscription(any[String], any[String], any[Option[String]])(any[HeaderCarrier], any[ExecutionContext])
    //        )
    //          .thenReturn(Future.successful(Right(validSubscriptionSuccessResponse)))
    //
    //        when(mockTaxEnrolmentService.checkAndCreateEnrolment(any[EnrolmentInfo]())(any[HeaderCarrier], any[ExecutionContext]))
    //          .thenReturn(Future.successful(Right(200)))
    //
    //        val sampleRegistrationInfo = RegistrationInfo(
    //          crn = "CRN123456",
    //          utr = "UTR654321",
    //          safeId = "SAFEID789012"
    //        )
    //
    //        val ukBased =
    //          Registration(isUPERegisteredInUK = true, isRegistrationStatus = RowStatus.InProgress, registrationInfo = Some(sampleRegistrationInfo))
    //        val userAnswers = UserAnswers(userAnswersId)
    //          .set(RegistrationPage, ukBased)
    //          .success
    //          .value
    //          .set(NominatedFilingMemberPage, FilingMember(nfmConfirmation = true, isNFMnStatus = RowStatus.Completed, safeId = Some("fmSafeId")))
    //          .success
    //          .value
    //          .set(
    //            SubscriptionPage,
    //            Subscription(
    //              MneOrDomestic.Uk,
    //              contactDetailsStatus = RowStatus.InProgress,
    //              groupDetailStatus = RowStatus.Completed,
    //              primaryContactName = Some("asd")
    //            )
    //          )
    //          .success
    //          .value
    //
    //        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()
    //        running(application) {
    //          val request = FakeRequest(POST, controllers.routes.CheckYourAnswersController.onSubmit.url)
    //            .withFormUrlEncodedBody(("value", "true"))
    //
    //          val result = route(application, request).value
    //          status(result) shouldBe SEE_OTHER
    //        }
    //      }
    //
    //      "return SEE_OTHER (Redirect) when both registrationInfo and safeId are present" in {
    //        when(
    //          mockSubscriptionService
    //            .checkAndCreateSubscription(any[String], any[String], any[Option[String]])(any[HeaderCarrier], any[ExecutionContext])
    //        )
    //          .thenReturn(Future.successful(Right(validSubscriptionSuccessResponse)))
    //
    //        when(mockRegisterWithoutIdService.sendUpeRegistrationWithoutId(anyString, any[UserAnswers])(any[HeaderCarrier], any[ExecutionContext]))
    //          .thenReturn(Future.successful(Right(SafeId("mockedSafeId"))))
    //
    //        when(mockRegisterWithoutIdService.sendFmRegistrationWithoutId(anyString, any[UserAnswers])(any[HeaderCarrier], any[ExecutionContext]))
    //          .thenReturn(Future.successful(Right(SafeId("mockedSafeId"))))
    //
    //        val successfulEnrolmentResponse = NO_CONTENT
    //        when(mockTaxEnrolmentService.checkAndCreateEnrolment(any[EnrolmentInfo])(any[HeaderCarrier], any[ExecutionContext]))
    //          .thenReturn(Future.successful(Right(successfulEnrolmentResponse)))
    //
    //        val traitUnderTest = new RegisterAndSubscribe {
    //          override val registerWithoutIdService = mockRegisterWithoutIdService
    //          override val subscriptionService      = mockSubscriptionService
    //          override val userAnswersConnectors    = mockUserAnswersConnectors
    //          override val taxEnrolmentService      = mockTaxEnrolmentService
    //        }
    //
    //        val result = traitUnderTest.createRegistrationAndSubscription(upeCheckAnswerDataWithoutPhone, nfmCheckAnswerData)(hc, ec, dataRequest)
    //
    //        status(result) mustBe SEE_OTHER
    //      }
    //
    //      "return SEE_OTHER when registrationInfo is present, safeId is not, and nfmConfirmation is true" in {
    //
    //        when(mockRegisterWithoutIdService.sendFmRegistrationWithoutId(anyString, any[UserAnswers])(any[HeaderCarrier], any[ExecutionContext]))
    //          .thenReturn(Future.successful(Right(SafeId("mockedSafeId"))))
    //
    //        when(
    //          mockSubscriptionService
    //            .checkAndCreateSubscription(any[String], any[String], any[Option[String]])(any[HeaderCarrier], any[ExecutionContext])
    //        )
    //          .thenReturn(Future.successful(Right(validSubscriptionSuccessResponse))) // Mock a successful subscription response
    //
    //        when(mockTaxEnrolmentService.checkAndCreateEnrolment(any[EnrolmentInfo]())(any[HeaderCarrier], any[ExecutionContext]))
    //          .thenReturn(Future.successful(Right(200)))
    //
    //        val traitUnderTest = new RegisterAndSubscribe {
    //          override val registerWithoutIdService = mockRegisterWithoutIdService
    //          override val subscriptionService      = mockSubscriptionService
    //          override val userAnswersConnectors    = mockUserAnswersConnectors
    //          override val taxEnrolmentService      = mockTaxEnrolmentService
    //        }
    //
    //        val result = traitUnderTest.createRegistrationAndSubscription(upeCheckAnswerDataWithoutPhone, nfmCheckAnswerData())(
    //          HeaderCarrier(),
    //          ExecutionContext.global,
    //          dataRequest
    //        )
    //        status(result) mustBe SEE_OTHER
    //      }
    //
    //      "return SEE_OTHER when registrationInfo is present, safeId is not, and nfmConfirmation is false" in {
    //
    //        val filingMember =
    //          FilingMember(
    //            nfmConfirmation = false,
    //            isNfmRegisteredInUK = Some(false),
    //            isNFMnStatus = RowStatus.InProgress,
    //            withoutIdRegData = Some(
    //              WithoutIdNfmData(
    //                registeredFmName = "Nfm name ",
    //                fmContactName = Some("Ashley Smith"),
    //                fmEmailAddress = Some("test@test.com"),
    //                contactNfmByTelephone = Some(true),
    //                telephoneNumber = Some("122223444"),
    //                registeredFmAddress = Some(
    //                  NfmRegisteredAddress(
    //                    addressLine1 = "1",
    //                    addressLine2 = Some("2"),
    //                    addressLine3 = "3",
    //                    addressLine4 = Some("4"),
    //                    postalCode = Some("5"),
    //                    countryCode = "GB"
    //                  )
    //                )
    //              )
    //            )
    //          )
    //
    //        when(mockRegisterWithoutIdService.sendFmRegistrationWithoutId(anyString, any[UserAnswers])(any[HeaderCarrier], any[ExecutionContext]))
    //          .thenReturn(Future.successful(Right(SafeId("mockedSafeId"))))
    //
    //        when(
    //          mockSubscriptionService
    //            .checkAndCreateSubscription(any[String], any[String], any[Option[String]])(any[HeaderCarrier], any[ExecutionContext])
    //        )
    //          .thenReturn(Future.successful(Right(validSubscriptionSuccessResponse)))
    //
    //        when(mockTaxEnrolmentService.checkAndCreateEnrolment(any[EnrolmentInfo]())(any[HeaderCarrier], any[ExecutionContext]))
    //          .thenReturn(Future.successful(Right(200)))
    //
    //        val traitUnderTest = new RegisterAndSubscribe {
    //          override val registerWithoutIdService = mockRegisterWithoutIdService
    //          override val subscriptionService      = mockSubscriptionService
    //          override val userAnswersConnectors    = mockUserAnswersConnectors
    //          override val taxEnrolmentService      = mockTaxEnrolmentService
    //        }
    //
    //        val result = traitUnderTest.createRegistrationAndSubscription(
    //          upeCheckAnswerDataWithoutPhone,
    //          filingMember
    //        )(
    //          HeaderCarrier(),
    //          ExecutionContext.global,
    //          dataRequest
    //        )
    //
    //        status(result)           shouldBe SEE_OTHER
    //        redirectLocation(result) shouldBe Some(routes.RegistrationConfirmationController.onPageLoad.url)
    //
    //      }
    //
    //      "return SEE_OTHER when registrationInfo is not present and safeId is present" in {
    //
    //        when(mockRegisterWithoutIdService.sendUpeRegistrationWithoutId(anyString, any[UserAnswers])(any[HeaderCarrier], any[ExecutionContext]))
    //          .thenReturn(Future.successful(Right(SafeId("mockedUpeSafeId"))))
    //
    //        when(
    //          mockSubscriptionService
    //            .checkAndCreateSubscription(any[String], any[String], any[Option[String]])(any[HeaderCarrier], any[ExecutionContext])
    //        )
    //          .thenReturn(
    //            Future.successful(Right(validSubscriptionSuccessResponse))
    //          )
    //
    //        when(mockTaxEnrolmentService.checkAndCreateEnrolment(any[EnrolmentInfo]())(any[HeaderCarrier], any[ExecutionContext]))
    //          .thenReturn(Future.successful(Right(200)))
    //
    //        val traitUnderTest = new RegisterAndSubscribe {
    //          override val registerWithoutIdService = mockRegisterWithoutIdService
    //          override val subscriptionService      = mockSubscriptionService
    //          override val userAnswersConnectors    = mockUserAnswersConnectors
    //          override val taxEnrolmentService      = mockTaxEnrolmentService
    //        }
    //
    //        val registration = Registration(
    //          isUPERegisteredInUK = false,
    //          isRegistrationStatus = RowStatus.InProgress,
    //          withoutIdRegData = Some(
    //            WithoutIdRegData(
    //              upeNameRegistration = "Paddington",
    //              upeContactName = Some("Paddington ltd"),
    //              contactUpeByTelephone = Some(false),
    //              emailAddress = Some("example@gmail.com"),
    //              upeRegisteredAddress = Some(
    //                UpeRegisteredAddress(
    //                  addressLine1 = "1",
    //                  addressLine2 = Some("2"),
    //                  addressLine3 = "3",
    //                  addressLine4 = Some("4"),
    //                  postalCode = "5",
    //                  countryCode = "GB"
    //                )
    //              )
    //            )
    //          ),
    //          registrationInfo = None
    //        )
    //
    //        val filingMember =
    //          FilingMember(
    //            nfmConfirmation = false,
    //            isNfmRegisteredInUK = Some(false),
    //            isNFMnStatus = RowStatus.InProgress,
    //            withoutIdRegData = Some(
    //              WithoutIdNfmData(
    //                registeredFmName = "Nfm name ",
    //                fmContactName = Some("Ashley Smith"),
    //                fmEmailAddress = Some("test@test.com"),
    //                contactNfmByTelephone = Some(true),
    //                telephoneNumber = Some("122223444"),
    //                registeredFmAddress = Some(
    //                  NfmRegisteredAddress(
    //                    addressLine1 = "1",
    //                    addressLine2 = Some("2"),
    //                    addressLine3 = "3",
    //                    addressLine4 = Some("4"),
    //                    postalCode = Some("5"),
    //                    countryCode = "GB"
    //                  )
    //                )
    //              )
    //            )
    //          )
    //
    //        val result = traitUnderTest.createRegistrationAndSubscription(registration, filingMember)(
    //          HeaderCarrier(),
    //          ExecutionContext.global,
    //          dataRequest
    //        )
    //
    //        // Check the result
    //        status(result) shouldBe SEE_OTHER
    //        redirectLocation(result) shouldBe Some(
    //          routes.RegistrationConfirmationController.onPageLoad.url
    //        )
    //      }
    //
    //      "return SEE_OTHER when neither registrationInfo nor safeId are present, and nfmConfirmation is true" in {
    //        val filingMember =
    //          FilingMember(
    //            nfmConfirmation = true,
    //            isNfmRegisteredInUK = Some(false),
    //            isNFMnStatus = RowStatus.InProgress,
    //            withoutIdRegData = Some(
    //              WithoutIdNfmData(
    //                registeredFmName = "Nfm name ",
    //                fmContactName = Some("Ashley Smith"),
    //                fmEmailAddress = Some("test@test.com"),
    //                contactNfmByTelephone = Some(true),
    //                telephoneNumber = Some("122223444"),
    //                registeredFmAddress = Some(
    //                  NfmRegisteredAddress(
    //                    addressLine1 = "1",
    //                    addressLine2 = Some("2"),
    //                    addressLine3 = "3",
    //                    addressLine4 = Some("4"),
    //                    postalCode = Some("5"),
    //                    countryCode = "GB"
    //                  )
    //                )
    //              )
    //            )
    //          )
    //
    //        when(mockRegisterWithoutIdService.sendUpeRegistrationWithoutId(anyString, any[UserAnswers])(any[HeaderCarrier], any[ExecutionContext]))
    //          .thenReturn(Future.successful(Right(SafeId("mockedUpeSafeId"))))
    //
    //        when(mockRegisterWithoutIdService.sendFmRegistrationWithoutId(anyString, any[UserAnswers])(any[HeaderCarrier], any[ExecutionContext]))
    //          .thenReturn(Future.successful(Right(SafeId("mockedFmSafeId"))))
    //
    //        when(
    //          mockSubscriptionService
    //            .checkAndCreateSubscription(any[String], any[String], any[Option[String]])(any[HeaderCarrier], any[ExecutionContext])
    //        )
    //          .thenReturn(Future.successful(Right(validSubscriptionSuccessResponse)))
    //
    //        when(mockTaxEnrolmentService.checkAndCreateEnrolment(any[EnrolmentInfo]())(any[HeaderCarrier], any[ExecutionContext]))
    //          .thenReturn(Future.successful(Right(200)))
    //
    //        val traitUnderTest = new RegisterAndSubscribe {
    //          override val registerWithoutIdService = mockRegisterWithoutIdService
    //          override val subscriptionService      = mockSubscriptionService
    //          override val userAnswersConnectors    = mockUserAnswersConnectors
    //          override val taxEnrolmentService      = mockTaxEnrolmentService
    //        }
    //
    //        val result = traitUnderTest.createRegistrationAndSubscription(
    //          upeCheckAnswerData,
    //          filingMember
    //        )(
    //          HeaderCarrier(),
    //          ExecutionContext.global,
    //          dataRequest
    //        )
    //
    //        // Validate
    //        status(result) shouldBe SEE_OTHER
    //        redirectLocation(result) shouldBe Some(
    //          routes.RegistrationConfirmationController.onPageLoad.url
    //        )
    //      }
    //
    //      "return SEE_OTHER when neither registrationInfo nor safeId are present, and nfmConfirmation is false" in {
    //
    //        val filingMember =
    //          FilingMember(
    //            nfmConfirmation = false,
    //            isNfmRegisteredInUK = Some(false),
    //            isNFMnStatus = RowStatus.InProgress,
    //            withoutIdRegData = Some(
    //              WithoutIdNfmData(
    //                registeredFmName = "Nfm name ",
    //                fmContactName = Some("Ashley Smith"),
    //                fmEmailAddress = Some("test@test.com"),
    //                contactNfmByTelephone = Some(true),
    //                telephoneNumber = Some("122223444"),
    //                registeredFmAddress = Some(
    //                  NfmRegisteredAddress(
    //                    addressLine1 = "1",
    //                    addressLine2 = Some("2"),
    //                    addressLine3 = "3",
    //                    addressLine4 = Some("4"),
    //                    postalCode = Some("5"),
    //                    countryCode = "GB"
    //                  )
    //                )
    //              )
    //            )
    //          )
    //
    //        when(mockRegisterWithoutIdService.sendUpeRegistrationWithoutId(anyString, any[UserAnswers])(any[HeaderCarrier], any[ExecutionContext]))
    //          .thenReturn(Future.successful(Right(SafeId("mockedUpeSafeId"))))
    //
    //        when(
    //          mockSubscriptionService
    //            .checkAndCreateSubscription(any[String], any[String], any[Option[String]])(any[HeaderCarrier], any[ExecutionContext])
    //        )
    //          .thenReturn(Future.successful(Right(validSubscriptionSuccessResponse)))
    //
    //        when(mockTaxEnrolmentService.checkAndCreateEnrolment(any[EnrolmentInfo]())(any[HeaderCarrier], any[ExecutionContext]))
    //          .thenReturn(Future.successful(Right(200)))
    //
    //        val traitUnderTest = new RegisterAndSubscribe {
    //          override val registerWithoutIdService = mockRegisterWithoutIdService
    //          override val subscriptionService      = mockSubscriptionService
    //          override val userAnswersConnectors    = mockUserAnswersConnectors
    //          override val taxEnrolmentService      = mockTaxEnrolmentService
    //        }
    //
    //        val result = traitUnderTest.createRegistrationAndSubscription(
    //          upeCheckAnswerData,
    //          filingMember
    //        )(
    //          HeaderCarrier(),
    //          ExecutionContext.global,
    //          dataRequest
    //        )
    //
    //        status(result) shouldBe SEE_OTHER
    //
    //      }
    //
    //      "return SEE_OTHER when sendFmRegistrationWithoutId returns a Left value" in {
    //
    //        val filingMember =
    //          FilingMember(
    //            nfmConfirmation = true,
    //            isNfmRegisteredInUK = Some(false),
    //            isNFMnStatus = RowStatus.InProgress,
    //            withoutIdRegData = Some(
    //              WithoutIdNfmData(
    //                registeredFmName = "Nfm name ",
    //                fmContactName = Some("Ashley Smith"),
    //                fmEmailAddress = Some("test@test.com"),
    //                contactNfmByTelephone = Some(true),
    //                telephoneNumber = Some("122223444"),
    //                registeredFmAddress = Some(
    //                  NfmRegisteredAddress(
    //                    addressLine1 = "1",
    //                    addressLine2 = Some("2"),
    //                    addressLine3 = "3",
    //                    addressLine4 = Some("4"),
    //                    postalCode = Some("5"),
    //                    countryCode = "GB"
    //                  )
    //                )
    //              )
    //            )
    //          )
    //
    //        when(mockRegisterWithoutIdService.sendFmRegistrationWithoutId(anyString, any[UserAnswers])(any[HeaderCarrier], any[ExecutionContext]))
    //          .thenReturn(Future.successful(Left(MandatoryInformationMissingError("Mandatory Information Missing Error"))))
    //
    //        val traitUnderTest = new RegisterAndSubscribe {
    //          override val registerWithoutIdService = mockRegisterWithoutIdService
    //          override val subscriptionService      = mockSubscriptionService
    //          override val userAnswersConnectors    = mockUserAnswersConnectors
    //          override val taxEnrolmentService      = mockTaxEnrolmentService
    //        }
    //
    //        val result = traitUnderTest.createRegistrationAndSubscription(
    //          upeCheckAnswerData,
    //          filingMember
    //        )(
    //          HeaderCarrier(),
    //          ExecutionContext.global,
    //          dataRequest
    //        )
    //
    //        status(result)           shouldBe SEE_OTHER
    //        redirectLocation(result) shouldBe Some(routes.UnderConstructionController.onPageLoad.url)
    //      }
    //
    //      "return SEE_OTHER when sendUpeRegistrationWithoutId returns a Left value" in {
    //
    //        val filingMember =
    //          FilingMember(
    //            nfmConfirmation = false,
    //            isNfmRegisteredInUK = Some(false),
    //            isNFMnStatus = RowStatus.InProgress,
    //            withoutIdRegData = Some(
    //              WithoutIdNfmData(
    //                registeredFmName = "Nfm name ",
    //                fmContactName = Some("Ashley Smith"),
    //                fmEmailAddress = Some("test@test.com"),
    //                contactNfmByTelephone = Some(true),
    //                telephoneNumber = Some("122223444"),
    //                registeredFmAddress = Some(
    //                  NfmRegisteredAddress(
    //                    addressLine1 = "1",
    //                    addressLine2 = Some("2"),
    //                    addressLine3 = "3",
    //                    addressLine4 = Some("4"),
    //                    postalCode = Some("5"),
    //                    countryCode = "GB"
    //                  )
    //                )
    //              )
    //            )
    //          )
    //
    //        when(mockRegisterWithoutIdService.sendUpeRegistrationWithoutId(anyString, any[UserAnswers])(any[HeaderCarrier], any[ExecutionContext]))
    //          .thenReturn(Future.successful(Left(MandatoryInformationMissingError("Mandatory Information Missing Error"))))
    //
    //        val traitUnderTest = new RegisterAndSubscribe {
    //          override val registerWithoutIdService = mockRegisterWithoutIdService
    //          override val subscriptionService      = mockSubscriptionService
    //          override val userAnswersConnectors    = mockUserAnswersConnectors
    //          override val taxEnrolmentService      = mockTaxEnrolmentService
    //        }
    //
    //        val result = traitUnderTest.createRegistrationAndSubscription(
    //          upeCheckAnswerData,
    //          filingMember
    //        )(
    //          HeaderCarrier(),
    //          ExecutionContext.global,
    //          dataRequest
    //        )
    //
    //        status(result)           shouldBe SEE_OTHER
    //        redirectLocation(result) shouldBe Some(routes.UnderConstructionController.onPageLoad.url)
    //      }
    //
    //      "return SEE_OTHER when checkAndCreateSubscription returns a Left value" in {
    //
    //        val filingMember =
    //          FilingMember(
    //            nfmConfirmation = false,
    //            isNfmRegisteredInUK = Some(false),
    //            isNFMnStatus = RowStatus.InProgress,
    //            withoutIdRegData = Some(
    //              WithoutIdNfmData(
    //                registeredFmName = "Nfm name ",
    //                fmContactName = Some("Ashley Smith"),
    //                fmEmailAddress = Some("test@test.com"),
    //                contactNfmByTelephone = Some(true),
    //                telephoneNumber = Some("122223444"),
    //                registeredFmAddress = Some(
    //                  NfmRegisteredAddress(
    //                    addressLine1 = "1",
    //                    addressLine2 = Some("2"),
    //                    addressLine3 = "3",
    //                    addressLine4 = Some("4"),
    //                    postalCode = Some("5"),
    //                    countryCode = "GB"
    //                  )
    //                )
    //              )
    //            )
    //          )
    //
    //        when(
    //          mockSubscriptionService.checkAndCreateSubscription(anyString, any[String], any[Option[String]])(any[HeaderCarrier], any[ExecutionContext])
    //        )
    //          .thenReturn(Future.successful(Left(SubscriptionCreateError)))
    //
    //        val traitUnderTest = new RegisterAndSubscribe {
    //          override val registerWithoutIdService = mockRegisterWithoutIdService
    //          override val subscriptionService      = mockSubscriptionService
    //          override val userAnswersConnectors    = mockUserAnswersConnectors
    //          override val taxEnrolmentService      = mockTaxEnrolmentService
    //        }
    //
    //        val result = traitUnderTest.createRegistrationAndSubscription(
    //          upeCheckAnswerData,
    //          filingMember
    //        )(
    //          HeaderCarrier(),
    //          ExecutionContext.global,
    //          dataRequest
    //        )
    //
    //        status(result)           shouldBe SEE_OTHER
    //        redirectLocation(result) shouldBe Some(routes.UnderConstructionController.onPageLoad.url)
    //      }
    //
    //    }
    //
    //  }
  }
}
