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

package controllers.rfm

import config.FrontendAppConfig
import connectors.{IncorporatedEntityIdentificationFrontendConnector, PartnershipIdentificationFrontendConnector, UserAnswersConnectors}
import controllers.actions._
import controllers.routes
import forms.RfmEntityTypeFormProvider
import models.{Mode, UserType}
import models.grs.{EntityType, RfmEntityType}
import pages.{RfmEntityTypePage, fmEntityTypePage}
import play.api.i18n.I18nSupport
import play.api.libs.json.Format.GenericFormat
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import uk.gov.hmrc.http.HttpVerbs.GET
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.RfmEntityTypeView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RfmEntityTypeController @Inject() (
  val userAnswersConnectors:                         UserAnswersConnectors,
  identify:                                          IdentifierAction,
  incorporatedEntityIdentificationFrontendConnector: IncorporatedEntityIdentificationFrontendConnector,
  partnershipIdentificationFrontendConnector:        PartnershipIdentificationFrontendConnector,
  getData:                                           DataRetrievalAction,
  requireData:                                       DataRequiredAction,
  formProvider:                                      RfmEntityTypeFormProvider,
  val controllerComponents:                          MessagesControllerComponents,
  view:                                              RfmEntityTypeView
)(implicit ec:                                       ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    //TODO
    // if (request.userAnswers.get(fmRegisteredInUKPage).contains(true)) {
    val preparedForm = request.userAnswers.get(RfmEntityTypePage) match {
      case None        => form
      case Some(value) => form.fill(value)
    }

    Ok(view(preparedForm, mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode))),
        value =>
          value match {
            case RfmEntityType.UkLimitedCompany =>
              for {
                updatedAnswers <-
                  Future.fromTry(request.userAnswers.set(RfmEntityTypePage, value))
                _                <- userAnswersConnectors.save(updatedAnswers.id, Json.toJson(updatedAnswers.data))
                createJourneyRes <- incorporatedEntityIdentificationFrontendConnector.createLimitedCompanyJourney(UserType.Rfm, mode)
              } yield Redirect(Call(GET, createJourneyRes.journeyStartUrl))
            case RfmEntityType.LimitedLiabilityPartnership =>
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(RfmEntityTypePage, value))
                _              <- userAnswersConnectors.save(updatedAnswers.id, Json.toJson(updatedAnswers.data))
                createJourneyRes <-
                  partnershipIdentificationFrontendConnector.createPartnershipJourney(UserType.Rfm, EntityType.LimitedLiabilityPartnership, mode)
              } yield Redirect(Call(GET, createJourneyRes.journeyStartUrl))

            case RfmEntityType.EntityTypeNotListed =>
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(RfmEntityTypePage, value))
                _              <- userAnswersConnectors.save(updatedAnswers.id, Json.toJson(updatedAnswers.data))
                createJourneyRes <-
                  partnershipIdentificationFrontendConnector.createPartnershipJourney(UserType.Fm, EntityType.LimitedLiabilityPartnership, mode)
              } yield Redirect(Call(GET, createJourneyRes.journeyStartUrl))
          }

        /*          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(RfmEntityTypePage, value))
            _              <- userAnswersConnectors.save(updatedAnswers.id, Json.toJson(updatedAnswers.data))
          } yield Redirect(routes.UnderConstructionController.onPageLoad)*/
      )
  }
}
