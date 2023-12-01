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
import models.{RegistrationWithoutIdInformationMissingError, SafeId, UKAddress, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages._
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.JsObject

import java.time.Instant
import scala.concurrent.Future

class RegisterWithoutIdServiceSpec extends SpecBase {

  val service: RegisterWithoutIdService = app.injector.instanceOf[RegisterWithoutIdService]

  override lazy val app: Application = new GuiceApplicationBuilder()
    .overrides(
      bind[RegistrationConnector].toInstance(mockRegistrationConnector)
    )
    .build()
  def userAnswersData(id: String, jsonObj: JsObject): UserAnswers = UserAnswers(id, jsonObj, Instant.ofEpochSecond(1))

  val ukAddress = UKAddress(
    addressLine1 = "1 drive",
    addressLine2 = None,
    addressLine3 = "la la land",
    addressLine4 = None,
    postalCode = "m19hgs",
    countryCode = "AB"
  )
  val validNoIdRegData = emptyUserAnswers
    .setOrException(upeNameRegistrationPage, "name")
    .setOrException(upeRegisteredInUKPage, false)
    .setOrException(upeRegisteredAddressPage, ukAddress)
    .setOrException(upeContactNamePage, "contactName")
    .setOrException(upeContactEmailPage, "some@email.com")
    .setOrException(upePhonePreferencePage, true)
    .setOrException(upeCapturePhonePage, "12312321")
  "RegisterWithoutIdService" when {
    "must return SafeId if all success" in {
      val userAnswers = emptyUserAnswers
      val response    = Future.successful(Right(Some(SafeId("XE1111123456789"))))
      when(mockRegistrationConnector.upeRegisterationWithoutID(any(), any())(any(), any())).thenReturn(response)
      val result = service.sendUpeRegistrationWithoutId("id", userAnswers)
      result.futureValue mustBe Right(SafeId("XE1111123456789"))
    }

    "must return error when safe id is missing" in {
      val userAnswers = emptyUserAnswers
      val response    = Future.successful(Right(None))
      when(mockRegistrationConnector.upeRegisterationWithoutID(any(), any())(any(), any())).thenReturn(response)
      val result = service.sendUpeRegistrationWithoutId("id", userAnswers)
      result.futureValue mustBe Left(RegistrationWithoutIdInformationMissingError("Missing safeId"))
    }

    "must return InternalServerError when safe id is missing" in {
      val userAnswers = emptyUserAnswers
      val response    = Future.successful(Left(models.InternalServerError))
      when(mockRegistrationConnector.upeRegisterationWithoutID(any(), any())(any(), any())).thenReturn(response)
      val result = service.sendUpeRegistrationWithoutId("id", userAnswers)
      result.futureValue mustBe Left(models.InternalServerError)
    }
  }
}
