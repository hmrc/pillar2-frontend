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

package controllers

import com.google.inject.Inject
import config.FrontendAppConfig
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import models.MneOrDomestic
import models.requests.DataRequest
import pages.{RegistrationPage, SubscriptionPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.countryOptions.CountryOptions
import viewmodels.checkAnswers._
import viewmodels.govuk.summarylist._
import views.html.CheckYourAnswersView
import views.html.errors.ErrorTemplate

class CheckYourAnswersController @Inject() (
  override val messagesApi: MessagesApi,
  identify:                 IdentifierAction,
  getData:                  DataRetrievalAction,
  requireData:              DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  view:                     CheckYourAnswersView,
  page_not_available:       ErrorTemplate,
  countryOptions:           CountryOptions
)(implicit appConfig:       FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    val notAvailable = page_not_available("page_not_available.title", "page_not_available.heading", "page_not_available.message")

    println("*******************************************" + isUpeWithIdDefined(request))
    val listUpe = (isUpeWithIdDefined(request), isIncorporatedEntityDefined(request)) match {
      case (true, true) =>
        SummaryListViewModel(
          rows = Seq(
            GrsReturnIncorporatedEntityCompanyNameUprSummary.row(request.userAnswers),
            GrsReturnIncorporatedEntityCompanyRegUprSummary.row(request.userAnswers),
            GrsReturnIncorporatedEntityCompanyUtrUprSummary.row(request.userAnswers)
          ).flatten
        )
      case (true, false) =>
        SummaryListViewModel(
          rows = Seq(
            GrsReturnPartnershipEntityCompanyNameUprSummary.row(request.userAnswers),
            GrsReturnPartnershipEntityCompanyRegUprSummary.row(request.userAnswers),
            GrsReturnPartnershipEntityCompanyUtrUprSummary.row(request.userAnswers)
          ).flatten
        )
      case (_, _) =>
        SummaryListViewModel(rows =
          Seq(
            UpeNameRegistrationSummary.row(request.userAnswers),
            UpeRegisteredAddressSummary.row(request.userAnswers, countryOptions),
            UpeContactNameSummary.row(request.userAnswers),
            UpeContactEmailSummary.row(request.userAnswers),
            UpeTelephonePreferenceSummary.row(request.userAnswers)
          ).flatten
        )
    }

    val listNfm = SummaryListViewModel(
      rows = Seq(
        ContactNameComplianceSummary.row(request.userAnswers),
        ContactEmailAddressSummary.row(request.userAnswers),
        ContactByTelephoneSummary.row(request.userAnswers),
        ContactCaptureTelephoneDetailsSummary.row(request.userAnswers)
      ).flatten
    )

    val listPrimary = SummaryListViewModel(
      rows = Seq(
        ContactNameComplianceSummary.row(request.userAnswers),
        ContactEmailAddressSummary.row(request.userAnswers),
        ContactByTelephoneSummary.row(request.userAnswers),
        ContactCaptureTelephoneDetailsSummary.row(request.userAnswers)
      ).flatten
    )

    val listSecondary = isSecondContactDefined(request) match {
      case true =>
        SummaryListViewModel(
          rows = Seq(
            AddSecondaryContactSummary.row(request.userAnswers),
            SecondaryContactNameSummary.row(request.userAnswers),
            SecondaryContactEmailSummary.row(request.userAnswers),
            SecondaryTelephonePreferenceSummary.row(request.userAnswers),
            SecondaryTelephoneSummary.row(request.userAnswers)
          ).flatten
        )
      case false =>
        SummaryListViewModel(rows = Seq())
    }

    val address = SummaryListViewModel(
      rows = Seq(
        ContactCorrespondenceAddressSummary.row(request.userAnswers, countryOptions)
      ).flatten
    )

    if (!isPreviousPagesDefined(request))
      Ok(view(listUpe, listNfm, listPrimary, listSecondary, address))
    else
      NotFound(notAvailable)
  }

  private def isPreviousPagesDefined(request: DataRequest[AnyContent]): Boolean =
    request.userAnswers
      .get(SubscriptionPage)
      .fold(false) { data =>
        ((data.domesticOrMne == MneOrDomestic.Uk) || (data.domesticOrMne == MneOrDomestic.UkAndOther)) &&
        data.accountingPeriod.fold(false)(data => data.startDate.toString.nonEmpty && data.endDate.toString.nonEmpty)

      }

  private def isUpeWithIdDefined(request: DataRequest[AnyContent]): Boolean =
    request.userAnswers
      .get(RegistrationPage)
      .fold(false) { data =>
        data.withIdRegData.isDefined && data.isUPERegisteredInUK.toString == "yes"
      }

  private def isIncorporatedEntityDefined(request: DataRequest[AnyContent]): Boolean =
    request.userAnswers
      .get(RegistrationPage)
      .fold(false) { data =>
        data.withIdRegData.fold(false)(data => data.incorporatedEntityRegistrationData.isDefined)
      }
  private def isSecondContactDefined(request: DataRequest[AnyContent]): Boolean =
    request.userAnswers
      .get(SubscriptionPage)
      .fold(false)((data => data.addSecondaryContact.fold(false)(contact => contact)))
}
