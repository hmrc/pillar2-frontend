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

package controllers.fm

import base.SpecBase
import connectors.UserAnswersConnectors
import forms.NfmContactNameFormProvider
import models.NormalMode
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.fmview.NFMContactNameView

import scala.concurrent.Future

class NfmContactNameControllerSpec extends SpecBase {
  val formProvider = new NfmContactNameFormProvider()
  def controller(): NfmContactNameController =
    new NfmContactNameController(
      mockUserAnswersConnectors,
      preAuthenticatedActionBuilders,
      preDataRetrievalActionImpl,
      preDataRequiredActionImpl,
      formProvider,
      stubMessagesControllerComponents(),
      viewNFMContactName
    )

  "NFMContactName Controller" when {
    implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(controllers.fm.routes.NfmContactNameController.onPageLoad(NormalMode))

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
      running(application) {
        val request = FakeRequest(GET, controllers.fm.routes.NfmContactNameController.onPageLoad(NormalMode).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[NFMContactNameView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider(), NormalMode)(
          request,
          appConfig(application),
          messages(application)
        ).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {
      val application = applicationBuilder(userAnswers = Some(userAnswersWithNoIdForNfm))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .build()
      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))
        val request =
          FakeRequest(POST, controllers.fm.routes.NfmContactNameController.onSubmit(NormalMode).url)
            .withFormUrlEncodedBody(("fmContactName", "Ashley Smith"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.fm.routes.NfmEmailAddressController.onPageLoad(NormalMode).url
      }
    }

    "must return a Bad Request when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, controllers.fm.routes.NfmContactNameController.onPageLoad(NormalMode).url)
            .withFormUrlEncodedBody(("value", ""))
        val result = route(application, request).value
        status(result) mustEqual BAD_REQUEST
      }
    }
  }
}
