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

package controllers.registration

import config.FrontendAppConfig
import connectors.{IncorporatedEntityIdentificationFrontendConnector, PartnershipIdentificationFrontendConnector, UserAnswersConnectors}
import controllers.actions._
import controllers.routes
import forms.EntityTypeFormProvider
import models.grs.EntityType
import models.registration.Registration
import models.requests.DataRequest
import models.{Mode, UserType}
import pages.RegistrationPage
import play.api.i18n.I18nSupport
import play.api.libs.json.Format.GenericFormat
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import uk.gov.hmrc.http.HttpVerbs.GET
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.RowStatus
import views.html.EntityTypeView
import views.html.errors.ErrorTemplate

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
  page_not_available:                                ErrorTemplate,
  view:                                              EntityTypeView
)(implicit ec:                                       ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    val notAvailable = page_not_available("page_not_available.title", "page_not_available.heading", "page_not_available.message")
    isPreviousPageDefined(request) match {
      case true =>
        request.userAnswers
          .get(RegistrationPage)
          .fold(NotFound(notAvailable)) { reg =>
            reg.orgType.fold(Ok(view(form, mode)))(data => Ok(view(form.fill(data), mode)))
          }
      case false =>
        NotFound(notAvailable)
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
              request.userAnswers
                .get(RegistrationPage)
                .map { reg =>
                  val regData = request.userAnswers.get(RegistrationPage).getOrElse(throw new Exception("no registration data found"))
                  for {
                    updatedAnswers <-
                      Future.fromTry(
                        request.userAnswers.set(
                          RegistrationPage,
                          regData.copy(
                            isUPERegisteredInUK = true,
                            orgType = Some(value),
                            isRegistrationStatus = RowStatus.InProgress,
                            withoutIdRegData = None,
                            withIdRegData = None
                          )
                        )
                      )
                    _                <- userAnswersConnectors.save(updatedAnswers.id, Json.toJson(updatedAnswers.data))
                    createJourneyRes <- incorporatedEntityIdentificationFrontendConnector.createLimitedCompanyJourney(UserType.Upe, mode)
                  } yield Redirect(Call(GET, createJourneyRes.journeyStartUrl))
                }
                .getOrElse(Future.successful(Redirect(routes.JourneyRecoveryController.onPageLoad())))

            case EntityType.LimitedLiabilityPartnership =>
              request.userAnswers
                .get(RegistrationPage)
                .map { reg =>
                  val regData = request.userAnswers.get(RegistrationPage).getOrElse(throw new Exception("no registration data found"))
                  for {
                    updatedAnswers <-
                      Future.fromTry(
                        request.userAnswers.set(
                          RegistrationPage,
                          regData.copy(
                            isUPERegisteredInUK = true,
                            orgType = Some(value),
                            isRegistrationStatus = RowStatus.InProgress,
                            withoutIdRegData = None,
                            withIdRegData = None
                          )
                        )
                      )
                    _ <- userAnswersConnectors.save(updatedAnswers.id, Json.toJson(updatedAnswers.data))
                    createJourneyRes <-
                      partnershipIdentificationFrontendConnector.createPartnershipJourney(UserType.Upe, EntityType.LimitedLiabilityPartnership, mode)
                  } yield Redirect(Call(GET, createJourneyRes.journeyStartUrl))
                }
                .getOrElse(Future.successful(Redirect(routes.JourneyRecoveryController.onPageLoad())))
          }
      )
  }

  private def isPreviousPageDefined(request: DataRequest[AnyContent]): Boolean =
    request.userAnswers
      .get(RegistrationPage)
      .fold(false)(data => data.isUPERegisteredInUK)
}
