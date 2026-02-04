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

import controllers.actions.*
import models.grs.*
import pages.*
import play.api.Logging
import play.api.mvc.*
import services.GrsReturnService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import javax.inject.{Inject, Named, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class GrsReturnController @Inject() (
  identify:                            IdentifierAction,
  @Named("RfmIdentifier") rfmIdentify: IdentifierAction,
  getData:                             DataRetrievalAction,
  requireData:                         DataRequiredAction,
  val controllerComponents:            MessagesControllerComponents,
  grsReturnService:                    GrsReturnService
)(using ec: ExecutionContext)
    extends FrontendBaseController
    with Logging {

  def continueUpe(journeyId: String): Action[AnyContent] = (identify andThen getData andThen requireData).async { request =>
    given Request[AnyContent] = request
    request.userAnswers
      .get(UpeEntityTypePage)
      .map {
        case EntityType.UkLimitedCompany =>
          grsReturnService.continueUpe(journeyId, EntityType.UkLimitedCompany, request.userAnswers)
        case EntityType.LimitedLiabilityPartnership =>
          grsReturnService.continueUpe(journeyId, EntityType.LimitedLiabilityPartnership, request.userAnswers)
      }
      .getOrElse(Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())))

  }

  def continueFm(journeyId: String): Action[AnyContent] = (identify andThen getData andThen requireData).async { request =>
    given Request[AnyContent] = request
    request.userAnswers
      .get(FmEntityTypePage)
      .map {
        case EntityType.UkLimitedCompany =>
          grsReturnService.continueFm(journeyId, EntityType.UkLimitedCompany, request.userAnswers)
        case EntityType.LimitedLiabilityPartnership =>
          grsReturnService.continueFm(journeyId, EntityType.LimitedLiabilityPartnership, request.userAnswers)
      }
      .getOrElse(Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())))

  }

  def continueRfm(journeyId: String): Action[AnyContent] =
    (rfmIdentify andThen getData andThen requireData).async { request =>
      given Request[AnyContent] = request
      request.userAnswers
        .get(RfmEntityTypePage)
        .map {
          case EntityType.UkLimitedCompany =>
            grsReturnService.continueRfm(journeyId, EntityType.UkLimitedCompany, request.userAnswers)
          case EntityType.LimitedLiabilityPartnership =>
            grsReturnService.continueRfm(journeyId, EntityType.LimitedLiabilityPartnership, request.userAnswers)
        }
        .getOrElse(Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())))

    }
}
