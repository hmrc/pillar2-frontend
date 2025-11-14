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

package services

import cats.data.OptionT
import config.FrontendAppConfig
import connectors.BarsConnector
import controllers.repayments.routes
import models.bars.*
import models.repayments.BankAccountDetails
import models.{Mode, UserAnswers}
import navigation.RepaymentNavigator
import pages.{BankAccountDetailsPage, BarsAccountNamePartialPage}
import play.api.Logging
import play.api.data.{Form, FormError}
import play.api.i18n.Messages
import play.api.mvc.Results.*
import play.api.mvc.{Request, Result}
import repositories.SessionRepository
import uk.gov.hmrc.http.HeaderCarrier
import views.html.repayments.BankAccountDetailsView

import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class BarsService @Inject() (
  barsConnector:     BarsConnector,
  sessionRepository: SessionRepository,
  navigator:         RepaymentNavigator,
  view:              BankAccountDetailsView
)(implicit
  ec:        ExecutionContext,
  appConfig: FrontendAppConfig
) extends Logging {

  def verifyBusinessAccount(
    details:     BankAccountDetails,
    userAnswers: UserAnswers,
    form:        Form[BankAccountDetails],
    mode:        Mode
  )(implicit
    hc:       HeaderCarrier,
    request:  Request[?],
    messages: Messages
  ): Future[Result] = {
    lazy val trackingId = UUID.randomUUID()

    for {
      barsResponse <- barsConnector.verify(Business(details.nameOnBankAccount), Account(details.sortCode, details.accountNumber), trackingId)
      _ = logger.info(s"Request has been sent to BARS with trackingId=$trackingId")
      result <- handleBarsResponse(barsResponse, userAnswers, mode, form)
    } yield result
  }

  private def handleBarsResponse(
    barsAccountResponse: BarsAccountResponse,
    userAnswers:         UserAnswers,
    mode:                Mode,
    form:                Form[BankAccountDetails]
  )(implicit request: Request[?], messages: Messages): Future[Result] = {
    import barsAccountResponse.*
    (
      accountNumberIsWellFormatted,
      accountExists,
      nameMatches,
      sortCodeIsPresentOnEISCD,
      nonStandardAccountDetailsRequiredForBacs,
      sortCodeSupportsDirectCredit
    ) match {
      case (
            accountNumberIsWellFormatted,
            AccountExists.Yes,
            NameMatches.Yes,
            SortCodeIsPresentOnEISCD.Yes,
            nonStandardAccountDetailsRequiredForBacs,
            sortCodeSupportsDirectCredit
          ) if isValidAccountDetails(accountNumberIsWellFormatted, sortCodeSupportsDirectCredit, nonStandardAccountDetailsRequiredForBacs) =>
        Future successful Redirect(
          navigator.nextPage(BankAccountDetailsPage, mode, userAnswers)
        )
      case (
            accountNumberIsWellFormatted,
            AccountExists.Yes,
            NameMatches.Partial,
            SortCodeIsPresentOnEISCD.Yes,
            nonStandardAccountDetailsRequiredForBacs,
            sortCodeSupportsDirectCredit
          ) if isValidAccountDetails(accountNumberIsWellFormatted, sortCodeSupportsDirectCredit, nonStandardAccountDetailsRequiredForBacs) =>
        handlePartialNameMatch(barsAccountResponse.accountName, userAnswers, mode)

      case (_, accountExists, _, _, _, _) if accountExists != AccountExists.Yes =>
        Future successful handleAccountExists(
          accountNumberIsWellFormatted,
          accountExists,
          nameMatches,
          sortCodeIsPresentOnEISCD,
          userAnswers,
          mode,
          form
        )

      case (_, _, nameMatches, _, _, _) if nameMatches != NameMatches.Yes =>
        Future successful handleNameMatches(
          accountNumberIsWellFormatted,
          accountExists,
          nameMatches,
          sortCodeIsPresentOnEISCD,
          userAnswers,
          mode,
          form
        )

      case (_, _, _, SortCodeIsPresentOnEISCD.Error, _, _) =>
        Future successful Redirect(routes.RepaymentErrorController.onPageLoadError)
      case (_, _, _, _, NonStandardAccountDetailsRequiredForBacs.Yes, _) =>
        Future successful Redirect(routes.RepaymentErrorController.onPageLoadBankDetailsError)
      case (_, _, _, _, _, SortCodeSupportsDirectCredit.Error) =>
        Future successful Redirect(routes.RepaymentErrorController.onPageLoadError)
      case (accountNumberIsWellFormatted, accountExists, nameMatches, sortCodeIsPresentOnEISCD, _, _) =>
        Future successful handleAndDisplayErrors(
          accountNumberIsWellFormatted,
          accountExists,
          nameMatches,
          sortCodeIsPresentOnEISCD,
          userAnswers,
          mode,
          form
        )

      case _ => Future successful Redirect(routes.RepaymentErrorController.onPageLoadError)
    }
  }

  private def isValidAccountDetails(
    accountNumberIsWellFormatted:             AccountNumberIsWellFormatted,
    sortCodeSupportsDirectCredit:             SortCodeSupportsDirectCredit,
    nonStandardAccountDetailsRequiredForBacs: NonStandardAccountDetailsRequiredForBacs
  ) =
    accountNumberIsWellFormatted != AccountNumberIsWellFormatted.No &&
      sortCodeSupportsDirectCredit != SortCodeSupportsDirectCredit.Error &&
      nonStandardAccountDetailsRequiredForBacs != NonStandardAccountDetailsRequiredForBacs.Yes

  private def handleAccountExists(
    accountNumberIsWellFormatted: AccountNumberIsWellFormatted,
    accountExists:                AccountExists,
    nameMatches:                  NameMatches,
    sortCodeIsPresentOnEISCD:     SortCodeIsPresentOnEISCD,
    userAnswers:                  UserAnswers,
    mode:                         Mode,
    form:                         Form[BankAccountDetails]
  )(implicit request: Request[?], messages: Messages): Result =
    accountExists match {
      case AccountExists.Inapplicable  => Redirect(routes.RepaymentErrorController.onPageLoadBankDetailsError)
      case AccountExists.Indeterminate => Redirect(routes.RepaymentErrorController.onPageLoadNotConfirmedDetails)
      case AccountExists.No            =>
        handleAndDisplayErrors(
          accountNumberIsWellFormatted,
          accountExists,
          nameMatches,
          sortCodeIsPresentOnEISCD,
          userAnswers,
          mode,
          form
        )
      case _ => Redirect(routes.RepaymentErrorController.onPageLoadError)
    }

  private def handleNameMatches(
    accountNumberIsWellFormatted: AccountNumberIsWellFormatted,
    accountExists:                AccountExists,
    nameMatches:                  NameMatches,
    sortCodeIsPresentOnEISCD:     SortCodeIsPresentOnEISCD,
    userAnswers:                  UserAnswers,
    mode:                         Mode,
    form:                         Form[BankAccountDetails]
  )(implicit request: Request[?], messages: Messages): Result =
    nameMatches match {
      case NameMatches.Inapplicable  => Redirect(routes.RepaymentErrorController.onPageLoadNotConfirmedDetails)
      case NameMatches.Indeterminate => Redirect(routes.RepaymentErrorController.onPageLoadNotConfirmedDetails)
      case NameMatches.No            =>
        handleAndDisplayErrors(
          accountNumberIsWellFormatted,
          accountExists,
          nameMatches,
          sortCodeIsPresentOnEISCD,
          userAnswers,
          mode,
          form
        )
      case _ => Redirect(routes.RepaymentErrorController.onPageLoadError)
    }

  private def handlePartialNameMatch(
    partialName: Option[String],
    userAnswers: UserAnswers,
    mode:        Mode
  ): Future[Result] = {
    for {
      accountName    <- OptionT.fromOption[Future](partialName)
      updatedAnswers <- OptionT.fromOption[Future](userAnswers.set(BarsAccountNamePartialPage, accountName).toOption)
      _              <- OptionT.liftF(sessionRepository.set(updatedAnswers))
    } yield Redirect(navigator.nextPage(BarsAccountNamePartialPage, mode, userAnswers))
  }.getOrElse(Redirect(routes.RepaymentErrorController.onPageLoadError))

  private def handleAndDisplayErrors(
    accountNumberIsWellFormatted: AccountNumberIsWellFormatted,
    accountExists:                AccountExists,
    nameMatches:                  NameMatches,
    sortCodeIsPresentOnEISCD:     SortCodeIsPresentOnEISCD,
    userAnswers:                  UserAnswers,
    mode:                         Mode,
    form:                         Form[BankAccountDetails]
  )(implicit request: Request[?], messages: Messages): Result = {
    val preparedForm = userAnswers.get(BankAccountDetailsPage).map(ua => form.fill(ua)).getOrElse(form)

    val maybeFormErrors: Seq[Option[FormError]] =
      Seq(
        Option.when(nameMatches == NameMatches.No)(FormError("accountHolderName", "repayments.bankAccountDetails.error.accountName")),
        Option.when(sortCodeIsPresentOnEISCD == SortCodeIsPresentOnEISCD.No)(FormError("sortCode", "repayments.bankAccountDetails.error.sortCode")),
        Option
          .when(accountNumberIsWellFormatted == AccountNumberIsWellFormatted.No)(
            FormError("accountNumber", "repayments.bankAccountDetails.error.accountNumber")
          )
          .orElse(Option.when(accountExists == AccountExists.No)(FormError("accountNumber", "repayments.bankAccountDetails.error.accountNumber")))
      )

    val bankAccountFormWithErrors = maybeFormErrors.foldLeft(preparedForm) { (form, maybeError) =>
      maybeError match {
        case Some(value) => form.withError(value)
        case None        => form
      }
    }

    BadRequest(view(bankAccountFormWithErrors, mode))
  }
}
