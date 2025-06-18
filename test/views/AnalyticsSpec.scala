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

package views

import base.ViewSpecBase
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.mvc.{AnyContentAsEmpty, Cookie}
import play.api.test.FakeRequest
import play.api.test.Helpers.GET
import views.html.{DashboardView, UnauthorisedView}

class AnalyticsSpec extends ViewSpecBase {

  val unauthorisedViewPage: UnauthorisedView = inject[UnauthorisedView]
  val authorisedViewPage:   DashboardView    = inject[DashboardView]
  val analyticsTrackingId:  String           = appConfig.googleAnalyticsTrackingId

  val organisationName: String = "Some Org name"
  val plrReference:     String = "XMPLR0012345678"

  val requestWithoutTrackingConsent: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, "/").withCookies(Cookie("userConsent", "false"))
  val requestWithTrackingConsent:    FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, "/").withCookies(Cookie("userConsent", "true"))

  "The unauthorised view" should {
    "not have the Google Analytics JavaScript in the source code, if user has not given consent" in {
      val unauthorisedView: Document = Jsoup.parse(unauthorisedViewPage()(requestWithoutTrackingConsent, appConfig, messages).toString())
      unauthorisedView.head.select("script").toString.contains(analyticsTrackingId) mustBe false
    }

    "have the Google Analytics JavaScript in the source code, if user has given consent" in {
      val unauthorisedView: Document = Jsoup.parse(unauthorisedViewPage()(requestWithTrackingConsent, appConfig, messages).toString())
      unauthorisedView.head.select("script").toString.contains(analyticsTrackingId) mustBe true
    }
  }

  "The authorised view" should {
    "not have the Google Analytics JavaScript in the source code, if user has not given consent" in {
      val authorisedView: Document = Jsoup.parse(
        authorisedViewPage(organisationName, registrationDate.toString, plrReference, false, true)(requestWithoutTrackingConsent, appConfig, messages)
          .toString()
      )
      authorisedView.head.select("script").toString.contains(analyticsTrackingId) mustBe false
    }

    "have the Google Analytics JavaScript in the source code, if user has given consent" in {
      val authorisedView: Document = Jsoup.parse(
        authorisedViewPage(organisationName, registrationDate.toString, plrReference, false, true)(requestWithTrackingConsent, appConfig, messages)
          .toString()
      )
      authorisedView.head.select("script").toString.contains(analyticsTrackingId) mustBe true
    }
  }

}
