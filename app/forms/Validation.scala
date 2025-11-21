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
  final val EmailRegex: String =
    """^(?!\.)("([^"\r\\]|\\["\r\\])*"|([-a-zA-Z0-9!#$%&'*+/=?^_`{|}~]|(?<!\.)\.)*)(?<!\.)@[a-zA-Z0-9][\w\.-]*[a-zA-Z0-9]\.[a-zA-Z][a-zA-Z\.]*[a-zA-Z]$"""
  final val GroupIdRegex:           String = "^X[A-Z]PLR[0-9]{10}$"
  final val RegistrationPhoneRegex: String = """^[A-Z0-9 )/(\-*#+]+$"""
  final val PhoneRegex:             String =
    "^(?:(?:\\(?(?:00|\\+)([1-4]\\d\\d|[1-9]\\d?)\\)?)?[-. /]?)?((?:\\(?\\d{1,}\\)?[-. /]?){0,})(?:[-. /]?(?:#|ext\\.?|extension|x)[-. /]?(\\d+))?$"
  final val BICSwiftRegex:             String = "^[A-Z]{6}[A-Z0-9]{2}([A-Z0-9]{3})?$"
  final val IBANRegex:                 String = "^[A-Z]{2}[0-9]{2}[0-9A-Z]{10,30}$"
  final val MonetaryRegex:             String = """^-?(\d*(\.\d{1,2})?)$"""
  final val SortCodeRegex:             String = """^[0-9]{6}$"""
  final val AccountNumberRegex:        String = """^[0-9]{8}$"""
  final val XSSRegex:                  String = """^[^<>"&]*$"""
  final val XSSRegexAllowAmpersand:    String = """^[^<>"]*$"""
  final val AddressRegexWithAmpersand: String = """^[a-zA-Z0-9\/\-,\.\\& ]+$"""
  final val AddressRegex:              String = """^[a-zA-Z0-9\/\-,\.\\ ]+$"""
}
