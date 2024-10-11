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
import models.{Mode, NormalMode, UserType}
import pages.{UpeEntityTypePage, UpeRegisteredInUKPage}
import play.api.Logging
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.libs.json.Format.GenericFormat
import play.api.libs.json.Json
import play.api.mvc._
import uk.gov.hmrc.http.HttpVerbs.GET
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.EntityTypeView

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
)(implicit ec:                                       ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  val form: Form[EntityType] = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) async { implicit request =>
    request.userAnswers
      .get(UpeRegisteredInUKPage)
      .map { ukBased =>
        request.userAnswers
          .get(UpeEntityTypePage)
          .map { entityType =>
            if (!ukBased & entityType.toString == EntityType.Other.toString) {
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(UpeRegisteredInUKPage, true))
                _              <- userAnswersConnectors.save(updatedAnswers.id, Json.toJson(updatedAnswers.data))
              } yield Ok(view(form.fill(entityType), mode))
            } else {
              Future.successful(Ok(view(form.fill(entityType), mode)))
            }
          }
          .getOrElse(Future.successful(Ok(view(form, mode))))
      }
      .getOrElse(Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode))),
        value =>
          value match {
            case EntityType.UkLimitedCompany =>
              logger.info("UPE- Initialising GRS journey with entity type chosen as UK Limited Company")
              for {
                updatedAnswers   <- Future.fromTry(request.userAnswers.set(UpeRegisteredInUKPage, true))
                updatedAnswers1  <- Future.fromTry(updatedAnswers.set(UpeEntityTypePage, value))
                _                <- userAnswersConnectors.save(updatedAnswers1.id, Json.toJson(updatedAnswers1.data))
                createJourneyRes <- incorporatedEntityIdentificationFrontendConnector.createLimitedCompanyJourney(UserType.Upe, mode)
              } yield Redirect(Call(GET, createJourneyRes.journeyStartUrl))

            case EntityType.LimitedLiabilityPartnership =>
              logger.info("UPE- Initialising GRS journey with entity type chosen as Limited Liability Partnership")
              for {
                updatedAnswers  <- Future.fromTry(request.userAnswers.set(UpeRegisteredInUKPage, true))
                updatedAnswers1 <- Future.fromTry(updatedAnswers.set(UpeEntityTypePage, value))
                _               <- userAnswersConnectors.save(updatedAnswers1.id, Json.toJson(updatedAnswers1.data))
                createJourneyRes <-
                  partnershipIdentificationFrontendConnector.createPartnershipJourney(UserType.Upe, EntityType.LimitedLiabilityPartnership, mode)
              } yield Redirect(Call(GET, createJourneyRes.journeyStartUrl))

            case EntityType.Other =>
              logger.info("UPE- Redirecting to the no ID journey as entity type not listed chosen")
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(UpeRegisteredInUKPage, false))
                updatedAnswer1 <- Future.fromTry(updatedAnswers.set(UpeEntityTypePage, value))
                _              <- userAnswersConnectors.save(updatedAnswer1.id, Json.toJson(updatedAnswer1.data))
              } yield Redirect(controllers.registration.routes.UpeNameRegistrationController.onPageLoad(NormalMode))
          }
      )
  }

}
