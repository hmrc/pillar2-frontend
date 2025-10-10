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

package views.btn

import base.ViewSpecBase
import org.jsoup.Jsoup
import views.html.btn.BTNUnderEnquiryWarningView

class BTNUnderEnquiryWarningViewSpec extends ViewSpecBase {

  "BTNUnderEnquiryWarningView" must {

    "render the correct content" in {

      val view = app.injector.instanceOf[BTNUnderEnquiryWarningView]

      val result = view()(request, appConfig, messages)

      val doc = Jsoup.parse(result.toString)

      doc.text() must include(messages("btn.underEnquiryWarning.heading"))
      doc.text() must include(messages("btn.underEnquiryWarning.p1"))
      doc.text() must include(messages("btn.underEnquiryWarning.p2"))
      doc.text() must include(messages("btn.underEnquiryWarning.continue"))
    }
  }
}
