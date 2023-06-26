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
import connectors.{IncorporatedEntityIdentificationFrontendConnector, UserAnswersConnectors}
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import controllers.routes
import forms.UPERegisteredInUKConfirmationFormProvider

import models.{Mode, UPERegisteredInUKConfirmation}
import pages.UPERegisteredInUKConfirmationPage

import models.grs.{OrgType, ServiceName}
import models.registration.{IncorporatedEntityCreateRegistrationRequest, RegistrationWithoutIdRequest}
import models.{Mode, UPERegisteredInUKConfirmation, registration}
import navigation.Navigator
import pages.{RegistrationWithoutIdRequestPage, UPERegisteredInUKConfirmationPage}

import play.api.i18n.I18nSupport
import play.api.libs.json.Format.GenericFormat
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import uk.gov.hmrc.http.HttpVerbs.GET
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.registrationview.UPERegisteredInUKConfirmationView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class UPERegisteredInUKConfirmationController @Inject() (
  val userAnswersConnectors:                         UserAnswersConnectors,
  incorporatedEntityIdentificationFrontendConnector: IncorporatedEntityIdentificationFrontendConnector,
  identify:                                          IdentifierAction,
  getData:                                           DataRetrievalAction,
  requireData:                                       DataRequiredAction,
  formProvider:                                      UPERegisteredInUKConfirmationFormProvider,
  val controllerComponents:                          MessagesControllerComponents,
  view:                                              UPERegisteredInUKConfirmationView
)(implicit ec:                                       ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    val preparedForm = request.userAnswers.get(UPERegisteredInUKConfirmationPage) match {
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
            case UPERegisteredInUKConfirmation.Yes =>
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(UPERegisteredInUKConfirmationPage, value))
                _              <- userAnswersConnectors.save(updatedAnswers.id, Json.toJson(updatedAnswers.data))
                updatedRequest <-
                  Future.fromTry(
                    updatedAnswers.set(RegistrationWithoutIdRequestPage, RegistrationWithoutIdRequest(Some(OrgType.UkLimitedCompany)))
                  )
                _ <- userAnswersConnectors.save(updatedRequest.id, Json.toJson(updatedRequest.data))
                createJourneyRes <- incorporatedEntityIdentificationFrontendConnector
                                      .createLimitedCompanyJourney(mode)
              } yield Redirect(Call(GET, createJourneyRes.journeyStartUrl))
            case UPERegisteredInUKConfirmation.No =>
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(UPERegisteredInUKConfirmationPage, value))
                _              <- userAnswersConnectors.save(updatedAnswers.id, Json.toJson(updatedAnswers.data))
              } yield Redirect(controllers.registration.routes.UpeNameRegistrationController.onPageLoad)
          }
      )
  }

  private def createRegistrationRequest(mode: Mode): IncorporatedEntityCreateRegistrationRequest =
    registration.IncorporatedEntityCreateRegistrationRequest(
      continueUrl = s"${appConfig.grsContinueUrl}/${mode.toString.toLowerCase}",
      businessVerificationCheck = appConfig.incorporatedEntityBvEnabled,
      optServiceName = Some(ServiceName().en.optServiceName),
      deskProServiceId = appConfig.appName,
      signOutUrl = appConfig.signOutUrl,
      accessibilityUrl = appConfig.accessibilityStatementPath,
      labels = ServiceName()
    )
}
