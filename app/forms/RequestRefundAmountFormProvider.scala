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

import forms.mappings.Mappings
import mapping.Constants
import play.api.data.Form

import javax.inject.Inject

class RequestRefundAmountFormProvider @Inject() extends Mappings {
  // private val currencyRegex = """^[A-Z0-9 )/(\-*#+]*$"""
  // /^(([1-9]{1,3}(,\d{3})*(\.\d{2})?)|(0\.[1-9]\d)|(0\.0[1-9]))$/
  private val currencyRegex = """^(([1-9]{1,3}(,\d{3})*(\.\d{2})?)|(0\.[1-9]\d)|(0\.0[1-9]))$"""
  val minValue              = -99999999999.99
  val maxValue              = 99999999999.99
  def apply(): Form[BigDecimal] =
    Form(
      "value" -> bigDecimal("payment.requestRefundAmount.error.required")
        // .verifying(maxLength(Constants.MAX_LENGTH_160, "payment.requestRefundAmount.error.length"))
        // .verifying(regexp(currencyRegex, "payment.requestRefundAmount.messages.error.format"))
        .verifying(minimumValue[BigDecimal](minValue, "payment.requestRefundAmount.error.minVale"))
        .verifying(maximumValue[BigDecimal](maxValue, "payment.requestRefundAmount.error.maxValue"))
    )
}
