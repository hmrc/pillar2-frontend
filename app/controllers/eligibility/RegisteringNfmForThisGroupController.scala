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

package controllers.eligibility

import cache.SessionData
import config.FrontendAppConfig
import forms.RegisteringNfmForThisGroupFormProvider
import models.RegisteringNfmForThisGroup
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.Pillar2SessionKeys
import views.html.RegisteringNfmForThisGroupView

import javax.inject.Inject
import scala.concurrent.Future

class RegisteringNfmForThisGroupController @Inject() (
  formProvider:             RegisteringNfmForThisGroupFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view:                     RegisteringNfmForThisGroupView,
  sessionData:              SessionData
)(implicit appConfig:       FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport {

  val form = formProvider()

  def onPageLoad: Action[AnyContent] = Action { implicit request =>
    val preparedForm = request.session.data.get(Pillar2SessionKeys.registeringNfmForThisGroup) match {
      case None        => form
      case Some(value) => form.fill(value)
    }

    Ok(view(preparedForm))
  }

  def onSubmit: Action[AnyContent] = Action.async { implicit request =>
    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors))),
        value =>
          value match {
            case RegisteringNfmForThisGroup.Yes.toString =>
              Future.successful(
                Redirect(controllers.eligibility.routes.BusinessActivityUKController.onPageLoad)
                  .withSession((sessionData.registeringNfmForThisGroup(value)))
              )
            case RegisteringNfmForThisGroup.No.toString =>
              Future.successful(
                Redirect(controllers.eligibility.routes.KbMnIneligibleController.onPageLoad)
                  .withSession((sessionData.registeringNfmForThisGroup(value)))
              )
          }
      )
  }
}
