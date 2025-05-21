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

package config

import base.SpecBase
import org.mockito.Mockito.when
import play.api.inject.Injector
import play.api.mvc.RequestHeader

class FrontendAppConfigSpec extends SpecBase {

  def injector:                   Injector          = app.injector
  val config:                     FrontendAppConfig = injector.instanceOf[FrontendAppConfig]
  implicit val mockRequestHeader: RequestHeader     = mock[RequestHeader]

  "FrontendAppConfig" when {

    ".btaHomePageUrl" must {
      "return bta homepage URL" in {
        config.btaHomePageUrl mustBe "http://localhost:9020/business-account"
      }
    }
    ".supportUrl" must {
      "return support URL" in {
        when(mockRequestHeader.uri).thenReturn("/some/test/uri")
        config.supportUrl mustBe "http://localhost:9250/contact/report-technical-problem?service=pillar2-frontend&referrerUrl=%2Fsome%2Ftest%2Furi"
      }
    }
    ".howToRegisterPlr2GuidanceUrl" must {
      "how to register pillar 2 guidance URL" in {
        config.howToRegisterPlr2GuidanceUrl mustBe "https://www.gov.uk/government/publications/introduction-of-the-new-multinational-top-up-tax"
      }
    }
  }

}
