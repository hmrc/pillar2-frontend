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

package stubsonly.connectors

import config.FrontendAppConfig
import play.api.Logger
import play.api.libs.json.JsValue
import uk.gov.hmrc.http.HttpReads.Implicits.readRaw
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpResponse}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class TestOnlyConnector @Inject() (
  appConfig:  FrontendAppConfig,
  httpClient: HttpClient
)(implicit
  val ec: ExecutionContext
) {

  private val logger = Logger(getClass)

  private val pillar2Url: String =
    s"${appConfig.pillar2BaseUrl}/report-pillar2-top-up-taxes/test-only"

  def clearAllData()(implicit hc: HeaderCarrier): Future[HttpResponse] =
    httpClient.GET(
      s"$pillar2Url/clear-all"
    )

  def clearCurrentData(id: String)(implicit hc: HeaderCarrier): Future[HttpResponse] =
    httpClient.GET(
      s"$pillar2Url/clear-current/$id"
    )

  def getAllRecords()(implicit hc: HeaderCarrier): Future[HttpResponse] =
    httpClient.GET(
      s"$pillar2Url/get-all"
    )

  def upsertRecord(id: String, data: JsValue)(implicit hc: HeaderCarrier): Future[Unit] =
    httpClient
      .POST[JsValue, HttpResponse](s"$pillar2Url/upsertRecord/$id", data)
      .map { response =>
        response.status match {
          case 200 => // OK
            ()
          case 201 => // Created
            ()
          case status @ (400 | 404) =>
            val errorMessage = s"Error status: $status, body: ${response.body}"
            logger.error(errorMessage)
            throw new RuntimeException(errorMessage)
          case otherStatus =>
            val errorMessage = s"Unexpected response status: $otherStatus, body: ${response.body}"
            logger.error(errorMessage)
            throw new RuntimeException(errorMessage)
        }
      }

  def deEnrol(groupId: String, pillar2Reference: String)(implicit hc: HeaderCarrier): Future[HttpResponse] =
    httpClient.GET(
      s"$pillar2Url/de-enrol/$groupId/$pillar2Reference"
    )

}
