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

package controllers.repayments

import base.{SpecBase, TestDateTimeUtils}
import cats.syntax.option.given
import controllers.actions.{FakeIdentifierAction, FakeSessionDataRequiredAction, FakeSessionDataRetrievalAction}
import models.longrunningsubmissions.LongRunningSubmission.Repayments
import models.repayments.RepaymentsStatus.{InProgress, SuccessfullyCompleted, UnexpectedResponseError}
import models.repayments.SendRepaymentDetails
import models.{UnexpectedResponse, UserAnswers}
import org.apache.pekko.Done
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*
import org.scalatest.concurrent.Eventually
import pages.*
import play.api.i18n.MessagesApi
import play.api.mvc.Result
import play.api.mvc.{MessagesControllerComponents, PlayBodyParsers}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import services.RepaymentService
import services.audit.AuditService
import uk.gov.hmrc.play.audit.http.connector.AuditResult
import utils.DateTimeUtils.*
import viewmodels.govuk.SummaryListFluency
import views.html.repayments.RepaymentsCheckYourAnswersView

import java.time.ZonedDateTime
import scala.concurrent.Future
import scala.jdk.CollectionConverters.given

class RepaymentsCheckYourAnswersControllerSpec extends SpecBase with SummaryListFluency with Eventually with TestDateTimeUtils {

  trait RepaymentsCheckYourAnswersControllerTestCase {

    def userAnswers: UserAnswers => Option[UserAnswers] = _.some

    protected def appliedUserAnswers: Option[UserAnswers] = userAnswers(emptyUserAnswers)

    lazy val sessionRepo: SessionRepository = {
      val repo = mock[SessionRepository]
      when(repo.get(any)).thenReturn(Future.successful(appliedUserAnswers))
      when(repo.set(any)).thenReturn(Future.successful(true))
      repo
    }

    val auditService: AuditService = {
      val service = mock[AuditService]
      when(service.auditRepayments(any)(using any)).thenReturn(Future.successful(AuditResult.Success))
      service
    }

    val repaymentService: RepaymentService = mock[RepaymentService]

    def controller = new RepaymentsCheckYourAnswersController(
      app.injector.instanceOf[MessagesApi],
      new FakeIdentifierAction(app.injector.instanceOf[PlayBodyParsers], pillar2OrganisationEnrolment),
      new FakeSessionDataRetrievalAction(appliedUserAnswers),
      new FakeSessionDataRequiredAction,
      sessionRepo,
      app.injector.instanceOf[MessagesControllerComponents],
      app.injector.instanceOf[RepaymentsCheckYourAnswersView],
      auditService,
      repaymentService,
      fixedClock
    )
  }

