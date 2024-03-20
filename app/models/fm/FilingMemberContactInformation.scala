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

package models.fm

import models.UserAnswers
import pages.{FmCapturePhonePage, FmContactEmailPage, FmContactNamePage, FmPhonePreferencePage}

case class FilingMemberContactInformation(
  fmContactName:   Option[String],
  fmEmailAddress:  Option[String],
  telephoneNumber: Option[String]
)

object FilingMemberContactInformation {
  def buildNonUkFmContactInfo(answers: UserAnswers) =
    FilingMemberContactInformation(
      answers.get(FmContactNamePage).orElse(None),
      answers.get(FmContactEmailPage).orElse(None),
      answers.get(FmPhonePreferencePage) match {
        case Some(true) => answers.get(FmCapturePhonePage)
        case _          => None
      }
    )
}
