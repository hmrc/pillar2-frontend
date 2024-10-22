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

package viewmodels.govuk

import play.twirl.api.Html
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.{Content, HtmlContent}
import uk.gov.hmrc.govukfrontend.views.viewmodels.fieldset.{Fieldset, Legend}
import viewmodels.LegendSize

object FieldsetFluency extends FieldsetFluency

trait FieldsetFluency {

  object FieldsetViewModel {

    def apply(legend: Legend): Fieldset =
      Fieldset(legend = Some(legend))
  }

  implicit class FluentFieldset(fieldset: Fieldset) {

    def describedBy(value: String): Fieldset =
      fieldset copy (describedBy = Some(value))

    def withCssClass(newClass: String): Fieldset =
      fieldset copy (classes = s"${fieldset.classes} $newClass")

    def withRole(role: String): Fieldset =
      fieldset copy (role = Some(role))

    def withAttribute(attribute: (String, String)): Fieldset =
      fieldset copy (attributes = fieldset.attributes + attribute)

    def withHtml(html: Html): Fieldset =
      fieldset copy (html = html)
  }

  object LegendViewModel {

    def apply(content: Content): Legend =
      Legend(content = content)
  }

  implicit class FluentLegend(legend: Legend) {

    // New version that allows for backward compatibility with an optional headingLevel parameter.
    def asPageHeading(size: LegendSize = LegendSize.ExtraLarge, headingLevel: Option[String] = None): Legend = {
      // Determine the default heading level based on size or allow overriding via headingLevel parameter.
      val level = headingLevel.getOrElse("h1")

      // Keep the old behavior as the default.
      val cssClass = size match {
        case LegendSize.ExtraLarge => "govuk-fieldset__legend--xl"
        case LegendSize.Large      => "govuk-fieldset__legend--l"
        case LegendSize.Medium     => "govuk-fieldset__legend--m"
        case LegendSize.Small      => "govuk-fieldset__legend--s"
      }

      legend.copy(
        isPageHeading = level == "h1",
        content = HtmlContent(
          s"""<$level class="govuk-fieldset__heading">${legend.content.asHtml}</$level>"""
        ),
        classes = cssClass
      )
    }

    def withCssClass(newClass: String): Legend =
      legend.copy(classes = s"${legend.classes} $newClass")
  }
}
