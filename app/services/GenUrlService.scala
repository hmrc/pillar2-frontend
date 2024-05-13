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

package services

import config.FrontendAppConfig

import javax.inject.Inject
class GenUrlService @Inject() (val appConfig: FrontendAppConfig) {

  def generateUrl(url: String, authorised: Boolean, plrReference: String = ""): Option[String] =
    (
      url.contains("/asa/"),
      authorised,
      url.contains("/replace-filing-member"),
      url.contains("clientPillar2Id"),
      url.contains("agentView=true")
    ) match {
      case (false, true, false, true, true) =>
        Some(controllers.routes.DashboardController.onPageLoad(Some(plrReference), true).url)
      case (_, _, true, _, _)         => None
      case (true, true, false, _, _)  => Some(appConfig.asaHomePageUrl)
      case (false, true, false, _, _) => Some(controllers.routes.IndexController.onPageLoad.url)
      case _                          => Some(appConfig.startPagePillar2Url)
    }
}
