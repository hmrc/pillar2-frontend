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
import helpers.SectionHash
import models.grs.{EntityType, GrsRegistrationResult, RegistrationStatus}
import models.registration._
import models.subscription.AccountingPeriod
import models.tasklist.SectionStatus
import models.{MneOrDomestic, NonUKAddress, UserAnswers}
import org.apache.pekko.Done
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import pages.UpeSectionConfirmationHashPage
import pages._
import play.api
import play.api.inject
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import utils.RowStatus

import java.time.LocalDate
import scala.concurrent.Future

class TaskListControllerSpec extends SpecBase {

  private val accountingPeriod = AccountingPeriod(LocalDate.now(), LocalDate.now())

  private val grsResponse = GrsResponse(
    Some(
      IncorporatedEntityRegistrationData(
        companyProfile = CompanyProfile(
          companyName = "ABC Limited",
          companyNumber = "1234",
          dateOfIncorporation = Some(LocalDate.now()),
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

  "Task List Controller" must {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = None)
        .overrides(
          bind[SessionRepository].toInstance(mockSessionRepository)
        )
        .build()

      running(application) {
        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(emptyUserAnswers)))
        val request = FakeRequest(GET, routes.TaskListController.onPageLoad.url)

        val result = route(application, request).value

        status(result) mustEqual OK

        contentAsString(result) should include(
          "Register your group"
        )
        contentAsString(result) should include(
          "Registration incomplete"
        )
        contentAsString(result) should include(
          "Review and submit"
        )
        contentAsString(result) should include(
          "Check your answers"
        )
        contentAsString(result) should include(
          "At the ‘Review and submit’ section of this registration, you can amend your answers and print or save them for your own records."
        )
      }
    }

    "redirect to tasklist if pillar 2 exists from read subscription API" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers.setOrException(PlrReferencePage, "1231")))
        .overrides(
          inject.bind[SessionRepository].toInstance(mockSessionRepository),
          inject.bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors)
        )
        .build()

      running(application) {
        when(mockSessionRepository.get(any())).thenReturn(Future.successful(None))
        when(mockUserAnswersConnectors.remove(any())(any())).thenReturn(Future.successful(Done))
        val request = FakeRequest(GET, routes.TaskListController.onPageLoad.url)

        val result = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.TaskListController.onPageLoad.url

      }
    }

    "redirected to subscription confirmation page if the user has already subscribed with a pillar 2 reference" in {
      val userAnswer = UserAnswers("id").setOrException(PlrReferencePage, "id")
      val application = applicationBuilder(None)
        .overrides(
          api.inject.bind[SessionRepository].toInstance(mockSessionRepository)
        )
        .build()
      running(application) {
        val request = FakeRequest(GET, controllers.routes.TaskListController.onPageLoad.url)
        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswer)))
        val result = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.RegistrationConfirmationController.onPageLoad.url
      }
    }

    "return OK and the correct view for a GET when all statuses are 'NotStarted'" in {
      val userAnswers = emptyUserAnswers
        .set(UpeRegisteredInUKPage, false)
        .success
        .value
        .set(NominateFilingMemberPage, false)
        .success
        .value

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          inject.bind[SessionRepository].toInstance(mockSessionRepository),
          inject.bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors)
        )
        .build()

      running(application) {
        when(mockSessionRepository.get(any())).thenReturn(Future.successful(None))
        when(mockUserAnswersConnectors.remove(any())(any())).thenReturn(Future.successful(Done))
        val request = FakeRequest(GET, routes.TaskListController.onPageLoad.url)
        val result  = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) should include("Register your group")
        contentAsString(result) should include("Registration incomplete")
      }
    }

    "build filingMemberInfo with 'edit' action when ultimateParentStatus is 'Completed' and filingMemberStatus is 'Completed'" in {
      val baseUa = emptyUserAnswers
        .setOrException(UpeRegisteredInUKPage, true)
        .setOrException(UpeEntityTypePage, EntityType.UkLimitedCompany)
        .setOrException(UpeGRSResponsePage, grsResponse)
        .setOrException(GrsUpeStatusPage, RowStatus.Completed)
        .setOrException(NominateFilingMemberPage, true)
        .setOrException(FmRegisteredInUKPage, true)
        .setOrException(FmEntityTypePage, EntityType.UkLimitedCompany)
        .setOrException(FmGRSResponsePage, grsResponse)
        .setOrException(GrsFilingMemberStatusPage, RowStatus.Completed)
      val userAnswers = baseUa.set(UpeSectionConfirmationHashPage, SectionHash.computeUpeHash(baseUa)).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          inject.bind[SessionRepository].toInstance(mockSessionRepository),
          inject.bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors)
        )
        .build()

      running(application) {
        when(mockSessionRepository.get(any())).thenReturn(Future.successful(None))
        when(mockUserAnswersConnectors.remove(any())(any())).thenReturn(Future.successful(Done))
        val request = FakeRequest(GET, routes.TaskListController.onPageLoad.url)
        val result  = route(application, request).value

        status(result) mustEqual OK

        val responseContent = contentAsString(result)
        responseContent should include("Edit Ultimate Parent Entity")
      }
    }

    "handle scenario when ultimateParentStatus is Completed and filingMemberStatus is NotStarted" in {
      val userAnswers = emptyUserAnswers
        .set(UpeRegisteredInUKPage, true)
        .success
        .value

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          inject.bind[SessionRepository].toInstance(mockSessionRepository),
          inject.bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors)
        )
        .build()

      running(application) {
        when(mockSessionRepository.get(any())).thenReturn(Future.successful(None))
        when(mockUserAnswersConnectors.remove(any())(any())).thenReturn(Future.successful(Done))
        val request = FakeRequest(GET, routes.TaskListController.onPageLoad.url)
        val result  = route(application, request).value

        status(result) mustEqual OK
        val responseContent = contentAsString(result)
        responseContent should include("Cannot start yet")
      }
    }

    "handle scenario when groupDetailStatus is Completed" in {
      val userAnswers = emptyUserAnswers
        .setOrException(UpeRegisteredInUKPage, true)
        .setOrException(NominateFilingMemberPage, true)
        .setOrException(SubMneOrDomesticPage, MneOrDomestic.Uk)
        .setOrException(SubAccountingPeriodPage, accountingPeriod)

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          inject.bind[SessionRepository].toInstance(mockSessionRepository),
          inject.bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors)
        )
        .build()

      running(application) {
        when(mockSessionRepository.get(any())).thenReturn(Future.successful(None))
        when(mockUserAnswersConnectors.remove(any())(any())).thenReturn(Future.successful(Done))
        val request = FakeRequest(GET, routes.TaskListController.onPageLoad.url)
        val result  = route(application, request).value

        status(result) mustEqual OK
        val responseContent = contentAsString(result)
        responseContent should include("Completed")
        responseContent should include("Edit further group details")
      }
    }

    "handle scenario when filingMemberStatus is Completed and groupDetailStatus is InProgress" in {
      val userAnswers = emptyUserAnswers
        .setOrException(UpeRegisteredInUKPage, true)
        .setOrException(NominateFilingMemberPage, true)
        .setOrException(FmRegisteredInUKPage, true)
        .setOrException(FmEntityTypePage, EntityType.UkLimitedCompany)
        .setOrException(FmGRSResponsePage, grsResponse)
        .setOrException(GrsFilingMemberStatusPage, RowStatus.Completed)
        .setOrException(SubMneOrDomesticPage, MneOrDomestic.Uk)

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          inject.bind[SessionRepository].toInstance(mockSessionRepository),
          inject.bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors)
        )
        .build()

      running(application) {
        when(mockSessionRepository.get(any())).thenReturn(Future.successful(None))
        when(mockUserAnswersConnectors.remove(any())(any())).thenReturn(Future.successful(Done))
        val request = FakeRequest(GET, routes.TaskListController.onPageLoad.url)
        val result  = route(application, request).value

        status(result) mustEqual OK
        val responseContent = contentAsString(result)

        responseContent should include("In progress")
        responseContent should include("Add further group details")
      }
    }

    "build groupDetailInfo with 'cannotStartYet' action when either filingMemberStatus or groupDetailStatus is 'InProgress'" in {
      val userAnswers = emptyUserAnswers
        .setOrException(UpeRegisteredInUKPage, true)
        .setOrException(UpeEntityTypePage, EntityType.UkLimitedCompany)
        .setOrException(UpeGRSResponsePage, grsResponse)
        .setOrException(GrsUpeStatusPage, RowStatus.Completed)
        .setOrException(NominateFilingMemberPage, true)
        .setOrException(FmRegisteredInUKPage, false)
        .setOrException(SubMneOrDomesticPage, MneOrDomestic.Uk)

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          inject.bind[SessionRepository].toInstance(mockSessionRepository),
          inject.bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors)
        )
        .build()

      running(application) {
        when(mockSessionRepository.get(any())).thenReturn(Future.successful(None))
        when(mockUserAnswersConnectors.remove(any())(any())).thenReturn(Future.successful(Done))
        val request = FakeRequest(GET, routes.TaskListController.onPageLoad.url)
        val result  = route(application, request).value

        status(result) mustEqual OK
        val responseContent = contentAsString(result)

        responseContent should include("Cannot start yet")
      }
    }

    "build groupDetailInfo with 'edit' action when groupDetailStatus is 'Completed'" in {
      val userAnswers = emptyUserAnswers
        .setOrException(SubMneOrDomesticPage, MneOrDomestic.Uk)
        .setOrException(SubAccountingPeriodPage, accountingPeriod)

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          inject.bind[SessionRepository].toInstance(mockSessionRepository),
          inject.bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors)
        )
        .build()

      running(application) {
        when(mockSessionRepository.get(any())).thenReturn(Future.successful(None))
        when(mockUserAnswersConnectors.remove(any())(any())).thenReturn(Future.successful(Done))
        val request = FakeRequest(GET, routes.TaskListController.onPageLoad.url)
        val result  = route(application, request).value

        status(result) mustEqual OK

        val responseContent = contentAsString(result)
        responseContent should include("Edit further group details")
      }
    }

    "build contactDetailsInfo with 'edit' action when all statuses are 'Completed'" in {
      val baseUa = emptyUserAnswers
        .setOrException(SubMneOrDomesticPage, MneOrDomestic.Uk)
        .setOrException(SubAccountingPeriodPage, accountingPeriod)
        .setOrException(UpeRegisteredInUKPage, true)
        .setOrException(UpeEntityTypePage, EntityType.UkLimitedCompany)
        .setOrException(UpeGRSResponsePage, grsResponse)
        .setOrException(GrsUpeStatusPage, RowStatus.Completed)
        .setOrException(NominateFilingMemberPage, true)
        .setOrException(FmRegisteredInUKPage, false)
        .setOrException(SubPrimaryContactNamePage, "name")
        .setOrException(SubRegisteredAddressPage, NonUKAddress("this", None, "over", None, None, countryCode = "AR"))
        .setOrException(SubMneOrDomesticPage, MneOrDomestic.Uk)
      val userAnswers = baseUa.set(UpeSectionConfirmationHashPage, SectionHash.computeUpeHash(baseUa)).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          inject.bind[SessionRepository].toInstance(mockSessionRepository),
          inject.bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors)
        )
        .build()

      running(application) {
        when(mockSessionRepository.get(any())).thenReturn(Future.successful(None))
        when(mockUserAnswersConnectors.remove(any())(any())).thenReturn(Future.successful(Done))
        val request = FakeRequest(GET, routes.TaskListController.onPageLoad.url)
        val result  = route(application, request).value
        status(result) mustEqual OK

        val controller             = application.injector.instanceOf[TaskListController]
        val groupDetailSection     = controller.groupSections(userAnswers)
        val contactDetailSection   = controller.contactSection(userAnswers)
        val reviewAndSubmitSection = controller.reviewSection(userAnswers)

        groupDetailSection.map(_.status) should contain theSameElementsAs Seq(
          SectionStatus.Completed,
          SectionStatus.InProgress,
          SectionStatus.Completed
        )
        contactDetailSection.status   shouldBe SectionStatus.InProgress
        contactDetailSection.name     shouldBe "taskList.task.contact.add"
        reviewAndSubmitSection.status shouldBe SectionStatus.CannotStart
      }
    }

    "build correct TaskInfo when ultimateParentStatus is 'Completed' and filingMemberStatus is 'Completed'" in {
      val baseUa = emptyUserAnswers
        .setOrException(SubMneOrDomesticPage, MneOrDomestic.Uk)
        .setOrException(SubAccountingPeriodPage, accountingPeriod)
        .setOrException(UpeRegisteredInUKPage, true)
        .setOrException(UpeEntityTypePage, EntityType.UkLimitedCompany)
        .setOrException(UpeGRSResponsePage, grsResponse)
        .setOrException(GrsUpeStatusPage, RowStatus.Completed)
        .setOrException(NominateFilingMemberPage, true)
        .setOrException(FmRegisteredInUKPage, false)
        .setOrException(FmNameRegistrationPage, "name")
        .setOrException(FmRegisteredAddressPage, NonUKAddress("this", None, "over", None, None, countryCode = "AR"))
        .setOrException(FmContactNamePage, "name")
        .setOrException(FmContactEmailPage, "test@test.com")
        .setOrException(FmPhonePreferencePage, false)
        .setOrException(SubMneOrDomesticPage, MneOrDomestic.Uk)
      val userAnswers = baseUa.set(UpeSectionConfirmationHashPage, SectionHash.computeUpeHash(baseUa)).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          inject.bind[SessionRepository].toInstance(mockSessionRepository),
          inject.bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors)
        )
        .build()

      running(application) {
        when(mockSessionRepository.get(any())).thenReturn(Future.successful(None))
        when(mockUserAnswersConnectors.remove(any())(any())).thenReturn(Future.successful(Done))
        val request = FakeRequest(GET, routes.TaskListController.onPageLoad.url)
        val result  = route(application, request).value
        status(result) mustEqual OK

        val controller             = application.injector.instanceOf[TaskListController]
        val groupDetailSection     = controller.groupSections(userAnswers)
        val reviewAndSubmitSection = controller.reviewSection(userAnswers)

        groupDetailSection.map(_.status).head shouldBe SectionStatus.Completed
        groupDetailSection.map(_.status)(1)   shouldBe SectionStatus.Completed
        reviewAndSubmitSection.status         shouldBe SectionStatus.CannotStart
      }
    }

    "build correct TaskInfo when filingMemberStatus is 'Completed' and groupDetailStatus is 'InProgress'" in {

      val baseUa = emptyUserAnswers
        .setOrException(SubMneOrDomesticPage, MneOrDomestic.Uk)
        .setOrException(SubAccountingPeriodPage, accountingPeriod)
        .setOrException(UpeRegisteredInUKPage, true)
        .setOrException(UpeEntityTypePage, EntityType.UkLimitedCompany)
        .setOrException(UpeGRSResponsePage, grsResponse)
        .setOrException(GrsUpeStatusPage, RowStatus.Completed)
        .setOrException(NominateFilingMemberPage, true)
        .setOrException(FmRegisteredInUKPage, false)
        .setOrException(SubPrimaryContactNamePage, "name")
        .setOrException(FmNameRegistrationPage, "name")
        .setOrException(FmRegisteredAddressPage, NonUKAddress("this", None, "over", None, None, countryCode = "AR"))
        .setOrException(SubUsePrimaryContactPage, true)
        .setOrException(SubPrimaryEmailPage, "test@test.com")
        .setOrException(SubPrimaryPhonePreferencePage, false)
        .setOrException(SubRegisteredAddressPage, NonUKAddress("this", None, "over", None, None, countryCode = "AR"))
      val userAnswers = baseUa.set(UpeSectionConfirmationHashPage, SectionHash.computeUpeHash(baseUa)).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          inject.bind[SessionRepository].toInstance(mockSessionRepository),
          inject.bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors)
        )
        .build()

      running(application) {
        when(mockSessionRepository.get(any())).thenReturn(Future.successful(None))
        when(mockUserAnswersConnectors.remove(any())(any())).thenReturn(Future.successful(Done))
        val request = FakeRequest(GET, routes.TaskListController.onPageLoad.url)
        val result  = route(application, request).value
        status(result) mustEqual OK

        val controller         = application.injector.instanceOf[TaskListController]
        val groupDetailSection = controller.groupSections(userAnswers)

        groupDetailSection.map(_.status) should contain theSameElementsAs Seq(
          SectionStatus.Completed,
          SectionStatus.Completed,
          SectionStatus.InProgress
        )
      }

    }

    "build correct TaskInfo when ultimateParentStatus is 'Completed', filingMemberStatus is 'Completed'" +
      "'groupDetailStatus' is Completed and contactDetailsStatus is 'Completed'" in {
        val baseUa = emptyUserAnswers
          .setOrException(UpeRegisteredInUKPage, true)
          .setOrException(SubMneOrDomesticPage, MneOrDomestic.Uk)
          .setOrException(SubAccountingPeriodPage, accountingPeriod)
          .setOrException(UpeEntityTypePage, EntityType.UkLimitedCompany)
          .setOrException(UpeGRSResponsePage, grsResponse)
          .setOrException(GrsUpeStatusPage, RowStatus.Completed)
          .setOrException(NominateFilingMemberPage, true)
          .setOrException(FmRegisteredInUKPage, false)
          .setOrException(SubPrimaryContactNamePage, "name")
          .setOrException(FmNameRegistrationPage, "name")
          .setOrException(FmRegisteredAddressPage, NonUKAddress("this", None, "over", None, None, countryCode = "AR"))
          .setOrException(SubUsePrimaryContactPage, true)
          .setOrException(SubPrimaryEmailPage, "test@test.com")
          .setOrException(SubPrimaryPhonePreferencePage, false)
          .setOrException(SubAddSecondaryContactPage, false)
          .setOrException(SubRegisteredAddressPage, NonUKAddress("this", None, "over", None, None, countryCode = "AR"))
          .setOrException(FmContactNamePage, "name")
          .setOrException(FmContactEmailPage, "test@test.com")
          .setOrException(FmPhonePreferencePage, false)
        val userAnswers = baseUa.set(UpeSectionConfirmationHashPage, SectionHash.computeUpeHash(baseUa)).success.value

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            inject.bind[SessionRepository].toInstance(mockSessionRepository),
            inject.bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors)
          )
          .build()

        running(application) {
          when(mockSessionRepository.get(any())).thenReturn(Future.successful(None))
          when(mockUserAnswersConnectors.remove(any())(any())).thenReturn(Future.successful(Done))
          val request = FakeRequest(GET, routes.TaskListController.onPageLoad.url)
          val result  = route(application, request).value
          status(result) mustEqual OK

          val controller             = application.injector.instanceOf[TaskListController]
          val groupDetailSection     = controller.groupSections(userAnswers)
          val contactDetailSection   = controller.contactSection(userAnswers)
          val reviewAndSubmitSection = controller.reviewSection(userAnswers)

          groupDetailSection.map(_.status) should contain theSameElementsAs Seq(
            SectionStatus.Completed,
            SectionStatus.Completed,
            SectionStatus.Completed
          )

          contactDetailSection.status shouldBe SectionStatus.Completed

          reviewAndSubmitSection.status shouldBe SectionStatus.NotStarted
        }
      }

  }
}
