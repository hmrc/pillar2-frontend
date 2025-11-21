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

package models.audit

import models.UkOrAbroadBankAccount
import models.repayments.{BankAccountDetails, NonUKBank}
import play.api.libs.json.*

case class RepaymentsAuditEvent(
  refundAmount:              BigDecimal,
  reasonForRequestingRefund: String,
  ukOrAbroadBankAccount:     UkOrAbroadBankAccount,
  uKBankAccountDetails:      Option[BankAccountDetails],
  nonUKBank:                 Option[NonUKBank],
  repaymentsContactName:     String,
  repaymentsContactEmail:    String,
  repaymentsContactByPhone:  Boolean,
  repaymentsPhoneDetails:    Option[String]
) extends AuditEvent {
  override val auditType:  String  = "Pillar2ClaimRepayments"
  override val detailJson: JsValue = Json.toJson(this)
}

object RepaymentsAuditEvent {
  implicit val formats: Format[RepaymentsAuditEvent] = Json.format[RepaymentsAuditEvent]
}
