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

package services

import config.FrontendAppConfig
import models.UserAnswers
import pages.PlrReferencePage
import uk.gov.hmrc.auth.core.Enrolment
import utils.Pillar2Reference

import javax.inject.Inject

class ReferenceNumberService @Inject() (appConfig: FrontendAppConfig) {

  def get(userAnswers: Option[UserAnswers], enrolments: Option[Set[Enrolment]]): Option[String] =
    Pillar2Reference
      .getPillar2ID(enrolments, appConfig.enrolmentKey, appConfig.enrolmentIdentifier)
      .orElse(userAnswers.flatMap(_.get(PlrReferencePage)))
}
