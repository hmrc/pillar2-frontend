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

package cache

import play.api.mvc.{AnyContent, Request, Session}
import utils.Pillar2SessionKeys

import javax.inject.Singleton

@Singleton
class SessionData {
  def updateBusinessActivityUKYesNo(value: String)(implicit request: Request[AnyContent]): Session =
    request.session +
      (Pillar2SessionKeys.businessActivityUKPageYesNo -> value)

  def updateTurnOverEligibilitySessionData(value: String)(implicit request: Request[AnyContent]): Session =
    request.session +
      (Pillar2SessionKeys.turnOverEligibilityValue -> value)
  def updateGroupTerritoriesYesNo(value: String)(implicit request: Request[AnyContent]): Session =
    request.session +
      (Pillar2SessionKeys.groupTerritoriesPageYesNo -> value)

  def registeringNfmForThisGroup(value: String)(implicit request: Request[AnyContent]): Session =
    request.session +
      (Pillar2SessionKeys.registeringNfmForThisGroup -> value)

  def updateMneOrDomestic(value: String)(implicit request: Request[AnyContent]): Session =
    request.session +
      (Pillar2SessionKeys.updateMneOrDomestic -> value)

}