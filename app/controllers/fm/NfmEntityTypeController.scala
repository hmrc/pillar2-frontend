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

import config.FrontendAppConfig
import connectors.{IncorporatedEntityIdentificationFrontendConnector, PartnershipIdentificationFrontendConnector, UserAnswersConnectors}
import controllers.actions._
import uk.gov.hmrc.http.HttpVerbs.GET
import forms.NfmEntityTypeFormProvider
import models.grs.EntityType
import models.Mode
import pages.NominatedFilingMemberPage
import play.api.i18n.I18nSupport
import play.api.libs.json.Format.GenericFormat
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
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
    with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    val preparedForm = request.userAnswers.get(NominatedFilingMemberPage) match {
      case None        => form
      case Some(value) => value.orgType.fold(form)(data => form.fill(data))
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
            case EntityType.UkLimitedCompany =>
              val regData =
                request.userAnswers.get(NominatedFilingMemberPage).getOrElse(throw new Exception("Is Filing Member in UK not been selected"))

              for {
                updatedAnswers <-
                  Future.fromTry(
                    request.userAnswers
                      .set(NominatedFilingMemberPage, regData.copy(orgType = Some(value), withIdRegData = None, withoutIdRegData = None))
                  )
                _ <- userAnswersConnectors.save(updatedAnswers.id, Json.toJson(updatedAnswers.data))

                createJourneyRes <- incorporatedEntityIdentificationFrontendConnector
                                      .createLimitedCompanyJourney(mode)
              } yield Redirect(Call(GET, createJourneyRes.journeyStartUrl))

            case EntityType.LimitedLiabilityPartnership =>
              val regData =
                request.userAnswers.get(NominatedFilingMemberPage).getOrElse(throw new Exception("Is Filing Member in UK not been selected"))
              for {
                updatedAnswers <-
                  Future.fromTry(
                    request.userAnswers
                      .set(NominatedFilingMemberPage, regData.copy(orgType = Some(value), withIdRegData = None, withoutIdRegData = None))
                  )
                _ <- userAnswersConnectors.save(updatedAnswers.id, Json.toJson(updatedAnswers.data))

                createJourneyRes <- partnershipIdentificationFrontendConnector
                                      .createPartnershipJourney(EntityType.LimitedLiabilityPartnership, mode)
              } yield Redirect(Call(GET, createJourneyRes.journeyStartUrl))
          }
      )
  }
}
