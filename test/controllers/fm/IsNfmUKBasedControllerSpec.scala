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
import forms.IsNFMUKBasedFormProvider
import models.fm.FilingMember
import models.grs.EntityType
import models.{NfmRegisteredInUkConfirmation, NfmRegistrationConfirmation, NormalMode, UserAnswers}
import pages.NominatedFilingMemberPage
import play.api.test.FakeRequest
import play.api.test.Helpers._
import org.mockito.Mockito.when
import org.mockito.ArgumentMatchers.any
import utils.RowStatus
import views.html.fmview.IsNFMUKBasedView
import play.api.inject.bind
import play.api.libs.json.Json

import scala.concurrent.Future

class IsNfmUKBasedControllerSpec extends SpecBase {

  val formProvider = new IsNFMUKBasedFormProvider()

  "IsNFMUKBased Controller" when {

    "must return OK and the correct view for a GET" in {
      val userAnswersWithNominatedFilingMember =
        emptyUserAnswers.set(NominatedFilingMemberPage, validWithIdFmData()).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswersWithNominatedFilingMember)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.fm.routes.IsNfmUKBasedController.onPageLoad(NormalMode).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[IsNFMUKBasedView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider(), NormalMode)(request, appConfig(application), messages(application)).toString
      }
    }

    "must return NOT_FOUND if previous page not defined" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.fm.routes.IsNfmUKBasedController.onPageLoad(NormalMode).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[IsNFMUKBasedView]

        status(result) mustEqual NOT_FOUND
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers =
        UserAnswers(userAnswersId)
          .set(
            NominatedFilingMemberPage,
            FilingMember(NfmRegistrationConfirmation.Yes, Some(NfmRegisteredInUkConfirmation.Yes), isNFMnStatus = RowStatus.InProgress)
          )
          .success
          .value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.fm.routes.IsNfmUKBasedController.onPageLoad(NormalMode).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[IsNFMUKBasedView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider().fill(NfmRegisteredInUkConfirmation.Yes), NormalMode)(
          request,
          appConfig(application),
          messages(application)
        ).toString
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, controllers.fm.routes.IsNfmUKBasedController.onPageLoad(NormalMode).url)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = formProvider().bind(Map("value" -> ""))

        val view = application.injector.instanceOf[IsNFMUKBasedView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(request, appConfig(application), messages(application)).toString
      }
    }

    "must redirect to NfmEntityType to choose companyType when Yes is submited" in {

      val application = applicationBuilder(userAnswers = Some(userAnswersWithIdForLimitedCompForFm))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .build()

      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))

        val request = FakeRequest(POST, controllers.fm.routes.IsNfmUKBasedController.onSubmit(NormalMode).url)
          .withFormUrlEncodedBody(("value", NfmRegisteredInUkConfirmation.Yes.toString))

        val boundForm = formProvider().bind(Map("value" -> NfmRegisteredInUkConfirmation.Yes.toString))

        val view = application.injector.instanceOf[IsNFMUKBasedView]

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.fm.routes.NfmEntityTypeController.onPageLoad(NormalMode).url
      }

    }

    "must redirect to UnderConstruction When No is submited" in {

      val application = applicationBuilder(userAnswers = Some(userAnswersWithIdForLimitedCompForFm))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .build()

      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))

        val request = FakeRequest(POST, controllers.fm.routes.IsNfmUKBasedController.onSubmit(NormalMode).url)
          .withFormUrlEncodedBody(("value", NfmRegisteredInUkConfirmation.No.toString))

        val boundForm = formProvider().bind(Map("value" -> NfmRegisteredInUkConfirmation.No.toString))

        val view = application.injector.instanceOf[IsNFMUKBasedView]

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.UnderConstructionController.onPageLoad.url
      }

    }

  }
}
