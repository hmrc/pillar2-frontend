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

package services

import base.SpecBase
import connectors.RegistrationConnector
import models.{RegistrationWithoutIdInformationMissingError, SafeId}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json

import scala.concurrent.Future

class RegisterWithoutIdServiceSpec extends SpecBase {

  val service: RegisterWithoutIdService = app.injector.instanceOf[RegisterWithoutIdService]

  override lazy val app: Application = new GuiceApplicationBuilder()
    .overrides(
      bind[RegistrationConnector].toInstance(mockRegistrationConnector)
    )
    .build()

  "RegisterWithoutIdService" when {
    "must return SafeId if all success" in {
      val userAnswers = userAnswersData("id", Json.obj("Registration" -> validNoIdRegData()))
      val response    = Future.successful(Right(Some(SafeId("XE1111123456789"))))
      when(mockRegistrationConnector.upeRegistrationWithoutID(any(), any())(any(), any())).thenReturn(response)
      val result = service.sendUpeRegistrationWithoutId("id", userAnswers)
      result.futureValue mustBe Right(SafeId("XE1111123456789"))
    }

    "must return error when safe id is missing" in {
      val userAnswers = userAnswersData("id", Json.obj("Registration" -> validNoIdRegData()))
      val response    = Future.successful(Right(None))
      when(mockRegistrationConnector.upeRegistrationWithoutID(any(), any())(any(), any())).thenReturn(response)
      val result = service.sendUpeRegistrationWithoutId("id", userAnswers)
      result.futureValue mustBe Left(RegistrationWithoutIdInformationMissingError("Missing safeId"))
    }

    "must return InternalServerError when safe id is missing" in {
      val userAnswers = userAnswersData("id", Json.obj("Registration" -> validNoIdRegData()))
      val response    = Future.successful(Left(models.InternalServerError))
      when(mockRegistrationConnector.upeRegistrationWithoutID(any(), any())(any(), any())).thenReturn(response)
      val result = service.sendUpeRegistrationWithoutId("id", userAnswers)
      result.futureValue mustBe Left(models.InternalServerError)
    }
  }
}
