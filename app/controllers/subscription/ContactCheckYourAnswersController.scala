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

package controllers.subscription

import com.google.inject.Inject
import config.FrontendAppConfig
import connectors.UserAnswersConnectors
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import models.requests.DataRequest
import models.subscription.Subscription
import pages.{SubscriptionPage, subAddSecondaryContactPage, subPrimaryContactNamePage, subPrimaryEmailPage, subPrimaryPhonePreferencePage}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.countryOptions.CountryOptions
import viewmodels.checkAnswers._
import viewmodels.govuk.summarylist._
import views.html.errors.ErrorTemplate
import views.html.subscriptionview.ContactCheckYourAnswersView

import scala.concurrent.{ExecutionContext, Future}
import play.api.libs.json.Json

class ContactCheckYourAnswersController @Inject() (
  val userAnswersConnectors: UserAnswersConnectors,
  identify:                  IdentifierAction,
  getData:                   DataRetrievalAction,
  requireData:               DataRequiredAction,
  val controllerComponents:  MessagesControllerComponents,
  view:                      ContactCheckYourAnswersView,
  countryOptions:            CountryOptions
)(implicit ec:               ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
      request.userAnswers.get(subAddSecondaryContactPage).map{ provided =>
        if(provided)
        (for {
          primaryName <- request.userAnswers.get(subPrimaryContactNamePage)
          primaryEmail <- request.userAnswers.get(subPrimaryEmailPage)
          primaryEmail <- request.userAnswers.get(subPrimaryEmailPage)

        })
      }


    }
}
