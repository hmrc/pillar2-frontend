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

package forms

import forms.behaviours.StringFieldBehaviours
import play.api.data.FormError

import scala.collection.immutable.Seq

class SecondaryTelephoneFormProviderSpec extends StringFieldBehaviours {

  val requiredKey = "secondaryTelephone.error.required"
  val lengthKey   = "secondaryTelephone.error.length"
  val formatKey   = "secondaryTelephone.error.format"
  val maxLength   = 24
  val formatReg   = """^[A-Z0-9 )/(\-*#+]*$"""

  val form = new SecondaryTelephoneFormProvider()("test")

  ".value" - {

    val fieldName = "value"

//    behave like fieldThatBindsValidData(
//      form,
//      fieldName,
//      regexWithMaxLength(maxLength, formatReg)
//    )
//
//    behave like regexFieldWithMaxLength(
//      form,
//      fieldName,
//      maxLength = maxLength,
//      regex = formatReg,
//      lengthError = FormError(fieldName, lengthKey, Seq(maxLength)),
//      formatError = FormError(fieldName, requiredKey, Seq("test"))
//    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey, Seq("test"))
    )
  }
}
