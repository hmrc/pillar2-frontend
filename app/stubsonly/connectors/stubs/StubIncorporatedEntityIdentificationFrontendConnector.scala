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

package stubsonly.connectors.stubs

import connectors.IncorporatedEntityIdentificationFrontendConnector
import models.Mode
import models.grs.OrgType.UkLimitedCompany
import models.grs.GrsCreateRegistrationResponse
import models.registration.IncorporatedEntityRegistrationData
import play.api.libs.json.Json
import stubsonly.utils.Base64Utils
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.Future

class StubIncorporatedEntityIdentificationFrontendConnector @Inject() () extends IncorporatedEntityIdentificationFrontendConnector {

  override def createLimitedCompanyJourney(mode: Mode)(implicit hc: HeaderCarrier): Future[GrsCreateRegistrationResponse] =
    Future.successful(
      GrsCreateRegistrationResponse(
        journeyStartUrl =
          s"/pillar-two/test-only/stub-grs-journey-data?continueUrl=${mode.toString.toLowerCase}&entityType=${UkLimitedCompany.toString}"
      )
    )

  override def getJourneyData(journeyId: String)(implicit hc: HeaderCarrier): Future[IncorporatedEntityRegistrationData] =
    Future.successful(Json.parse(Base64Utils.base64UrlDecode(journeyId)).as[IncorporatedEntityRegistrationData])

}
