/*
 * Copyright 2025 HM Revenue & Customs
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

package models.audit

import models.btn.BTNSuccessResponse
import models.btn.BtnResponse
import models.hip.ApiFailureResponse
import models.subscription.AccountingPeriod
import play.api.libs.functional.syntax.*
import play.api.libs.json.*
import uk.gov.hmrc.http.HttpResponse

import java.time.*
import scala.util.Try

case class CreateBtnAuditEvent(
  pillarReference:            String,
  accountingPeriodStart:      LocalDate,
  accountingPeriodEnd:        LocalDate,
  entitiesInsideAndOutsideUK: Boolean,
  apiResponseData:            ApiResponseData
) extends AuditEvent {
  override val auditType:  String  = BtnAuditCommonValues.auditType
  override val detailJson: JsValue = Json.toJson(this)
}

object CreateBtnAuditEvent {
  given writes: OWrites[CreateBtnAuditEvent] = Json.writes[CreateBtnAuditEvent]
}

sealed trait ApiResponseData {
  def statusCode:      Int
  def processedAt:     ZonedDateTime
  def responseMessage: String
}

final case class ApiResponseSuccess(
  statusCode:  Int,
  processedAt: ZonedDateTime
) extends ApiResponseData {
  val responseMessage = "Success"
}

final case class ApiResponseFailure(
  statusCode:      Int,
  processedAt:     ZonedDateTime,
  errorCode:       String,
  responseMessage: String
) extends ApiResponseData

object ApiResponseData {
  def fromBtnResponse(btnResponse: BtnResponse)(using clock: Clock): ApiResponseData = btnResponse.result match {
    case Right(success) =>
      ApiResponseSuccess(
        btnResponse.httpStatusCode,
        success.processingDate
      )
    case Left(failure) =>
      ApiResponseFailure(
        btnResponse.httpStatusCode,
        ZonedDateTime.now(clock),
        failure.errorCode,
        failure.message
      )
  }

  def fromHttpResponse(response: HttpResponse)(using clock: Clock): ApiResponseData = {

    val jsonOpt = Try(response.json).toOption

    response.status match {
      case 201 =>
        val processingDate = jsonOpt
          .flatMap(_.validate[BTNSuccessResponse].asOpt)
          .map(_.success.processingDate)
          .getOrElse(ZonedDateTime.now(clock))
        ApiResponseSuccess(response.status, processingDate)

      case _ =>
        val (errorCode, message) = jsonOpt.flatMap(_.validate[ApiFailureResponse].asOpt) match {
          case Some(etmpError) =>
            (etmpError.errors.code, etmpError.errors.text)
          case None =>
            // Fallback for non-422 errors (like 400/500 from API platform/ETMP)
            val jsonFallbackCode = jsonOpt
              .flatMap(j =>
                (j \ "code")
                  .asOpt[String]
                  .orElse((j \ "failures" \ 0 \ "code").asOpt[String])
              )
              .getOrElse("UNKNOWN")

            val jsonFallbackMessage = jsonOpt
              .flatMap(j =>
                (j \ "message")
                  .asOpt[String]
                  .orElse((j \ "failures" \ 0 \ "reason").asOpt[String])
              )
              .getOrElse(response.body)

            (jsonFallbackCode, jsonFallbackMessage)
        }

        ApiResponseFailure(response.status, ZonedDateTime.now(clock), errorCode, message)
    }
  }

  given writes: Writes[ApiResponseData] = {
    case success @ ApiResponseSuccess(_, _)       => Json.toJson(success)
    case failure @ ApiResponseFailure(_, _, _, _) => Json.toJson(failure)
  }
}

object ApiResponseSuccess {
  given writes: Writes[ApiResponseSuccess] = (
    (__ \ "statusCode").write[Int] and
      (__ \ "messageResponseData" \ "success" \ "processingDate").write[Instant] and
      (__ \ "messageResponseData" \ "success" \ "responseMessage").write[String]
  )(resp => (resp.statusCode, resp.processedAt.toInstant, resp.responseMessage))
}

object ApiResponseFailure {
  given writes: Writes[ApiResponseFailure] = (
    (__ \ "statusCode").write[Int] and
      (__ \ "messageResponseData" \ "failure" \ "processingDate").write[Instant] and
      (__ \ "messageResponseData" \ "failure" \ "responseMessage").write[String] and
      (__ \ "messageResponseData" \ "failure" \ "errorCode").write[String]
  )(resp => (resp.statusCode, resp.processedAt.toInstant, resp.responseMessage, resp.errorCode))
}

case class BtnAlreadySubmittedAuditEvent(
  pillarReference:         String,
  accountingPeriod:        AccountingPeriod,
  entitiesInsideOutsideUk: Boolean
) extends AuditEvent {
  override val auditType:  String  = BtnAuditCommonValues.auditType
  override val detailJson: JsValue = Json.toJson(this)
}

object BtnAlreadySubmittedAuditEvent {
  given writes: Writes[BtnAlreadySubmittedAuditEvent] = (
    (__ \ "pillarReference").write[String] and
      (__ \ "accountingPeriodStart").write[LocalDate] and
      (__ \ "accountingPeriodEnd").write[LocalDate] and
      (__ \ "entitiesInsideAndOutsideUK").write[Boolean]
  ) { event =>
    (
      event.pillarReference,
      event.accountingPeriod.startDate,
      event.accountingPeriod.endDate,
      event.entitiesInsideOutsideUk
    )
  }
}

private object BtnAuditCommonValues {
  val auditType = "belowThresholdNotification"
}
