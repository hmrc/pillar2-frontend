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

package views.registration

import base.ViewSpecBase
import forms.RegisteringNfmForThisGroupFormProvider
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.html.registrationview.RegisteringNfmForThisGroupView

class RegisteringNfmForThisGroupViewSpec extends ViewSpecBase {

  lazy val formProvider: RegisteringNfmForThisGroupFormProvider = new RegisteringNfmForThisGroupFormProvider
  lazy val page:         RegisteringNfmForThisGroupView         = inject[RegisteringNfmForThisGroupView]
  lazy val view:         Document                               = Jsoup.parse(page(formProvider())(request, appConfig, messages).toString())
  lazy val viewWithErrors: Document =
    Jsoup.parse(page(formProvider().bind(Map("registeringNfmGroup" -> "")))(request, appConfig, messages).toString())
  lazy val pageTitle: String = "Are you registering as the group’s nominated filing member?"

  "Registering Nfm for this group view" should {
    "have a title" in {
      view.title() mustBe s"$pageTitle - Report Pillar 2 Top-up Taxes - GOV.UK"
    }

    // FIXME: this test is not properly testing H1 - it is testing some classes
    "have a unique H1 heading" in {
      view.getElementsByClass("govuk-caption-l hmrc-caption-l").text() must
        be("Check if you need to report Pillar 2 Top-up Taxes")
    }

    "have a hint" in {
      view.getElementsByClass("govuk-hint").text() must
        be("The nominated filing member is responsible for managing the group’s Pillar 2 Top-up Taxes returns and keeping business records.")
    }

    "have Yes/No radio buttons" in {
      val radioButtons = view.getElementsByClass("govuk-radios").first().children()

      radioButtons.size()        must be(2)
      radioButtons.get(0).text() must be("Yes")
      radioButtons.get(1).text() must be("No")
    }

    "have a continue button" in {
      view.getElementsByClass("govuk-button").text() must be("Continue")
    }
  }

  "Registering Nfm for this group view when clicking continue with missing values" should {
    "have an error summary" in {
      viewWithErrors.getElementsByClass("govuk-error-summary").text() must
        include("Select yes if you are registering as the group’s nominated filing member");

      viewWithErrors.getElementsByClass("govuk-form-group govuk-form-group--error").text() must
        include("Select yes if you are registering as the group’s nominated filing member")
    }
  }
}
