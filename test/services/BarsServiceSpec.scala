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

import base.SpecBase
import connectors.BarsConnector
import forms.BankAccountDetailsFormProvider
import helpers.ViewInstances
import models.NormalMode
import models.bars._
import models.repayments.BankAccountDetails
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages.BankAccountDetailsPage
import play.api.data.FormError
import play.api.inject.bind
import play.api.mvc.{AnyContentAsFormUrlEncoded, Request, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers.{POST, defaultAwaitTimeout, running}
import services.BarsServiceSpec._
import views.html.repayments.BankAccountDetailsView

import scala.concurrent.Future

class BarsServiceSpec extends SpecBase with ViewInstances {

  "BarsService" when {

    "redirect to contact name page when bars returns successful response" in {
      val userAnswer = emptyUserAnswers.setOrException(BankAccountDetailsPage, bankAccountDetails)

      val application = applicationBuilder(userAnswers = Some(userAnswer))
        .overrides(
          bind[BarsConnector].toInstance(mockBarsConnector)
        )
        .build()

      val service = application.injector.instanceOf[BarsService]

      running(application) {
        when(mockBarsConnector.verify(any(), any(), any())(any())).thenReturn(Future successful barsAccountResponse())

        val result = service.verifyBusinessAccount(bankAccountDetails, userAnswer, formProvider(), NormalMode)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some("/report-pillar2-top-up-taxes/repayment/contact-details/input-name")
      }
    }

    "redirect to partial account name page when bars returns successful response with partial name match" in {
      val userAnswer = emptyUserAnswers.setOrException(BankAccountDetailsPage, bankAccountDetails)

      val application = applicationBuilder(userAnswers = Some(userAnswer))
        .overrides(
          bind[BarsConnector].toInstance(mockBarsConnector)
        )
        .build()

      val service = application.injector.instanceOf[BarsService]

      running(application) {
        when(mockBarsConnector.verify(any(), any(), any())(any()))
          .thenReturn(Future successful barsAccountResponse(nameMatches = NameMatches.Partial, accountName = Some("Epic Adv")))

        val result = service.verifyBusinessAccount(bankAccountDetails, userAnswer, formProvider(), NormalMode)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some("/report-pillar2-top-up-taxes/repayment/error/partial-name")
      }
    }

    "return bad request with error forms when bars returns accountNumberIsWellFormatted = no" in {
      val userAnswer = emptyUserAnswers.setOrException(BankAccountDetailsPage, bankAccountDetails)

      val application = applicationBuilder(userAnswers = Some(userAnswer))
        .overrides(
          bind[BarsConnector].toInstance(mockBarsConnector)
        )
        .build()

      val service = application.injector.instanceOf[BarsService]
      val view    = application.injector.instanceOf[BankAccountDetailsView]

      running(application) {
        when(mockBarsConnector.verify(any(), any(), any())(any()))
          .thenReturn(Future successful barsAccountResponse(accountNumberIsWellFormatted = AccountNumberIsWellFormatted.No))

        val formWithError =
          formProvider().fill(bankAccountDetails).withError(FormError("accountNumber", "repayments.bankAccountDetails.error.accountNumber"))

        val result = service.verifyBusinessAccount(
          bankAccountDetails,
          userAnswer,
          formProvider().fill(bankAccountDetails),
          NormalMode
        )(hc, request, messages(application))

        status(result) mustBe BAD_REQUEST
        contentAsString(result) mustBe
          view(
            formWithError,
            NormalMode
          )(
            request,
            applicationConfig,
            messages(application)
          ).toString
      }
    }

    "return bad request with error forms when bars returns accountExists = no" in {
      val userAnswer = emptyUserAnswers.setOrException(BankAccountDetailsPage, bankAccountDetails)

      val application = applicationBuilder(userAnswers = Some(userAnswer))
        .overrides(
          bind[BarsConnector].toInstance(mockBarsConnector)
        )
        .build()

      val service = application.injector.instanceOf[BarsService]
      val view    = application.injector.instanceOf[BankAccountDetailsView]

      running(application) {
        when(mockBarsConnector.verify(any(), any(), any())(any()))
          .thenReturn(Future successful barsAccountResponse(accountExists = AccountExists.No))

        val formWithError =
          formProvider().fill(bankAccountDetails).withError(FormError("accountNumber", "repayments.bankAccountDetails.error.accountNumber"))

        val result = service.verifyBusinessAccount(
          bankAccountDetails,
          userAnswer,
          formProvider().fill(bankAccountDetails),
          NormalMode
        )(hc, request, messages(application))

        status(result) mustBe BAD_REQUEST
        contentAsString(result) mustBe
          view(
            formWithError,
            NormalMode
          )(
            request,
            applicationConfig,
            messages(application)
          ).toString
      }
    }

    "redirect to could not confirm bank details page when bars returns accountExists = inapplicable" in {
      val userAnswer = emptyUserAnswers.setOrException(BankAccountDetailsPage, bankAccountDetails)

      val application = applicationBuilder(userAnswers = Some(userAnswer))
        .overrides(
          bind[BarsConnector].toInstance(mockBarsConnector)
        )
        .build()

      val service = application.injector.instanceOf[BarsService]

      running(application) {
        when(mockBarsConnector.verify(any(), any(), any())(any()))
          .thenReturn(Future successful barsAccountResponse(accountExists = AccountExists.Inapplicable))

        val result = service.verifyBusinessAccount(bankAccountDetails, userAnswer, formProvider(), NormalMode)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some("/report-pillar2-top-up-taxes/repayment/error/bank-details")
      }
    }

    "redirect to could not confirm bank details page when bars returns accountExists = indeterminate" in {
      val userAnswer = emptyUserAnswers.setOrException(BankAccountDetailsPage, bankAccountDetails)

      val application = applicationBuilder(userAnswers = Some(userAnswer))
        .overrides(
          bind[BarsConnector].toInstance(mockBarsConnector)
        )
        .build()

      val service = application.injector.instanceOf[BarsService]

      running(application) {
        when(mockBarsConnector.verify(any(), any(), any())(any()))
          .thenReturn(Future successful barsAccountResponse(accountExists = AccountExists.Indeterminate))

        val result = service.verifyBusinessAccount(bankAccountDetails, userAnswer, formProvider(), NormalMode)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some("/report-pillar2-top-up-taxes/repayment/error/could-not-confirm")
      }
    }

    "redirect to bars error page when bars returns accountExists = error" in {
      val userAnswer = emptyUserAnswers.setOrException(BankAccountDetailsPage, bankAccountDetails)

      val application = applicationBuilder(userAnswers = Some(userAnswer))
        .overrides(
          bind[BarsConnector].toInstance(mockBarsConnector)
        )
        .build()

      val service = application.injector.instanceOf[BarsService]

      running(application) {
        when(mockBarsConnector.verify(any(), any(), any())(any()))
          .thenReturn(Future successful barsAccountResponse(accountExists = AccountExists.Error))

        val result = service.verifyBusinessAccount(bankAccountDetails, userAnswer, formProvider(), NormalMode)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some("/report-pillar2-top-up-taxes/repayment/error/view")
      }
    }

    "return bad request with error form when bars returns nameMatches = no" in {
      val userAnswer = emptyUserAnswers.setOrException(BankAccountDetailsPage, bankAccountDetails)

      val application = applicationBuilder(userAnswers = Some(userAnswer))
        .overrides(
          bind[BarsConnector].toInstance(mockBarsConnector)
        )
        .build()

      val service = application.injector.instanceOf[BarsService]
      val view    = application.injector.instanceOf[BankAccountDetailsView]

      running(application) {
        when(mockBarsConnector.verify(any(), any(), any())(any()))
          .thenReturn(Future successful barsAccountResponse(nameMatches = NameMatches.No))

        val formWithError =
          formProvider().fill(bankAccountDetails).withError(FormError("accountHolderName", "repayments.bankAccountDetails.error.accountName"))

        val result = service.verifyBusinessAccount(
          bankAccountDetails,
          userAnswer,
          formProvider().fill(bankAccountDetails),
          NormalMode
        )(hc, request, messages(application))

        status(result) mustBe BAD_REQUEST
        contentAsString(result) mustBe
          view(
            formWithError,
            NormalMode
          )(
            request,
            applicationConfig,
            messages(application)
          ).toString
      }
    }

    "redirect to could not confirm bank details page when bars returns nameMatches = inapplicable" in {
      val userAnswer = emptyUserAnswers.setOrException(BankAccountDetailsPage, bankAccountDetails)

      val application = applicationBuilder(userAnswers = Some(userAnswer))
        .overrides(
          bind[BarsConnector].toInstance(mockBarsConnector)
        )
        .build()

      val service = application.injector.instanceOf[BarsService]

      running(application) {
        when(mockBarsConnector.verify(any(), any(), any())(any()))
          .thenReturn(Future successful barsAccountResponse(nameMatches = NameMatches.Inapplicable))

        val result = service.verifyBusinessAccount(bankAccountDetails, userAnswer, formProvider(), NormalMode)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some("/report-pillar2-top-up-taxes/repayment/error/could-not-confirm")
      }
    }

    "redirect to could not confirm bank details page when bars returns nameMatches = indeterminate" in {
      val userAnswer = emptyUserAnswers.setOrException(BankAccountDetailsPage, bankAccountDetails)

      val application = applicationBuilder(userAnswers = Some(userAnswer))
        .overrides(
          bind[BarsConnector].toInstance(mockBarsConnector)
        )
        .build()

      val service = application.injector.instanceOf[BarsService]

      running(application) {
        when(mockBarsConnector.verify(any(), any(), any())(any()))
          .thenReturn(Future successful barsAccountResponse(nameMatches = NameMatches.Indeterminate))

        val result = service.verifyBusinessAccount(bankAccountDetails, userAnswer, formProvider(), NormalMode)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some("/report-pillar2-top-up-taxes/repayment/error/could-not-confirm")
      }
    }

    "redirect to bars error page when bars returns nameMatches = error" in {
      val userAnswer = emptyUserAnswers.setOrException(BankAccountDetailsPage, bankAccountDetails)

      val application = applicationBuilder(userAnswers = Some(userAnswer))
        .overrides(
          bind[BarsConnector].toInstance(mockBarsConnector)
        )
        .build()

      val service = application.injector.instanceOf[BarsService]

      running(application) {
        when(mockBarsConnector.verify(any(), any(), any())(any()))
          .thenReturn(Future successful barsAccountResponse(nameMatches = NameMatches.Error))

        val result = service.verifyBusinessAccount(bankAccountDetails, userAnswer, formProvider(), NormalMode)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some("/report-pillar2-top-up-taxes/repayment/error/view")
      }
    }

    "return bad request with error form when bars returns sortCodeIsPresentOnEISCD = no" in {
      val userAnswer = emptyUserAnswers.setOrException(BankAccountDetailsPage, bankAccountDetails)

      val application = applicationBuilder(userAnswers = Some(userAnswer))
        .overrides(
          bind[BarsConnector].toInstance(mockBarsConnector)
        )
        .build()

      val service = application.injector.instanceOf[BarsService]
      val view    = application.injector.instanceOf[BankAccountDetailsView]

      running(application) {
        when(mockBarsConnector.verify(any(), any(), any())(any()))
          .thenReturn(Future successful barsAccountResponse(sortCodeIsPresentOnEISCD = SortCodeIsPresentOnEISCD.No))

        val formWithError =
          formProvider().fill(bankAccountDetails).withError(FormError("sortCode", "repayments.bankAccountDetails.error.sortCode"))

        val result = service.verifyBusinessAccount(
          bankAccountDetails,
          userAnswer,
          formProvider().fill(bankAccountDetails),
          NormalMode
        )(hc, request, messages(application))

        status(result) mustBe BAD_REQUEST
        contentAsString(result) mustBe
          view(
            formWithError,
            NormalMode
          )(
            request,
            applicationConfig,
            messages(application)
          ).toString
      }
    }

    "redirect to bars error page when bars returns sortCodeIsPresentOnEISCD = error" in {
      val userAnswer = emptyUserAnswers.setOrException(BankAccountDetailsPage, bankAccountDetails)

      val application = applicationBuilder(userAnswers = Some(userAnswer))
        .overrides(
          bind[BarsConnector].toInstance(mockBarsConnector)
        )
        .build()

      val service = application.injector.instanceOf[BarsService]

      running(application) {
        when(mockBarsConnector.verify(any(), any(), any())(any()))
          .thenReturn(Future successful barsAccountResponse(sortCodeIsPresentOnEISCD = SortCodeIsPresentOnEISCD.Error))

        val result = service.verifyBusinessAccount(bankAccountDetails, userAnswer, formProvider(), NormalMode)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some("/report-pillar2-top-up-taxes/repayment/error/view")
      }
    }

    "redirect to could not confirm bank details page when bars returns nonStandardAccountDetailsRequiredForBacs = yes" in {
      val userAnswer = emptyUserAnswers.setOrException(BankAccountDetailsPage, bankAccountDetails)

      val application = applicationBuilder(userAnswers = Some(userAnswer))
        .overrides(
          bind[BarsConnector].toInstance(mockBarsConnector)
        )
        .build()

      val service = application.injector.instanceOf[BarsService]

      running(application) {
        when(mockBarsConnector.verify(any(), any(), any())(any()))
          .thenReturn(Future successful barsAccountResponse(nonStandardAccountDetailsRequiredForBacs = NonStandardAccountDetailsRequiredForBacs.Yes))

        val result = service.verifyBusinessAccount(bankAccountDetails, userAnswer, formProvider(), NormalMode)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some("/report-pillar2-top-up-taxes/repayment/error/bank-details")
      }
    }

    "redirect to bars error page when bars returns sortCodeSupportsDirectCredit = error" in {
      val userAnswer = emptyUserAnswers.setOrException(BankAccountDetailsPage, bankAccountDetails)

      val application = applicationBuilder(userAnswers = Some(userAnswer))
        .overrides(
          bind[BarsConnector].toInstance(mockBarsConnector)
        )
        .build()

      val service = application.injector.instanceOf[BarsService]

      running(application) {
        when(mockBarsConnector.verify(any(), any(), any())(any()))
          .thenReturn(Future successful barsAccountResponse(sortCodeSupportsDirectCredit = SortCodeSupportsDirectCredit.Error))

        val result = service.verifyBusinessAccount(bankAccountDetails, userAnswer, formProvider(), NormalMode)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some("/report-pillar2-top-up-taxes/repayment/error/view")
      }
    }

    "redirect to bars error page when bars returns response with partial but account name with partial details" in {
      val userAnswer = emptyUserAnswers.setOrException(BankAccountDetailsPage, bankAccountDetails)

      val application = applicationBuilder(userAnswers = Some(userAnswer))
        .overrides(
          bind[BarsConnector].toInstance(mockBarsConnector)
        )
        .build()

      val service = application.injector.instanceOf[BarsService]

      running(application) {
        when(mockBarsConnector.verify(any(), any(), any())(any()))
          .thenReturn(Future successful barsAccountResponse(nameMatches = NameMatches.Partial))

        val result: Future[Result] = service.verifyBusinessAccount(bankAccountDetails, userAnswer, formProvider(), NormalMode)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some("/report-pillar2-top-up-taxes/repayment/error/view")
      }
    }

    "display all multiple errors if bars returns multiple errors" in {
      val userAnswer = emptyUserAnswers.setOrException(BankAccountDetailsPage, bankAccountDetails)

      val application = applicationBuilder(userAnswers = Some(userAnswer))
        .overrides(
          bind[BarsConnector].toInstance(mockBarsConnector)
        )
        .build()

      val service = application.injector.instanceOf[BarsService]
      val view    = application.injector.instanceOf[BankAccountDetailsView]

      running(application) {
        when(mockBarsConnector.verify(any(), any(), any())(any()))
          .thenReturn(
            Future successful barsAccountResponse(
              sortCodeIsPresentOnEISCD = SortCodeIsPresentOnEISCD.No,
              nameMatches = NameMatches.No,
              accountExists = AccountExists.No
            )
          )

        val formWithError =
          formProvider()
            .fill(bankAccountDetails)
            .withError(FormError("accountHolderName", "repayments.bankAccountDetails.error.accountName"))
            .withError(FormError("sortCode", "repayments.bankAccountDetails.error.sortCode"))
            .withError(FormError("accountNumber", "repayments.bankAccountDetails.error.accountNumber"))

        val result = service.verifyBusinessAccount(
          bankAccountDetails,
          userAnswer,
          formProvider().fill(bankAccountDetails),
          NormalMode
        )(hc, request, messages(application))

        status(result) mustBe BAD_REQUEST
        contentAsString(result) mustBe
          view(
            formWithError,
            NormalMode
          )(
            request,
            applicationConfig,
            messages(application)
          ).toString
      }
    }

    "fallback to bars error page if requirements are not met" in {
      val userAnswer = emptyUserAnswers.setOrException(BankAccountDetailsPage, bankAccountDetails)

      val application = applicationBuilder(userAnswers = Some(userAnswer))
        .overrides(
          bind[BarsConnector].toInstance(mockBarsConnector)
        )
        .build()

      val service = application.injector.instanceOf[BarsService]

      running(application) {
        when(mockBarsConnector.verify(any(), any(), any())(any()))
          .thenReturn(
            Future successful barsAccountResponse(
              sortCodeSupportsDirectCredit = SortCodeSupportsDirectCredit.Error,
              sortCodeIsPresentOnEISCD = SortCodeIsPresentOnEISCD.Error,
              accountExists = AccountExists.Error
            )
          )

        val result = service.verifyBusinessAccount(bankAccountDetails, userAnswer, formProvider(), NormalMode)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some("/report-pillar2-top-up-taxes/repayment/error/view")
      }
    }

  }
}

object BarsServiceSpec {
  val bankAccountDetails: BankAccountDetails = BankAccountDetails("Barclays", "Epic Adventure Inc", "206705", "86473611")

  val formProvider: BankAccountDetailsFormProvider = new BankAccountDetailsFormProvider()

  implicit val request: Request[AnyContentAsFormUrlEncoded] =
    FakeRequest(POST, controllers.repayments.routes.BankAccountDetailsController.onSubmit(NormalMode).url)
      .withFormUrlEncodedBody(
        "bankName"          -> bankAccountDetails.bankName,
        "accountHolderName" -> bankAccountDetails.nameOnBankAccount,
        "sortCode"          -> bankAccountDetails.sortCode,
        "accountNumber"     -> bankAccountDetails.accountNumber
      )

  def barsAccountResponse(
    nameMatches:                              NameMatches = NameMatches.Yes,
    accountName:                              Option[String] = None,
    accountNumberIsWellFormatted:             AccountNumberIsWellFormatted = AccountNumberIsWellFormatted.Yes,
    accountExists:                            AccountExists = AccountExists.Yes,
    sortCodeIsPresentOnEISCD:                 SortCodeIsPresentOnEISCD = SortCodeIsPresentOnEISCD.Yes,
    nonStandardAccountDetailsRequiredForBacs: NonStandardAccountDetailsRequiredForBacs = NonStandardAccountDetailsRequiredForBacs.No,
    sortCodeSupportsDirectCredit:             SortCodeSupportsDirectCredit = SortCodeSupportsDirectCredit.No
  ): BarsAccountResponse = BarsAccountResponse(
    accountNumberIsWellFormatted,
    sortCodeIsPresentOnEISCD,
    sortCodeBankName = Some("BARCLAYS BANK UK PLC"),
    nonStandardAccountDetailsRequiredForBacs,
    accountExists,
    nameMatches,
    accountName,
    SortCodeSupportsDirectDebit.No,
    sortCodeSupportsDirectCredit,
    iban = Some("GB21BARC20670586473611")
  )
}
