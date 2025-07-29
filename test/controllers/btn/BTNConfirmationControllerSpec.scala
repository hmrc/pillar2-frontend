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

package controllers.btn

import base.SpecBase
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.http.HeaderNames
import play.api.inject._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.FopService
import views.html.btn.BTNConfirmationView

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import scala.concurrent.Future

class BTNConfirmationControllerSpec extends SpecBase {

  "BTNConfirmationController" when {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), subscriptionLocalData = Some(someSubscriptionLocalData)).build()

      val currentDate: String = LocalDate.now.format(DateTimeFormatter.ofPattern("d MMMM y"))
      val date:        String = someSubscriptionLocalData.subAccountingPeriod.startDate.format(DateTimeFormatter.ofPattern("d MMMM y"))

      running(application) {
        val request = FakeRequest(GET, controllers.btn.routes.BTNConfirmationController.onPageLoad.url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[BTNConfirmationView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual view(Some("OrgName"), currentDate, date, isAgent = false)(
          request,
          applicationConfig,
          messages(application)
        ).toString
      }
    }

    "onDownloadRfmAnswers" should {
      "return OK and the correct view" in {
        val mockFopService = mock[FopService]
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), subscriptionLocalData = Some(someSubscriptionLocalData))
          .overrides(
            bind[FopService].toInstance(mockFopService)
          )
          .build()
        when(mockFopService.render(any())).thenReturn(Future.successful("hello".getBytes))

        running(application) {
          val request = FakeRequest(GET, controllers.btn.routes.BTNConfirmationController.onDownloadConfirmation.url)
          val result  = route(application, request).value
          status(result) mustEqual OK
          contentAsString(result) mustEqual "hello"
          header(HeaderNames.CONTENT_DISPOSITION, result).value mustEqual "attachment; filename=below-threshold-notification-confirmation.pdf"
        }
      }
    }
  }
}
