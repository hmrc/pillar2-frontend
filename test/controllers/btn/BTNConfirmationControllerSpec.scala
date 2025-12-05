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

package controllers.btn

import base.SpecBase
import cats.syntax.option.*
import controllers.actions.*
import models.UserAnswers
import models.obligationsandsubmissions.*
import org.mockito.Mockito.when
import pages.{BTNChooseAccountingPeriodPage, BtnConfirmationPage}
import play.api.mvc.AnyContentAsEmpty
import play.api.mvc.Result
import play.api.mvc.{MessagesControllerComponents, PlayBodyParsers}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.btn.BTNConfirmationView

import java.time.{LocalDate, ZonedDateTime}
import scala.concurrent.Future

class BTNConfirmationControllerSpec extends SpecBase {

  val submittedAt: ZonedDateTime = ZonedDateTime.now()
  val accountingPeriodStart = someSubscriptionLocalData.subAccountingPeriod.startDate

  val btnConfirmationView: BTNConfirmationView = app.injector.instanceOf[BTNConfirmationView]

  val emptyObligationsData: ObligationsAndSubmissionsSuccess = ObligationsAndSubmissionsSuccess(
    processingDate = ZonedDateTime.now(),
    accountingPeriodDetails = Seq.empty
  )

  trait BTNConfirmationControllerTestCase {
    val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, controllers.btn.routes.BTNConfirmationController.onPageLoad.url)

    def userAnswers: UserAnswers => Option[UserAnswers] = _.some

    private def constructedUserAnswers = userAnswers(emptyUserAnswers)

    def obligationsData: ObligationsAndSubmissionsSuccess = emptyObligationsData

    lazy val mockSessionRepo: SessionRepository = {
      val repo = mock[SessionRepository]
      when(repo.get(emptyUserAnswers.id)).thenReturn(Future.successful(constructedUserAnswers))
      repo
    }

