/*
 * Copyright 2025 HM Revenue & Customs
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
import pages._
import play.api.libs.json.Json

import java.security.MessageDigest

object SectionHash {
  private def sha256Hex(value: String): String = {
    val d = MessageDigest.getInstance("SHA-256").digest(value.getBytes("UTF-8"))
    d.map("%02x".format(_)).mkString
  }

  def computeUpeHash(ua: UserAnswers): String = {
    val snapshot = Json
      .obj(
        "registeredInUK" -> ua.get(UpeRegisteredInUKPage),
        "name"           -> ua.get(UpeNameRegistrationPage),
        "address"        -> ua.get(UpeRegisteredAddressPage),
        "contactName"    -> ua.get(UpeContactNamePage),
        "contactEmail"   -> ua.get(UpeContactEmailPage),
        "phonePref"      -> ua.get(UpePhonePreferencePage),
        "phone"          -> ua.get(UpeCapturePhonePage),
        "entityType"     -> ua.get(UpeEntityTypePage),
        "grsResp"        -> ua.get(UpeGRSResponsePage),
        "grsStatus"      -> ua.get(GrsUpeStatusPage)
      )
      .toString

    sha256Hex(snapshot)
  }
}