  "Repayments Check Your Answers Controller" when {

    "loading the CYA page" should {

      val request = FakeRequest(GET, controllers.repayments.routes.RepaymentsCheckYourAnswersController.onPageLoad().url)

      "redirect to the error return page when the repayments status flag is set to SuccessfullyCompleted" in new RepaymentsCheckYourAnswersControllerTestCase {
        override def userAnswers: UserAnswers => Option[UserAnswers] = _.setOrException(RepaymentsStatusPage, SuccessfullyCompleted).some

        val result: Future[Result] = controller.onPageLoad()(request)

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).get mustEqual controllers.repayments.routes.RepaymentErrorReturnController.onPageLoad().url
      }

      "return OK and the correct view if an answer is provided to every contact detail question" in new RepaymentsCheckYourAnswersControllerTestCase {
        override def userAnswers: UserAnswers => Option[UserAnswers] = _.setOrException(RepaymentsRefundAmountPage, BigDecimal("9.99"))
          .setOrException(ReasonForRequestingRefundPage, "Reason for refund")
          .some

        val result: Future[Result] = controller.onPageLoad()(request)

        status(result) mustEqual OK
        contentAsString(result) must include("Check your answers before submitting your repayment request")
        contentAsString(result) must include("Request details")
        contentAsString(result) must include("Bank account details")
        contentAsString(result) must include("Contact details")
      }

      "display specific row values in check your answers summary for Non-UK bank account" in new RepaymentsCheckYourAnswersControllerTestCase {
        override def userAnswers: UserAnswers => Option[UserAnswers] = _.setOrException(RepaymentsRefundAmountPage, BigDecimal(1000))
          .setOrException(ReasonForRequestingRefundPage, "Test Reason")
          .setOrException(UkOrAbroadBankAccountPage, models.UkOrAbroadBankAccount.ForeignBankAccount)
          .setOrException(NonUKBankPage, models.repayments.NonUKBank("HSBC2", "Test Name2", Some("HBUKGB4C"), Some("GB29NWBK60161331926820")))
          .setOrException(RepaymentsContactNamePage, "Repayment Contact Name change")
          .setOrException(RepaymentsContactEmailPage, "email@change.com")
          .setOrException(RepaymentsContactByPhonePage, false)
          .some

        val result: Future[Result] = controller.onPageLoad()(request)

        status(result) mustEqual OK
        contentAsString(result) must include("£1000")
        contentAsString(result) must include("Test Reason")
        contentAsString(result) must include("Non-UK bank account")
        contentAsString(result) must include("HSBC2")
        contentAsString(result) must include("Test Name2")
        contentAsString(result) must include("HBUKGB4C")
        contentAsString(result) must include("GB29NWBK60161331926820")
        contentAsString(result) must include("Repayment Contact Name change")
        contentAsString(result) must include("email@change.com")
        contentAsString(result) must include("No")
      }

      "display specific row values in check your answers summary for UK bank account" in new RepaymentsCheckYourAnswersControllerTestCase {
        override def userAnswers: UserAnswers => Option[UserAnswers] = _.setOrException(RepaymentsRefundAmountPage, BigDecimal(1000))
          .setOrException(ReasonForRequestingRefundPage, "Test Reason")
          .setOrException(UkOrAbroadBankAccountPage, models.UkOrAbroadBankAccount.UkBankAccount)
          .setOrException(BankAccountDetailsPage, models.repayments.BankAccountDetails("Natwest", "Epic Adventure Inc", "206705", "86473611"))
          .setOrException(RepaymentsContactNamePage, "Repayment Contact Name")
          .setOrException(RepaymentsContactEmailPage, "repayment@email.com")
          .setOrException(RepaymentsContactByPhonePage, true)
          .setOrException(RepaymentsPhoneDetailsPage, "789765423")
          .some

        val result: Future[Result] = controller.onPageLoad()(request)

        status(result) mustEqual OK
        contentAsString(result) must include("£1000")
        contentAsString(result) must include("Test Reason")
        contentAsString(result) must include("UK bank account")
        contentAsString(result) must include("Natwest")
        contentAsString(result) must include("Epic Adventure Inc")
        contentAsString(result) must include("206705")
        contentAsString(result) must include("86473611")
      }

      "must display row 4 value Natwest from acceptance test scenario" in new RepaymentsCheckYourAnswersControllerTestCase {
        override def userAnswers: UserAnswers => Option[UserAnswers] = _.setOrException(RepaymentsRefundAmountPage, BigDecimal(1000))
          .setOrException(ReasonForRequestingRefundPage, "Test Reason")
          .setOrException(UkOrAbroadBankAccountPage, models.UkOrAbroadBankAccount.UkBankAccount)
          .setOrException(BankAccountDetailsPage, models.repayments.BankAccountDetails("Natwest", "Epic Adventure Inc", "206705", "86473611"))
          .setOrException(RepaymentsContactNamePage, "Repayment Contact Name")
          .setOrException(RepaymentsContactEmailPage, "repayment@email.com")
          .setOrException(RepaymentsContactByPhonePage, false)
          .some

        val result:          Future[Result] = controller.onPageLoad()(request)
        val responseContent: String         = contentAsString(result)

        status(result) mustEqual OK
        responseContent must include("Natwest")
      }

      "must display row 7 value 86473611 from acceptance test scenario" in new RepaymentsCheckYourAnswersControllerTestCase {
        override def userAnswers: UserAnswers => Option[UserAnswers] = _.setOrException(RepaymentsRefundAmountPage, BigDecimal(1000))
          .setOrException(ReasonForRequestingRefundPage, "Test Reason")
          .setOrException(UkOrAbroadBankAccountPage, models.UkOrAbroadBankAccount.UkBankAccount)
          .setOrException(BankAccountDetailsPage, models.repayments.BankAccountDetails("Natwest", "Epic Adventure Inc", "206705", "86473611"))
          .setOrException(RepaymentsContactNamePage, "Repayment Contact Name")
          .setOrException(RepaymentsContactEmailPage, "repayment@email.com")
          .setOrException(RepaymentsContactByPhonePage, false)
          .some

        val result:          Future[Result] = controller.onPageLoad()(request)
        val responseContent: String         = contentAsString(result)

        status(result) mustEqual OK
        responseContent must include("86473611")
      }

      "must display row 4 value HSBC from acceptance test scenario" in new RepaymentsCheckYourAnswersControllerTestCase {
        override def userAnswers: UserAnswers => Option[UserAnswers] = _.setOrException(RepaymentsRefundAmountPage, BigDecimal(1000))
          .setOrException(ReasonForRequestingRefundPage, "Test Reason")
          .setOrException(UkOrAbroadBankAccountPage, models.UkOrAbroadBankAccount.ForeignBankAccount)
          .setOrException(NonUKBankPage, models.repayments.NonUKBank("HSBC", "Test Name", Some("HBUKGB4C"), Some("GB29NWBK60161331926820")))
          .setOrException(RepaymentsContactNamePage, "Repayment Contact Name")
          .setOrException(RepaymentsContactEmailPage, "repayment@email.com")
          .setOrException(RepaymentsContactByPhonePage, false)
          .some

        val result: Future[Result] = controller.onPageLoad()(request)

        val responseContent: String = contentAsString(result)
        status(result) mustEqual OK
        responseContent must include("HSBC")
      }

      "must display row 7 value GB29NWBK60161331926820 from acceptance test scenario" in new RepaymentsCheckYourAnswersControllerTestCase {
        override def userAnswers: UserAnswers => Option[UserAnswers] = _.setOrException(RepaymentsRefundAmountPage, BigDecimal(1000))
          .setOrException(ReasonForRequestingRefundPage, "Test Reason")
          .setOrException(UkOrAbroadBankAccountPage, models.UkOrAbroadBankAccount.ForeignBankAccount)
          .setOrException(NonUKBankPage, models.repayments.NonUKBank("HSBC", "Test Name", Some("HBUKGB4C"), Some("GB29NWBK60161331926820")))
          .setOrException(RepaymentsContactNamePage, "Repayment Contact Name")
          .setOrException(RepaymentsContactEmailPage, "repayment@email.com")
          .setOrException(RepaymentsContactByPhonePage, false)
          .some

        val result:          Future[Result] = controller.onPageLoad()(request)
        val responseContent: String         = contentAsString(result)

        status(result) mustEqual OK
        responseContent must include("GB29NWBK60161331926820")
      }

      "must display row 5 value O'Connor Construction from acceptance test scenario" in new RepaymentsCheckYourAnswersControllerTestCase {
        override def userAnswers: UserAnswers => Option[UserAnswers] = _.setOrException(RepaymentsRefundAmountPage, BigDecimal(1000))
          .setOrException(ReasonForRequestingRefundPage, "Test Reason")
          .setOrException(UkOrAbroadBankAccountPage, models.UkOrAbroadBankAccount.UkBankAccount)
          .setOrException(BankAccountDetailsPage, models.repayments.BankAccountDetails("Natwest", "O'Connor Construction", "609593", "96863604"))
          .setOrException(RepaymentsContactNamePage, "Repayment Contact Name")
          .setOrException(RepaymentsContactEmailPage, "repayment@email.com")
          .setOrException(RepaymentsContactByPhonePage, false)
          .some

        val result:          Future[Result] = controller.onPageLoad()(request)
        val responseContent: String         = contentAsString(result)

        status(result) mustEqual OK
        responseContent must include("O'Connor Construction")
      }

      "must display row 6 value 609593 from acceptance test scenario" in new RepaymentsCheckYourAnswersControllerTestCase {
        override def userAnswers: UserAnswers => Option[UserAnswers] = _.setOrException(RepaymentsRefundAmountPage, BigDecimal(1000))
          .setOrException(ReasonForRequestingRefundPage, "Test Reason")
          .setOrException(UkOrAbroadBankAccountPage, models.UkOrAbroadBankAccount.UkBankAccount)
          .setOrException(BankAccountDetailsPage, models.repayments.BankAccountDetails("Natwest", "O'Connor Construction", "609593", "96863604"))
          .setOrException(RepaymentsContactNamePage, "Repayment Contact Name")
          .setOrException(RepaymentsContactEmailPage, "repayment@email.com")
          .setOrException(RepaymentsContactByPhonePage, false)
          .some

        val result:          Future[Result] = controller.onPageLoad()(request)
        val responseContent: String         = contentAsString(result)

        status(result) mustEqual OK
        responseContent must include("609593")
      }

      "must display row 7 value 96863604 from acceptance test scenario" in new RepaymentsCheckYourAnswersControllerTestCase {
        override def userAnswers: UserAnswers => Option[UserAnswers] = _.setOrException(RepaymentsRefundAmountPage, BigDecimal(1000))
          .setOrException(ReasonForRequestingRefundPage, "Test Reason")
          .setOrException(UkOrAbroadBankAccountPage, models.UkOrAbroadBankAccount.UkBankAccount)
          .setOrException(BankAccountDetailsPage, models.repayments.BankAccountDetails("Natwest", "O'Connor Construction", "609593", "96863604"))
          .setOrException(RepaymentsContactNamePage, "Repayment Contact Name")
          .setOrException(RepaymentsContactEmailPage, "repayment@email.com")
          .setOrException(RepaymentsContactByPhonePage, false)
          .some

        val result:          Future[Result] = controller.onPageLoad()(request)
        val responseContent: String         = contentAsString(result)

        status(result) mustEqual OK
        responseContent must include("96863604")
      }

      "on submit method" should {

        val request = FakeRequest(POST, controllers.repayments.routes.RepaymentsCheckYourAnswersController.onSubmit().url)

        "redirect to incomplete data page if isRepaymentsJourneyCompleted returns false" in new RepaymentsCheckYourAnswersControllerTestCase {
          override def userAnswers: UserAnswers => Option[UserAnswers] =
            _ => completeRepaymentDataUkBankAccount.remove(RepaymentsRefundAmountPage).success.value.some

          val result: Future[Result] = controller.onSubmit()(request)
          status(result) mustBe SEE_OTHER
          redirectLocation(result).value mustEqual controllers.repayments.routes.RepaymentsIncompleteDataController.onPageLoad.url
        }

        "redirect to waiting room page and save SuccessfullyCompleted status in case of a success response" in
          new RepaymentsCheckYourAnswersControllerTestCase {
            override def userAnswers: UserAnswers => Option[UserAnswers] = _ => completeRepaymentDataUkBankAccount.some

            val answersCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])

            when(repaymentService.getRepaymentData(any())).thenReturn(Some(validRepaymentPayloadUkBank))
            when(repaymentService.sendRepaymentDetails(any[SendRepaymentDetails])(using any())).thenReturn(Future.successful(Done))

            val result: Future[Result] = controller.onSubmit()(request)

            status(result) mustBe SEE_OTHER
            redirectLocation(result).value mustEqual controllers.routes.WaitingRoomController.onPageLoad(Repayments).url

            val initialPersist:      UserAnswers = appliedUserAnswers.value.setOrException(RepaymentsStatusPage, InProgress)
            val persistOnCompletion: UserAnswers = emptyUserAnswers
              .setOrException(PlrReferencePage, initialPersist.get(PlrReferencePage).value)
              .setOrException(RepaymentsStatusPage, SuccessfullyCompleted)
              .setOrException(RepaymentConfirmationPage, ZonedDateTime.ofInstant(fixedNow, utcZoneId).toDateAtTimeFormat)
              .setOrException(RepaymentCompletionStatus, true)

            eventually {
              // Eventually doesn't know how to retry a 'verify(..., times(...))', so we use this instead.
              // As it's not simple to filter by method, we count all invocations.
              mockingDetails(sessionRepo).getInvocations.size() mustBe 4
              verify(sessionRepo, times(2)).set(answersCaptor.capture())
              answersCaptor.getAllValues.asScala.map(_.data) must contain theSameElementsInOrderAs Seq(
                initialPersist.data,
                persistOnCompletion.data
              )
            }
          }

