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

package models.subscription

import models.{MneOrDomestic, NonUKAddress, RichJsObject}
import pages.QuestionPage
import play.api.libs.json._
import queries.{Gettable, Settable}

import scala.util.{Failure, Success, Try}

case class SubscriptionLocalData(
  plrReference:                String,
  subMneOrDomestic:            MneOrDomestic,
  subAccountingPeriod:         AccountingPeriod,
  subPrimaryContactName:       String,
  subPrimaryEmail:             String,
  subPrimaryPhonePreference:   Boolean,
  subPrimaryCapturePhone:      Option[String],
  subAddSecondaryContact:      Boolean,
  subSecondaryContactName:     Option[String],
  subSecondaryEmail:           Option[String],
  subSecondaryCapturePhone:    Option[String],
  subSecondaryPhonePreference: Option[Boolean],
  subRegisteredAddress:        NonUKAddress,
  accountStatus:               Option[AccountStatus],
  organisationName:            Option[String]
) {

  private lazy val jsObj = Json.toJsObject(this)
  def get[A](page: Gettable[A])(implicit rds: Reads[A]): Option[A] =
    Reads.optionNoError(Reads.at(page.path)).reads(jsObj).getOrElse(None)

  def set[A](page: Settable[A], value: A)(implicit writes: Writes[A]): Try[SubscriptionLocalData] = {
    val updatedData = jsObj.setObject(page.path, Json.toJson(value)) match {
      case JsSuccess(jsValue, _) =>
        Success(jsValue)
      case JsError(errors) =>
        Failure(JsResultException(errors))
    }

    updatedData.map(_.as[SubscriptionLocalData])
  }
  def setOrException[A](page: QuestionPage[A], value: A)(implicit writes: Writes[A]): SubscriptionLocalData =
    set(page, value) match {
      case Success(ua) => ua
      case Failure(ex) => throw ex
    }
  def remove[A](page: Settable[A]): Try[SubscriptionLocalData] = {
    val updatedData = jsObj.removeObject(page.path) match {
      case JsSuccess(jsValue, _) =>
        Success(jsValue)
      case JsError(e) =>
        Failure(JsResultException(e))
    }
    updatedData.map(_.as[SubscriptionLocalData])
  }

}

object SubscriptionLocalData {
  implicit val format: OFormat[SubscriptionLocalData] = Json.format[SubscriptionLocalData]
}
