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

package utils

import models.{ApiError, MneOrDomestic, SubscriptionCreateError}
import models.subscription.{AccountStatus, AccountingPeriod, ContactDetailsType, FilingMemberDetails, Subscription, SubscriptionAddress, UpeCorrespAddressDetails, UpeDetails}
import play.api.libs.json.{JsError, JsSuccess, JsValue}

object SubscriptionTransformer {

  def jsValueToSubscription(jsValue: JsValue): Either[ApiError, Subscription] = {
    val upeDetailsResult               = (jsValue \ "upeDetails").validate[UpeDetails]
    val upeCorrespAddressDetailsResult = (jsValue \ "upeCorrespAddressDetails").validate[UpeCorrespAddressDetails]
    val primaryContactNameResult       = (jsValue \ "primaryContactName").validate[String]
    val primaryContactEmailResult      = (jsValue \ "primaryContactEmail").validate[String]
    val secondaryContactNameResult     = (jsValue \ "secondaryContactName").validate[String]
    val secondaryContactEmailResult    = (jsValue \ "secondaryContactEmail").validate[String]
    val filingMemberDetailsResult      = (jsValue \ "filingMemberDetails").validate[FilingMemberDetails]
    val accountingPeriodResult         = (jsValue \ "accountingPeriod").validate[AccountingPeriod]
    val accountStatusResult            = (jsValue \ "accountStatus").validate[AccountStatus]

    val results = List(
      upeDetailsResult,
      upeCorrespAddressDetailsResult,
      primaryContactNameResult,
      primaryContactEmailResult,
      secondaryContactNameResult,
      secondaryContactEmailResult,
      filingMemberDetailsResult,
      accountingPeriodResult,
      accountStatusResult
    )

    results.collect { case JsError(errors) => errors }.foreach { errors =>
      errors.foreach { case (path, e) =>
        println(s"Error at $path: ${e.mkString(", ")}")
      }
    }

    for {
      upeDetails               <- upeDetailsResult.asOpt
      upeCorrespAddressDetails <- upeCorrespAddressDetailsResult.asOpt
      primaryContactName       <- primaryContactNameResult.asOpt
      primaryContactEmail      <- primaryContactEmailResult.asOpt
      secondaryContactName     <- secondaryContactNameResult.asOpt
      secondaryContactEmail    <- secondaryContactEmailResult.asOpt
      filingMemberDetails      <- filingMemberDetailsResult.asOpt
      accountingPeriod         <- accountingPeriodResult.asOpt
      accountStatus            <- accountStatusResult.asOpt
    } yield Subscription(
      domesticOrMne = if (upeDetails.domesticOnly) MneOrDomestic.Uk else MneOrDomestic.UkAndOther,
      groupDetailStatus = RowStatus.Completed,
      accountingPeriod = Some(accountingPeriod),
      contactDetailsStatus = RowStatus.Completed,
      primaryContactName = Some(primaryContactName),
      primaryContactEmail = Some(primaryContactEmail),
      secondaryContactName = Some(secondaryContactName),
      secondaryContactEmail = Some(secondaryContactEmail),
      secondaryContactTelephone = None,
      correspondenceAddress = None,
      accountStatus = Some(accountStatus),
      upeDetails = Some(upeDetails),
      upeCorrespAddressDetails = Some(upeCorrespAddressDetails),
      filingMemberDetails = Some(filingMemberDetails)
    )
  }.toRight(SubscriptionCreateError)
}
