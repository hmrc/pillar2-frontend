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

package views.rfm

import base.ViewSpecBase
import forms.RfmSecurityCheckFormProvider
import models.NormalMode
import org.jsoup.Jsoup
import views.html.rfm.SecurityCheckView

class SecurityCheckViewSpec extends ViewSpecBase {

  val formProvider = new RfmSecurityCheckFormProvider
  val page         = inject[SecurityCheckView]

  val view = Jsoup.parse(page(formProvider(), NormalMode)(request, appConfig, messages).toString())

  "Security Check View" should {

    "have a title" in {
      view.getElementsByTag("title").text must include("Enter the group’s Pillar 2 top-up taxes ID")
    }

    "have a caption" in {
      view.getElementsByClass("govuk-caption-l").text must include("Replace filing member")
    }

    "have a heading" in {
      view.getElementsByTag("h1").text must include("Enter the group’s Pillar 2 top-up taxes ID")
    }

    "have a hint description" in {
      view.getElementsByClass("govuk-hint").get(0).text must include(
        "This is 15 characters, for example, " +
          "XMPLR0123456789. The current filing member can find it within their Pillar 2 top-up taxes account."
      )
    }

    "have a button" in {
      view.getElementsByClass("govuk-button").text must include("Continue")
    }
  }
}
