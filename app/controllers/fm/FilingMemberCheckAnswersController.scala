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

import com.google.inject.Inject
import config.FrontendAppConfig
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import models.fm.ContactNFMByTelephone
import models.requests.DataRequest
import pages.{NominatedFilingMemberPage, RegistrationPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.{ContactNfmByTelephoneSummary, NfmCaptureTelephoneDetailsSummary, NfmContactNameSummary, NfmEmailAddressSummary, NfmNameRegistrationControllerSummary, NfmRegisteredAddressSummary, UpeTelephonePreferenceSummary}
import viewmodels.govuk.summarylist._
import views.html.CheckYourAnswersView
import views.html.errors.ErrorTemplate
import views.html.fmview.FilingMemberCheckYourAnswersView

class FilingMemberCheckAnswersController @Inject() (
  override val messagesApi: MessagesApi,
  identify:                 IdentifierAction,
  getData:                  DataRetrievalAction,
  requireData:              DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  page_not_available:       ErrorTemplate,
  view:                     FilingMemberCheckYourAnswersView
)(implicit appConfig:       FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    val notAvailable = page_not_available("page_not_available.title", "page_not_available.heading", "page_not_available.message")
    val telephonePreference = request.userAnswers.get(NominatedFilingMemberPage) match {
      case Some(value) =>
        value.withoutIdRegData.fold(false)(data => data.contactNfmByTelephone.fold(false)(tel => (tel == ContactNFMByTelephone.Yes)))
      case _ => false
    }
    val list = SummaryListViewModel(
      rows = Seq(
        NfmNameRegistrationControllerSummary.row(request.userAnswers),
        NfmRegisteredAddressSummary.row(request.userAnswers),
        NfmContactNameSummary.row(request.userAnswers),
        NfmEmailAddressSummary.row(request.userAnswers),
        ContactNfmByTelephoneSummary.row(request.userAnswers),
        telephonePreference match {
          case true => NfmCaptureTelephoneDetailsSummary.row(request.userAnswers)
          case _    => None
        }
      ).flatten
    )
    if (isPreviousPagesDefined(request))
      Ok(view(list))
    else
      NotFound(notAvailable)

  }

  private def isPreviousPagesDefined(request: DataRequest[AnyContent]): Boolean =
    request.userAnswers
      .get(NominatedFilingMemberPage)
      .fold(false)(data =>
        data.withoutIdRegData.fold(false)(withoutId =>
          withoutId.registeredFmAddress.isDefined &&
            withoutId.fmContactName.isDefined &&
            withoutId.fmEmailAddress.isDefined &&
            withoutId.contactNfmByTelephone.fold(false)(contactTel =>
              (contactTel == ContactNFMByTelephone.Yes && withoutId.telephoneNumber.isDefined) ||
                (contactTel == ContactNFMByTelephone.No)
            )
        )
      )
}
