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

package services

import akka.Done
import base.SpecBase
import config.FrontendAppConfig
import connectors.{EnrolmentConnector, EnrolmentStoreProxyConnector, RegistrationConnector, SubscriptionConnector}
import models.InternalIssueError
import models.registration.RegistrationInfo
import models.subscription._
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import pages._
import play.api.inject.bind
import play.api.test.Helpers.running
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class GenUrlServiceSpec extends SpecBase {

  val id           = "testId"
  val plrReference = "testPlrRef"

  "GenUrlService" must {

    "generateUrl" when {
      val genUrl = new GenUrlService(appConfig = new FrontendAppConfig(configuration, servicesConfig))

      "generate url for replace filing member" in {
        genUrl.generateUrl("/replace-filing-member", false) mustEqual None
      }
      "generate url for replace filing member  with  authorised false" in {
        genUrl.generateUrl("/replace-filing-member", true) mustEqual None
      }

      "generate url for ASA" in {
        genUrl.generateUrl("/asa/", true).getOrElse("") mustEqual appConfig.asaHomePageUrl
      }

      "generate url for any other page" in {
        genUrl.generateUrl("/upe/", false).getOrElse("") mustEqual appConfig.startPagePillar2Url
      }
      "generate url for any other page e.g new  member" in {
        genUrl.generateUrl("/nfm/", true).getOrElse("") mustEqual "/report-pillar2-top-up-taxes"
      }

    }
  }

}
