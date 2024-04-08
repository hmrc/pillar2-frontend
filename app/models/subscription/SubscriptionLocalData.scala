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

import akka.Done
import models.{MneOrDomestic, NonUKAddress, RichJsObject}
import pages._
import play.api.libs.json._
import queries.{Gettable, Settable}

import java.time.LocalDate
import scala.util.{Failure, Success, Try}

case class SubscriptionLocalData(
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
  subRegisteredAddress:        NonUKAddress
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

  def remove[A](page: Settable[A]): Try[SubscriptionLocalData] = {
    val updatedData = jsObj.removeObject(page.path) match {
      case JsSuccess(jsValue, _) =>
        Success(jsValue)
      case JsError(e) =>
        Failure(JsResultException(e))
    }
    updatedData.map(_.as[SubscriptionLocalData])
  }

  def setOrException[A](page: QuestionPage[A], value: A)(implicit writes: Writes[A]): SubscriptionLocalData =
    set(page, value) match {
      case Success(ua) => ua
      case Failure(ex) => throw ex
    }

  def manageContactDetailStatus: Boolean = {
    val p1  = get(SubPrimaryContactNamePage).isDefined
    val p2  = get(SubPrimaryEmailPage).isDefined
    val p3  = get(SubPrimaryPhonePreferencePage).isDefined
    val p4  = get(SubAddSecondaryContactPage).getOrElse(false)
    val s1  = get(SubSecondaryContactNamePage).isDefined
    val s2  = get(SubSecondaryEmailPage).isDefined
    val s3  = get(SubSecondaryPhonePreferencePage).isDefined
    val ad1 = get(SubRegisteredAddressPage).isDefined
    (p1, p2, p3, p4, s1, s2, s3, ad1) match {
      case (true, true, true, true, true, true, true, true) => true
      case (true, true, true, false, _, _, _, true)         => true
      case _                                                => false
    }
  }
}

object SubscriptionLocalData {
  implicit val format: OFormat[SubscriptionLocalData] = Json.format[SubscriptionLocalData]
}
