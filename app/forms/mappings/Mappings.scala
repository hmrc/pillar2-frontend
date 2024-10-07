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

package forms.mappings

import models.Enumerable
import play.api.data.FieldMapping
import play.api.data.Forms.of

import java.time.LocalDate

// Ensure that Mappings trait extends both Formatters and Constraints
trait Mappings extends Formatters with Constraints {

  // String formatter usage for text fields
  protected def text(errorKey: String = "error.required", args: Seq[String] = Seq.empty): FieldMapping[String] =
    of(stringFormatter(errorKey, args))

  // Formatter for pillar2Id
  protected def pillar2Id(errorKey: String = "error.required", args: Seq[String] = Seq.empty): FieldMapping[String] =
    of(pillar2IdFormatter(errorKey, args))

  // Formatter for bank account
  protected def bankAccount(errorKey: String = "error.required", args: Seq[String] = Seq.empty): FieldMapping[String] =
    of(bankAccountFormatter(errorKey, args))

  // Formatter for sort code
  protected def sortCode(errorKey: String = "error.required", args: Seq[String] = Seq.empty): FieldMapping[String] =
    of(sortCodeFormatter(errorKey, args))

  // Formatter for integer fields with validation
  protected def int(
    requiredKey:    String = "error.required",
    wholeNumberKey: String = "error.wholeNumber",
    nonNumericKey:  String = "error.nonNumeric",
    invalidLength:  String = "error.length",
    args:           Seq[String] = Seq.empty
  ): FieldMapping[Int] =
    of(intFormatter(requiredKey, wholeNumberKey, nonNumericKey, invalidLength, args))

  // Formatter for boolean fields
  protected def boolean(
    requiredKey: String = "error.required",
    invalidKey:  String = "error.boolean",
    args:        Seq[String] = Seq.empty
  ): FieldMapping[Boolean] =
    of(booleanFormatter(requiredKey, invalidKey, args))

  // Formatter for currency values
  protected def currency(
    requiredKey:     String = "error.required",
    invalidCurrency: String = "error.invalidNumeric"
  ): FieldMapping[BigDecimal] =
    of(currencyFormatter(requiredKey, invalidCurrency))

  // Formatter for enumerations
  protected def enumerable[A](requiredKey: String = "error.required", invalidKey: String = "error.invalid", args: Seq[String] = Seq.empty)(implicit
    ev:                                    Enumerable[A]
  ): FieldMapping[A] =
    of(enumerableFormatter[A](requiredKey, invalidKey, args))

  // Formatter for LocalDate with validation
  protected def localDate(
    invalidKey:         String,
    allRequiredKey:     String,
    twoRequiredKey:     String,
    requiredKey:        String,
    invalidDay:         String,
    invalidDayLength:   String,
    invalidMonth:       String,
    invalidMonthLength: String,
    invalidYear:        String,
    invalidYearLength:  String,
    args:               Seq[String] = Seq.empty,
    messageKeyPart:     String
  ): FieldMapping[LocalDate] =
    of(
      new LocalDateFormatter(
        invalidKey,
        allRequiredKey,
        twoRequiredKey,
        requiredKey,
        invalidDay,
        invalidDayLength,
        invalidMonth,
        invalidMonthLength,
        invalidYear,
        invalidYearLength,
        args,
        messageKeyPart
      )
    )
}
