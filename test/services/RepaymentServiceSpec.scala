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

package services

import base.SpecBase
import connectors.RepaymentConnector
import models.UnexpectedResponse
import models.repayments.SendRepaymentDetails
import org.apache.pekko.Done
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.inject
import repositories.SessionRepository

import scala.concurrent.Future

class RepaymentServiceSpec extends SpecBase {

  "ReferenceNumberService" when {

    "getRepaymentData" should {
      "Successfully create SendRepaymentObject from userAnswers if an answer has been provided for uk bank account" in {

        val service = app.injector.instanceOf[RepaymentService]
        val result  = service.getRepaymentData(completeRepaymentDataUkBankAccount)
        result mustBe Some(validRepaymentPayloadUkBank)
      }

      "Successfully create SendRepaymentObject from userAnswers if an answer has been provided for a non-uk bank account" in {

        val service = app.injector.instanceOf[RepaymentService]
        val result  = service.getRepaymentData(completeRepaymentDataNonUkBankAccount)
        result mustBe Some(validRepaymentPayloadNonUkBank)
      }
      "return None if referenceNumber is missing" in {
        val service = app.injector.instanceOf[RepaymentService]
        val result  = service.getRepaymentData(repaymentNoReferenceNumber)
        result mustBe None
      }
      "return None if contact name is missing" in {
        val service = app.injector.instanceOf[RepaymentService]
        val result  = service.getRepaymentData(repaymentNoContactName)
        result mustBe None
      }
      "return None if amount is missing" in {
        val service = app.injector.instanceOf[RepaymentService]
        val result  = service.getRepaymentData(repaymentNoAmount)
        result mustBe None
      }
      "return None if bank account type is missing" in {
        val service = app.injector.instanceOf[RepaymentService]
        val result  = service.getRepaymentData(repaymentNoBankAccountType)
        result mustBe None
      }
      "return None if bank account detail for foreign account type is missing" in {
        val service = app.injector.instanceOf[RepaymentService]
        val result  = service.getRepaymentData(repaymentNoBankAccountDetailForeign)
        result mustBe None
      }
      "return None if bank account detail for a uk account is missing" in {
        val service = app.injector.instanceOf[RepaymentService]
        val result  = service.getRepaymentData(repaymentNoUKBankAccountDetail)
        result mustBe None
      }
    }

    "sendRepaymentDetails" should {
      "return true and delete frontend database if successful" in {
        val application = applicationBuilder().overrides(
          inject.bind[RepaymentConnector].toInstance(mockRepaymentConnector),
          inject.bind[SessionRepository].toInstance(mockSessionRepository)
        )
        val service = application.injector.instanceOf[RepaymentService]
        when(mockRepaymentConnector.repayment(any[SendRepaymentDetails])(any())).thenReturn(Future.successful(Done))
        when(mockSessionRepository.clear(any())).thenReturn(Future.successful(true))
        service.sendRepaymentDetails(validRepaymentPayloadUkBank).futureValue mustEqual Done
      }
      "return failed results if connector fails" in {
        val application = applicationBuilder().overrides(
          inject.bind[RepaymentConnector].toInstance(mockRepaymentConnector),
          inject.bind[SessionRepository].toInstance(mockSessionRepository)
        )
        val service = application.injector.instanceOf[RepaymentService]
        when(mockRepaymentConnector.repayment(any[SendRepaymentDetails])(any())).thenReturn(Future.failed(UnexpectedResponse))
        when(mockSessionRepository.clear(any())).thenReturn(Future.successful(true))
        service.sendRepaymentDetails(validRepaymentPayloadUkBank).failed.futureValue mustEqual UnexpectedResponse
      }
    }
  }

}
