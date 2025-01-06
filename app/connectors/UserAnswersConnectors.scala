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

package connectors

import models.{InternalIssueError, UserAnswers}
import org.apache.pekko.Done
import play.api.Logging
import play.api.http.Status._
import play.api.libs.json.{JsObject, JsValue, Json}
import uk.gov.hmrc.http.HttpReads.Implicits.readRaw
import uk.gov.hmrc.http._
import uk.gov.hmrc.http.client.HttpClientV2
import utils.FutureConverter.FutureOps

import javax.inject.{Inject, Named, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UserAnswersConnectors @Inject() (
  @Named("pillar2Url") pillar2BaseUrl: String,
  httpClient:                          HttpClientV2
)(implicit ec:                         ExecutionContext)
    extends Logging {
  private val url = s"$pillar2BaseUrl/report-pillar2-top-up-taxes"

  def save(id: String, data: JsValue)(implicit headerCarrier: HeaderCarrier): Future[JsValue] =
    httpClient
      .post(url"$url/user-cache/registration-subscription/$id")
      .withBody(Json.toJson(data))
      .execute[HttpResponse]
      .map { response =>
        response.status match {
          case OK => data
          case _  => throw new HttpException(response.body, response.status)
        }
      }

  def get(id: String)(implicit headerCarrier: HeaderCarrier): Future[Option[JsValue]] =
    httpClient
      .get(url"$url/user-cache/registration-subscription/$id")
      .execute[HttpResponse](readRaw, ec)
      .map { response =>
        response.status match {
          case OK        => Some(response.json)
          case NOT_FOUND => None
          case _         => throw new HttpException(response.body, response.status)
        }
      }

  def getUserAnswer(id: String)(implicit headerCarrier: HeaderCarrier): Future[Option[UserAnswers]] =
    httpClient
      .get(url"$url/user-cache/registration-subscription/$id")
      .execute[HttpResponse](readRaw, ec)
      .flatMap { response =>
        response.status match {
          case OK        => Future.successful(Some(UserAnswers(id = id, data = response.json.as[JsObject])))
          case NOT_FOUND => Future.successful(None)
          case _         => Future.failed(InternalIssueError)
        }
      }

  def remove(id: String)(implicit headerCarrier: HeaderCarrier): Future[Done] =
    httpClient
      .delete(url"$url/user-cache/registration-subscription/$id")
      .execute[HttpResponse]
      .flatMap(response => if (response.status == OK) Done.toFuture else Future.failed(InternalIssueError))

}
