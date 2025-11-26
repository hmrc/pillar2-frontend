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

package controllers.eligibility

import base.SpecBase
import forms.GroupTerritoriesFormProvider
import helpers.ViewInstances
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages.UpeEqPage
import play.api.inject
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.GroupTerritoriesView

import scala.concurrent.Future

class GroupTerritoriesControllerSpec extends SpecBase with ViewInstances {

  val formProvider = new GroupTerritoriesFormProvider()

  "Group Territories Controller" when {

    "must return OK and the correct view for a GET when page previously not answered" in {
      val application = applicationBuilder(None).build()
      running(application) {
        val request =
          FakeRequest(GET, controllers.eligibility.routes.GroupTerritoriesController.onPageLoad.url)

        val result = route(application, request).value
        val view   = application.injector.instanceOf[GroupTerritoriesView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider())(
          request,
          applicationConfig,
          messages(application)
        ).toString
      }
    }

    "redirect to journey recovery if no session id is found" in {
      val controller: GroupTerritoriesController = new GroupTerritoriesController(
        formProvider,
        stubMessagesControllerComponents(),
        viewGroupTerritories,
        mockSessionRepository
      )
      val request = FakeRequest(GET, controllers.eligibility.routes.GroupTerritoriesController.onPageLoad.url)
      val result  = controller.onPageLoad()()(request)
      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustBe controllers.routes.JourneyRecoveryController.onPageLoad().url
    }

    "must return OK and the correct view for a GET when page previously answered" in {
      val application = applicationBuilder(None)
        .overrides(inject.bind[SessionRepository].toInstance(mockSessionRepository))
        .build()
      running(application) {
        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(emptyUserAnswers.setOrException(UpeEqPage, true))))
        val request =
          FakeRequest(GET, controllers.eligibility.routes.GroupTerritoriesController.onPageLoad.url)

        val result = route(application, request).value
        val view   = application.injector.instanceOf[GroupTerritoriesView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider().fill(true))(
          request,
          applicationConfig,
          messages(application)
        ).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {
      val application = applicationBuilder(None).build()
      running(application) {
        val request =
          FakeRequest(POST, controllers.eligibility.routes.GroupTerritoriesController.onSubmit.url).withFormUrlEncodedBody(
            "value" -> "true"
          )
        val result = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.eligibility.routes.BusinessActivityUKController.onPageLoad.url

      }
    }
    "must redirect to the next page when valid data is submitted with no selected" in {
      val application = applicationBuilder(None).build()
      running(application) {
        val request =
          FakeRequest(POST, controllers.eligibility.routes.GroupTerritoriesController.onSubmit.url).withFormUrlEncodedBody(
            "value" -> "false"
          )
        val result = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.eligibility.routes.RegisteringNfmForThisGroupController.onPageLoad.url

      }
    }
    "return  BAD_REQUEST if invalid data is submitted " in {
      val application = applicationBuilder(None).build()
      running(application) {
        val request =
          FakeRequest(POST, controllers.eligibility.routes.GroupTerritoriesController.onSubmit.url).withFormUrlEncodedBody(
            "value" -> ""
          )
        val result = route(application, request).value
        status(result) mustEqual BAD_REQUEST
      }
    }

    "redirect to journey recovery if no session id is found for POST" in {
      val controller: GroupTerritoriesController = new GroupTerritoriesController(
        formProvider,
        stubMessagesControllerComponents(),
        viewGroupTerritories,
        mockSessionRepository
      )
      val request = FakeRequest(GET, controllers.eligibility.routes.GroupTerritoriesController.onSubmit.url)
      val result  = controller.onSubmit()()(request)
      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustBe controllers.routes.JourneyRecoveryController.onPageLoad().url
    }
  }
}
