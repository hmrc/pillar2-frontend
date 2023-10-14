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

import connectors.RegistrationConnector
import models.UserAnswers
import play.api.Logging
import uk.gov.hmrc.http.HeaderCarrier
import models.{ApiError, MandatoryInformationMissingError, RegistrationWithoutIdInformationMissingError, SafeId}
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RegisterWithoutIdService @Inject() (registrationConnector: RegistrationConnector)(implicit ec: ExecutionContext) extends Logging {

  def sendUpeRegistrationWithoutId(id: String, userAnswers: UserAnswers)(implicit
    hc:                                HeaderCarrier,
    ec:                                ExecutionContext
  ): Future[Either[ApiError, SafeId]] =
    registrationConnector
      .upeRegistrationWithoutID(id, userAnswers) map {
      case Right(Some(safeId)) =>
        Right(safeId)
      case Right(None) =>
        logger.warn("Upe Registration WithoutId Information MissingError SafeId missing")
        Left(RegistrationWithoutIdInformationMissingError("Missing safeId"))
      case Left(error) =>
        logger.warn(s"Upe Registration WithoutId Information $error")
        Left(error)
    }

  def sendFmRegistrationWithoutId(id: String, userAnswers: UserAnswers)(implicit
    hc:                               HeaderCarrier,
    ec:                               ExecutionContext
  ): Future[Either[ApiError, SafeId]] =
    registrationConnector
      .fmRegisterationWithoutID(id, userAnswers) map {
      case Right(Some(safeId)) => Right(safeId)
      case Right(None) =>
        logger.warn("Filing Member Registration WithoutId Information MissingError SafeId missing")
        Left(RegistrationWithoutIdInformationMissingError("Missing safeId"))
      case Left(error) =>
        logger.warn(s"Filing Member Registration WithoutId Information $error")
        Left(error)
    }

}
