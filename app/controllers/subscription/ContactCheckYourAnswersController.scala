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

package controllers.subscription

import com.google.inject.Inject
import config.FrontendAppConfig
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import models.MneOrDomestic
import models.requests.DataRequest
import pages.SubscriptionPage
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers._
import viewmodels.govuk.summarylist._
import views.html.errors.ErrorTemplate
import views.html.subscriptionview.{ContactCheckYourAnswersView, SubCheckYourAnswersView}

class ContactCheckYourAnswersController @Inject() (
  identify:                 IdentifierAction,
  getData:                  DataRetrievalAction,
  requireData:              DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  page_not_available:       ErrorTemplate,
  view:                     ContactCheckYourAnswersView
)(implicit appConfig:       FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    val notAvailable = page_not_available("page_not_available.title", "page_not_available.heading", "page_not_available.message")

    val list = SummaryListViewModel(
      rows = Seq(
        ContactNameComplianceSummary.row(request.userAnswers),
        ContactEmailAddressSummary.row(request.userAnswers),
        ContactByTelephoneSummary.row(request.userAnswers),
        ContactCaptureTelephoneDetailsSummary.row(request.userAnswers)
      ).flatten
    )

    val listWithSecondaryContact = SummaryListViewModel(
      rows = Seq(
        ContactNameComplianceSummary.row(request.userAnswers),
        ContactEmailAddressSummary.row(request.userAnswers),
        ContactByTelephoneSummary.row(request.userAnswers),
        ContactCaptureTelephoneDetailsSummary.row(request.userAnswers),
        AddSecondaryContactSummary.row(request.userAnswers),
        SecondaryContactNameSummary.row(request.userAnswers),
        SecondaryContactEmailSummary.row(request.userAnswers),
        SecondaryTelephonePreferenceSummary.row(request.userAnswers),
        SecondaryTelephoneSummary.row(request.userAnswers)
      ).flatten
    )

    if (isPreviousPagesDefined(request))
      Ok(view(listWithSecondaryContact))
    else
      NotFound(notAvailable)
  }
  private def isPreviousPagesDefined(request: DataRequest[AnyContent]): Boolean =
    request.userAnswers
      .get(SubscriptionPage)
      .fold(false) { data =>
        (data.domesticOrMne == MneOrDomestic.Uk) || (data.domesticOrMne == MneOrDomestic.UkAndOther) &&
        data.accountingPeriod.fold(false)(data => data.startDate.toString.nonEmpty && data.endDate.toString.nonEmpty)
      }

  private def isSecondaryContactDefined(request: DataRequest[AnyContent]): Boolean =
    request.userAnswers
      .get(SubscriptionPage)
      .fold(false) { data =>
        data.addSecondaryContact.isDefined
      }
}