        "redirect to waiting room page and save UnexpectedResponseError status in case of an unsuccessful response" in
          new RepaymentsCheckYourAnswersControllerTestCase {
            override def userAnswers: UserAnswers => Option[UserAnswers] = _ =>
              completeRepaymentDataUkBankAccount
                .setOrException(RepaymentsStatusPage, UnexpectedResponseError)
                .some

            val answersCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])

            when(mockRepaymentService.getRepaymentData(any()))
              .thenReturn(Some(validRepaymentPayloadUkBank))
            when(mockRepaymentService.sendRepaymentDetails(any[SendRepaymentDetails])(using any()))
              .thenReturn(Future.failed(UnexpectedResponse))

            val result: Future[Result] = controller.onSubmit()(request)
            status(result) mustBe SEE_OTHER
            redirectLocation(result).value mustEqual controllers.routes.WaitingRoomController.onPageLoad(Repayments).url

            val initialPersist: UserAnswers = appliedUserAnswers.value
              .setOrException(RepaymentsStatusPage, InProgress)
            val persistOnCompletion: UserAnswers = appliedUserAnswers.value
              .setOrException(RepaymentsStatusPage, UnexpectedResponseError)

            eventually {
              // Eventually doesn't know how to retry a 'verify(..., times(...))', so we use this instead.
              // As it's not simple to filter by method, we count all invocations.
              mockingDetails(sessionRepo).getInvocations.size() mustBe 4
              verify(sessionRepo, times(2)).set(answersCaptor.capture())
              answersCaptor.getAllValues.asScala.map(_.data) must contain theSameElementsInOrderAs Seq(
                initialPersist.data,
                persistOnCompletion.data
              )
            }
          }
      }
    }
  }
}
