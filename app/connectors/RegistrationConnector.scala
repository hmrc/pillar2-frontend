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
import play.api.http.Status.OK
import play.api.libs.json._
import uk.gov.hmrc.http.HttpReads.Implicits.readRaw
import uk.gov.hmrc.http.HttpReads.is2xx
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpException, HttpResponse}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure => ScalaFailure, Success => ScalaSuccess}

class RegistrationConnector @Inject() (val userAnswersConnectors: UserAnswersConnectors, val config: FrontendAppConfig, val http: HttpClient)
    extends Logging {
  val upeRegistrationUrl = s"${config.pillar2BaseUrl}/report-pillar2-top-up-taxes/upe/registration"
  val fmRegistrationUrl  = s"${config.pillar2BaseUrl}/report-pillar2-top-up-taxes/fm/registration"

  def upeRegistrationWithoutID(id: String, userAnswers: UserAnswers)(implicit
    hc:                            HeaderCarrier,
    ec:                            ExecutionContext
  ): Future[Either[ApiError, Option[SafeId]]] =
    http.POSTEmpty(s"$upeRegistrationUrl/$id").flatMap {
      case response if is2xx(response.status) =>
        logger.info(s"Received successful response: ${response.body}")

        val safeIdOpt: Option[String] = response.json.asOpt[RegisterationWithoutIDResponse].map(_.safeId.value)
        logger.info(s"Parsed safeId from response: $safeIdOpt")

        userAnswers.get(RegistrationPage) match {
          case Some(regData) =>
            safeIdOpt match {
              case Some(safeId) =>
                logger.info(s"Retrieved RegistrationPage data: $regData")

                val updatedRegData = regData.copy(registrationInfo = regData.registrationInfo.map(_.copy(safeId = safeId)))

                userAnswers.set(RegistrationPage, updatedRegData).toOption match {
                  case Some(updatedUserAnswers) =>
                    logger.info(s"Successfully updated userAnswers with new safeId: $updatedUserAnswers")

                    userAnswersConnectors
                      .save(id, Json.toJson(updatedUserAnswers.data))
                      .map(_ => Right(Some(SafeId(safeId))))
                      .recover { case e: Exception =>
                        logger.error(s"Failed to save updated user answers: ${e.getMessage}")
                        Left(InternalServerError): Either[ApiError, Option[SafeId]]
                      }

                  case None =>
                    logger.error("Failed to update and set userAnswers with new safeId")
                    Future.successful(Left(InternalServerError): Either[ApiError, Option[SafeId]])
                }

              case None =>
                logger.error("safeIdOpt is None")
                Future.successful(Left(InternalServerError): Either[ApiError, Option[SafeId]])
            }

          case None =>
            logger.error("Failed to get RegistrationPage from userAnswers")
            Future.successful(Left(InternalServerError): Either[ApiError, Option[SafeId]])
        }

      case errorResponse =>
        logger.warn(s"Upe RegisterWithoutID call failed with Status ${errorResponse.status} and body: ${errorResponse.body}")
        Future.successful(Left(InternalServerError))
    }

  def fmRegisterationWithoutID(id: String, userAnswers: UserAnswers)(implicit
    hc:                            HeaderCarrier,
    ec:                            ExecutionContext
  ): Future[Either[ApiError, Option[SafeId]]] = {

    // Extract the FilingMember data from UserAnswers
    val nfmDataOption = userAnswers.get(NominatedFilingMemberPage)

    nfmDataOption match {
      case Some(nfmData) if nfmData.withoutIdRegData.isDefined =>
        // Serialize the WithoutIdNfmData to JSON
        val registrationDataJson = Json.toJson(nfmData.withoutIdRegData.get)

        // Make the API call
        http.POST[JsValue, HttpResponse](s"$fmRegistrationUrl/$id", registrationDataJson).flatMap { response =>
          response.status match {
            case OK =>
              // Changes made here:
              val safeIdOpt = (response.json \ "registerWithoutIDResponse" \ "responseDetail" \ "SAFEID").asOpt[String]

              safeIdOpt match {
                case Some(fmsafeId) =>
                  // Update the FilingMember data with the safeId
                  val updatedFilingMemberData = nfmData.copy(safeId = Some(fmsafeId))

                  // Update the UserAnswers with the new FilingMember data
                  userAnswers.set(NominatedFilingMemberPage, updatedFilingMemberData) match {
                    case ScalaSuccess(updatedAnswers) =>
                      userAnswersConnectors
                        .save(id, Json.toJson(updatedAnswers))
                        .map { _ =>
                          Right(updatedFilingMemberData.safeId.map(id => SafeId(id)))
                        }
                        .recover {
                          case e: HttpException =>
                            println(s"Failed to save data with status: ${e.responseCode}. Message: ${e.getMessage}")
                            logger.error(s"Failed to save data with status: ${e.responseCode}. Message: ${e.getMessage}")
                            Left(InternalServerError): Either[ApiError, Option[SafeId]]
                          case e =>
                            logger.error(s"Unexpected error: ${e.getMessage}")
                            Left(InternalServerError): Either[ApiError, Option[SafeId]]
                        }
                    case ScalaFailure(e) =>
                      logger.error(s"Failed to update UserAnswers: ${e.getMessage}")
                      Future.successful(Left(InternalServerError): Either[ApiError, Option[SafeId]])
                  }

                case None =>
                  logger.error(s"SAFEID not found in response.")
                  Future.successful(Left(InternalServerError): Either[ApiError, Option[SafeId]])
              }

            case _ =>
              logger.error(s"API call failed with status: ${response.status}. Response body: ${response.body}")
              Future.successful(Left(InternalServerError))
          }
        }

      case _ =>
        logger.error("WithoutIdNfmData not found in UserAnswers.")
        Future.successful(Left(InternalServerError))
    }
  }

}
