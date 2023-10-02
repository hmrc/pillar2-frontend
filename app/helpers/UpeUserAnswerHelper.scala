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

package helpers

import models.UserAnswers
import pages.RegistrationPage
import utils.RowStatus

trait UpeUserAnswerHelper {

  self: UserAnswers =>

  def upeContactName: String =
    get(RegistrationPage)
      .flatMap { reg =>
        reg.withoutIdRegData.flatMap(withoutID => withoutID.upeContactName)
      }
      .getOrElse("")

  def upeNameRegistration: String =
    get(RegistrationPage)
      .flatMap { reg =>
        reg.withoutIdRegData.map(withoutID => withoutID.upeNameRegistration)
      }
      .getOrElse("")

  def upeGRSBookmarkLogic: Option[Boolean] =
    get(RegistrationPage).flatMap { reg =>
      if (reg.isUPERegisteredInUK & reg.withoutIdRegData.isEmpty & reg.isRegistrationStatus == RowStatus.InProgress) {
        Some(true)
      } else {
        None
      }
    }

  def upeNoIDBookmarkLogic: Option[Boolean] =
    get(RegistrationPage).flatMap { reg =>
      if (!reg.isUPERegisteredInUK & reg.withIdRegData.isEmpty & reg.orgType.isEmpty) {
        Some(true)
      } else {
        None
      }
    }

}
