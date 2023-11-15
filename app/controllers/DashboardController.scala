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

package controllers

import javax.inject.Inject
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.DashboardView
import config.FrontendAppConfig
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import models.UserAnswers
import models.registration.RegistrationInfo
import models.subscription.ReadSubscriptionRequestParameters
import pages.{UpeRegInformationPage, upeNameRegistrationPage}
import play.api.Logging
import services.ReadSubscriptionService
import uk.gov.hmrc.auth.core.Enrolment

import java.time.format.DateTimeFormatter
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DashboardController @Inject() (
  getData:                     DataRetrievalAction,
  identify:                    IdentifierAction,
  requireData:                 DataRequiredAction,
  val readSubscriptionService: ReadSubscriptionService,
  val controllerComponents:    MessagesControllerComponents,
  view:                        DashboardView
)(implicit ec:                 ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    val userId = request.userId
    val plrReference = request.enrolments
      .flatMap(_.find { enrolment =>
        enrolment.key.equalsIgnoreCase("HMRC-PLR-ORG") &&
        enrolment.identifiers.exists(_.key.equalsIgnoreCase("PLRID"))
      })
      .flatMap(_.identifiers.find(_.key.equalsIgnoreCase("PLRID")))
      .map(_.value)

    plrReference match {
      case Some(ref) =>
        for {
          readSub <- readSubscriptionService.readSubscription(ReadSubscriptionRequestParameters(userId, ref))
          result <- readSub match {
                      case Right(userAnswers) =>
                        getSome(userAnswers) match {
                          case Some((companyName, regDate)) =>
                            Future.successful(Ok(view(companyName, regDate.format(DateTimeFormatter.ofPattern("d MMMM yyyy")), ref)))
                          case None => Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
                        }
                      case Left(error) =>
                        logger.error(s"Error retrieving subscription: $error")
                        Future.successful(InternalServerError("Internal Server Error occurred"))
                    }
        } yield result

      case None =>
        Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
    }
  }

  private def getSome(userAnswers: UserAnswers) =
    (userAnswers.get(upeNameRegistrationPage), userAnswers.get(UpeRegInformationPage).flatMap(_.registrationDate)) match {
      case (Some(x), Some(y)) => Option(x, y)
      case _                  => None
    }

//  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
//    val plrReference = request.enrolments
//      .flatMap(_.find { enrolment =>
//        enrolment.key.equalsIgnoreCase("HMRC-PLR-ORG") &&
//          enrolment.identifiers.exists(_.key.equalsIgnoreCase("PLRID"))
//      })
//      .flatMap(_.identifiers.find(_.key.equalsIgnoreCase("PLRID")))
//      .map(_.value)
//    for {
//      ref <- plrReference
//      userId = request.userId
//      readSub <- readSubscriptionService.readSubscription(ReadSubscriptionRequestParameters(userId, ref))
//    } yield {
//      readSub match {
//        case Right(userAnswers) => getSome(userAnswers) match {
//          case Some(Tuple2(companyName, regDate)) => Ok(view(companyName, regDate.format(DateTimeFormatter.ofPattern("d MMMM yyyy")), ref))
//          case None => Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
//        }
//        case Left(error) => logger.error(s"Error retrieving subscription  $error")
//          Future.successful(InternalServerError())
//      }
//
//    }
//  }

//  private def getSome(userAnswers: UserAnswers) = {
//    (userAnswers.get(upeNameRegistrationPage), userAnswers.get(UpeRegInformationPage).flatMap(_.registrationDate)) match {
//      case (Some(x), Some(y)) => Option(x, y)
//      case _ => None
//    }
//  }

}
