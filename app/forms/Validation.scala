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

package forms

object Validation {
  final val EMAIL_REGEX =
    """^(?!\.)("([^"\r\\]|\\["\r\\])*"|([-a-zA-Z0-9!#$%&'*+/=?^_`{|}~]|(?<!\.)\.)*)(?<!\.)@[a-zA-Z0-9][\w\.-]*[a-zA-Z0-9]\.[a-zA-Z][a-zA-Z\.]*[a-zA-Z]$"""
  final val GROUPID_REGEX              = "^X[A-Z]PLR[0-9]{10}$"
  final val TELEPHONE_REGEX            = "^[0-9 +()]{0,25}$"
  final val REPAYMENTS_TELEPHONE_REGEX = "^[0-9 +()]{0,50}$"
  final val BIC_SWIFT_REGEX            = "^[A-Z]{6}[A-Z0-9]{2}([A-Z0-9]{3})?$"
  final val IBAN_REGEX                 = "^[A-Z]{2}[0-9]{2}[0-9A-Z]{10,30}$"
  final val MONETARY_REGEX             = """^-?(\d*(\.\d{1,2})?)$"""
}
