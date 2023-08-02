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
import connectors.UserAnswersConnectors
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import forms.NfmRegisteredAddressFormProvider
import models.fm.{FilingMember, NfmRegisteredAddress, WithoutIdNfmData}
import models.requests.DataRequest
import models.{Mode, NfmRegisteredInUkConfirmation, NfmRegistrationConfirmation}
import navigation.Navigator
import pages.{NominatedFilingMemberPage, RegistrationPage}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.RowStatus
import utils.countryOptions.CountryOptions

import views.html.fmview.NfmRegisteredAddressView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class NfmRegisteredAddressController @Inject() (
  val userAnswersConnectors: UserAnswersConnectors,
  identify:                  IdentifierAction,
  getData:                   DataRetrievalAction,
  requireData:               DataRequiredAction,
  formProvider:              NfmRegisteredAddressFormProvider,
  CountryOptions:            CountryOptions,
  val controllerComponents:  MessagesControllerComponents,
  view:                      NfmRegisteredAddressView
)(implicit ec:               ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport {
  val form: Form[NfmRegisteredAddress] = formProvider()
  val countryList = CountryOptions.options.sortWith((s, t) => s.label(0).toLower < t.label(0).toLower)
  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    val userName = getUserName(request)
    val preparedForm = request.userAnswers.get(NominatedFilingMemberPage) match {
      case None        => form
      case Some(value) => value.withoutIdRegData.fold(form)(data => data.registeredFmAddress.fold(form)(address => form.fill(address)))
    }
    Ok(view(preparedForm, mode, userName, countryList))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    val userName = getUserName(request)

    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, userName, countryList))),
        value =>
          for {
            updatedAnswers <- Future.fromTry(
                                request.userAnswers.set(
                                  NominatedFilingMemberPage,
                                  FilingMember(
                                    NfmRegistrationConfirmation.Yes,
                                    isNfmRegisteredInUK = Some(NfmRegisteredInUkConfirmation.No),
                                    isNFMnStatus = RowStatus.InProgress,
                                    withoutIdRegData = Some(WithoutIdNfmData(registeredFmAddress = Some(value), registeredFmName = userName))
                                  )
                                )
                              )
            _ <- userAnswersConnectors.save(updatedAnswers.id, Json.toJson(updatedAnswers.data))
          } yield Redirect(controllers.routes.UnderConstructionController.onPageLoad)
      )
  }
  private def getUserName(request: DataRequest[AnyContent]): String = {
    val fmDetails = request.userAnswers.get(NominatedFilingMemberPage)
    fmDetails.fold("")(fmData => fmData.withoutIdRegData.fold("")(withoutId => withoutId.registeredFmName))
  }

}
