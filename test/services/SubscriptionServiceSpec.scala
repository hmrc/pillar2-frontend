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

package services

import base.SpecBase
import connectors.SubscriptionConnector
import models.SubscriptionCreateError
import models.subscription.SubscriptionResponse
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder

import java.time.LocalDate
import scala.concurrent.Future

class SubscriptionServiceSpec extends SpecBase {

  val service: SubscriptionService = app.injector.instanceOf[SubscriptionService]

  override lazy val app: Application = new GuiceApplicationBuilder()
    .overrides(
      bind[SubscriptionConnector].toInstance(mockSubscriptionConnector)
    )
    .build()

  "SubscriptionService" when {
    "must return Pillar2Id if all success" in {
      val response = SubscriptionResponse(
        plrReference = "XE1111123456789",
        formBundleNumber = "12345678",
        processingDate = LocalDate.now().atStartOfDay()
      )
      when(mockSubscriptionConnector.crateSubscription(any())(any(), any())).thenReturn(Future.successful(Some(response)))
      service.checkAndCreateSubscription("id", "123456789", Some("987654321")).map { res =>
        res mustBe (Right(response))
      }
    }

    "must return when there is problem of creating" in {
      val response = Future.successful(None)
      when(mockSubscriptionConnector.crateSubscription(any())(any(), any())).thenReturn(response)
      service.checkAndCreateSubscription("id", "123456789", Some("987654321")).map { res =>
        res mustBe Left(SubscriptionCreateError)
      }
    }

  }
}
