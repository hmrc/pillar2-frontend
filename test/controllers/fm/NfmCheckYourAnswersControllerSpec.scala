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
import org.mockito.Mockito.when
import pages._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import viewmodels.checkAnswers._
import viewmodels.govuk.SummaryListFluency
import views.html.fmview.FilingMemberCheckYourAnswersView

class NfmCheckYourAnswersControllerSpec extends SpecBase with SummaryListFluency {
  def controller(): NfmCheckYourAnswersController =
    new NfmCheckYourAnswersController(
      preAuthenticatedActionBuilders,
      preDataRetrievalActionImpl,
      preDataRequiredActionImpl,
      stubMessagesControllerComponents(),
      viewpageNotAvailable,
      viewCheckYourAnswersFilingMember,
      mockCountryOptions
    )
  val completeUserAnswer = emptyUserAnswers
    .set(
      NominatedFilingMemberPage,
      nfmCheckAnswerData()
    )
    .success
    .value

  val noTelephoneUserAnswers = emptyUserAnswers
    .set(
      NominatedFilingMemberPage,
      nfmCheckAnswerDataWithoutPhone()
    )
    .success
    .value

  val phonenumberProvided = Seq(
    NfmNameRegistrationSummary.row(completeUserAnswer),
    NfmRegisteredAddressSummary.row(completeUserAnswer, mockCountryOptions),
    NfmContactNameSummary.row(completeUserAnswer),
    NfmEmailAddressSummary.row(completeUserAnswer),
    NfmTelephonePreferenceSummary.row(completeUserAnswer),
    NfmContactTelephoneSummary.row(completeUserAnswer)
  ).flatten

  val noPhonenumber = Seq(
    NfmNameRegistrationSummary.row(noTelephoneUserAnswers),
    NfmRegisteredAddressSummary.row(noTelephoneUserAnswers, mockCountryOptions),
    NfmContactNameSummary.row(noTelephoneUserAnswers),
    NfmEmailAddressSummary.row(noTelephoneUserAnswers),
    NfmTelephonePreferenceSummary.row(noTelephoneUserAnswers)
  ).flatten

  "Nfm no ID Check Your Answers Controller" must {

    "must return Not Found and the correct view with empty user answers" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        when(mockCountryOptions.getCountryNameFromCode("GB")).thenReturn("United Kingdom")
        val request = FakeRequest(GET, controllers.fm.routes.NfmCheckYourAnswersController.onPageLoad.url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[FilingMemberCheckYourAnswersView]
        val list = SummaryListViewModel(Seq.empty)

        status(result) mustEqual NOT_FOUND
      }
    }
    "must return OK and the correct view if an answer is provided to every question " in {
      val application = applicationBuilder(userAnswers = Some(completeUserAnswer)).build()
      running(application) {
        when(mockCountryOptions.getCountryNameFromCode("GB")).thenReturn("United Kingdom")
        val request = FakeRequest(GET, controllers.fm.routes.NfmCheckYourAnswersController.onPageLoad.url)
        val result  = route(application, request).value
        val view    = application.injector.instanceOf[FilingMemberCheckYourAnswersView]
        val list    = SummaryListViewModel(phonenumberProvided)
        status(result) mustEqual OK
        contentAsString(result) must include(
          "Group details"
        )
      }

    }
    "must return OK and the correct view if an answer is provided to every question except telephone preference " in {
      val application = applicationBuilder(userAnswers = Some(noTelephoneUserAnswers)).build()
      running(application) {
        val request = FakeRequest(GET, controllers.fm.routes.NfmCheckYourAnswersController.onPageLoad.url)
        val result  = route(application, request).value
        val view    = application.injector.instanceOf[FilingMemberCheckYourAnswersView]
        val list    = SummaryListViewModel(noPhonenumber)
        status(result) mustEqual OK
        contentAsString(result) must include(
          "Check your answers"
        )

      }

    }

  }
}
