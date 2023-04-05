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

import config.FrontendAppConfig
import org.mockito.Mockito
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.http.HttpClient
import uk.gov.hmrc.play.audit.http.connector.AuditConnector

//TODO: Add all mocking instants in here.
trait AllMocks extends MockitoSugar { me: BeforeAndAfterEach =>

  val mockAuditConnector:    AuditConnector    = mock[AuditConnector]
  val mockAuthConnector:     AuthConnector     = mock[AuthConnector]
  val mockFrontendAppConfig: FrontendAppConfig = mock[FrontendAppConfig]

  override protected def beforeEach(): Unit =
    Seq(
      mockAuditConnector,
      mockAuthConnector,
      mockFrontendAppConfig
    ).foreach(Mockito.reset(_))
}
