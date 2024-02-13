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
import models.grs.{EntityType, GrsRegistrationResult, RegistrationStatus}
import models.registration.{CompanyProfile, GrsResponse, IncorporatedEntityAddress, IncorporatedEntityRegistrationData}
import models.subscription.AccountingPeriod
import models.{MneOrDomestic, NonUKAddress, TaskAction, TaskStatus, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import pages._
import play.api
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import utils.RowStatus
import views.html.TaskListView

import java.time.LocalDate
import scala.concurrent.Future
case class TaskInfo(name: String, status: String, link: Option[String], action: Option[String])
class TaskListControllerSpec extends SpecBase {

  private val accountingPeriod = AccountingPeriod(LocalDate.now(), LocalDate.now())

  private val grsResponse = GrsResponse(
    Some(
      IncorporatedEntityRegistrationData(
        companyProfile = CompanyProfile(
          companyName = "ABC Limited",
          companyNumber = "1234",
          dateOfIncorporation = LocalDate.now(),
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

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, routes.TaskListController.onPageLoad.url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[TaskListView]

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
      }
    }

    "redirected to subscription confirmation page if the user has already subscribed with a pillar 2 reference" in {
      val userAnswer = UserAnswers("id").setOrException(plrReferencePage, "id")
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
        .set(upeRegisteredInUKPage, false)
        .success
        .value
        .set(NominateFilingMemberPage, false)
        .success
        .value

      val application = applicationBuilder(Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, routes.TaskListController.onPageLoad.url)
        val result  = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) should include("Register your group")
        contentAsString(result) should include("Registration incomplete")
      }
    }

    "build filingMemberInfo with 'edit' action when ultimateParentStatus is 'Completed' and filingMemberStatus is 'Completed'" in {
      val userAnswers = emptyUserAnswers
        .setOrException(upeRegisteredInUKPage, true)
        .setOrException(upeEntityTypePage, EntityType.UkLimitedCompany)
        .setOrException(upeGRSResponsePage, grsResponse)
        .setOrException(GrsUpeStatusPage, RowStatus.Completed)
        .setOrException(NominateFilingMemberPage, true)
        .setOrException(fmRegisteredInUKPage, true)
        .setOrException(fmEntityTypePage, EntityType.UkLimitedCompany)
        .setOrException(fmGRSResponsePage, grsResponse)
        .setOrException(GrsFilingMemberStatusPage, RowStatus.Completed)

      val application = applicationBuilder(Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, routes.TaskListController.onPageLoad.url)
        val result  = route(application, request).value

        status(result) mustEqual OK

        val responseContent = contentAsString(result)
        responseContent should include("Edit ultimate parent")
      }
    }

    "handle scenario when ultimateParentStatus is Completed and filingMemberStatus is NotStarted" in {
      val userAnswers = emptyUserAnswers
        .set(upeRegisteredInUKPage, true)
        .success
        .value

      val application = applicationBuilder(Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, routes.TaskListController.onPageLoad.url)
        val result  = route(application, request).value

        status(result) mustEqual OK
        val responseContent = contentAsString(result)
        responseContent should include("Cannot start yet")
      }
    }

    "handle scenario when groupDetailStatus is Completed" in {
      val userAnswers = emptyUserAnswers
        .setOrException(upeRegisteredInUKPage, true)
        .setOrException(NominateFilingMemberPage, true)
        .setOrException(subMneOrDomesticPage, MneOrDomestic.Uk)
        .setOrException(subAccountingPeriodPage, accountingPeriod)

      val application = applicationBuilder(Some(userAnswers)).build()

      running(application) {
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
        .setOrException(upeRegisteredInUKPage, true)
        .setOrException(NominateFilingMemberPage, true)
        .setOrException(fmRegisteredInUKPage, true)
        .setOrException(fmEntityTypePage, EntityType.UkLimitedCompany)
        .setOrException(fmGRSResponsePage, grsResponse)
        .setOrException(GrsFilingMemberStatusPage, RowStatus.Completed)
        .setOrException(subMneOrDomesticPage, MneOrDomestic.Uk)

      val application = applicationBuilder(Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, routes.TaskListController.onPageLoad.url)
        val result  = route(application, request).value

        status(result) mustEqual OK
        val responseContent = contentAsString(result)

        responseContent should include("In progress<")
        responseContent should include(" Add further group details")
      }
    }

    "build groupDetailInfo with 'cannotStartYet' action when either filingMemberStatus or groupDetailStatus is 'InProgress'" in {
      val userAnswers = emptyUserAnswers
        .setOrException(upeRegisteredInUKPage, true)
        .setOrException(upeEntityTypePage, EntityType.UkLimitedCompany)
        .setOrException(upeGRSResponsePage, grsResponse)
        .setOrException(GrsUpeStatusPage, RowStatus.Completed)
        .setOrException(NominateFilingMemberPage, true)
        .setOrException(fmRegisteredInUKPage, false)
        .setOrException(subMneOrDomesticPage, MneOrDomestic.Uk)
      val application = applicationBuilder(Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, routes.TaskListController.onPageLoad.url)
        val result  = route(application, request).value

        status(result) mustEqual OK
        val responseContent = contentAsString(result)

        responseContent should include("Cannot start yet")
      }
    }

    "build groupDetailInfo with 'edit' action when groupDetailStatus is 'Completed'" in {
      val userAnswers = emptyUserAnswers
        .setOrException(subMneOrDomesticPage, MneOrDomestic.Uk)
        .setOrException(subAccountingPeriodPage, accountingPeriod)

      val application = applicationBuilder(Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, routes.TaskListController.onPageLoad.url)
        val result  = route(application, request).value

        status(result) mustEqual OK

        val responseContent = contentAsString(result)
        responseContent should include("Edit further group details")
      }
    }

    "build contactDetailsInfo with 'edit' action when all statuses are 'Completed'" in {
      val userAnswers = emptyUserAnswers
        .setOrException(subMneOrDomesticPage, MneOrDomestic.Uk)
        .setOrException(subAccountingPeriodPage, accountingPeriod)
        .setOrException(upeRegisteredInUKPage, true)
        .setOrException(upeEntityTypePage, EntityType.UkLimitedCompany)
        .setOrException(upeGRSResponsePage, grsResponse)
        .setOrException(GrsUpeStatusPage, RowStatus.Completed)
        .setOrException(NominateFilingMemberPage, true)
        .setOrException(fmRegisteredInUKPage, false)
        .setOrException(subPrimaryContactNamePage, "name")
        .setOrException(subRegisteredAddressPage, NonUKAddress("this", None, "over", None, None, countryCode = "AR"))
        .setOrException(subMneOrDomesticPage, MneOrDomestic.Uk)

      val application = applicationBuilder(Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, routes.TaskListController.onPageLoad.url)

        val result = route(application, request).value

        status(result) mustEqual OK

        val controller = application.injector.instanceOf[TaskListController]
        val (ultimateParentInfo, filingMemberInfo, _, _, _) =
          controller.buildTaskInfo(
            TaskStatus.Completed.toString,
            TaskStatus.Completed.toString,
            TaskStatus.NotStarted.toString,
            TaskStatus.NotStarted.toString,
            TaskStatus.NotStarted.toString
          )

        ultimateParentInfo.status shouldBe TaskStatus.Completed.toString
        ultimateParentInfo.action shouldBe Some(TaskAction.Edit.toString.toLowerCase())
        filingMemberInfo.status   shouldBe TaskStatus.Completed.toString
        filingMemberInfo.action   shouldBe Some(TaskAction.Edit.toString.toLowerCase())
      }
    }

    "build correct TaskInfo when ultimateParentStatus is 'Completed' and filingMemberStatus is 'Completed'" in {
      val userAnswers = emptyUserAnswers
        .setOrException(subMneOrDomesticPage, MneOrDomestic.Uk)
        .setOrException(subAccountingPeriodPage, accountingPeriod)
        .setOrException(upeRegisteredInUKPage, true)
        .setOrException(upeEntityTypePage, EntityType.UkLimitedCompany)
        .setOrException(upeGRSResponsePage, grsResponse)
        .setOrException(GrsUpeStatusPage, RowStatus.Completed)
        .setOrException(NominateFilingMemberPage, true)
        .setOrException(fmRegisteredInUKPage, false)
        .setOrException(subPrimaryContactNamePage, "name")
        .setOrException(subRegisteredAddressPage, NonUKAddress("this", None, "over", None, None, countryCode = "AR"))
        .setOrException(subMneOrDomesticPage, MneOrDomestic.Uk)

      val application = applicationBuilder(Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, routes.TaskListController.onPageLoad.url)

        val result = route(application, request).value

        status(result) mustEqual OK

        val controller = application.injector.instanceOf[TaskListController]
        val (ultimateParentInfo, filingMemberInfo, _, _, _) =
          controller.buildTaskInfo(
            TaskStatus.Completed.toString,
            TaskStatus.Completed.toString,
            TaskStatus.NotStarted.toString,
            TaskStatus.NotStarted.toString,
            TaskStatus.NotStarted.toString
          )

        ultimateParentInfo.status shouldBe TaskStatus.Completed.toString
        ultimateParentInfo.action shouldBe Some(TaskAction.Edit.toString.toLowerCase())
        filingMemberInfo.status   shouldBe TaskStatus.Completed.toString
        filingMemberInfo.action   shouldBe Some(TaskAction.Edit.toString.toLowerCase())
      }
    }

    "build correct TaskInfo when cyaStatus is 'Completed'" in {
      val userAnswers = emptyUserAnswers
        .setOrException(subMneOrDomesticPage, MneOrDomestic.Uk)
        .setOrException(subAccountingPeriodPage, accountingPeriod)
        .setOrException(upeRegisteredInUKPage, true)
        .setOrException(upeEntityTypePage, EntityType.UkLimitedCompany)
        .setOrException(upeGRSResponsePage, grsResponse)
        .setOrException(GrsUpeStatusPage, RowStatus.Completed)
        .setOrException(NominateFilingMemberPage, true)
        .setOrException(fmRegisteredInUKPage, false)
        .setOrException(subPrimaryContactNamePage, "name")
        .setOrException(subRegisteredAddressPage, NonUKAddress("this", None, "over", None, None, countryCode = "AR"))
        .setOrException(subMneOrDomesticPage, MneOrDomestic.Uk)
      val application = applicationBuilder(Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, routes.TaskListController.onPageLoad.url)

        val result = route(application, request).value

        status(result) mustEqual OK

        val controller = application.injector.instanceOf[TaskListController]
        val (_, _, _, _, cyaInfo) =
          controller.buildTaskInfo(
            TaskStatus.NotStarted.toString,
            TaskStatus.NotStarted.toString,
            TaskStatus.NotStarted.toString,
            TaskStatus.NotStarted.toString,
            TaskStatus.Completed.toString
          )

        cyaInfo.status shouldBe TaskStatus.Completed.toString
        cyaInfo.action shouldBe Some(TaskAction.Edit.toString.toLowerCase())
      }

    }

    "build correct TaskInfo when filingMemberStatus is 'Completed' and groupDetailStatus is 'InProgress'" in {

      val userAnswers = emptyUserAnswers
        .setOrException(subMneOrDomesticPage, MneOrDomestic.Uk)
        .setOrException(subAccountingPeriodPage, accountingPeriod)
        .setOrException(upeRegisteredInUKPage, true)
        .setOrException(upeEntityTypePage, EntityType.UkLimitedCompany)
        .setOrException(upeGRSResponsePage, grsResponse)
        .setOrException(GrsUpeStatusPage, RowStatus.Completed)
        .setOrException(NominateFilingMemberPage, true)
        .setOrException(fmRegisteredInUKPage, false)
        .setOrException(subPrimaryContactNamePage, "name")
        .setOrException(subRegisteredAddressPage, NonUKAddress("this", None, "over", None, None, countryCode = "AR"))
        .setOrException(subMneOrDomesticPage, MneOrDomestic.Uk)
      val application = applicationBuilder(Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, routes.TaskListController.onPageLoad.url)

        val result = route(application, request).value

        status(result) mustEqual OK

        val controller = application.injector.instanceOf[TaskListController]
        val (_, _, groupDetailInfo, _, _) =
          controller.buildTaskInfo(
            TaskStatus.Completed.toString,
            TaskStatus.Completed.toString,
            TaskStatus.InProgress.toString,
            TaskStatus.NotStarted.toString,
            TaskStatus.NotStarted.toString
          )

        groupDetailInfo.status shouldBe TaskStatus.InProgress.toString
        groupDetailInfo.action shouldBe Some(TaskAction.Add.toString.toLowerCase())
      }

    }

    "build correct TaskInfo when filingMemberStatus is 'Completed' and contactDetailsStatus is 'Completed'" in {
      val userAnswers = emptyUserAnswers
        .setOrException(subMneOrDomesticPage, MneOrDomestic.Uk)
        .setOrException(subAccountingPeriodPage, accountingPeriod)
        .setOrException(upeRegisteredInUKPage, true)
        .setOrException(upeEntityTypePage, EntityType.UkLimitedCompany)
        .setOrException(upeGRSResponsePage, grsResponse)
        .setOrException(GrsUpeStatusPage, RowStatus.Completed)
        .setOrException(NominateFilingMemberPage, true)
        .setOrException(fmRegisteredInUKPage, false)
        .setOrException(subPrimaryContactNamePage, "name")

      val application = applicationBuilder(Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, routes.TaskListController.onPageLoad.url)

        val result = route(application, request).value

        status(result) mustEqual OK

        val controller = application.injector.instanceOf[TaskListController]
        val (_, _, _, contactDetailsInfo, _) =
          controller.buildTaskInfo(
            TaskStatus.Completed.toString,
            TaskStatus.Completed.toString,
            TaskStatus.Completed.toString,
            TaskStatus.InProgress.toString,
            TaskStatus.NotStarted.toString
          )

        contactDetailsInfo.status shouldBe TaskStatus.InProgress.toString
        contactDetailsInfo.action shouldBe Some(TaskAction.Add.toString.toLowerCase())
      }
    }

    "build correct TaskInfo with default values when filingMemberStatus and groupDetailStatus do not match any case" in {
      val userAnswers = emptyUserAnswers
        .setOrException(subMneOrDomesticPage, MneOrDomestic.Uk)
        .setOrException(subAccountingPeriodPage, accountingPeriod)
        .setOrException(upeRegisteredInUKPage, true)
        .setOrException(upeEntityTypePage, EntityType.UkLimitedCompany)
        .setOrException(upeGRSResponsePage, grsResponse)
        .setOrException(GrsUpeStatusPage, RowStatus.Completed)
        .setOrException(NominateFilingMemberPage, true)
        .setOrException(fmRegisteredInUKPage, false)
        .setOrException(subPrimaryContactNamePage, "name")

      val application = applicationBuilder(Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, routes.TaskListController.onPageLoad.url)

        val result = route(application, request).value

        status(result) mustEqual OK

        val controller = application.injector.instanceOf[TaskListController]
        val (_, _, groupDetailInfo, _, _) =
          controller.buildTaskInfo(
            TaskStatus.Default.toString,
            TaskStatus.Default.toString,
            TaskStatus.Default.toString,
            TaskStatus.Default.toString,
            TaskStatus.Default.toString
          )

        groupDetailInfo.name   shouldBe "groupDetail"
        groupDetailInfo.status shouldBe TaskStatus.CannotStartYet.toString
        groupDetailInfo.link   shouldBe None
        groupDetailInfo.action shouldBe None
      }
    }

  }
}