    def controller = new BTNConfirmationController(
      app.injector.instanceOf[MessagesControllerComponents],
      new FakeSubscriptionDataRetrievalAction(someSubscriptionLocalData.some),
      new FakeSubscriptionDataRequiredAction,
      new FakeIdentifierAction(app.injector.instanceOf[PlayBodyParsers], pillar2OrganisationEnrolment),
      btnConfirmationView,
      mockSessionRepo,
      new FakeObligationsAndSubmissionsDataRetrievalAction(obligationsData)
    )
  }

  "BTNConfirmationController" when {

    "onPageLoad" should {

      "return OK and the correct view for a GET" in new BTNConfirmationControllerTestCase {

        override def userAnswers: UserAnswers => Option[UserAnswers] = _.setOrException(BtnConfirmationPage, submittedAt).some

        val result: Future[Result] = controller.onPageLoad(request)

        status(result) mustEqual OK

        contentAsString(result) mustEqual btnConfirmationView(
          Some("OrgName"),
          submittedAt,
          accountingPeriodStart,
          isAgent = false,
          showUnderEnquiryWarning = false
        )(
          request,
          applicationConfig,
          messages(app)
        ).toString
      }

      "show underEnquiry warning when chosen accounting period has underEnquiry flag set to true" in new BTNConfirmationControllerTestCase {

        val chosenPeriod: AccountingPeriodDetails = AccountingPeriodDetails(
          startDate = LocalDate.now().minusYears(1),
          endDate = LocalDate.now(),
          dueDate = LocalDate.now().plusMonths(6),
          underEnquiry = true,
          obligations = Seq(Obligation(ObligationType.UKTR, ObligationStatus.Open, canAmend = true, Seq.empty))
        )

        override def userAnswers: UserAnswers => Option[UserAnswers] =
          _.setOrException(BTNChooseAccountingPeriodPage, chosenPeriod).setOrException(BtnConfirmationPage, submittedAt).some

        override def obligationsData: ObligationsAndSubmissionsSuccess = ObligationsAndSubmissionsSuccess(
          processingDate = ZonedDateTime.now(),
          accountingPeriodDetails = Seq(chosenPeriod)
        )

        val result: Future[Result] = controller.onPageLoad(request)

        status(result) mustEqual OK

        contentAsString(result) mustEqual btnConfirmationView(
          Some("OrgName"),
          submittedAt,
          accountingPeriodStart,
          isAgent = false,
          showUnderEnquiryWarning = true
        )(
          request,
          applicationConfig,
          messages(app)
        ).toString
      }

      "show underEnquiry warning when a subsequent accounting period has underEnquiry flag set to true" in new BTNConfirmationControllerTestCase {
        val subsequentPeriod: AccountingPeriodDetails = AccountingPeriodDetails(
          startDate = LocalDate.now().minusMonths(6),
          endDate = LocalDate.now().plusMonths(6),
          dueDate = LocalDate.now().plusMonths(12),
          underEnquiry = true,
          obligations = Seq(Obligation(ObligationType.UKTR, ObligationStatus.Open, canAmend = true, Seq.empty))
        )

        val chosenPeriod: AccountingPeriodDetails = AccountingPeriodDetails(
          startDate = LocalDate.now().minusYears(1),
          endDate = LocalDate.now().minusMonths(6),
          dueDate = LocalDate.now(),
          underEnquiry = false,
          obligations = Seq(Obligation(ObligationType.UKTR, ObligationStatus.Open, canAmend = true, Seq.empty))
        )

        override def userAnswers: UserAnswers => Option[UserAnswers] =
          _.setOrException(BTNChooseAccountingPeriodPage, chosenPeriod).setOrException(BtnConfirmationPage, submittedAt).some

        override def obligationsData: ObligationsAndSubmissionsSuccess = ObligationsAndSubmissionsSuccess(
          processingDate = ZonedDateTime.now(),
          accountingPeriodDetails = Seq(subsequentPeriod, chosenPeriod)
        )

        val result: Future[Result] = controller.onPageLoad(request)

        status(result) mustEqual OK

        contentAsString(result) mustEqual btnConfirmationView(
          Some("OrgName"),
          submittedAt,
          accountingPeriodStart,
          isAgent = false,
          showUnderEnquiryWarning = true
        )(
          request,
          applicationConfig,
          messages(app)
        ).toString
      }

      "not show underEnquiry warning when neither chosen nor subsequent periods have underEnquiry flag set" in new BTNConfirmationControllerTestCase {
        val chosenPeriod: AccountingPeriodDetails = AccountingPeriodDetails(
          startDate = LocalDate.now().minusYears(1),
          endDate = LocalDate.now(),
          dueDate = LocalDate.now().plusMonths(6),
          underEnquiry = false,
          obligations = Seq(Obligation(ObligationType.UKTR, ObligationStatus.Open, canAmend = true, Seq.empty))
        )

        val subsequentPeriod: AccountingPeriodDetails = AccountingPeriodDetails(
          startDate = LocalDate.now().minusYears(2),
          endDate = LocalDate.now().minusYears(1),
          dueDate = LocalDate.now().minusMonths(6),
          underEnquiry = false,
          obligations = Seq(Obligation(ObligationType.UKTR, ObligationStatus.Open, canAmend = true, Seq.empty))
        )

        override def userAnswers: UserAnswers => Option[UserAnswers] =
          _.setOrException(BTNChooseAccountingPeriodPage, chosenPeriod).setOrException(BtnConfirmationPage, submittedAt).some

        override def obligationsData: ObligationsAndSubmissionsSuccess = ObligationsAndSubmissionsSuccess(
          processingDate = ZonedDateTime.now(),
          accountingPeriodDetails = Seq(chosenPeriod, subsequentPeriod)
        )

        val result: Future[Result] = controller.onPageLoad(request)

        status(result) mustEqual OK

        contentAsString(result) mustEqual btnConfirmationView(
          Some("OrgName"),
          submittedAt,
          accountingPeriodStart,
          isAgent = false,
          showUnderEnquiryWarning = false
        )(
          request,
          applicationConfig,
          messages(app)
        ).toString
      }

      "redirect to journey recovery when session is missing" in new BTNConfirmationControllerTestCase {
        override def userAnswers: UserAnswers => Option[UserAnswers] = _ => Option.empty[UserAnswers]

        val result: Future[Result] = controller.onPageLoad(request)

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe controllers.routes.JourneyRecoveryController.onPageLoad().url
      }

      "redirect to journey recovery when submittedAt is missing" in new BTNConfirmationControllerTestCase {
        override def userAnswers: UserAnswers => Option[UserAnswers] = _ => emptyUserAnswers.some

        val result: Future[Result] = controller.onPageLoad(request)

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
