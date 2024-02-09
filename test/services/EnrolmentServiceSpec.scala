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

import akka.Done
import base.SpecBase
import models.{EnrolmentInfo, RegistrationWithoutIdInformationMissingError, SafeId, UKAddress, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages._
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.JsObject

import java.time.Instant
import scala.concurrent.Future

class EnrolmentServiceSpec extends SpecBase {

  val service: EnrolmentService = app.injector.instanceOf[EnrolmentService]

  override lazy val app: Application = new GuiceApplicationBuilder()
    .overrides(
      bind[EnrolmentService].toInstance(mockEnrolmentService)
    )
    .build()
  def userAnswersData(id: String, jsonObj: JsObject): UserAnswers = UserAnswers(id, jsonObj, Instant.ofEpochSecond(1))

  "RegisterWithoutIdService" when {
    "must return SafeId if all success" in {
      val result = service.createEnrolment(EnrolmentInfo(ctUtr = Some("utr"), crn = Some("crn"), plrId = "pillar2ID"))
      when(mockEnrolmentService.createEnrolment(any())).thenReturn(Future.successful(Done))
      result.futureValue mustBe Done
    }

    "must return error when safe id is missing" in {
      val result = service.createEnrolment(EnrolmentInfo(ctUtr = Some("utr"), crn = Some("crn"), plrId = "pillar2ID"))
      when(mockEnrolmentService.createEnrolment(any())).thenReturn(Future.failed(models.InternalServerError))
      result.futureValue mustBe models.InternalServerError
    }

  }
}
