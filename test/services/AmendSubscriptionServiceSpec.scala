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
import connectors.{AmendSubscriptionConnector, SubscriptionConnector}
import models.SubscriptionCreateError
import models.subscription.{AmendSubscriptionRequestParameters, SubscriptionResponse}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.JsValue
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}

import java.time.LocalDate
import scala.concurrent.{ExecutionContext, Future}
import play.api.libs.json._
class AmendSubscriptionServiceSpec extends SpecBase {

  val service: AmendSubscriptionService = app.injector.instanceOf[AmendSubscriptionService]

  override lazy val app: Application = new GuiceApplicationBuilder()
    .overrides(
      bind[AmendSubscriptionConnector].toInstance(mockAmendSubscriptionConnector)
    )
    .build()

  "AmendSubscriptionService" when {

    "must return Pillar2Id if all success" in {
      val mockResponse: Option[JsValue] =
        Some(Json.parse("""{"success":{"processingDate":"2022-01-31T09:26:17Z","formBundleNumber":"119000004320"}}"""))
      when(mockAmendSubscriptionConnector.amendSubscription(any[AmendSubscriptionRequestParameters])(any[HeaderCarrier], any[ExecutionContext]))
        .thenReturn(Future.successful(mockResponse))

      service.amendSubscription(AmendSubscriptionRequestParameters("id")).map { res =>
        res mustBe mockResponse
      }
    }

    "must return when there is a problem of creating" in {
      when(mockAmendSubscriptionConnector.amendSubscription(any[AmendSubscriptionRequestParameters])(any[HeaderCarrier], any[ExecutionContext]))
        .thenReturn(Future.successful(None))

      service.amendSubscription(AmendSubscriptionRequestParameters("id")).map { res =>
        res mustBe Left(SubscriptionCreateError)
      }
    }

  }
}
