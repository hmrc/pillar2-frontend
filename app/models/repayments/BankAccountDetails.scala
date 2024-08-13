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

package models.repayments

import play.api.libs.json.{Json, OFormat}
import play.twirl.api.HtmlFormat

final case class BankAccountDetails(bankName: String, nameOnBankAccount: String, sortCode: String, accountNumber: String) {

  val field1 = HtmlFormat.escape(bankName).toString + "<br>"
  val field2 = HtmlFormat.escape(nameOnBankAccount.mkString("")) + "<br>"
  val field3 = HtmlFormat.escape(sortCode).toString + "<br>"
  val field4 = HtmlFormat.escape(accountNumber.mkString("")) + "<br>"
  val fullDetails: String = field1 + field2 + field3 + field4
}

object BankAccountDetails {
  implicit val format: OFormat[BankAccountDetails] = Json.format[BankAccountDetails]
}
