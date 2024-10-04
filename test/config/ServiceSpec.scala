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

package config

import base.SpecBase
import play.api.{ConfigLoader, Configuration}

class ServiceSpec extends SpecBase {

  val config: Configuration = Configuration(
    "microservice.services.test-service.host"     -> "localhost",
    "microservice.services.test-service.port"     -> "8080",
    "microservice.services.test-service.protocol" -> "http"
  )

  "Service" when {

    "constructed via the ConfigLoader" must {

      "return the correct host, port, and protocol" in {
        val service: Service = config.get[Service]("microservice.services.test-service")
        service.host mustBe "localhost"
        service.port mustBe "8080"
        service.protocol mustBe "http"
      }

      "return the correct base URL" in {
        val service: Service = config.get[Service]("microservice.services.test-service")
        service.baseUrl mustBe "http://localhost:8080"
      }
    }

    "converted to String" must {

      "return the baseUrl when called" in {
        val service: Service = Service("localhost", "8080", "http")
        service.toString mustBe "http://localhost:8080"
      }
    }

    "convertToString" should {
      "return the baseUrl of the service" in {
        val service = Service("localhost", "9000", "http")
        val baseUrl: String = service
        baseUrl mustBe "http://localhost:9000"
      }
    }
  }
}
