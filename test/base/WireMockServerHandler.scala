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

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}
import play.api.libs.json.{Format, JsValue, Json}

trait WireMockServerHandler extends BeforeAndAfterAll with BeforeAndAfterEach {
  this: SpecBase =>

  protected lazy val server: WireMockServer = new WireMockServer(wireMockConfig.dynamicPort())

  override def beforeAll(): Unit = {
    server.start()
    super.beforeAll()
  }

  override def beforeEach(): Unit = {
    server.resetAll()
    super.beforeEach()
  }

  override def afterAll(): Unit = {
    super.afterAll()
    server.stop()
  }

  protected def stubResponse(expectedEndpoint: String, returnStatus: Int, returnBody: String): StubMapping =
    server.stubFor(
      post(urlEqualTo(s"$expectedEndpoint"))
        .willReturn(
          aResponse()
            .withStatus(returnStatus)
            .withBody(returnBody)
        )
    )

  protected def stubResponse[T: Format](expectedEndpoint: String, expectedBody: T)(returnStatus: Int, returnBody: String): StubMapping =
    server.stubFor(
      post(urlEqualTo(s"$expectedEndpoint"))
        .withRequestBody(equalToJson(Json.toJson(expectedBody).toString()))
        .willReturn(
          aResponse()
            .withStatus(returnStatus)
            .withBody(returnBody)
        )
    )

  protected def stubGet(
    expectedEndpoint: String,
    expectedStatus:   Int,
    expectedBody:     String = "",
    headers:          Map[String, String] = Map.empty
  ): StubMapping = {

    val requestMaybeHeaders = if headers.nonEmpty then {
      headers.foldLeft(get(urlEqualTo(expectedEndpoint))) { case (req, (key, value)) => req.withHeader(key, equalTo(value)) }
    } else get(urlEqualTo(expectedEndpoint))

    server.stubFor(
      requestMaybeHeaders
        .willReturn(
          Option(expectedBody)
            .filter(_.nonEmpty)
            .fold(aResponse().withStatus(expectedStatus))(body => aResponse().withStatus(expectedStatus).withBody(body))
        )
    )
  }

  protected def stubGetUserAnswerConnector(expectedEndpoint: String, expectedStatus: Int, expectedBody: JsValue): StubMapping =
    server.stubFor(
      get(urlEqualTo(s"$expectedEndpoint"))
        .willReturn(
          aResponse()
            .withStatus(expectedStatus)
            .withBody(expectedBody.toString())
        )
    )

  protected def stubDelete(expectedEndpoint: String, expectedStatus: Int, expectedBody: String): StubMapping =
    server.stubFor(
      delete(urlEqualTo(s"$expectedEndpoint"))
        .willReturn(
          aResponse()
            .withStatus(expectedStatus)
            .withBody(expectedBody)
        )
    )

  protected def stubResponseForPutRequest(expectedEndpoint: String, expectedStatus: Int, responseBody: Option[String] = None): StubMapping =
    server.stubFor(
      put(urlEqualTo(expectedEndpoint))
        .willReturn(
          aResponse()
            .withStatus(expectedStatus)
            .withBody(responseBody.getOrElse(""))
        )
    )
}
