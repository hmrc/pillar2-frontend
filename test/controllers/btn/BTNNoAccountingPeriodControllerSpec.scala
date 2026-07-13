/*
 * Copyright 2025 HM Revenue & Customs
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

package controllers.btn

import base.SpecBase
import connectors.SubscriptionConnector
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.Application
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import uk.gov.hmrc.http.HeaderCarrier
import views.html.btn.BTNNoAccountingPeriodView

import scala.concurrent.{ExecutionContext, Future}

class BTNNoAccountingPeriodControllerSpec extends SpecBase {

  def application: Application = applicationBuilder(subscriptionLocalData = Some(someSubscriptionLocalData), userAnswers = Some(emptyUserAnswers))
    .overrides(
      bind[SubscriptionConnector].toInstance(mockSubscriptionConnector)
    )
    .build()

  "BTNNoAccountingPeriodController" should {

    "return OK and the correct view for a GET" in
      running(application) {
        when(mockSubscriptionConnector.getSubscriptionCache(any())(using any[HeaderCarrier], any[ExecutionContext]))
          .thenReturn(Future.successful(Some(someSubscriptionLocalData)))

        val request = FakeRequest(GET, routes.BTNNoAccountingPeriodController.onPageLoad.url)
        val result  = route(application, request).value

        val view = application.injector.instanceOf[BTNNoAccountingPeriodView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual view(isAgent = false)(request, applicationConfig, messages(application)).toString
      }

    "redirect to BTN error page when no subscription data is found" in {
      val testApplication: Application = applicationBuilder(subscriptionLocalData = None, userAnswers = Some(emptyUserAnswers))
        .overrides(
          bind[SubscriptionConnector].toInstance(mockSubscriptionConnector)
        )
        .build()

      running(testApplication) {
        val request = FakeRequest(GET, routes.BTNNoAccountingPeriodController.onPageLoad.url)
        val result  = route(testApplication, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.btn.routes.BTNProblemWithServiceController.onPageLoad.url
      }
    }
  }
}
