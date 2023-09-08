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

package connectors

import config.FrontendAppConfig
import models.registration.RegisterationWithoutIDResponse
import models.{ApiError, InternalServerError, SafeId, UserAnswers}
import pages.{NominatedFilingMemberPage, RegistrationPage}
import play.api.Logging
import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.http.HttpReads.is2xx
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpResponse}
import uk.gov.hmrc.http.HttpReads.Implicits.readRaw

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RegistrationConnector @Inject() (val userAnswersConnectors: UserAnswersConnectors, val config: FrontendAppConfig, val http: HttpClient)
    extends Logging {
  val upeRegistrationUrl = s"${config.pillar2BaseUrl}/report-pillar2-top-up-taxes/upe/registration"
  val fmRegistrationUrl  = s"${config.pillar2BaseUrl}/report-pillar2-top-up-taxes/fm/registration"

  def upeRegisterationWithoutID(id: String, userAnswers: UserAnswers)(implicit
    hc:                             HeaderCarrier,
    ec:                             ExecutionContext
  ): Future[Either[ApiError, Option[SafeId]]] =
    http.POSTEmpty(s"$upeRegistrationUrl/$id") map {
      case response if is2xx(response.status) =>
        val safeId  = response.json.asOpt[RegisterationWithoutIDResponse].map(_.safeId)
        val regData = userAnswers.get(RegistrationPage).getOrElse(throw new Exception("Upe Registration Data not available"))
        val safeIdValue = safeId match {
          case Some(value) => Some(value.value)
          case _           => None
        }
        println(s" IN UPE ------------------SafeIdValue -----------$safeIdValue")
        /*        val v = for {
          updatedAnswersUpe <- Future.fromTry(userAnswers.set(RegistrationPage, regData.copy(safeId = safeIdValue)))
          savedAnswer       <- userAnswersConnectors.save(updatedAnswersUpe.id, Json.toJson(updatedAnswersUpe.data))
        } yield (UserAnswers(id = id, data = savedAnswer.as[JsObject]))*/
        /*        Future.fromTry(userAnswers.set(RegistrationPage, regData.copy(safeId = safeIdValue))).map { updatedAnswers =>
          userAnswersConnectors.save(updatedAnswers.id, Json.toJson(updatedAnswers.data))
        }*/
        Right(safeId)

      case errorResponse =>
        logger.warn(s"Upe RegisterWithoutID call failed with Status ${errorResponse.status}")
        Left(InternalServerError)
    }

  def fmRegisterationWithoutID(id: String, userAnswers: UserAnswers)(implicit
    hc:                            HeaderCarrier,
    ec:                            ExecutionContext
  ): Future[Either[ApiError, Option[SafeId]]] =
    http.POSTEmpty(s"$fmRegistrationUrl/$id") map {
      case response if is2xx(response.status) =>
        val fmsafeId = response.json.asOpt[RegisterationWithoutIDResponse].map(_.safeId)
        val nfmData  = userAnswers.get(NominatedFilingMemberPage).getOrElse(throw new Exception("Fm Registration Data not available"))
        val safeIdValue = fmsafeId match {
          case Some(value) => Some(value.value)
          case _           => None
        }
        println(s" IN FM ------------------SafeIdValue -----------$safeIdValue")
        /*        for {
          updatedAnswers <- Future.fromTry(userAnswers.set(NominatedFilingMemberPage, nfmData.copy(safeId = safeIdValue)))
          _              <- userAnswersConnectors.save(updatedAnswers.id, Json.toJson(updatedAnswers.data))
        } yield ()*/
        Right(fmsafeId)
      case errorResponse =>
        logger.warn(s"Filing Member RegisterWithoutID call failed with Status ${errorResponse.status}")
        Left(InternalServerError)
    }
}
