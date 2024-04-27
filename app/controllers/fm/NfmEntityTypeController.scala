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

package controllers.fm

import config.FrontendAppConfig
import connectors.{IncorporatedEntityIdentificationFrontendConnector, PartnershipIdentificationFrontendConnector, UserAnswersConnectors}
import controllers.actions._
import forms.NfmEntityTypeFormProvider
import models.grs.EntityType
import models.{Mode, NormalMode, UserType}
import pages.{FmEntityTypePage, FmRegisteredInUKPage}
import play.api.Logging
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.libs.json.Format.GenericFormat
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import uk.gov.hmrc.http.HttpVerbs.GET
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.NfmEntityTypeView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class NfmEntityTypeController @Inject() (
  val userAnswersConnectors:                         UserAnswersConnectors,
  incorporatedEntityIdentificationFrontendConnector: IncorporatedEntityIdentificationFrontendConnector,
  partnershipIdentificationFrontendConnector:        PartnershipIdentificationFrontendConnector,
  identify:                                          IdentifierAction,
  getData:                                           DataRetrievalAction,
  requireData:                                       DataRequiredAction,
  formProvider:                                      NfmEntityTypeFormProvider,
  val controllerComponents:                          MessagesControllerComponents,
  view:                                              NfmEntityTypeView
)(implicit ec:                                       ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  val form: Form[EntityType] = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) async { implicit request =>
    request.userAnswers
      .get(FmRegisteredInUKPage)
      .map { ukBased =>
        request.userAnswers
          .get(FmEntityTypePage)
          .map { entityType =>
            if (!ukBased & entityType == EntityType.Other) { // TODO Check logic - would have to be registered in the UK to get to this page
              for {
                updatedAnswers <- Future.fromTry(
                                    request.userAnswers.set(FmRegisteredInUKPage, true)
                                  ) // TODO - Check logic - should we be setting this to true on page load ?
                _ <- userAnswersConnectors.save(updatedAnswers.id, Json.toJson(updatedAnswers.data))
              } yield Ok(view(form.fill(entityType), mode))
            } else {
              Future.successful(Ok(view(form.fill(entityType), mode)))
            }
          }
          .getOrElse(Future.successful(Ok(view(form, mode))))
      }
      .getOrElse(Future.successful(Redirect(controllers.subscription.routes.InprogressTaskListController.onPageLoad)))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode))),
        value =>
          value match {
            case EntityType.UkLimitedCompany =>
              logger.info("Filing Member- Initialising GRS journey with entity type chosen as UK Limited Company")
              for {
                updatedAnswers   <- Future.fromTry(request.userAnswers.set(FmRegisteredInUKPage, true))
                updatedAnswers1  <- Future.fromTry(updatedAnswers.set(FmEntityTypePage, value))
                _                <- userAnswersConnectors.save(updatedAnswers1.id, Json.toJson(updatedAnswers1.data))
                createJourneyRes <- incorporatedEntityIdentificationFrontendConnector.createLimitedCompanyJourney(UserType.Fm, mode)
              } yield Redirect(Call(GET, createJourneyRes.journeyStartUrl))
            case EntityType.LimitedLiabilityPartnership =>
              logger.info("Filing Member- Initialising GRS journey with entity type chosen as Limited Liability Partnership")
              for {
                updatedAnswers  <- Future.fromTry(request.userAnswers.set(FmRegisteredInUKPage, true))
                updatedAnswers1 <- Future.fromTry(updatedAnswers.set(FmEntityTypePage, value))
                _               <- userAnswersConnectors.save(updatedAnswers1.id, Json.toJson(updatedAnswers1.data))
                createJourneyRes <-
                  partnershipIdentificationFrontendConnector.createPartnershipJourney(UserType.Fm, EntityType.LimitedLiabilityPartnership, mode)
              } yield Redirect(Call(GET, createJourneyRes.journeyStartUrl))
            case EntityType.Other =>
              logger.info("Filing Member- Redirecting to the no ID journey as entity type not listed chosen")
              for {
                updatedAnswers  <- Future.fromTry(request.userAnswers.set(FmRegisteredInUKPage, false))
                updatedAnswers1 <- Future.fromTry(updatedAnswers.set(FmEntityTypePage, value))
                _               <- userAnswersConnectors.save(updatedAnswers1.id, updatedAnswers1.data)
              } yield Redirect(controllers.fm.routes.NfmNameRegistrationController.onPageLoad(NormalMode))
          }
      )
  }
}
