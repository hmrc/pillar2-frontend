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
      when(mockRegistrationConnector.upeRegisterationWithoutID(any(), any())(any(), any())).thenReturn(response)
      val result = service.sendUpeRegistrationWithoutId("id", userAnswers)
      result.futureValue mustBe Right(SafeId("XE1111123456789"))
    }

    "must return error when safe id is missing" in {
      val userAnswers = userAnswersData("id", Json.obj("Registration" -> validNoIdRegData()))
      val response    = Future.successful(Right(None))
      when(mockRegistrationConnector.upeRegisterationWithoutID(any(), any())(any(), any())).thenReturn(response)
      val result = service.sendUpeRegistrationWithoutId("id", userAnswers)
      result.futureValue mustBe Left(RegistrationWithoutIdInformationMissingError("Missing safeId"))
    }

    "must return InternalServerError when safe id is missing" in {
      val userAnswers = userAnswersData("id", Json.obj("Registration" -> validNoIdRegData()))
      val response    = Future.successful(Left(models.InternalServerError))
      when(mockRegistrationConnector.upeRegisterationWithoutID(any(), any())(any(), any())).thenReturn(response)
      val result = service.sendUpeRegistrationWithoutId("id", userAnswers)
      result.futureValue mustBe Left(models.InternalServerError)
    }
  }
}
