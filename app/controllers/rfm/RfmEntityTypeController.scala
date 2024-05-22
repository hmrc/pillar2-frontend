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
import forms.RfmEntityTypeFormProvider
import models.grs.EntityType
import models.{Mode, UserType}
import pages.{RfmEntityTypePage, RfmUkBasedPage}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.libs.json.Format.GenericFormat
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import uk.gov.hmrc.http.HttpVerbs.GET
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.rfm.RfmEntityTypeView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RfmEntityTypeController @Inject() (
  val userAnswersConnectors:                         UserAnswersConnectors,
  rfmIdentify:                                       RfmIdentifierAction,
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

  val form: Form[EntityType] = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (rfmIdentify andThen getData andThen requireData) async { implicit request =>
    val rfmAccessEnabled = appConfig.rfmAccessEnabled
    if (rfmAccessEnabled) {

      request.userAnswers
        .get(RfmUkBasedPage)
        .map { ukBased =>
          request.userAnswers
            .get(RfmEntityTypePage)
            .map { entityType =>
              if (!ukBased & entityType == EntityType.Other) {
                for {
                  updatedAnswers <- Future.fromTry(request.userAnswers.set(RfmUkBasedPage, true))
                  _              <- userAnswersConnectors.save(updatedAnswers.id, Json.toJson(updatedAnswers.data))
                } yield Ok(view(form.fill(entityType), mode))
              } else {
                Future.successful(Ok(view(form.fill(entityType), mode)))
              }
            }
            .getOrElse(Future.successful(Ok(view(form, mode))))
        }
        .getOrElse(Future.successful(Redirect(controllers.rfm.routes.RfmJourneyRecoveryController.onPageLoad)))
    } else {
      Future.successful(Redirect(controllers.routes.UnderConstructionController.onPageLoad))
    }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (rfmIdentify andThen getData andThen requireData).async { implicit request =>
    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode))),
        value =>
          value match {
            case EntityType.UkLimitedCompany =>
              for {
                updatedAnswers <-
                  Future.fromTry(request.userAnswers.set(RfmEntityTypePage, value))
                _                <- userAnswersConnectors.save(updatedAnswers.id, Json.toJson(updatedAnswers.data))
                createJourneyRes <- incorporatedEntityIdentificationFrontendConnector.createLimitedCompanyJourney(UserType.Rfm, mode)
              } yield Redirect(Call(GET, createJourneyRes.journeyStartUrl))
            case EntityType.LimitedLiabilityPartnership =>
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(RfmEntityTypePage, value))
                _              <- userAnswersConnectors.save(updatedAnswers.id, Json.toJson(updatedAnswers.data))
                createJourneyRes <-
                  partnershipIdentificationFrontendConnector.createPartnershipJourney(UserType.Rfm, EntityType.LimitedLiabilityPartnership, mode)
              } yield Redirect(Call(GET, createJourneyRes.journeyStartUrl))

            case EntityType.Other =>
              for {
                updatedAnswers  <- Future.fromTry(request.userAnswers.set(RfmUkBasedPage, false))
                updatedAnswers1 <- Future.fromTry(updatedAnswers.set(RfmEntityTypePage, value))
                _               <- userAnswersConnectors.save(updatedAnswers1.id, updatedAnswers1.data)
              } yield Redirect(controllers.rfm.routes.RfmNameRegistrationController.onPageLoad(mode))
          }
      )
  }
}
