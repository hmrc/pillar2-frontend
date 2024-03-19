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

sealed trait EntityType

object EntityType extends Enumerable.Implicits {

  case object UkLimitedCompany extends WithName("ukLimitedCompany") with EntityType
  case object LimitedLiabilityPartnership extends WithName("limitedLiabilityPartnership") with EntityType
  case object Other extends WithName("companyTypeNotListed") with EntityType

  val values: Seq[EntityType] = Seq(
    UkLimitedCompany,
    LimitedLiabilityPartnership,
    Other
  )

  def options(implicit messages: Messages): Seq[RadioItem] =
    Seq(
      RadioItem(
        content = Text(messages(s"entityType.${UkLimitedCompany.toString}")),
        value = Some(UkLimitedCompany.toString),
        id = Some(s"value_0"),
        hint = Some(Hint(content = Text(messages(s"entityType.hint.${UkLimitedCompany.toString}")), classes = "govuk-hint govuk-radios__hint"))
      ),
      RadioItem(
        content = Text(messages(s"entityType.${LimitedLiabilityPartnership.toString}")),
        value = Some(LimitedLiabilityPartnership.toString),
        id = Some(s"value_1")
      ),
      RadioItem(divider = Some("or")),
      RadioItem(
        content = Text(messages(s"entityType.${Other.toString}")),
        value = Some(Other.toString),
        id = Some(s"value_2"),
        hint = Some(Hint(content = Text(messages(s"entityType.hint.${Other.toString}")), classes = "govuk-hint govuk-radios__hint"))
      )
    )

  implicit val enumerable: Enumerable[EntityType] =
    Enumerable(values.map(v => v.toString -> v): _*)
}
