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

package stubsonly.controllers.stubs

import config.FrontendAppConfig
import controllers.actions.{DataRetrievalAction, IdentifierAction}
import models.grs.EntityType
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import stubsonly.data.GrsStubData
import stubsonly.forms.GrsStubFormProvider
import stubsonly.models.GrsStubFormData
import stubsonly.utils.Base64Utils
import stubsonly.views.html.StubGrsRegistrationDataView
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import javax.inject.{Inject, Singleton}

@Singleton
class StubGrsJourneyDataController @Inject() (
  val controllerComponents: MessagesControllerComponents,
  identity:                 IdentifierAction,
  getData:                  DataRetrievalAction,
  grsStubFormProvider:      GrsStubFormProvider,
  view:                     StubGrsRegistrationDataView
)(implicit appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with GrsStubData
    with I18nSupport {

  val form: Form[GrsStubFormData] = grsStubFormProvider()

  private val registrationSuccessBvDisabledF: EntityType => String =
    constructGrsStubFormData(_, None, registered, identifiersMatch = true)
  private val registrationSuccessBvEnabledF: EntityType => String =
    constructGrsStubFormData(_, bvPassed, registered, identifiersMatch = true)
  private val registrationFailedPartyTypeMismatchF: EntityType => String =
    constructGrsStubFormData(_, None, registrationFailedPartyTypeMismatch, identifiersMatch = true)
  private val registrationFailedGenericF: EntityType => String =
    constructGrsStubFormData(_, None, registrationFailedGeneric, identifiersMatch = true)
  private val registrationNotCalledIdentifierMismatchF: EntityType => String =
    constructGrsStubFormData(_, None, registrationNotCalled, identifiersMatch = false)
  private val registrationNotCalledBvFailedF: EntityType => String =
    constructGrsStubFormData(_, bvFailed, registrationNotCalled, identifiersMatch = true)

  def onPageLoad(continueUrl: String, orgType: String): Action[AnyContent] = Action { implicit request =>
    val e: EntityType = EntityType.enumerable.withName(orgType).get

    Ok(
      view(
        form,
        registrationSuccessBvDisabledF(e),
        registrationSuccessBvEnabledF(e),
        registrationFailedPartyTypeMismatchF(e),
        registrationFailedGenericF(e),
        registrationNotCalledIdentifierMismatchF(e),
        registrationNotCalledBvFailedF(e),
        continueUrl,
        orgType
      )
    )
  }

  def onSubmit(continueUrl: String, entityType: String): Action[AnyContent] = (identity andThen getData) { implicit request =>
    val e: EntityType = EntityType.enumerable.withName(entityType).get

    form
      .bindFromRequest()
      .fold(
        formWithErrors =>
          BadRequest(
            view(
              formWithErrors,
              registrationSuccessBvDisabledF(e),
              registrationSuccessBvEnabledF(e),
              registrationFailedPartyTypeMismatchF(e),
              registrationFailedGenericF(e),
              registrationNotCalledIdentifierMismatchF(e),
              registrationNotCalledBvFailedF(e),
              continueUrl,
              entityType
            )
          ),
        grsStubFormData =>
          Redirect(
            s"/report-pillar2-top-up-taxes/grs-return/" +
              s"$continueUrl?journeyId=${Base64Utils
                  .base64UrlEncode(grsStubFormData.grsJourneyDataJson)}"
          )
      )
  }

}
