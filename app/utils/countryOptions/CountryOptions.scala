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

package utils.countryOptions

import com.typesafe.config.ConfigException
import config.FrontendAppConfig
import mapping.Constants.UK_COUNTRY_CODE
import models.grs.EntityType
import play.api.Environment
import play.api.libs.json.Json
import utils.InputOption

import javax.inject.{Inject, Singleton}

@Singleton
class CountryOptions @Inject() (environment: Environment, config: FrontendAppConfig) {

  def conditionalUkInclusion(countryPage: Option[Boolean], entityTypePage: Option[EntityType]): Seq[InputOption] = {
    val isUkRegistered = entityTypePage match {
      case Some(EntityType.Other) => true
      case _                      => countryPage.fold(false)(identity)
    }
    options(isUkRegistered)
  }

  def options(includeUk: Boolean = true): Seq[InputOption] =
    CountryOptions.getCountries(environment, fileName = config.locationCanonicalList, includeUk)

  def getCountryNameFromCode(code: String): String =
    options()
      .find(_.value == code)
      .map(_.label)
      .getOrElse(code)

}
object CountryOptions {

  private def getCountries(environment: Environment, fileName: String, includeUk: Boolean): Seq[InputOption] =
    environment
      .resourceAsStream(fileName)
      .flatMap { in =>
        val locationJsValue = Json.parse(in)
        Json.fromJson[Seq[Seq[String]]](locationJsValue).asOpt.map { countryList =>
          val countries = countryList.map { country =>
            InputOption(country(1).replaceAll("country:", ""), country.head)
          }

          val filteredCountries = if (includeUk) countries else countries.filterNot(_.value == UK_COUNTRY_CODE)
          filteredCountries.sortBy(_.label.toLowerCase)
        }
      }
      .getOrElse {
        throw new ConfigException.BadValue(fileName, "country json does not exist")
      }

}
