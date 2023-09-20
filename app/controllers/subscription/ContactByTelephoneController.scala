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

import config.FrontendAppConfig
import connectors.UserAnswersConnectors
import controllers.actions._
import forms.{ContactByTelephoneFormProvider, ContactNfmByTelephoneFormProvider}
import models.{Mode, NormalMode}
import models.fm.ContactNFMByTelephone
import models.requests.DataRequest
import models.subscription.ContactByTelephone
import pages.{NominatedFilingMemberPage, SubscriptionPage}
import play.api.i18n.I18nSupport
import play.api.libs.json.Format.GenericFormat
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.RowStatus
import views.html.errors.ErrorTemplate
import views.html.fmview.ContactNfmByTelephoneView
import views.html.subscriptionview.ContactByTelephoneView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ContactByTelephoneController @Inject() (
  val userAnswersConnectors: UserAnswersConnectors,
  identify:                  IdentifierAction,
  getData:                   DataRetrievalAction,
  requireData:               DataRequiredAction,
  formProvider:              ContactByTelephoneFormProvider,
  val controllerComponents:  MessagesControllerComponents,
  page_not_available:        ErrorTemplate,
  view:                      ContactByTelephoneView
)(implicit ec:               ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    val userName     = getUserName(request)
    val form         = formProvider(userName)
    val notAvailable = page_not_available("page_not_available.title", "page_not_available.heading", "page_not_available.message")
    isPreviousPageDefined(request) match {
      case true =>
        request.userAnswers
          .get(SubscriptionPage)
          .fold(NotFound(notAvailable)) { reg =>
            reg.contactByTelephone.fold(Ok(view(form, mode, userName)))(data => Ok(view(form.fill(data), mode, userName)))
          }

      case false => NotFound(notAvailable)
    }

  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    val userName = getUserName(request)
    val form     = formProvider(userName)
    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, userName))),
        value =>
          value match {
            case ContactByTelephone.Yes =>
              val subRegData =
                request.userAnswers.get(SubscriptionPage).getOrElse(throw new Exception("Is NFM registered in UK not been selected"))
              for {
                updatedAnswers <-
                  Future.fromTry(
                    request.userAnswers
                      set (SubscriptionPage, subRegData.copy(
                        contactByTelephone = Some(value),
                        telephoneNumber = subRegData.telephoneNumber,
                        primaryContactEmail = subRegData.primaryContactEmail,
                        domesticOrMne = subRegData.domesticOrMne,
                        accountingPeriod = subRegData.accountingPeriod,
                        useContactPrimary = subRegData.useContactPrimary,
                        primaryContactTelephone = subRegData.primaryContactTelephone,
                        primaryContactName = subRegData.primaryContactName,
                        groupDetailStatus = subRegData.groupDetailStatus,
                        contactDetailsStatus = subRegData.contactDetailsStatus
                      ))
                  )
                _ <- userAnswersConnectors.save(updatedAnswers.id, Json.toJson(updatedAnswers.data))
              } yield Redirect(controllers.subscription.routes.ContactCaptureTelephoneDetailsController.onPageLoad(NormalMode))
            case ContactByTelephone.No =>
              val subRegData =
                request.userAnswers.get(SubscriptionPage).getOrElse(throw new Exception("Is NFM registered in UK not been selected"))
              for {
                updatedAnswers <-
                  Future.fromTry(
                    request.userAnswers
                      set (SubscriptionPage,
                      subRegData.copy(
                        contactByTelephone = Some(value),
                        telephoneNumber = None,
                        primaryContactEmail = subRegData.primaryContactEmail,
                        domesticOrMne = subRegData.domesticOrMne,
                        accountingPeriod = subRegData.accountingPeriod,
                        useContactPrimary = subRegData.useContactPrimary,
                        primaryContactTelephone = subRegData.primaryContactTelephone,
                        primaryContactName = subRegData.primaryContactName,
                        groupDetailStatus = subRegData.groupDetailStatus,
                        contactDetailsStatus = subRegData.contactDetailsStatus
                      ))
                  )
                _ <- userAnswersConnectors.save(updatedAnswers.id, Json.toJson(updatedAnswers.data))
              } yield Redirect(controllers.routes.UnderConstructionController.onPageLoad)
          }
      )
  }

  private def getUserName(request: DataRequest[AnyContent]): String = {
    val registration = request.userAnswers.get(SubscriptionPage)
    registration.fold("")(regData => regData.primaryContactName.fold("")(primaryContactName => primaryContactName))
  }

  private def isPreviousPageDefined(request: DataRequest[AnyContent]): Boolean =
    request.userAnswers
      .get(SubscriptionPage)
      .fold(false)(data => data.primaryContactEmail.isDefined)

}
