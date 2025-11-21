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

case object DuplicateSafeIdPage extends QuestionPage[Boolean] {

  override def path: JsPath = JsPath \ toString

  override def toString: String = "DuplicateSafeId"

  override def cleanup(value: Option[Boolean], userAnswers: UserAnswers): Try[UserAnswers] =
    if value.contains(true) then {
      cleanNfmData(userAnswers)
    } else if value.contains(false) then {
      cleanNfmData(userAnswers).flatMap(_.set(NominateFilingMemberPage, false))
    } else {
      super.cleanup(value, userAnswers)
    }

  private def cleanNfmData(userAnswers: UserAnswers): Try[UserAnswers] =
    userAnswers
      .remove(FmRegisteredInUKPage)
      .flatMap(
        _.remove(FmNameRegistrationPage).flatMap(
          _.remove(FmRegisteredAddressPage).flatMap(
            _.remove(FmContactNamePage).flatMap(
              _.remove(FmContactEmailPage).flatMap(
                _.remove(FmPhonePreferencePage).flatMap(
                  _.remove(FmCapturePhonePage).flatMap(
                    _.remove(GrsFilingMemberStatusPage).flatMap(
                      _.remove(FmEntityTypePage).flatMap(
                        _.remove(FmGRSResponsePage).flatMap(
                          _.remove(FmSafeIDPage)
                        )
                      )
                    )
                  )
                )
              )
            )
          )
        )
      )

}
