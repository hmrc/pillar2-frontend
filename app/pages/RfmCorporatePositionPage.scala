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

package pages

import models.UserAnswers
import models.rfm.CorporatePosition
import play.api.libs.json.JsPath

import scala.util.Try

case object RfmCorporatePositionPage extends QuestionPage[CorporatePosition] {

  override def path: JsPath = JsPath \ toString

  override def toString: String = "rfmCorporatePosition"

  override def cleanup(value: Option[CorporatePosition], userAnswers: UserAnswers): Try[UserAnswers] =
    value match {
      case Some(CorporatePosition.NewNfm) =>
        super.cleanup(value, userAnswers)

      case Some(CorporatePosition.Upe) =>
        userAnswers
          .remove(RfmUkBasedPage)
          .flatMap(
            _.remove(RfmNameRegistrationPage).flatMap(
              _.remove(RfmRegisteredAddressPage).flatMap(
                _.remove(RfmEntityTypePage).flatMap(
                  _.remove(RfmGrsDataPage).flatMap(
                    _.remove(RfmGRSUkLimitedPage) flatMap (
                      _.remove(RfmGRSUkPartnershipPage)
                    )
                  )
                )
              )
            )
          )
    }
}
