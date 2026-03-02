/*
 * Copyright 2026 HM Revenue & Customs
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
import models.UserAnswers
import models.btn.BTNRequest
import models.btn.BTNStatus
import models.subscription.AccountingPeriod
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.concurrent.Eventually
import org.scalatest.time.{Millis, Seconds, Span}
import play.api.libs.json.Json
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.AuditResult

import java.time.*
import scala.concurrent.Future
import scala.jdk.CollectionConverters.*

class BtnSubmissionServiceSpec extends SpecBase with Eventually {

  private val service = new BtnSubmissionService(mockBTNService, mockSessionRepository, mockAuditService)

  private given HeaderCarrier = HeaderCarrier()
  private given Clock         = Clock.fixed(LocalDate.now().atStartOfDay().toInstant(ZoneOffset.UTC), ZoneOffset.UTC)

  "BtnSubmissionService.startSubmission" should {
    "set BTNStatus to processing and then persist the final status after a successful submission" in {
      given PatienceConfig =
        PatienceConfig(timeout = Span(2, Seconds), interval = Span(25, Millis))

      val userId    = "userId-1"
      val pillar2Id = "XMPLR0123456789"

      val ap  = AccountingPeriod(LocalDate.now().minusYears(1), LocalDate.now().minusDays(1))
      val req = BTNRequest(ap.startDate, ap.endDate)

      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))
      when(mockSessionRepository.get(userId)).thenReturn(Future.successful(Some(emptyUserAnswers)))
      when(mockBTNService.submitBTN(any())(using any[HeaderCarrier], any[String]))
        .thenReturn(
          Future.successful(
            uk.gov.hmrc.http.HttpResponse(201, Json.obj("success" -> Json.obj("processingDate" -> "2024-03-14T09:26:17Z")).toString())
          )
        )
      when(mockAuditService.auditBTNSubmission(any(), any(), any(), any())(using any[HeaderCarrier]))
        .thenReturn(Future.successful(AuditResult.Success))

      service.startSubmission(userId, emptyUserAnswers, pillar2Id, ap, req).futureValue

      val answersCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])

      org.mockito.Mockito.verify(mockSessionRepository, org.mockito.Mockito.atLeastOnce()).set(answersCaptor.capture())
      answersCaptor.getAllValues.asScala.exists(_.get(BTNStatus).contains(BTNStatus.processing)) mustBe true

      eventually {
        org.mockito.Mockito.verify(mockSessionRepository, org.mockito.Mockito.atLeast(2)).set(answersCaptor.capture())
        answersCaptor.getAllValues.asScala.exists(_.get(BTNStatus).contains(BTNStatus.submitted)) mustBe true
      }
    }
  }
}
