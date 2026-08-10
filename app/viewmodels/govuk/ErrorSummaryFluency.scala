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

import play.api.data.Form
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.{Content, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.errorsummary.{ErrorLink, ErrorSummary}

object errorsummary extends ErrorSummaryFluency

trait ErrorSummaryFluency {

  def firstErrorField(form: Form[?], key: String): Option[String] =
    form.error(key).flatMap { error =>
      List("day", "month", "year").find(error.args.contains)
    }

  object ErrorSummaryViewModel {

    def apply(
      form:                   Form[?],
      errorLinkOverrides:     Map[String, String] = Map.empty,
      errorMessageOverrides:  Map[String, String] = Map.empty,
      deduplicateMessageKeys: Set[String] = Set.empty
    )(using messages: Messages): ErrorSummary = {

      val errors = form.errors
        .foldLeft((Set.empty[String], Seq.empty[ErrorLink])) { case ((seenMessages, errorLinks), error) =>
          val messageKey = errorMessageOverrides.getOrElse(error.message, error.message)
          val message    = messages(messageKey, error.args*)
          val errorLink  = ErrorLink(
            href = Some(s"#${errorLinkOverrides.getOrElse(error.key, error.key)}"),
            content = HtmlContent(message)
          )

          if deduplicateMessageKeys.contains(error.message) && seenMessages.contains(message) then {
            (seenMessages, errorLinks)
          } else {
            (seenMessages + message, errorLinks :+ errorLink)
          }
        }
        ._2

      ErrorSummary(
        errorList = errors,
        title = Text(messages("error.summary.title"))
      )
    }
  }

  extension (errorSummary: ErrorSummary) {

    def withDescription(description: Content): ErrorSummary =
      errorSummary.copy(description = description)

    def withCssClass(newClass: String): ErrorSummary =
      errorSummary.copy(classes = s"${errorSummary.classes} $newClass")

    def withAttribute(attribute: (String, String)): ErrorSummary =
      errorSummary.copy(attributes = errorSummary.attributes + attribute)
  }
}
