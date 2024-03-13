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

package models.grs

import models.{Enumerable, WithName}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.hint.Hint
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem

sealed trait RfmEntityType

object RfmEntityType extends Enumerable.Implicits {

  case object UkLimitedCompany extends WithName("ukLimitedCompany") with RfmEntityType
  case object LimitedLiabilityPartnership extends WithName("limitedLiabilityPartnership") with RfmEntityType
  case object EntityTypeNotListed extends WithName("entityTypeNotListed") with RfmEntityType
  case object Or extends WithName("or") with RfmEntityType

  val values: Seq[RfmEntityType] = Seq(
    UkLimitedCompany,
    LimitedLiabilityPartnership,
    Or,
    EntityTypeNotListed
  )

  def options(implicit messages: Messages): Seq[RadioItem] = values.zipWithIndex.map {
    case (Or, index) => RadioItem(divider = Some("or"))
    case (EntityTypeNotListed, index) =>
      RadioItem(
        content = Text(messages(s"rfmEntityType.${EntityTypeNotListed.toString}")),
        value = Some(EntityTypeNotListed.toString),
        id = Some(s"value_$index"),
        hint = Some(Hint(content = Text(messages(s"rfmEntityType.${EntityTypeNotListed.toString}.hint"))))
      )
    case (UkLimitedCompany, index) =>
      RadioItem(
        content = Text(messages(s"rfmEntityType.${UkLimitedCompany.toString}")),
        value = Some(UkLimitedCompany.toString),
        id = Some(s"value_$index"),
        hint = Some(Hint(content = Text(messages(s"rfmEntityType.${UkLimitedCompany.toString}.hint"))))
      )

    case (LimitedLiabilityPartnership, index) =>
      RadioItem(
        content = Text(messages(s"rfmEntityType.${LimitedLiabilityPartnership.toString}")),
        value = Some(LimitedLiabilityPartnership.toString),
        id = Some(s"value_$index")
      )
  }

  implicit val enumerable: Enumerable[RfmEntityType] =
    Enumerable(values.map(v => v.toString -> v): _*)
}
