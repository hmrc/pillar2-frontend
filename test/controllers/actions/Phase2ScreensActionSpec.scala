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

package controllers.actions

import base.SpecBase
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import scala.concurrent.Future

class Phase2ScreensActionSpec extends SpecBase {

  class TestController(
    identify:                 IdentifierAction,
    getData:                  DataRetrievalAction,
    requireData:              DataRequiredAction,
    checkPhase2Screens:       Phase2ScreensAction,
    val controllerComponents: MessagesControllerComponents
  ) extends FrontendBaseController {

    def testAction(): Action[AnyContent] =
      (identify andThen checkPhase2Screens andThen getData andThen requireData).async { _ =>
        Future.successful(Ok("Success"))
      }
  }

  "Phase2ScreensAction" when {
    "phase2ScreensEnabled is true" should {
      "allow the request to continue" in {
        val application = applicationBuilder()
          .configure("features.phase2ScreensEnabled" -> true)
          .build()

        running(application) {
          val identify             = application.injector.instanceOf[IdentifierAction]
          val getData              = application.injector.instanceOf[DataRetrievalAction]
          val requireData          = application.injector.instanceOf[DataRequiredAction]
          val checkPhase2Screens   = application.injector.instanceOf[Phase2ScreensAction]
          val controllerComponents = application.injector.instanceOf[MessagesControllerComponents]

          val controller = new TestController(identify, getData, requireData, checkPhase2Screens, controllerComponents)

          val request = FakeRequest("GET", "/test")
          val result  = controller.testAction()(request)

          status(result) mustEqual OK
          contentAsString(result) mustEqual "Success"
        }
      }
    }

    "phase2ScreensEnabled is false" should {
      "redirect to dashboard" in {
        val application = applicationBuilder()
          .configure("features.phase2ScreensEnabled" -> false)
          .build()

        running(application) {
          val identify             = application.injector.instanceOf[IdentifierAction]
          val getData              = application.injector.instanceOf[DataRetrievalAction]
          val requireData          = application.injector.instanceOf[DataRequiredAction]
          val checkPhase2Screens   = application.injector.instanceOf[Phase2ScreensAction]
          val controllerComponents = application.injector.instanceOf[MessagesControllerComponents]

          val controller = new TestController(identify, getData, requireData, checkPhase2Screens, controllerComponents)

          val request = FakeRequest("GET", "/test")
          val result  = controller.testAction()(request)

          status(result) mustEqual SEE_OTHER
          redirectLocation(result) mustEqual Some(controllers.routes.DashboardController.onPageLoad.url)
        }
      }
    }
  }
}
