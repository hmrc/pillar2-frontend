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
import models.registration.{Registration, WithoutIdRegData}
import models.{CaptureTelephoneDetails, ContactUPEByTelephone, UPERegisteredInUKConfirmation, UpeRegisteredAddress}
import pages._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import utils.RowStatus
import viewmodels.checkAnswers._
import viewmodels.govuk.SummaryListFluency
import views.html.registrationview.UpeCheckYourAnswersView

class UpeCheckYourAnswersControllerSpec extends SpecBase with SummaryListFluency {

  def controller(): UpeCheckYourAnswersController =
    new UpeCheckYourAnswersController(
      preAuthenticatedActionBuilders,
      preDataRetrievalActionImpl,
      preDataRequiredActionImpl,
      stubMessagesControllerComponents(),
      viewCheckYourAnswersUPE
    )
  val user           = emptyUserAnswers
  val addressExample = UpeRegisteredAddress("1", Some("2"), "3", Some("4"), Some("5"), "GB")

  val completeUserAnswer = user
    .set(
      RegistrationPage,
      new Registration(
        isUPERegisteredInUK = UPERegisteredInUKConfirmation.No,
        isRegistrationStatus = RowStatus.InProgress,
        withoutIdRegData = Some(
          WithoutIdRegData(
            upeNameRegistration = "Paddington",
            upeContactName = Some("Paddington ltd"),
            contactUpeByTelephone = Some(ContactUPEByTelephone.Yes),
            telephoneNumber = Some("123444"),
            emailAddress = Some("example@gmail.com"),
            upeRegisteredAddress = Some(
              UpeRegisteredAddress(
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
      RegistrationPage,
      new Registration(
        isUPERegisteredInUK = UPERegisteredInUKConfirmation.No,
        isRegistrationStatus = RowStatus.InProgress,
        withoutIdRegData = Some(
          WithoutIdRegData(
            upeNameRegistration = "Paddington",
            upeContactName = Some("Paddington ltd"),
            contactUpeByTelephone = Some(ContactUPEByTelephone.No),
            emailAddress = Some("example@gmail.com"),
            upeRegisteredAddress = Some(
              UpeRegisteredAddress(
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

  val phonenumberProvided1 = Seq()

  val phonenumberProvided = Seq(
    UpeNameRegistrationSummary.row(completeUserAnswer),
    UpeRegisteredAddressSummary.row(completeUserAnswer),
    UpeContactNameSummary.row(completeUserAnswer),
    UpeContactEmailSummary.row(completeUserAnswer),
    UpeTelephonePreferenceSummary.row(completeUserAnswer),
    UPEContactTelephoneSummary.row(completeUserAnswer)
  ).flatten

  val noPhonenumber = Seq(
    UpeNameRegistrationSummary.row(noTelephoneUserAnswers),
    UpeRegisteredAddressSummary.row(noTelephoneUserAnswers),
    UpeContactNameSummary.row(noTelephoneUserAnswers),
    UpeContactEmailSummary.row(noTelephoneUserAnswers),
    UpeTelephonePreferenceSummary.row(noTelephoneUserAnswers)
  ).flatten

  "UPE no ID Check Your Answers Controller" must {

    "must return OK and the correct view with empty user answers" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.registration.routes.UpeCheckYourAnswersController.onPageLoad.url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[UpeCheckYourAnswersView]
        val list = SummaryListViewModel(Seq.empty)

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(list)(request, appConfig(application), messages(application)).toString
      }
    }
    "must return OK and the correct view if an answer is provided to every question " in {
      val application = applicationBuilder(userAnswers = Some(completeUserAnswer)).build()
      running(application) {
        val request = FakeRequest(GET, controllers.registration.routes.UpeCheckYourAnswersController.onPageLoad.url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[UpeCheckYourAnswersView]
        val list = SummaryListViewModel(phonenumberProvided)

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(list)(request, appConfig(application), messages(application)).toString
      }

    }
    "must return OK and the correct view if an answer is provided to every question except telephone preference " in {
      val application = applicationBuilder(userAnswers = Some(noTelephoneUserAnswers)).build()
      running(application) {
        val request = FakeRequest(GET, controllers.registration.routes.UpeCheckYourAnswersController.onPageLoad.url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[UpeCheckYourAnswersView]
        val list = SummaryListViewModel(noPhonenumber)

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(list)(request, appConfig(application), messages(application)).toString
      }

    }

  }
}
