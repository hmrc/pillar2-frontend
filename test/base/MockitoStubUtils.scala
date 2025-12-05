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

package base

import org.mockito.ArgumentMatchers.{any, argThat}
import org.mockito.Mockito.when
import org.mockito.{ArgumentMatcher, ArgumentMatchers}
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.JsValue
import play.api.test.*
import uk.gov.hmrc.http.client.RequestBuilder
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads}

import java.net.URL
import scala.concurrent.{ExecutionContext, Future}

trait MockitoStubUtils extends AnyWordSpec with SpecBase with FutureAwaits with DefaultAwaitTimeout with MockitoSugar {

  case class UrlMatcher(stringElement: String) extends ArgumentMatcher[URL] {
    def matches(url: URL): Boolean = url.getPath.contains(stringElement)
  }

  def executeGet[A]: Future[A] = {
    val mockGetRequestBuilder: RequestBuilder = mock[RequestBuilder]
    when(mockGetRequestBuilder.setHeader(any[(String, String)])).thenReturn(mockGetRequestBuilder)
    when(mockHttpClient.get(any[URL])(any[HeaderCarrier])).thenReturn(mockGetRequestBuilder)
    mockGetRequestBuilder.execute[A](any[HttpReads[A]], any[ExecutionContext])
  }

  def executeGet[A](urlElement: String): Future[A] = {
    val mockGetRequestBuilder: RequestBuilder = mock[RequestBuilder]
    when(mockGetRequestBuilder.setHeader(any[(String, String)])).thenReturn(mockGetRequestBuilder)
    when(mockHttpClient.get(argThat(UrlMatcher(urlElement)))(using any[HeaderCarrier])).thenReturn(mockGetRequestBuilder)
    mockGetRequestBuilder.execute[A](any[HttpReads[A]], any[ExecutionContext])
  }

  def executeDelete[A]: Future[A] = {
    val mockDeleteRequestBuilder: RequestBuilder = mock[RequestBuilder]
    when(mockHttpClient.delete(any[URL])(any[HeaderCarrier])).thenReturn(mockDeleteRequestBuilder)
    mockDeleteRequestBuilder.execute[A](any[HttpReads[A]], any[ExecutionContext])
  }

  def executePostNoBody[A](urlElement: Option[String] = None): Future[A] = {
    val requestBuilder: RequestBuilder = mock[RequestBuilder]
    val urlMatcher = urlElement.fold(any[URL])(elem => argThat(UrlMatcher(elem)))
    when(mockHttpClient.post(urlMatcher)(any[HeaderCarrier])).thenReturn(requestBuilder)
    when(requestBuilder.setHeader(any[(String, String)])).thenReturn(requestBuilder)
    when(requestBuilder.withBody(any())(using any(), any(), any())).thenReturn(requestBuilder)
    requestBuilder.execute[A](any[HttpReads[A]], any[ExecutionContext])
  }

  def executePost[A](body: JsValue): Future[A] = {
    val mockPostRequestBuilder: RequestBuilder = mock[RequestBuilder]
    when(mockPostRequestBuilder.setHeader(any[(String, String)])).thenReturn(mockPostRequestBuilder)
    when(mockHttpClient.post(any[URL])(any[HeaderCarrier])).thenReturn(mockPostRequestBuilder)
    when(mockPostRequestBuilder.withBody(ArgumentMatchers.eq(body))(any(), any(), any())).thenReturn(mockPostRequestBuilder)
    mockPostRequestBuilder.execute[A](any[HttpReads[A]], any[ExecutionContext])
  }

  def executePutNoBody[A]: Future[A] = {
    val mockRequestBuilder: RequestBuilder = mock[RequestBuilder]
    when(mockHttpClient.put(any[URL])(any[HeaderCarrier])).thenReturn(mockRequestBuilder)
    when(mockRequestBuilder.setHeader(any[(String, String)])).thenReturn(mockRequestBuilder)
    when(mockRequestBuilder.withBody(any())(using any(), any(), any())).thenReturn(mockRequestBuilder)
    mockRequestBuilder.execute[A](any[HttpReads[A]], any[ExecutionContext])
  }
}
