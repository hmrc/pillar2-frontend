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

package utils.countryOptions

import com.google.inject.Inject
import config.FrontendAppConfig
import mapping.Constants.UK_COUNTRY_CODE
import play.api.Environment
import play.api.i18n.Messages
import utils.InputOption

import javax.inject.Singleton

@Singleton
class CountryOptionsNonUK @Inject() (
  environment: Environment,
  config:      FrontendAppConfig
) extends CountryOptions(environment, config) {
  override def options()(implicit messages: Messages): Seq[InputOption] =
    CountryOptions.getCountries(environment, getFileName()).filterNot(x => x.value == UK_COUNTRY_CODE)
}
