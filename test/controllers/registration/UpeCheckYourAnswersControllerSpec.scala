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
import models.{CaptureTelephoneDetails, ContactUPEByTelephone, UpeRegisteredAddress}
import pages._
import play.api.test.FakeRequest
import play.api.test.Helpers._
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
    .set(UpeContactEmailPage, "example@gmail.com")
    .success
    .value
    .set(UpeContactNamePage, "Paddington ltd")
    .success
    .value
    .set(ContactUPEByTelephonePage, ContactUPEByTelephone.Yes)
    .success
    .value
    .set(CaptureTelephoneDetailsPage, CaptureTelephoneDetails("123444"))
    .success
    .value
    .set(UpeNameRegistrationPage, "Paddington")
    .success
    .value
    .set(UpeRegisteredAddressPage, addressExample)
    .success
    .value

  val noTelephone = user
    .set(UpeContactEmailPage, "example@gmail.com")
    .success
    .value
    .set(UpeContactNamePage, "Paddington ltd")
    .success
    .value
    .set(ContactUPEByTelephonePage, ContactUPEByTelephone.Yes)
    .success
    .value
    .set(UpeNameRegistrationPage, "Paddington")
    .success
    .value
    .set(UpeRegisteredAddressPage, addressExample)
    .success
    .value

  val phonenumberProvided = Seq(
    UpeNameRegistrationSummary.row(user.set(UpeNameRegistrationPage, "Paddington").success.value),
    UpeRegisteredAddressSummary.row(user.set(UpeRegisteredAddressPage, addressExample).success.value),
    UpeContactNameSummary.row(user.set(UpeContactNamePage, "Paddington ltd").success.value),
    UpeContactEmailSummary.row(user.set(UpeContactEmailPage, "example@gmail.com").success.value),
    UpeTelephonePreferenceSummary.row(user.set(ContactUPEByTelephonePage, ContactUPEByTelephone.Yes).success.value),
    UPEContactTelephoneSummary.row(user.set(CaptureTelephoneDetailsPage, CaptureTelephoneDetails("123444")).success.value)
  ).flatten

  val noPhonenumber = Seq(
    UpeNameRegistrationSummary.row(user.set(UpeNameRegistrationPage, "Paddington").success.value),
    UpeRegisteredAddressSummary.row(user.set(UpeRegisteredAddressPage, addressExample).success.value),
    UpeContactNameSummary.row(user.set(UpeContactNamePage, "Paddington ltd").success.value),
    UpeContactEmailSummary.row(user.set(UpeContactEmailPage, "example@gmail.com").success.value),
    UpeTelephonePreferenceSummary.row(user.set(ContactUPEByTelephonePage, ContactUPEByTelephone.Yes).success.value)
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
      val application = applicationBuilder(userAnswers = Some(noTelephone)).build()
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
