/*
 * Copyright 2026 HM Revenue & Customs
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
import config.FrontendAppConfig
import org.mockito.Mockito.verifyNoInteractions
import play.api.libs.concurrent.Futures
import uk.gov.hmrc.http.HeaderCarrier

import java.time.Clock

class SubscriptionOrchestrationServiceSpec extends SpecBase {

  private given FrontendAppConfig = applicationConfig
  private given Clock             = Clock.systemUTC()
  private val futures: Futures = app.injector.instanceOf[Futures]

  private val service =
    new SubscriptionOrchestrationService(
      subscriptionService = mockSubscriptionService,
      userAnswersConnectors = mockUserAnswersConnectors,
      sessionRepository = mockSessionRepository,
      futures = futures
    )

  private given HeaderCarrier = HeaderCarrier()

  "SubscriptionOrchestrationService.onSubmit" should {
    "redirect to the in-progress task list when finalStatusCheck is false" in {
      val result = service.onSubmit(userId = "user-id-1", userAnswers = emptyUserAnswers).futureValue

      result.header.status mustBe SEE_OTHER
      result.header.headers(LOCATION) mustBe controllers.subscription.routes.InprogressTaskListController.onPageLoad.url
      verifyNoInteractions(mockSubscriptionService)
    }
  }
}
