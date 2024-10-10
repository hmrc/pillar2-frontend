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

import base.SpecBase
import com.typesafe.config.ConfigException
import mapping.Constants.{ENGLISH, WELSH}
import models.grs.EntityType
import org.mockito.MockitoSugar
import play.api.Application
import play.api.i18n.{Lang, MessagesApi, MessagesImpl}
import utils.InputOption

import scala.language.implicitConversions

class CountryOptionsSpec extends SpecBase with MockitoSugar {

  override lazy val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  def application(language: String = ENGLISH): Application = {
    val config = language match {
      case ENGLISH => Map("location.canonical.list.all" -> "countries-canonical-list-test.json")
      case WELSH   => Map("location.canonical.list.allCY" -> "countries-canonical-list-test-cy.json")
      case _       => Map("location.canonical.list.all" -> "countries-canonical-test.json")
    }

    applicationBuilder().configure(config).build()
  }

  val countryOption:         CountryOptions   = application().injector.instanceOf[CountryOptions]
  val countryListUkIncluded: Seq[InputOption] = Seq(InputOption("ES", "Spain"), InputOption("GB", "United Kingdom"))
  val countryListUkExcluded: Seq[InputOption] = Seq(InputOption("ES", "Spain"))

  "Country Options" must {
    implicit def messages(language: String = ENGLISH): MessagesImpl = MessagesImpl(lang = Lang(language), messagesApi = messagesApi)

    "render correct InputOptions with country list and country code - English" should {

      "include UK in the country list" when {
        "EntityType.Other i.e. companyTypeNotListed, irrespective of registered/base location" in {

          countryOption.conditionalUkInclusion(None, entityTypePage = Some(EntityType.Other))(messages()) mustEqual countryListUkIncluded
        }

        "UK-registered/based" in {

          countryOption.conditionalUkInclusion(Some(true), entityTypePage = Some(EntityType.UkLimitedCompany))(
            messages()
          ) mustEqual countryListUkIncluded
        }

        "force-inclusion, skipping location/type checks" in {

          countryOption.options()(messages()) mustEqual countryListUkIncluded
        }
      }

      "not include UK in the country list" when {
        "unknown registered/base location and not companyTypeNotListed" in {

          countryOption.conditionalUkInclusion(None, entityTypePage = Some(EntityType.UkLimitedCompany))(messages()) mustEqual countryListUkExcluded
        }

        "not UK-registered/based and not companyTypeNotListed" in {

          countryOption.conditionalUkInclusion(Some(false), entityTypePage = Some(EntityType.UkLimitedCompany))(
            messages()
          ) mustEqual countryListUkExcluded
        }

        "not include irrespective of location/type checks" in {

          countryOption.options(includeUk = false)(messages()) mustEqual countryListUkExcluded
        }
      }
    }

    "render correct InputOptions with all country list and country code - Welsh" in {
      val countryOption: CountryOptions = application(WELSH).injector.instanceOf[CountryOptions]

      countryOption.options()(messages(WELSH)) mustEqual Seq(InputOption("ES", "Sbaen"), InputOption("GB", "Y Deyrnas Unedig"))
    }
    "throw the error if the country json does not exist" in {

      an[ConfigException.BadValue] shouldBe thrownBy {
        application("INVALID").injector.instanceOf[CountryOptions].options()
      }
    }
  }
}
