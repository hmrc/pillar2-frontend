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
import models.registration.{Registration, RegistrationInfo, WithoutIdRegData}
import models.requests.DataRequest
import models.subscription.{Subscription, SubscriptionResponse, SuccessResponse}
import models.{ApiError, EnrolmentCreationError, EnrolmentExistsError, EnrolmentInfo, MandatoryInformationMissingError, MneOrDomestic, NormalMode, RegistrationWithoutIdInformationMissingError, SafeId, SubscriptionCreateError, UpeRegisteredAddress, UserAnswers}
import org.mockito.Mockito.{reset, times, verify, when}
import play.api.mvc._
import uk.gov.hmrc.auth.core._
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier
import viewmodels.govuk.SummaryListFluency
import views.html.CheckYourAnswersView
import models.fm.{FilingMember, NfmRegisteredAddress, WithoutIdNfmData}
import org.mockito.ArgumentMatchers.{any, anyString}
import org.scalatest.AppendedClues.convertToClueful
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import pages.{NominatedFilingMemberPage, RegistrationPage, SubscriptionPage}
import play.api.libs.json.Json
import play.inject.guice.GuiceApplicationBuilder
import services.SubscriptionService
import uk.gov.hmrc.auth.core.{AffinityGroup, Assistant, CredentialRole, Enrolments}
import uk.gov.hmrc.auth.core.AffinityGroup.Individual
import uk.gov.hmrc.auth.core.retrieve.Retrieval
import utils.{RegistrationType, RowStatus}

import java.time.LocalDate
import scala.concurrent.{ExecutionContext, Future}

class CheckYourAnswersControllerSpec extends SpecBase with SummaryListFluency {

  val controller = new CheckYourAnswersController(
    mockMessagesApi,
    mockIdentifierAction,
    mockDataRetrievalAction,
    mockDataRequiredAction,
    mockRegisterWithoutIdService,
    mockSubscriptionService,
    mockUserAnswersConnectors,
    mockTaxEnrolmentService,
    mockControllerComponents,
    mockCheckYourAnswersView
  )(mockFrontendAppConfig)

