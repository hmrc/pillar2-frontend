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

import play.api.data.FieldMapping
import play.api.data.Forms.of

trait AddressMappings extends Mappings with Constraints with Transforms {

  protected def optionalPostcode(
    requiredKeyGB:    String = "address.postcode.error.invalid.GB",
    invalidLengthKey: String = "address.postcode.error.length",
    countryFieldName: String = "countryCode"
  ): FieldMapping[Option[String]] = of(optionalPostcodeFormatter(requiredKeyGB, invalidLengthKey, countryFieldName))
  protected def mandatoryPostcode(
    requiredKeyGB:    String = "address.postcode.error.invalid.GB",
    requiredKeyOther: String = "address.postcode.error.required",
    invalidLengthKey: String = "address.postcode.error.length",
    countryFieldName: String = "countryCode"
  ): FieldMapping[String] = of(mandatoryPostcodeFormatter(requiredKeyGB, requiredKeyOther, invalidLengthKey, countryFieldName))

}

object AddressMappings {
  val maxAddressLineLength = 35
  val maxPostCodeLength    = 10
}
