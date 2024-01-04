/*
 * Copyright 2024 HM Revenue & Customs
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

import config.FrontendAppConfig
import connectors.{IncorporatedEntityIdentificationFrontendConnector, PartnershipIdentificationFrontendConnector, UserAnswersConnectors}
import controllers.actions._
import forms.EntityTypeFormProvider
import models.grs.EntityType
import models.{Mode, UserType}
import pages.{upeEntityTypePage, upeRegisteredInUKPage}
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.libs.json.Format.GenericFormat
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import uk.gov.hmrc.http.HttpVerbs.GET
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.EntityTypeView
import uk.gov.hmrc.http.HeaderCarrier

import utils.Pillar2SessionKeys

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class EntityTypeController @Inject() (
  val userAnswersConnectors:                         UserAnswersConnectors,
  incorporatedEntityIdentificationFrontendConnector: IncorporatedEntityIdentificationFrontendConnector,
  partnershipIdentificationFrontendConnector:        PartnershipIdentificationFrontendConnector,
  identify:                                          IdentifierAction,
  getData:                                           DataRetrievalAction,
  requireData:                                       DataRequiredAction,
  formProvider:                                      EntityTypeFormProvider,
  val controllerComponents:                          MessagesControllerComponents,
  view:                                              EntityTypeView
)(implicit ec:                                       ExecutionContext, appConfig: FrontendAppConfig, hc: HeaderCarrier)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    if (request.userAnswers.get(upeRegisteredInUKPage).contains(true)) {
      val preparedForm = request.userAnswers.get(upeEntityTypePage) match {
        case None        => form
        case Some(value) => form.fill(value)
      }
      Ok(view(preparedForm, mode))
    } else {
      Redirect(controllers.routes.BookmarkPreventionController.onPageLoad)
    }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode))),
        value =>
          value match {
            case EntityType.UkLimitedCompany =>
              logger.info(s"[Session ID: ${Pillar2SessionKeys.sessionId(hc)}] Calling UK Limited Company in EntityTypeController class")
              for {
                updatedAnswers   <- Future.fromTry(request.userAnswers.set(upeEntityTypePage, value))
                _                <- userAnswersConnectors.save(updatedAnswers.id, Json.toJson(updatedAnswers.data))
                createJourneyRes <- incorporatedEntityIdentificationFrontendConnector.createLimitedCompanyJourney(UserType.Upe, mode)
              } yield Redirect(Call(GET, createJourneyRes.journeyStartUrl))

            case EntityType.LimitedLiabilityPartnership =>
              logger.info(s"[Session ID: ${Pillar2SessionKeys.sessionId(hc)}] Calling Limited Liability Partnership in EntityTypeController class")
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(upeEntityTypePage, value))
                _              <- userAnswersConnectors.save(updatedAnswers.id, Json.toJson(updatedAnswers.data))
                createJourneyRes <-
                  partnershipIdentificationFrontendConnector.createPartnershipJourney(UserType.Upe, EntityType.LimitedLiabilityPartnership, mode)
              } yield Redirect(Call(GET, createJourneyRes.journeyStartUrl))
          }
      )
  }

}