  "Check Your Answers Controller" must {

    "createRegistrationAndSubscription" must {

      implicit val hc:          HeaderCarrier           = HeaderCarrier()
      implicit val ec:          ExecutionContext        = ExecutionContext.global
      implicit val dataRequest: DataRequest[AnyContent] = DataRequest(FakeRequest(), "sessionId", emptyUserAnswers)

      "handle scenario where RegistrationInfo is present but FilingMember SafeId is absent" when {

        "filingMember has nfmConfirmation as true" in {

          when(mockRegisterWithoutIdService.sendUpeRegistrationWithoutId(anyString, any[UserAnswers])(any[HeaderCarrier], any[ExecutionContext]))
            .thenReturn(Future.successful(Right(SafeId("mockedSafeId"))))

          when(
            mockSubscriptionService
              .checkAndCreateSubscription(any[String], any[String], any[Option[String]])(any[HeaderCarrier], any[ExecutionContext])
          )
            .thenReturn(Future.successful(Right(validSubscriptionSuccessResponse)))

          when(mockRegisterWithoutIdService.sendFmRegistrationWithoutId(anyString, any[UserAnswers])(any[HeaderCarrier], any[ExecutionContext]))
            .thenReturn(Future.successful(Right(SafeId("XMPLR0012345678"))))

          when(mockTaxEnrolmentService.checkAndCreateEnrolment(any[EnrolmentInfo]())(any[HeaderCarrier], any[ExecutionContext]))
            .thenReturn(Future.successful(Right(200)))

          val traitUnderTest = new RegisterAndSubscribe {
            override val registerWithoutIdService = mockRegisterWithoutIdService
            override val subscriptionService      = mockSubscriptionService
            override val userAnswersConnectors    = mockUserAnswersConnectors
            override val taxEnrolmentService      = mockTaxEnrolmentService
          }

          val sampleRegistrationInfo = RegistrationInfo(
            crn = "CRN123456",
            utr = "UTR654321",
            safeId = "SAFEID789012"
          )

          val registration = Registration(
            isUPERegisteredInUK = false,
            isRegistrationStatus = RowStatus.InProgress,
            withoutIdRegData = Some(
              WithoutIdRegData(
                upeNameRegistration = "Paddington",
                upeContactName = Some("Paddington ltd"),
                contactUpeByTelephone = Some(false),
                emailAddress = Some("example@gmail.com"),
                upeRegisteredAddress = Some(
                  UpeRegisteredAddress(
                    addressLine1 = "1",
                    addressLine2 = Some("2"),
                    addressLine3 = "3",
                    addressLine4 = Some("4"),
                    postalCode = "5",
                    countryCode = "GB"
                  )
                )
              )
            ),
            registrationInfo = Some(sampleRegistrationInfo)
          )

          val filingMember =
            FilingMember(
              nfmConfirmation = true,
              isNfmRegisteredInUK = Some(false),
              isNFMnStatus = RowStatus.InProgress,
              withoutIdRegData = Some(
                WithoutIdNfmData(
                  registeredFmName = "Nfm name ",
                  fmContactName = Some("Ashley Smith"),
                  fmEmailAddress = Some("test@test.com"),
                  contactNfmByTelephone = Some(true),
                  telephoneNumber = Some("122223444"),
                  registeredFmAddress = Some(
                    NfmRegisteredAddress(
                      addressLine1 = "1",
                      addressLine2 = Some("2"),
                      addressLine3 = "3",
                      addressLine4 = Some("4"),
                      postalCode = Some("5"),
                      countryCode = "GB"
                    )
                  )
                )
              )
            )

          val result = traitUnderTest.createRegistrationAndSubscription(registration, filingMember)(
            HeaderCarrier(),
            ExecutionContext.global,
            dataRequest
          )

          status(result) shouldBe SEE_OTHER

        }

        "filingMember has nfmConfirmation as false" in {

          when(mockRegisterWithoutIdService.sendUpeRegistrationWithoutId(anyString, any[UserAnswers])(any[HeaderCarrier], any[ExecutionContext]))
            .thenReturn(Future.successful(Right(SafeId("mockedSafeId"))))

          when(
            mockSubscriptionService
              .checkAndCreateSubscription(any[String], any[String], any[Option[String]])(any[HeaderCarrier], any[ExecutionContext])
          )
            .thenReturn(Future.successful(Right(validSubscriptionSuccessResponse)))

          when(mockRegisterWithoutIdService.sendFmRegistrationWithoutId(anyString, any[UserAnswers])(any[HeaderCarrier], any[ExecutionContext]))
            .thenReturn(Future.successful(Right(SafeId("XMPLR0012345678"))))

          val successfulEnrolmentResponse = NO_CONTENT
          when(mockTaxEnrolmentService.checkAndCreateEnrolment(any[EnrolmentInfo])(any[HeaderCarrier], any[ExecutionContext]))
            .thenReturn(Future.successful(Right(successfulEnrolmentResponse)))

          val traitUnderTest = new RegisterAndSubscribe {
            override val registerWithoutIdService = mockRegisterWithoutIdService
            override val subscriptionService      = mockSubscriptionService
            override val userAnswersConnectors    = mockUserAnswersConnectors
            override val taxEnrolmentService      = mockTaxEnrolmentService
          }

          val sampleRegistrationInfo = RegistrationInfo(
            crn = "CRN123456",
            utr = "UTR654321",
            safeId = "SAFEID789012"
          )

          val registration = Registration(
            isUPERegisteredInUK = false,
            isRegistrationStatus = RowStatus.InProgress,
            withoutIdRegData = Some(
              WithoutIdRegData(
                upeNameRegistration = "Paddington",
                upeContactName = Some("Paddington ltd"),
                contactUpeByTelephone = Some(false),
                emailAddress = Some("example@gmail.com"),
                upeRegisteredAddress = Some(
                  UpeRegisteredAddress(
                    addressLine1 = "1",
                    addressLine2 = Some("2"),
                    addressLine3 = "3",
                    addressLine4 = Some("4"),
                    postalCode = "5",
                    countryCode = "GB"
                  )
                )
              )
            ),
            registrationInfo = Some(sampleRegistrationInfo)
          )

          val filingMember =
            FilingMember(
              nfmConfirmation = false,
              isNfmRegisteredInUK = Some(false),
              isNFMnStatus = RowStatus.InProgress,
              withoutIdRegData = Some(
                WithoutIdNfmData(
                  registeredFmName = "Nfm name ",
                  fmContactName = Some("Ashley Smith"),
                  fmEmailAddress = Some("test@test.com"),
                  contactNfmByTelephone = Some(true),
                  telephoneNumber = Some("122223444"),
                  registeredFmAddress = Some(
                    NfmRegisteredAddress(
                      addressLine1 = "1",
                      addressLine2 = Some("2"),
                      addressLine3 = "3",
                      addressLine4 = Some("4"),
                      postalCode = Some("5"),
                      countryCode = "GB"
                    )
                  )
                )
              )
            )

          val result = traitUnderTest.createRegistrationAndSubscription(registration, filingMember)(
            HeaderCarrier(),
            ExecutionContext.global,
            dataRequest
          )

          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(
            routes.RegistrationConfirmationController.onPageLoad.url
          )

        }
      }

      "handle scenario where RegistrationInfo is present, FilingMember SafeId is absent, and nfmConfirmation is true" in {

        when(mockRegisterWithoutIdService.sendUpeRegistrationWithoutId(anyString, any[UserAnswers])(any[HeaderCarrier], any[ExecutionContext]))
          .thenReturn(Future.successful(Right(SafeId("mockedSafeId"))))

        when(
          mockSubscriptionService
            .checkAndCreateSubscription(any[String], any[String], any[Option[String]])(any[HeaderCarrier], any[ExecutionContext])
        )
          .thenReturn(Future.successful(Right(validSubscriptionSuccessResponse)))

        when(mockRegisterWithoutIdService.sendFmRegistrationWithoutId(anyString, any[UserAnswers])(any[HeaderCarrier], any[ExecutionContext]))
          .thenReturn(Future.successful(Right(SafeId("XMPLR0012345678"))))

        when(mockTaxEnrolmentService.checkAndCreateEnrolment(any[EnrolmentInfo]())(any[HeaderCarrier], any[ExecutionContext]))
          .thenReturn(Future.successful(Right(200)))

        val traitUnderTest = new RegisterAndSubscribe {
          override val registerWithoutIdService = mockRegisterWithoutIdService
          override val subscriptionService      = mockSubscriptionService
          override val userAnswersConnectors    = mockUserAnswersConnectors
          override val taxEnrolmentService      = mockTaxEnrolmentService
        }

        val sampleRegistrationInfo = RegistrationInfo(
          crn = "CRN123456",
          utr = "UTR654321",
          safeId = "SAFEID789012"
        )

        val registration = Registration(
          isUPERegisteredInUK = false,
          isRegistrationStatus = RowStatus.InProgress,
          withoutIdRegData = Some(
            WithoutIdRegData(
              upeNameRegistration = "Paddington",
              upeContactName = Some("Paddington ltd"),
              contactUpeByTelephone = Some(false),
              emailAddress = Some("example@gmail.com"),
              upeRegisteredAddress = Some(
                UpeRegisteredAddress(
                  addressLine1 = "1",
                  addressLine2 = Some("2"),
                  addressLine3 = "3",
                  addressLine4 = Some("4"),
                  postalCode = "5",
                  countryCode = "GB"
                )
              )
            )
          ),
          registrationInfo = Some(sampleRegistrationInfo)
        )

        val filingMember =
          FilingMember(
            nfmConfirmation = true,
            isNfmRegisteredInUK = Some(false),
            isNFMnStatus = RowStatus.InProgress,
            withoutIdRegData = Some(
              WithoutIdNfmData(
                registeredFmName = "Nfm name ",
                fmContactName = Some("Ashley Smith"),
                fmEmailAddress = Some("test@test.com"),
                contactNfmByTelephone = Some(true),
                telephoneNumber = Some("122223444"),
                registeredFmAddress = Some(
                  NfmRegisteredAddress(
                    addressLine1 = "1",
                    addressLine2 = Some("2"),
                    addressLine3 = "3",
                    addressLine4 = Some("4"),
                    postalCode = Some("5"),
                    countryCode = "GB"
                  )
                )
              )
            )
          )

        val result = traitUnderTest.createRegistrationAndSubscription(registration, filingMember)(
          HeaderCarrier(),
          ExecutionContext.global,
          dataRequest
        )

        status(result) shouldBe SEE_OTHER
        redirectLocation(result) shouldBe Some(
          routes.RegistrationConfirmationController.onPageLoad.url
        )

      }

      "handle scenario where RegistrationInfo is absent but FilingMember SafeId is present" in {

        when(mockRegisterWithoutIdService.sendUpeRegistrationWithoutId(anyString, any[UserAnswers])(any[HeaderCarrier], any[ExecutionContext]))
          .thenReturn(Future.successful(Right(SafeId("mockedSafeId"))))

        when(
          mockSubscriptionService
            .checkAndCreateSubscription(any[String], any[String], any[Option[String]])(any[HeaderCarrier], any[ExecutionContext])
        )
          .thenReturn(Future.successful(Right(validSubscriptionSuccessResponse)))

        when(mockTaxEnrolmentService.checkAndCreateEnrolment(any[EnrolmentInfo]())(any[HeaderCarrier], any[ExecutionContext]))
          .thenReturn(Future.successful(Right(200)))

        val traitUnderTest = new RegisterAndSubscribe {
          override val registerWithoutIdService = mockRegisterWithoutIdService
          override val subscriptionService      = mockSubscriptionService
          override val userAnswersConnectors    = mockUserAnswersConnectors
          override val taxEnrolmentService      = mockTaxEnrolmentService
        }

        val registration = Registration(
          isUPERegisteredInUK = false,
          isRegistrationStatus = RowStatus.InProgress,
          withoutIdRegData = Some(
            WithoutIdRegData(
              upeNameRegistration = "Paddington",
              upeContactName = Some("Paddington ltd"),
              contactUpeByTelephone = Some(false),
              emailAddress = Some("example@gmail.com"),
              upeRegisteredAddress = Some(
                UpeRegisteredAddress(
                  addressLine1 = "1",
                  addressLine2 = Some("2"),
                  addressLine3 = "3",
                  addressLine4 = Some("4"),
                  postalCode = "5",
                  countryCode = "GB"
                )
              )
            )
          ),
          registrationInfo = None
        )

        val filingMember =
          FilingMember(
            nfmConfirmation = false,
            isNfmRegisteredInUK = Some(false),
            isNFMnStatus = RowStatus.InProgress,
            withoutIdRegData = Some(
              WithoutIdNfmData(
                registeredFmName = "Nfm name ",
                fmContactName = Some("Ashley Smith"),
                fmEmailAddress = Some("test@test.com"),
                contactNfmByTelephone = Some(true),
                telephoneNumber = Some("122223444"),
                registeredFmAddress = Some(
                  NfmRegisteredAddress(
                    addressLine1 = "1",
                    addressLine2 = Some("2"),
                    addressLine3 = "3",
                    addressLine4 = Some("4"),
                    postalCode = Some("5"),
                    countryCode = "GB"
                  )
                )
              )
            ),
            safeId = Some("1234")
          )

        val result = traitUnderTest.createRegistrationAndSubscription(registration, filingMember)(
          HeaderCarrier(),
          ExecutionContext.global,
          dataRequest
        )

        status(result) shouldBe SEE_OTHER
        redirectLocation(result) shouldBe Some(
          routes.RegistrationConfirmationController.onPageLoad.url
        )

      }

      "handle scenario where RegistrationInfo is present but FilingMember SafeId is absent and nfmConfirmation is true" in {

        when(mockRegisterWithoutIdService.sendFmRegistrationWithoutId(anyString, any[UserAnswers])(any[HeaderCarrier], any[ExecutionContext]))
          .thenReturn(Future.successful(Right(SafeId("XMPLR0012345678"))))

        when(mockTaxEnrolmentService.checkAndCreateEnrolment(any[EnrolmentInfo]())(any[HeaderCarrier], any[ExecutionContext]))
          .thenReturn(Future.successful(Right(200)))

        // 3. Call the method
        val result = controller.createRegistrationAndSubscription(mockRegistration, mockFilingMember)

        // 4. Verify the expected outcome
        status(result)           shouldBe SEE_OTHER // or whatever your expected status code is
        redirectLocation(result) shouldBe Some(routes.UnderConstructionController.onPageLoad.url)

      }

      "redirect to RegistrationConfirmationController when registration info and FM Safe ID are provided" in {
        when(mockAuthConnector.authorise[Option[String]](any(), any[Retrieval[Option[String]]]())(any[HeaderCarrier], any[ExecutionContext]))
          .thenReturn(Future.successful(Some(userAnswersId)))
        when(mockAuthConnector.authorise[Enrolments](any(), any[Retrieval[Enrolments]]())(any[HeaderCarrier], any[ExecutionContext]))
          .thenReturn(Future.successful(Enrolments(Set())))
        when(
          mockAuthConnector
            .authorise[Option[AffinityGroup]](any(), any[Retrieval[Option[AffinityGroup]]]())(any[HeaderCarrier], any[ExecutionContext])
        )
          .thenReturn(Future.successful(Some(Individual)))
        when(
          mockAuthConnector
            .authorise[Option[CredentialRole]](any(), any[Retrieval[Option[CredentialRole]]]())(any[HeaderCarrier], any[ExecutionContext])
        )
          .thenReturn(Future.successful(Some(Assistant)))

        when(mockUserAnswersConnectors.getUserAnswer(userAnswersId)).thenReturn(Future.successful(Some(emptyUserAnswers)))
        when(
          mockAuthConnector
            .authorise[Option[AffinityGroup]](any(), any[Retrieval[Option[AffinityGroup]]]())(any[HeaderCarrier], any[ExecutionContext])
        )
          .thenReturn(Future.successful(Some(Individual)))
        when(
          mockAuthConnector
            .authorise[Option[CredentialRole]](any(), any[Retrieval[Option[CredentialRole]]]())(any[HeaderCarrier], any[ExecutionContext])
        )
          .thenReturn(Future.successful(Some(Assistant)))

        when(
          mockSubscriptionService
            .checkAndCreateSubscription(any[String], any[String], any[Option[String]])(any[HeaderCarrier], any[ExecutionContext])
        )
          .thenReturn(Future.successful(Right(validSubscriptionSuccessResponse)))

        when(mockTaxEnrolmentService.checkAndCreateEnrolment(any[EnrolmentInfo]())(any[HeaderCarrier], any[ExecutionContext]))
          .thenReturn(Future.successful(Right(200)))

        val sampleRegistrationInfo = RegistrationInfo(
          crn = "CRN123456",
          utr = "UTR654321",
          safeId = "SAFEID789012"
        )

        val ukBased =
          Registration(isUPERegisteredInUK = true, isRegistrationStatus = RowStatus.InProgress, registrationInfo = Some(sampleRegistrationInfo))
        val userAnswers = UserAnswers(userAnswersId)
          .set(RegistrationPage, ukBased)
          .success
          .value
          .set(NominatedFilingMemberPage, FilingMember(nfmConfirmation = true, isNFMnStatus = RowStatus.Completed, safeId = Some("fmSafeId")))
          .success
          .value
          .set(
            SubscriptionPage,
            Subscription(
              MneOrDomestic.Uk,
              contactDetailsStatus = RowStatus.InProgress,
              groupDetailStatus = RowStatus.Completed,
              primaryContactName = Some("asd")
            )
          )
          .success
          .value

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()
        running(application) {
          val request = FakeRequest(POST, controllers.routes.CheckYourAnswersController.onSubmit.url)
            .withFormUrlEncodedBody(("value", "true"))

          val result = route(application, request).value
          status(result) shouldBe SEE_OTHER
        }
      }

      "return SEE_OTHER (Redirect) when both registrationInfo and safeId are present" in {
        when(
          mockSubscriptionService
            .checkAndCreateSubscription(any[String], any[String], any[Option[String]])(any[HeaderCarrier], any[ExecutionContext])
        )
          .thenReturn(Future.successful(Right(validSubscriptionSuccessResponse)))

        when(mockRegisterWithoutIdService.sendUpeRegistrationWithoutId(anyString, any[UserAnswers])(any[HeaderCarrier], any[ExecutionContext]))
          .thenReturn(Future.successful(Right(SafeId("mockedSafeId"))))

        when(mockRegisterWithoutIdService.sendFmRegistrationWithoutId(anyString, any[UserAnswers])(any[HeaderCarrier], any[ExecutionContext]))
          .thenReturn(Future.successful(Right(SafeId("mockedSafeId"))))

        val successfulEnrolmentResponse = NO_CONTENT
        when(mockTaxEnrolmentService.checkAndCreateEnrolment(any[EnrolmentInfo])(any[HeaderCarrier], any[ExecutionContext]))
          .thenReturn(Future.successful(Right(successfulEnrolmentResponse)))

        val traitUnderTest = new RegisterAndSubscribe {
          override val registerWithoutIdService = mockRegisterWithoutIdService
          override val subscriptionService      = mockSubscriptionService
          override val userAnswersConnectors    = mockUserAnswersConnectors
          override val taxEnrolmentService      = mockTaxEnrolmentService
        }

        val result = traitUnderTest.createRegistrationAndSubscription(upeCheckAnswerDataWithoutPhone, nfmCheckAnswerData)(hc, ec, dataRequest)

        status(result) mustBe SEE_OTHER
      }

      "return SEE_OTHER when registrationInfo is present, safeId is not, and nfmConfirmation is true" in {

        when(mockRegisterWithoutIdService.sendFmRegistrationWithoutId(anyString, any[UserAnswers])(any[HeaderCarrier], any[ExecutionContext]))
          .thenReturn(Future.successful(Right(SafeId("mockedSafeId"))))

        when(
          mockSubscriptionService
            .checkAndCreateSubscription(any[String], any[String], any[Option[String]])(any[HeaderCarrier], any[ExecutionContext])
        )
          .thenReturn(Future.successful(Right(validSubscriptionSuccessResponse))) // Mock a successful subscription response

        when(mockTaxEnrolmentService.checkAndCreateEnrolment(any[EnrolmentInfo]())(any[HeaderCarrier], any[ExecutionContext]))
          .thenReturn(Future.successful(Right(200)))

        val traitUnderTest = new RegisterAndSubscribe {
          override val registerWithoutIdService = mockRegisterWithoutIdService
          override val subscriptionService      = mockSubscriptionService
          override val userAnswersConnectors    = mockUserAnswersConnectors
          override val taxEnrolmentService      = mockTaxEnrolmentService
        }

        val result = traitUnderTest.createRegistrationAndSubscription(upeCheckAnswerDataWithoutPhone, nfmCheckAnswerData())(
          HeaderCarrier(),
          ExecutionContext.global,
          dataRequest
        )
        status(result) mustBe SEE_OTHER
      }

      "return SEE_OTHER when registrationInfo is present, safeId is not, and nfmConfirmation is false" in {

        val filingMember =
          FilingMember(
            nfmConfirmation = false,
            isNfmRegisteredInUK = Some(false),
            isNFMnStatus = RowStatus.InProgress,
            withoutIdRegData = Some(
              WithoutIdNfmData(
                registeredFmName = "Nfm name ",
                fmContactName = Some("Ashley Smith"),
                fmEmailAddress = Some("test@test.com"),
                contactNfmByTelephone = Some(true),
                telephoneNumber = Some("122223444"),
                registeredFmAddress = Some(
                  NfmRegisteredAddress(
                    addressLine1 = "1",
                    addressLine2 = Some("2"),
                    addressLine3 = "3",
                    addressLine4 = Some("4"),
                    postalCode = Some("5"),
                    countryCode = "GB"
                  )
                )
              )
            )
          )

        when(mockRegisterWithoutIdService.sendFmRegistrationWithoutId(anyString, any[UserAnswers])(any[HeaderCarrier], any[ExecutionContext]))
          .thenReturn(Future.successful(Right(SafeId("mockedSafeId"))))

        when(
          mockSubscriptionService
            .checkAndCreateSubscription(any[String], any[String], any[Option[String]])(any[HeaderCarrier], any[ExecutionContext])
        )
          .thenReturn(Future.successful(Right(validSubscriptionSuccessResponse)))

        when(mockTaxEnrolmentService.checkAndCreateEnrolment(any[EnrolmentInfo]())(any[HeaderCarrier], any[ExecutionContext]))
          .thenReturn(Future.successful(Right(200)))

        val traitUnderTest = new RegisterAndSubscribe {
          override val registerWithoutIdService = mockRegisterWithoutIdService
          override val subscriptionService      = mockSubscriptionService
          override val userAnswersConnectors    = mockUserAnswersConnectors
          override val taxEnrolmentService      = mockTaxEnrolmentService
        }

        val result = traitUnderTest.createRegistrationAndSubscription(
          upeCheckAnswerDataWithoutPhone,
          filingMember
        )(
          HeaderCarrier(),
          ExecutionContext.global,
          dataRequest
        )

        status(result)           shouldBe SEE_OTHER
        redirectLocation(result) shouldBe Some(routes.RegistrationConfirmationController.onPageLoad.url)

      }

      "return SEE_OTHER when registrationInfo is not present and safeId is present" in {

        when(mockRegisterWithoutIdService.sendUpeRegistrationWithoutId(anyString, any[UserAnswers])(any[HeaderCarrier], any[ExecutionContext]))
          .thenReturn(Future.successful(Right(SafeId("mockedUpeSafeId"))))

        when(
          mockSubscriptionService
            .checkAndCreateSubscription(any[String], any[String], any[Option[String]])(any[HeaderCarrier], any[ExecutionContext])
        )
          .thenReturn(
            Future.successful(Right(validSubscriptionSuccessResponse))
          )

        when(mockTaxEnrolmentService.checkAndCreateEnrolment(any[EnrolmentInfo]())(any[HeaderCarrier], any[ExecutionContext]))
          .thenReturn(Future.successful(Right(200)))

        val traitUnderTest = new RegisterAndSubscribe {
          override val registerWithoutIdService = mockRegisterWithoutIdService
          override val subscriptionService      = mockSubscriptionService
          override val userAnswersConnectors    = mockUserAnswersConnectors
          override val taxEnrolmentService      = mockTaxEnrolmentService
        }

        val registration = Registration(
          isUPERegisteredInUK = false,
          isRegistrationStatus = RowStatus.InProgress,
          withoutIdRegData = Some(
            WithoutIdRegData(
              upeNameRegistration = "Paddington",
              upeContactName = Some("Paddington ltd"),
              contactUpeByTelephone = Some(false),
              emailAddress = Some("example@gmail.com"),
              upeRegisteredAddress = Some(
                UpeRegisteredAddress(
                  addressLine1 = "1",
                  addressLine2 = Some("2"),
                  addressLine3 = "3",
                  addressLine4 = Some("4"),
                  postalCode = "5",
                  countryCode = "GB"
                )
              )
            )
          ),
          registrationInfo = None
        )

        val filingMember =
          FilingMember(
            nfmConfirmation = false,
            isNfmRegisteredInUK = Some(false),
            isNFMnStatus = RowStatus.InProgress,
            withoutIdRegData = Some(
              WithoutIdNfmData(
                registeredFmName = "Nfm name ",
                fmContactName = Some("Ashley Smith"),
                fmEmailAddress = Some("test@test.com"),
                contactNfmByTelephone = Some(true),
                telephoneNumber = Some("122223444"),
                registeredFmAddress = Some(
                  NfmRegisteredAddress(
                    addressLine1 = "1",
                    addressLine2 = Some("2"),
                    addressLine3 = "3",
                    addressLine4 = Some("4"),
                    postalCode = Some("5"),
                    countryCode = "GB"
                  )
                )
              )
            )
          )

        val result = traitUnderTest.createRegistrationAndSubscription(registration, filingMember)(
          HeaderCarrier(),
          ExecutionContext.global,
          dataRequest
        )

        // Check the result
        status(result) shouldBe SEE_OTHER
        redirectLocation(result) shouldBe Some(
          routes.RegistrationConfirmationController.onPageLoad.url
        )
      }

      "return SEE_OTHER when neither registrationInfo nor safeId are present, and nfmConfirmation is true" in {
        val filingMember =
          FilingMember(
            nfmConfirmation = true,
            isNfmRegisteredInUK = Some(false),
            isNFMnStatus = RowStatus.InProgress,
            withoutIdRegData = Some(
              WithoutIdNfmData(
                registeredFmName = "Nfm name ",
                fmContactName = Some("Ashley Smith"),
                fmEmailAddress = Some("test@test.com"),
                contactNfmByTelephone = Some(true),
                telephoneNumber = Some("122223444"),
                registeredFmAddress = Some(
                  NfmRegisteredAddress(
                    addressLine1 = "1",
                    addressLine2 = Some("2"),
                    addressLine3 = "3",
                    addressLine4 = Some("4"),
                    postalCode = Some("5"),
                    countryCode = "GB"
                  )
                )
              )
            )
          )

        when(mockRegisterWithoutIdService.sendUpeRegistrationWithoutId(anyString, any[UserAnswers])(any[HeaderCarrier], any[ExecutionContext]))
          .thenReturn(Future.successful(Right(SafeId("mockedUpeSafeId"))))

        when(mockRegisterWithoutIdService.sendFmRegistrationWithoutId(anyString, any[UserAnswers])(any[HeaderCarrier], any[ExecutionContext]))
          .thenReturn(Future.successful(Right(SafeId("mockedFmSafeId"))))

        when(
          mockSubscriptionService
            .checkAndCreateSubscription(any[String], any[String], any[Option[String]])(any[HeaderCarrier], any[ExecutionContext])
        )
          .thenReturn(Future.successful(Right(validSubscriptionSuccessResponse)))

        when(mockTaxEnrolmentService.checkAndCreateEnrolment(any[EnrolmentInfo]())(any[HeaderCarrier], any[ExecutionContext]))
          .thenReturn(Future.successful(Right(200)))

        val traitUnderTest = new RegisterAndSubscribe {
          override val registerWithoutIdService = mockRegisterWithoutIdService
          override val subscriptionService      = mockSubscriptionService
          override val userAnswersConnectors    = mockUserAnswersConnectors
          override val taxEnrolmentService      = mockTaxEnrolmentService
        }

        val result = traitUnderTest.createRegistrationAndSubscription(
          upeCheckAnswerData,
          filingMember
        )(
          HeaderCarrier(),
          ExecutionContext.global,
          dataRequest
        )

        // Validate
        status(result) shouldBe SEE_OTHER
        redirectLocation(result) shouldBe Some(
          routes.RegistrationConfirmationController.onPageLoad.url
        )
      }

      "return SEE_OTHER when neither registrationInfo nor safeId are present, and nfmConfirmation is false" in {

        val filingMember =
          FilingMember(
            nfmConfirmation = false,
            isNfmRegisteredInUK = Some(false),
            isNFMnStatus = RowStatus.InProgress,
            withoutIdRegData = Some(
              WithoutIdNfmData(
                registeredFmName = "Nfm name ",
                fmContactName = Some("Ashley Smith"),
                fmEmailAddress = Some("test@test.com"),
                contactNfmByTelephone = Some(true),
                telephoneNumber = Some("122223444"),
                registeredFmAddress = Some(
                  NfmRegisteredAddress(
                    addressLine1 = "1",
                    addressLine2 = Some("2"),
                    addressLine3 = "3",
                    addressLine4 = Some("4"),
                    postalCode = Some("5"),
                    countryCode = "GB"
                  )
                )
              )
            )
          )

        when(mockRegisterWithoutIdService.sendUpeRegistrationWithoutId(anyString, any[UserAnswers])(any[HeaderCarrier], any[ExecutionContext]))
          .thenReturn(Future.successful(Right(SafeId("mockedUpeSafeId"))))

        when(
          mockSubscriptionService
            .checkAndCreateSubscription(any[String], any[String], any[Option[String]])(any[HeaderCarrier], any[ExecutionContext])
        )
          .thenReturn(Future.successful(Right(validSubscriptionSuccessResponse)))

        when(mockTaxEnrolmentService.checkAndCreateEnrolment(any[EnrolmentInfo]())(any[HeaderCarrier], any[ExecutionContext]))
          .thenReturn(Future.successful(Right(200)))

        val traitUnderTest = new RegisterAndSubscribe {
          override val registerWithoutIdService = mockRegisterWithoutIdService
          override val subscriptionService      = mockSubscriptionService
          override val userAnswersConnectors    = mockUserAnswersConnectors
          override val taxEnrolmentService      = mockTaxEnrolmentService
        }

        val result = traitUnderTest.createRegistrationAndSubscription(
          upeCheckAnswerData,
          filingMember
        )(
          HeaderCarrier(),
          ExecutionContext.global,
          dataRequest
        )

        status(result) shouldBe SEE_OTHER

      }

      "return SEE_OTHER when sendFmRegistrationWithoutId returns a Left value" in {

        val filingMember =
          FilingMember(
            nfmConfirmation = true,
            isNfmRegisteredInUK = Some(false),
            isNFMnStatus = RowStatus.InProgress,
            withoutIdRegData = Some(
              WithoutIdNfmData(
                registeredFmName = "Nfm name ",
                fmContactName = Some("Ashley Smith"),
                fmEmailAddress = Some("test@test.com"),
                contactNfmByTelephone = Some(true),
                telephoneNumber = Some("122223444"),
                registeredFmAddress = Some(
                  NfmRegisteredAddress(
                    addressLine1 = "1",
                    addressLine2 = Some("2"),
                    addressLine3 = "3",
                    addressLine4 = Some("4"),
                    postalCode = Some("5"),
                    countryCode = "GB"
                  )
                )
              )
            )
          )

        when(mockRegisterWithoutIdService.sendFmRegistrationWithoutId(anyString, any[UserAnswers])(any[HeaderCarrier], any[ExecutionContext]))
          .thenReturn(Future.successful(Left(MandatoryInformationMissingError("Mandatory Information Missing Error"))))

        val traitUnderTest = new RegisterAndSubscribe {
          override val registerWithoutIdService = mockRegisterWithoutIdService
          override val subscriptionService      = mockSubscriptionService
          override val userAnswersConnectors    = mockUserAnswersConnectors
          override val taxEnrolmentService      = mockTaxEnrolmentService
        }

        val result = traitUnderTest.createRegistrationAndSubscription(
          upeCheckAnswerData,
          filingMember
        )(
          HeaderCarrier(),
          ExecutionContext.global,
          dataRequest
        )

        status(result)           shouldBe SEE_OTHER
        redirectLocation(result) shouldBe Some(routes.UnderConstructionController.onPageLoad.url)
      }

      "return SEE_OTHER when sendUpeRegistrationWithoutId returns a Left value" in {

        val filingMember =
          FilingMember(
            nfmConfirmation = false,
            isNfmRegisteredInUK = Some(false),
            isNFMnStatus = RowStatus.InProgress,
            withoutIdRegData = Some(
              WithoutIdNfmData(
                registeredFmName = "Nfm name ",
                fmContactName = Some("Ashley Smith"),
                fmEmailAddress = Some("test@test.com"),
                contactNfmByTelephone = Some(true),
                telephoneNumber = Some("122223444"),
                registeredFmAddress = Some(
                  NfmRegisteredAddress(
                    addressLine1 = "1",
                    addressLine2 = Some("2"),
                    addressLine3 = "3",
                    addressLine4 = Some("4"),
                    postalCode = Some("5"),
                    countryCode = "GB"
                  )
                )
              )
            )
          )

        when(mockRegisterWithoutIdService.sendUpeRegistrationWithoutId(anyString, any[UserAnswers])(any[HeaderCarrier], any[ExecutionContext]))
          .thenReturn(Future.successful(Left(MandatoryInformationMissingError("Mandatory Information Missing Error"))))

        val traitUnderTest = new RegisterAndSubscribe {
          override val registerWithoutIdService = mockRegisterWithoutIdService
          override val subscriptionService      = mockSubscriptionService
          override val userAnswersConnectors    = mockUserAnswersConnectors
          override val taxEnrolmentService      = mockTaxEnrolmentService
        }

        val result = traitUnderTest.createRegistrationAndSubscription(
          upeCheckAnswerData,
          filingMember
        )(
          HeaderCarrier(),
          ExecutionContext.global,
          dataRequest
        )

        status(result)           shouldBe SEE_OTHER
        redirectLocation(result) shouldBe Some(routes.UnderConstructionController.onPageLoad.url)
      }

      "return SEE_OTHER when checkAndCreateSubscription returns a Left value" in {

        val filingMember =
          FilingMember(
            nfmConfirmation = false,
            isNfmRegisteredInUK = Some(false),
            isNFMnStatus = RowStatus.InProgress,
            withoutIdRegData = Some(
              WithoutIdNfmData(
                registeredFmName = "Nfm name ",
                fmContactName = Some("Ashley Smith"),
                fmEmailAddress = Some("test@test.com"),
                contactNfmByTelephone = Some(true),
                telephoneNumber = Some("122223444"),
                registeredFmAddress = Some(
                  NfmRegisteredAddress(
                    addressLine1 = "1",
                    addressLine2 = Some("2"),
                    addressLine3 = "3",
                    addressLine4 = Some("4"),
                    postalCode = Some("5"),
                    countryCode = "GB"
                  )
                )
              )
            )
          )

        when(
          mockSubscriptionService.checkAndCreateSubscription(anyString, any[String], any[Option[String]])(any[HeaderCarrier], any[ExecutionContext])
        )
          .thenReturn(Future.successful(Left(SubscriptionCreateError)))

        val traitUnderTest = new RegisterAndSubscribe {
          override val registerWithoutIdService = mockRegisterWithoutIdService
          override val subscriptionService      = mockSubscriptionService
          override val userAnswersConnectors    = mockUserAnswersConnectors
          override val taxEnrolmentService      = mockTaxEnrolmentService
        }

        val result = traitUnderTest.createRegistrationAndSubscription(
          upeCheckAnswerData,
          filingMember
        )(
          HeaderCarrier(),
          ExecutionContext.global,
          dataRequest
        )

        status(result)           shouldBe SEE_OTHER
        redirectLocation(result) shouldBe Some(routes.UnderConstructionController.onPageLoad.url)
      }

    }
  }
}
