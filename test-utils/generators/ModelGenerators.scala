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

package generators

import models._
import models.grs.{EntityType, RfmEntityType}
import models.subscription.DashboardInfo
import org.scalacheck.{Arbitrary, Gen}

trait ModelGenerators {

  implicit lazy val arbitraryRfmEntityTypeController: Arbitrary[RfmEntityType] =
    Arbitrary {
      Gen.oneOf(RfmEntityType.values.toSeq)
    }

  implicit lazy val arbitraryMneOrDomestic: Arbitrary[MneOrDomestic] =
    Arbitrary {
      Gen.oneOf(MneOrDomestic.values.toSeq)
    }

  implicit lazy val arbitraryEntityType: Arbitrary[EntityType] =
    Arbitrary {
      Gen.oneOf(EntityType.values.toSeq)
    }

}
