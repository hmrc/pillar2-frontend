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

import com.google.inject.Inject
import config.FrontendAppConfig
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import models.requests.DataRequest
import pages.{RegistrationPage, upePhonePreferencePage}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.countryOptions.CountryOptions
import viewmodels.checkAnswers._
import viewmodels.govuk.summarylist._
import views.html.errors.ErrorTemplate
import views.html.registrationview.UpeCheckYourAnswersView

class UpeCheckYourAnswersController @Inject() (
  identify:                 IdentifierAction,
  getData:                  DataRetrievalAction,
  requireData:              DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  view:                     UpeCheckYourAnswersView,
  countryOptions:           CountryOptions
)(implicit appConfig:       FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    //here you need to implement a logic to make sure every question is answered

    val list = SummaryListViewModel(
      rows = Seq(
        UpeNameRegistrationSummary.row(request.userAnswers),
        UpeRegisteredAddressSummary.row(request.userAnswers, countryOptions),
        UpeContactNameSummary.row(request.userAnswers),
        UpeContactEmailSummary.row(request.userAnswers),
        UpeTelephonePreferenceSummary.row(request.userAnswers),
        request.userAnswers.get(upePhonePreferencePage) match {
          case Some(true) => UPEContactTelephoneSummary.row(request.userAnswers)
          case _          => None
        }
      ).flatten
    )
    Ok(view(list))
  }

  /*
      This CYA:
      a) Has the UPE gone through no ID or ID
        i) ID: Get their org type, GRS data and then send
        ii) NOID: get name, address, contact Name, telephone, telephone preference

        a) Has the UPE gone through no ID or ID
    i) ID: Get their org type, GRS data and then send
    ii) NOID: get name, address, contact Name, telephone, telephone preference

    sub:
    a) Has the UPE gone through no ID or ID
    i) ID: Get their org type, GRS data and then send
    ii) NOID: get name, address, contact Name, telephone, telephone preference
    a) Has the UPE gone through no ID or ID
    i) ID: Get their org type, GRS data and then send
    ii) NOID: get name, address, contact Name, telephone, telephone preference
   */

}
