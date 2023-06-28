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

package controllers.registration

import base.SpecBase
import forms.UpeContactEmailFormProvider
import models.{NormalMode, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.registrationview.UpeContactEmailView

import scala.concurrent.Future

class GrsReturnControllerSpec extends SpecBase {

  def controller(): GrsReturnController =
    new GrsReturnController(
      mockUserAnswersConnectors,
      preAuthenticatedActionBuilders,
      preDataRetrievalActionImpl,
      preDataRequiredActionImpl,
      stubMessagesControllerComponents(),
      mockIncorporatedEntityIdentificationFrontendConnector
    )

  "GrsReturn Controller" when {

    "must return 303 redirect to the next page" in {

      val request = FakeRequest()

      when(mockIncorporatedEntityIdentificationFrontendConnector.getJourneyData(any())(any()))
        .thenReturn(Future.successful(validRegisterWithIdResponse))
      when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))
      val result = controller().continue(NormalMode, "journeyId")(request)

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.routes.TaskListController.onPageLoad.url

    }
    // TODO some invalid route is not as the design has got the page. so goes underconstuction page

  }
}
