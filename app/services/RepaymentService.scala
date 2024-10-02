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

import connectors._
import models.repayments._
import models.{UkOrAbroadBankAccount, UserAnswers}
import org.apache.pekko.Done
import pages._
import play.api.Logging
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

@Singleton
class RepaymentService @Inject() (
  repaymentConnector: RepaymentConnector
) extends Logging {

  def getRepaymentData(userAnswers: UserAnswers): Option[SendRepaymentDetails] =
    for {
      referenceNumber <- userAnswers.get(PlrReferencePage)
      name            <- userAnswers.get(RepaymentsContactNamePage)
      reason          <- userAnswers.get(ReasonForRequestingRefundPage)
      repaymentAmount <- userAnswers.get(RepaymentsRefundAmountPage)
      bankDetails     <- getBankDetails(userAnswers)
      email           <- userAnswers.get(RepaymentsContactEmailPage)
    } yield {
      val telephone = userAnswers.get(RepaymentsTelephoneDetailsPage).getOrElse("")
      SendRepaymentDetails(
        repaymentDetails =
          RepaymentDetails(plrReference = referenceNumber, name = name, utr = None, reasonForRepayment = reason, refundAmount = repaymentAmount),
        bankDetails = bankDetails,
        contactDetails = RepaymentContactDetails(contactDetails = s"$name, $email, $telephone")
      )
    }

  private def getBankDetails(userAnswers: UserAnswers): Option[BankDetails] =
    userAnswers.get(UkOrAbroadBankAccountPage).flatMap { typeOfBankAccount =>
      if (typeOfBankAccount == UkOrAbroadBankAccount.UkBankAccount) {
        userAnswers
          .get(BankAccountDetailsPage)
          .map(bankDetails =>
            BankDetails(
              nameOnBankAccount = bankDetails.nameOnBankAccount,
              bankName = bankDetails.bankName,
              sortCode = Some(bankDetails.sortCode),
              accountNumber = Some(bankDetails.accountNumber),
              iban = None,
              bic = None,
              countryCode = None
            )
          )
      } else {
        userAnswers
          .get(NonUKBankPage)
          .map(bankDetails =>
            BankDetails(
              nameOnBankAccount = bankDetails.nameOnBankAccount,
              bankName = bankDetails.bankName,
              sortCode = None,
              accountNumber = None,
              iban = Some(bankDetails.iban),
              bic = Some(bankDetails.bic),
              countryCode = None
            )
          )
      }
    }

  def sendRepaymentDetails(data: SendRepaymentDetails)(implicit headerCarrier: HeaderCarrier): Future[Done] =
    repaymentConnector.repayment(data)

}
