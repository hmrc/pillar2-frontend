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
import pages.SubscriptionPage
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
  page_not_available:        ErrorTemplate,
  view:                      ContactCheckYourAnswersView,
  countryOptions:            CountryOptions
)(implicit ec:               ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    val notAvailable = page_not_available("page_not_available.title", "page_not_available.heading", "page_not_available.message")
    (for {
      sub <- request.userAnswers.get(SubscriptionPage)
      list = isPrimaryPhoneDefined(sub) match {
               case true =>
                 SummaryListViewModel(
                   rows = Seq(
                     ContactNameComplianceSummary.row(request.userAnswers),
                     ContactEmailAddressSummary.row(request.userAnswers),
                     ContactByTelephoneSummary.row(request.userAnswers),
                     ContactCaptureTelephoneDetailsSummary.row(request.userAnswers)
                   ).flatten
                 )
               case false =>
                 val subRegData = request.userAnswers.get(SubscriptionPage).getOrElse(throw new Exception("Subscription data not available"))
                 for {
                   updatedAnswers <-
                     Future
                       .fromTry(
                         request.userAnswers.set(
                           SubscriptionPage,
                           subRegData.copy(
                             contactByTelephone = Some(false)
                           )
                         )
                       )
                   _ <- userAnswersConnectors.save(updatedAnswers.id, Json.toJson(updatedAnswers.data))
                 } yield ()

                 SummaryListViewModel(
                   rows = Seq(
                     ContactNameComplianceSummary.row(request.userAnswers),
                     ContactEmailAddressSummary.row(request.userAnswers),
                     ContactByTelephoneSummary.row(request.userAnswers)
                   ).flatten
                 )
             }

      addSecondaryContactList =
        SummaryListViewModel(
          rows = Seq(
            AddSecondaryContactSummary.row(request.userAnswers)
          ).flatten
        )

      listSecondary = (isSecondContactDefined(request), isSecondaryPhoneDefined(request)) match {
                        case (true, true) =>
                          SummaryListViewModel(
                            rows = Seq(
                              SecondaryContactNameSummary.row(request.userAnswers),
                              SecondaryContactEmailSummary.row(request.userAnswers),
                              SecondaryTelephonePreferenceSummary.row(request.userAnswers),
                              SecondaryTelephoneSummary.row(request.userAnswers)
                            ).flatten
                          )
                        case (true, false) =>
                          val subRegData = request.userAnswers.get(SubscriptionPage).getOrElse(throw new Exception("Subscription data not available"))
                          for {
                            updatedAnswers <-
                              Future
                                .fromTry(
                                  request.userAnswers.set(
                                    SubscriptionPage,
                                    subRegData.copy(
                                      secondaryTelephonePreference = Some(false),
                                      secondaryContactTelephone = None
                                    )
                                  )
                                )
                            _ <- userAnswersConnectors.save(updatedAnswers.id, Json.toJson(updatedAnswers.data))
                          } yield ()
                          SummaryListViewModel(
                            rows = Seq(
                              SecondaryContactNameSummary.row(request.userAnswers),
                              SecondaryContactEmailSummary.row(request.userAnswers),
                              SecondaryTelephonePreferenceSummary.row(request.userAnswers)
                            ).flatten
                          )
                        case _ =>
                          val subRegData = request.userAnswers.get(SubscriptionPage).getOrElse(throw new Exception("Subscription data not available"))
                          for {
                            updatedAnswers <-
                              Future
                                .fromTry(
                                  request.userAnswers.set(
                                    SubscriptionPage,
                                    subRegData.copy(
                                      addSecondaryContact = Some(false),
                                      secondaryContactName = None,
                                      secondaryContactEmail = None,
                                      secondaryContactTelephone = None
                                    )
                                  )
                                )
                            _ <- userAnswersConnectors.save(updatedAnswers.id, Json.toJson(updatedAnswers.data))
                          } yield ()
                          SummaryListViewModel(rows = Seq())
                      }
      address = SummaryListViewModel(
                  rows = Seq(
                    ContactCorrespondenceAddressSummary.row(request.userAnswers, countryOptions)
                  ).flatten
                )

    } yield
      if (isPreviousPagesDefined(request))
        Ok(view(list, addSecondaryContactList, listSecondary, address))
      else
        NotFound(notAvailable)).getOrElse(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))

  }
  private def isPreviousPagesDefined(request: DataRequest[AnyContent]): Boolean =
    request.userAnswers
      .get(SubscriptionPage)
      .fold(false) { data =>
        data.groupDetailStatus.toString == "Completed"
      }

  private def isSecondContactDefined(request: DataRequest[AnyContent]): Boolean =
    request.userAnswers
      .get(SubscriptionPage)
      .fold(false)(data =>
        data.addSecondaryContact.fold(false)(contact =>
          contact
            && data.secondaryContactName.isDefined
            && data.secondaryContactEmail.isDefined
            && data.secondaryTelephonePreference.isDefined
        )
      )

  private def isPrimaryPhoneDefined(data: Subscription): Boolean =
    data.contactByTelephone.fold(false)(contact => contact) && data.primaryContactTelephone.isDefined

  private def isSecondaryPhoneDefined(request: DataRequest[AnyContent]): Boolean =
    request.userAnswers
      .get(SubscriptionPage)
      .fold(false)(data =>
        data.secondaryTelephonePreference.fold(false) { phone =>
          phone && data.secondaryContactTelephone.isDefined
        }
      )
}