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

import forms.behaviours.{DecimalFieldBehaviours, StringFieldBehaviours}
import org.scalacheck.Gen
import play.api.data.{Form, FormError}

class RequestRefundAmountFormProviderSpec extends DecimalFieldBehaviours with StringFieldBehaviours {

  val fieldName   = "value"
  val requiredKey = "repayment.requestRepaymentAmount.error.required"
  val invalidKey  = "repayment.requestRepaymentAmount.error.format"
  val minValueKey = "repayment.requestRepaymentAmount.error.minValue"
  val maxValueKey = "repayment.requestRepaymentAmount.error.maxValue"
  val maximum     = 99999999999.99
  val minimum     = 0.0

  val validDataGenerator: Gen[String] = decimalInRangeWithCommas(minimum, maximum)

  val formProvider = new RequestRefundAmountFormProvider()
  private val form: Form[BigDecimal] = formProvider()

  ".value" - {

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )

    behave like decimalField(form, fieldName, FormError(fieldName, invalidKey, List()))

    behave like decimalFieldWithMinimum(
      form,
      fieldName,
      BigDecimal.valueOf(minimum),
      FormError(fieldName, minValueKey, Seq(BigDecimal(minimum)))
    )

    behave like decimalFieldWithMaximum(
      form,
      fieldName,
      BigDecimal.valueOf(maximum),
      FormError(fieldName, maxValueKey, Seq(BigDecimal(maximum)))
    )
  }

}
