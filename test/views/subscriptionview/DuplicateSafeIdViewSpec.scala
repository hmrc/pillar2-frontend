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

package views.subscriptionview

import base.ViewSpecBase
import forms.DuplicateSafeIdFormProvider
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.html.subscriptionview.DuplicateSafeIdView

class DuplicateSafeIdViewSpec extends ViewSpecBase {

  val formProvider = new DuplicateSafeIdFormProvider
  val page: DuplicateSafeIdView = inject[DuplicateSafeIdView]

  val view: Document = Jsoup.parse(page(formProvider())(request, appConfig, messages).toString())

  "Duplicate SafeId View" should {

    "have a title" in {
      view.getElementsByTag("title").text must include("There is a problem with your registration")
    }

    "have a heading" in {
      view.getElementsByTag("h1").text must include("There is a problem with your registration")
    }

    "have a paragraph body" in {
      view.getElementsByClass("govuk-body").first().text must include(
        "You indicated that the ultimate parent entity has nominated a different company within your group to act as the filing member."
      )
      view.getElementsByClass("govuk-body").get(1).text must include(
        "However, the details you provided for the nominated filing member are the same as those for the ultimate parent entity."
      )
      view.getElementsByClass("govuk-body").get(2).text must include("Before submitting your registration, you must either:")
      view.getElementsByTag("li").get(0).text must include(
        "provide the details of the company that will act as your nominated filing member"
      )
      view.getElementsByTag("li").get(1).text must include("keep your ultimate parent entity as the default filing member")
    }

    "has legend" in {
      view.getElementsByClass("govuk-fieldset__legend").get(0).text must include(
        "Has a different company in your group been nominated to act as your filing member?"
      )
    }

    "have radio items" in {
      view.getElementsByClass("govuk-label govuk-radios__label").get(0).text must include("Yes")
      view.getElementsByClass("govuk-label govuk-radios__label").get(1).text must include("No")
    }

    "have a button" in {
      view.getElementsByClass("govuk-button").text must include("Save and continue")
    }
  }
}
