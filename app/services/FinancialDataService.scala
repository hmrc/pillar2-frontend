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

import cats.data.{Validated, ValidatedNec}
import cats.syntax.apply.*
import cats.syntax.functorFilter.*
import cats.syntax.option.*
import cats.syntax.validated.*
import connectors.FinancialDataConnector
import models.financialdata.FinancialTransaction.{OutstandingCharge, Payment}
import models.financialdata.*
import play.api.Logging
import services.FinancialDataService.IgnoredEtmpTransaction.{DidNotPassFilter, RequiredValueMissing, UnrelatedValue}
import services.FinancialDataService.parseFinancialDataResponse
import uk.gov.hmrc.http.HeaderCarrier

import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class FinancialDataService @Inject() (financialDataConnector: FinancialDataConnector)(implicit ec: ExecutionContext) {

  /** Parses financial data from the API into something a bit easier to take decisions against */
  def retrieveFinancialData(pillar2Id: String, fromDate: LocalDate, toDate: LocalDate)(implicit hc: HeaderCarrier): Future[FinancialData] =
    financialDataConnector
      .retrieveFinancialData(pillar2Id, fromDate, toDate)
      .map(parseFinancialDataResponse)

}

object FinancialDataService extends Logging {
  def parseFinancialDataResponse(response: FinancialDataResponse): FinancialData = FinancialData {
    response.financialTransactions
      .map(parseFinancialTransactionToDomain)
      .mapFilter {
        case Validated.Valid(responseTransaction) => Some(responseTransaction)
        case Validated.Invalid(reasons)           =>
          logger.debug {
            val concatReasons = reasons.map(_.errorMessage).toNonEmptyList.toList.mkString("", " ", ".")
            s"Dropping financial transaction while mapping to domain: $concatReasons"
          }
          Option.empty[FinancialTransaction]
      }
  }

  private val parseFinancialTransactionToDomain
    : FinancialDataResponse.FinancialTransaction => ValidatedNec[IgnoredEtmpTransaction, FinancialTransaction] = response =>
    parseMainTransactionRef(response).andThen {
      case EtmpMainTransactionRef.PaymentTransaction =>
        FinancialTransaction.Payment(Payment.FinancialItems(response.items.map(responseItemToDomain))).validNec
      case mainRef: EtmpMainTransactionRef.ChargeRef =>
        parseChargeTransaction(response, mainRef)
    }

  private def parseChargeTransaction(
    responseTransaction: FinancialDataResponse.FinancialTransaction,
    mainReference:       EtmpMainTransactionRef.ChargeRef
  ): ValidatedNec[IgnoredEtmpTransaction, FinancialTransaction.OutstandingCharge] =
    (
      parseTaxPeriod(responseTransaction),
      parseSubtransactionRef(responseTransaction),
      parseOutstandingAmount(responseTransaction),
      parseOutstandingChargeFinancialItems(responseTransaction.items)
    ).mapN(OutstandingCharge(mainReference)(_, _, _, _))

  private val parseMainTransactionRef: FinancialDataResponse.FinancialTransaction => ValidatedNec[IgnoredEtmpTransaction, EtmpMainTransactionRef] =
    response =>
      Validated
        .fromOption(response.mainTransaction, ifNone = RequiredValueMissing("Main transaction reference"))
        .andThen { mainTxRef =>
          Validated.fromOption(
            EtmpMainTransactionRef.withValueOpt(mainTxRef),
            ifNone = UnrelatedValue("Main transaction reference", mainTxRef)
          )
        }
        .toValidatedNec

  private val parseSubtransactionRef: FinancialDataResponse.FinancialTransaction => ValidatedNec[IgnoredEtmpTransaction, EtmpSubtransactionRef] =
    response =>
      Validated
        .fromOption(response.subTransaction, ifNone = RequiredValueMissing("Subtransaction reference"))
        .andThen { subTxRef =>
          Validated.fromOption(
            EtmpSubtransactionRef.withValueOpt(subTxRef),
            ifNone = UnrelatedValue("Sub transaction reference", subTxRef)
          )
        }
        .toValidatedNec

  private val parseTaxPeriod: FinancialDataResponse.FinancialTransaction => ValidatedNec[IgnoredEtmpTransaction, TaxPeriod] = response =>
    (
      response.taxPeriodFrom.toValidNec(RequiredValueMissing("taxPeriodFrom")),
      response.taxPeriodTo.toValidNec(RequiredValueMissing("taxPeriodFrom"))
    ).mapN((from, to) => TaxPeriod(from, to))

  private val parseOutstandingAmount: FinancialDataResponse.FinancialTransaction => ValidatedNec[IgnoredEtmpTransaction, BigDecimal] =
    _.outstandingAmount
      .toValid(RequiredValueMissing("outstandingAmount"))
      .andThen { outstanding =>
        Validated
          .cond(outstanding > 0, outstanding, DidNotPassFilter("outstandingAmount", outstanding, "Outstanding amount was not greater than zero."))
      }
      .toValidatedNec

  private val parseOutstandingChargeFinancialItems
    : Seq[FinancialDataResponse.FinancialItem] => ValidatedNec[IgnoredEtmpTransaction, OutstandingCharge.FinancialItems] =
    responseItems =>
      responseItems
        .flatMap(_.dueDate)
        .minOption
        .toValidNec(IgnoredEtmpTransaction.RequiredValueMissing("dueDate"))
        .map(earliestDueDate => OutstandingCharge.FinancialItems(earliestDueDate, responseItems.map(responseItemToDomain)))

  private val responseItemToDomain: FinancialDataResponse.FinancialItem => FinancialItem = responseItem =>
    FinancialItem(responseItem.dueDate, responseItem.clearingDate)

  sealed abstract class IgnoredEtmpTransaction(val errorMessage: String) extends IllegalArgumentException(errorMessage)

  object IgnoredEtmpTransaction {
    case class RequiredValueMissing(field: String) extends IgnoredEtmpTransaction(s"$field was missing")
    case class UnrelatedValue(field: String, value: String) extends IgnoredEtmpTransaction(s"$field has invalid value $value")
    case class DidNotPassFilter[A](field: String, value: A, reason: String)
        extends IgnoredEtmpTransaction(s"$field's value $value did not meet criteria $reason")
  }
}
