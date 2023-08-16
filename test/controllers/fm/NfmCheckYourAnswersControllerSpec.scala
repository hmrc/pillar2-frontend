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
import models.fm.{ContactNFMByTelephone, FilingMember, NfmRegisteredAddress, WithoutIdNfmData}
import models.registration.{Registration, WithoutIdRegData}
import models.{ContactUPEByTelephone, NfmRegisteredInUkConfirmation, NfmRegistrationConfirmation, UPERegisteredInUKConfirmation, UpeRegisteredAddress}
import pages._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.shaded.ahc.io.netty.util.concurrent.Future
import utils.RowStatus
import viewmodels.checkAnswers._
import viewmodels.govuk.SummaryListFluency
import views.html.fmview.NfmCheckYourAnswersView
import views.html.registrationview.UpeCheckYourAnswersView

import scala.concurrent.Await

class NfmCheckYourAnswersControllerSpec extends SpecBase with SummaryListFluency {

  def controller(): NfmCheckYourAnswersController =
    new NfmCheckYourAnswersController(
      preAuthenticatedActionBuilders,
      preDataRetrievalActionImpl,
      preDataRequiredActionImpl,
      stubMessagesControllerComponents(),
      viewpageNotAvailable,
      viewCheckYourAnswersNfm
    )
  val user           = emptyUserAnswers
  val addressExample = NfmRegisteredAddress("1", Some("2"), "3", Some("4"), Some("5"), "GB")

  val completeUserAnswer = user
    .set(
      NominatedFilingMemberPage,
      new FilingMember(
        nfmConfirmation = NfmRegistrationConfirmation.Yes,
        isNfmRegisteredInUK = Some(NfmRegisteredInUkConfirmation.No),
        isNFMnStatus = RowStatus.InProgress,
        withoutIdRegData = Some(
          WithoutIdNfmData(
            registeredFmName = "Nfm name ",
            fmContactName = Some("Ashley Smith"),
            fmEmailAddress = Some("test@test.com"),
            contactNfmByTelephone = Some(ContactNFMByTelephone.Yes),
            telephoneNumber = Some("122223444"),
            registeredFmAddress = Some(
              NfmRegisteredAddress(
                addressLine1 = "1",
                addressLine2 = Some("2"),
                addressLine3 = "3",
                addressLine4 = Some("4"),
                postalCode = Some("5"),
                countryCode = "GB"
              )
            )
          )
        )
      )
    )
    .success
    .value

  val noTelephoneUserAnswers = user
    .set(
      NominatedFilingMemberPage,
      new FilingMember(
        nfmConfirmation = NfmRegistrationConfirmation.Yes,
        isNfmRegisteredInUK = Some(NfmRegisteredInUkConfirmation.No),
        isNFMnStatus = RowStatus.InProgress,
        withoutIdRegData = Some(
          WithoutIdNfmData(
            registeredFmName = "Fm Name Ashley",
            fmContactName = Some("Ashley Smith"),
            fmEmailAddress = Some("test@test.com"),
            contactNfmByTelephone = Some(ContactNFMByTelephone.No),
            registeredFmAddress = Some(
              NfmRegisteredAddress(
                addressLine1 = "1",
                addressLine2 = Some("2"),
                addressLine3 = "3",
                addressLine4 = Some("4"),
                postalCode = Some("ne5 2dh"),
                countryCode = "GB"
              )
            )
          )
        )
      )
    )
    .success
    .value

  val phonenumberProvided = Seq(
    NfmNameRegistrationSummary.row(completeUserAnswer),
    NfmRegisteredAddressSummary.row(completeUserAnswer),
    NfmContactNameSummary.row(completeUserAnswer),
    NfmEmailAddressSummary.row(completeUserAnswer),
    NfmTelephonePreferenceSummary.row(completeUserAnswer),
    NfmContactTelephoneSummary.row(completeUserAnswer)
  ).flatten

  val noPhonenumber = Seq(
    NfmNameRegistrationSummary.row(noTelephoneUserAnswers),
    NfmRegisteredAddressSummary.row(noTelephoneUserAnswers),
    NfmContactNameSummary.row(noTelephoneUserAnswers),
    NfmEmailAddressSummary.row(noTelephoneUserAnswers),
    NfmTelephonePreferenceSummary.row(noTelephoneUserAnswers)
  ).flatten

  "Nfm no ID Check Your Answers Controller" must {

    "must return Not Found and the correct view with empty user answers" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.fm.routes.NfmCheckYourAnswersController.onPageLoad.url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[NfmCheckYourAnswersView]
        val list = SummaryListViewModel(Seq.empty)

        status(result) mustEqual NOT_FOUND
      }
    }
    "must return OK and the correct view if an answer is provided to every question " in {
      println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$44444" + completeUserAnswer)
      val application = applicationBuilder(userAnswers = Some(completeUserAnswer)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.fm.routes.NfmCheckYourAnswersController.onPageLoad.url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[NfmCheckYourAnswersView]
        val list = SummaryListViewModel(phonenumberProvided)

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(list)(request, appConfig(application), messages(application)).toString
      }

    }
//    "must return OK and the correct view if an answer is provided to every question except telephone preference " in {
//      val application = applicationBuilder(userAnswers = Some(noTelephoneUserAnswers)).build()
//      running(application) {
//        val request = FakeRequest(GET, controllers.fm.routes.NfmCheckYourAnswersController.onPageLoad.url)
//
//        val result = route(application, request).value
//
//        val view = application.injector.instanceOf[UpeCheckYourAnswersView]
//        val list = SummaryListViewModel(noPhonenumber)
//
//        status(result) mustEqual OK
//        contentAsString(result) mustEqual view(list)(request, appConfig(application), messages(application)).toString
//      }
//
//    }

  }
}
