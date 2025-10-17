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

    ".enrolmentKey and .enrolmentIdentifier" must {
      "return correct enrolment values" in {
        config.enrolmentKey mustBe "HMRC-PILLAR2-ORG"
        config.enrolmentIdentifier mustBe "PLRID"
      }
    }

    ".timeout and .countdown" must {
      "return correct timeout values" in {
        config.timeout mustBe 900
        config.countdown mustBe 120
      }
    }

    ".accessibilityStatementPath" must {
      "return correct accessibility statement path" in {
        config.accessibilityStatementPath mustBe "/accessibility-statement/pillar2"
      }
    }

    ".phase2ScreensEnabled" must {
      "return correct feature flag value" in {
        config.phase2ScreensEnabled mustBe false
      }
    }

    ".newHomepageEnabled" must {
      "return correct feature flag value" in {
        config.newHomepageEnabled mustBe false
      }
    }

    ".enablePayByBankAccount" must {
      "return correct feature flag value" in {
        config.enablePayByBankAccount mustBe true
      }
    }

    ".subscriptionPollingTimeoutSeconds and .subscriptionPollingIntervalSeconds" must {
      "return correct polling configuration values" in {
        config.subscriptionPollingTimeoutSeconds mustBe 20
        config.subscriptionPollingIntervalSeconds mustBe 2
      }
    }

    ".incorporatedEntityBvEnabled and .partnershipBvEnabled" must {
      "return correct BV enabled flags" in {
        config.incorporatedEntityBvEnabled mustBe false
        config.partnershipBvEnabled mustBe false
      }
    }

    ".cacheTtl" must {
      "return correct cache TTL value" in {
        config.cacheTtl mustBe 900
      }
    }

    ".researchUrl" must {
      "return correct research URL" in {
        config.researchUrl mustBe "https://docs.google.com/forms/d/e/1FAIpQLScinYzxH9XrSPZzqc_kiF2sfO8u5z75YdgzaSp49WwdzJ7XzQ/viewform?usp=sharing&ouid=112858317209071254702"
      }
    }

    ".howToPayPillar2TaxesUrl" must {
      "return correct guidance URL" in {
        config.howToPayPillar2TaxesUrl mustBe "https://www.gov.uk/guidance/pay-pillar-2-top-up-taxes-domestic-top-up-tax-and-multinational-top-up-tax"
      }
    }

    ".btaAccessEnabled" must {
      "return correct feature flag value" in {
        config.btaAccessEnabled mustBe true
      }
    }

    ".handleObligationsAndSubmissions500Errors" must {
      "return correct feature flag value" in {
        config.handleObligationsAndSubmissions500Errors mustBe false
      }
    }

    ".obligationsAndSubmissionsTimeoutMilliseconds" must {
      "return correct timeout value" in {
        config.obligationsAndSubmissionsTimeoutMilliseconds mustBe 5000
      }
    }
  }
}
