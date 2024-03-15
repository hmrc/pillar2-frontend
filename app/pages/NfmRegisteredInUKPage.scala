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
import play.api.libs.json.JsPath

import scala.util.Try

case object NfmRegisteredInUKPage extends QuestionPage[Boolean] {

  override def path: JsPath = JsPath \ toString

  override def toString: String = "nfmRegisteredInUK"

  override def cleanup(value: Option[Boolean], userAnswers: UserAnswers): Try[UserAnswers] =
    if (value.contains(true)) {
      userAnswers
        .remove(NfmNameRegistrationPage)
        .flatMap(
          _.remove(NfmRegisteredAddressPage).flatMap(
            _.remove(NfmContactNamePage).flatMap(
              _.remove(NfmContactEmailPage).flatMap(
                _.remove(NfmPhonePreferencePage).flatMap(
                  _.remove(NfmCapturePhonePage)
                )
              )
            )
          )
        )
    } else if (value.contains(false)) {
      userAnswers
        .remove(GrsNfmStatusPage)
        .flatMap(
          _.remove(NfmEntityTypePage).flatMap(
            _.remove(NfmGRSResponsePage)
          )
        )
    } else {
      super.cleanup(value, userAnswers)
    }
}
