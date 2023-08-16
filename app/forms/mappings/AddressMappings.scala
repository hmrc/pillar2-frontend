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

package forms.mappings

import play.api.data.Forms.of
import play.api.data.{FieldMapping, FormError, Mapping}
import utils.countryOptions.CountryOptions

trait AddressMappings extends Mappings with Constraints with Transforms {

  protected def optionalPostcode(
    requiredKey:      Option[String],
    invalidKey:       String,
    nonUkLengthKey:   String,
    countryFieldName: String
  ): FieldMapping[Option[String]] =
    of(optionalPostcodeFormatter(requiredKey, invalidKey, nonUkLengthKey, countryFieldName))

  def countryMapping(countryOptions: CountryOptions, keyRequired: String, keyInvalid: String): Mapping[String] =
    text(keyRequired)
      .verifying(country(countryOptions, keyInvalid))

}

object AddressMappings {
  val maxAddressLineLength = 35
  val maxPostCodeLength    = 8
}
