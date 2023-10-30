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
import forms.MneOrDomesticFormProvider
import models.Mode
import models.subscription.Subscription
import pages.{NominatedFilingMemberPage, SubscriptionPage}
import play.api.i18n.I18nSupport
import play.api.libs.json.Format.GenericFormat
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.RowStatus
import views.html.errors.ErrorTemplate
import views.html.subscriptionview.MneOrDomesticView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class MneOrDomesticController @Inject() (
  val userAnswersConnectors: UserAnswersConnectors,
  identify:                  IdentifierAction,
  getData:                   DataRetrievalAction,
  requireData:               DataRequiredAction,
  formProvider:              MneOrDomesticFormProvider,
  page_not_available:        ErrorTemplate,
  val controllerComponents:  MessagesControllerComponents,
  view:                      MneOrDomesticView
)(implicit ec:               ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    val notAvailable = page_not_available("page_not_available.title", "page_not_available.heading", "page_not_available.message")
    val nfmData      = request.userAnswers.get(NominatedFilingMemberPage).fold(false)(data => data.isNFMnStatus == RowStatus.Completed)
    nfmData match {
      case true =>
        val preparedForm = request.userAnswers.get(SubscriptionPage) match {
          case None        => form
          case Some(value) => form.fill(value.domesticOrMne)
        }
        Ok(view(preparedForm, mode))
      case false => NotFound(notAvailable)

    }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode))),
        value => {
          val subscriptionData =
            request.userAnswers
              .get(SubscriptionPage)
              .getOrElse(
                Subscription(domesticOrMne = value, groupDetailStatus = RowStatus.InProgress, contactDetailsStatus = RowStatus.NotStarted)
              )
          for {
            updatedAnswers <-
              Future
                .fromTry(
                  request.userAnswers.set(
                    SubscriptionPage,
                    subscriptionData.copy(
                      domesticOrMne = value,
                      accountingPeriod = subscriptionData.accountingPeriod,
                      useContactPrimary = subscriptionData.useContactPrimary,
                      primaryContactName = subscriptionData.primaryContactName,
                      contactByTelephone = subscriptionData.contactByTelephone,
                      primaryContactTelephone = subscriptionData.primaryContactTelephone,
                      primaryContactEmail = subscriptionData.primaryContactEmail,
                      groupDetailStatus = subscriptionData.groupDetailStatus,
                      contactDetailsStatus = subscriptionData.contactDetailsStatus,
                      addSecondaryContact = subscriptionData.addSecondaryContact,
                      secondaryContactName = subscriptionData.secondaryContactName,
                      secondaryContactEmail = subscriptionData.secondaryContactEmail,
                      secondaryTelephonePreference = subscriptionData.secondaryTelephonePreference,
                      secondaryContactTelephone = subscriptionData.secondaryContactTelephone,
                      correspondenceAddress = subscriptionData.correspondenceAddress
                    )
                  )
                )
            _ <- userAnswersConnectors.save(updatedAnswers.id, Json.toJson(updatedAnswers.data))
          } yield Redirect(controllers.subscription.routes.GroupAccountingPeriodController.onPageLoad(mode))
        }
      )
  }

}
