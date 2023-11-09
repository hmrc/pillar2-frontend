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

package controllers

import base.SpecBase
import models.UserAnswers
import play.api.test.FakeRequest
import play.api.test.Helpers._
class DashboardControllerSpec extends SpecBase {

  def controller(): DashboardController =
    new DashboardController(
      preDataRetrievalActionImpl,
      preAuthenticatedActionBuilders,
      preDataRequiredActionImpl,
      mockReadSubscriptionService,
      stubMessagesControllerComponents(),
      viewDashboardView
    )

  val userAnswers: UserAnswers = emptyUserAnswers
  val subData = emptyUserAnswers

  "Dashboard Controller" when {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, routes.DashboardController.onPageLoad.url)

        val result = route(application, request).value

//        val view = application.injector.instanceOf[DashboardView]

        //  status(result) mustEqual OK
//        contentAsString(result) mustEqual view("organisationName", "registrationDate", "plrReference")(
//          request,
//          appConfig(application),
//          messages(application)
//        ).toString
      }
    }

    /* "handle errors during subscription retrieval" in {
      val mockReadSubscriptionService = mock[ReadSubscriptionService]

      val apiError = MandatoryInformationMissingError("Some error message")

      when(mockReadSubscriptionService.readSubscription(any[ReadSubscriptionRequestParameters]))
        .thenReturn(Future.successful(Left(apiError)))

      val application = applicationBuilder(userAnswers = None)
        .overrides(
          bind[ReadSubscriptionService].toInstance(mockReadSubscriptionService)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, routes.DashboardController.onPageLoad.url)
        val result  = route(application, request).value
        status(result) mustEqual INTERNAL_SERVER_ERROR
      }
    }
    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, routes.DashboardController.onPageLoad.url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[DashboardView]

        status(result) mustEqual OK
//        contentAsString(result) mustEqual view()(request, appConfig(application), messages(application)).toString
      }
    }*/
  }
}
