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

package controllers.actions

import base.SpecBase
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import scala.concurrent.Future

class AmendMultipleAccountingPeriodScreensActionSpec extends SpecBase {

  class TestController(
    identify:                    IdentifierAction,
    checkAmendMultipleAPScreens: AmendMultipleAccountingPeriodScreensAction,
    val controllerComponents:    MessagesControllerComponents
  ) extends FrontendBaseController {

    def testAction(): Action[AnyContent] =
      (identify andThen checkAmendMultipleAPScreens).async { _ =>
        Future.successful(Ok("Success"))
      }
  }

  "AmendMultipleAccountingPeriodScreensAction" when {
    "amendMultipleAccountingPeriods is true" should {
      "allow the request to continue" in {
        val application = applicationBuilder()
          .configure("features.amendMultipleAccountingPeriods" -> true)
          .build()

        running(application) {
          val identify                    = application.injector.instanceOf[IdentifierAction]
          val getData                     = application.injector.instanceOf[SubscriptionDataRetrievalAction]
          val requireData                 = application.injector.instanceOf[SubscriptionDataRequiredAction]
          val checkAmendMultipleAPScreens = application.injector.instanceOf[AmendMultipleAccountingPeriodScreensAction]
          val controllerComponents        = application.injector.instanceOf[MessagesControllerComponents]

          val controller = new TestController(identify, checkAmendMultipleAPScreens, controllerComponents)

          val request = FakeRequest("GET", "/test")
          val result  = controller.testAction()(request)

          status(result) mustEqual OK
          contentAsString(result) mustEqual "Success"
        }
      }
    }

    "amendMultipleAccountingPeriods is false" should {
      "redirect to homepage" in {
        val application = applicationBuilder()
          .configure("features.amendMultipleAccountingPeriods" -> false)
          .build()

        running(application) {
          val identify                    = application.injector.instanceOf[IdentifierAction]
          val checkAmendMultipleAPScreens = application.injector.instanceOf[AmendMultipleAccountingPeriodScreensAction]
          val controllerComponents        = application.injector.instanceOf[MessagesControllerComponents]

          val controller = new TestController(identify, checkAmendMultipleAPScreens, controllerComponents)

          val request = FakeRequest("GET", "/test")
          val result  = controller.testAction()(request)

          status(result) mustEqual SEE_OTHER
          redirectLocation(result) mustEqual Some(controllers.routes.HomepageController.onPageLoad().url)
        }
      }
    }
  }
}
