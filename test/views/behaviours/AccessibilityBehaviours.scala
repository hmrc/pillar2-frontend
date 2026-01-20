/*
 * Copyright 2026 HM Revenue & Customs
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

package views.behaviours

import org.jsoup.select.Elements
import org.scalatest.matchers.must.Matchers.{be, contain, include, must, mustBe}
import org.scalatest.wordspec.AnyWordSpec

import scala.jdk.CollectionConverters.*

trait AccessibilityBehaviours extends AnyWordSpec {

  def behaveLikeAccessiblePage(scenarios: Seq[ViewScenario], requireTitleAndH1Tests: Boolean = true): Unit =
    scenarios.map { scenario =>
      val doc = scenario.doc
      val serviceName: String = " - Report Pillar 2 Top-up Taxes - GOV.UK"
      val title   = doc.title().trim
      val heading = doc.getElementsByTag("h1").text().trim

      s"meet fundamental accessibility requirements for ${scenario.viewName}" must {

        if requireTitleAndH1Tests then {
          "have exactly one H1" in {
            val numberOfH1Tags: Elements = doc.getElementsByTag("h1")

            numberOfH1Tags.size mustBe 1
          }

          "have service name in the title" in {
            title must include(serviceName)
          }

          "have a matching title and H1" in {
            val titleWithoutServiceName: String = doc.title.stripSuffix(serviceName).trim

            titleWithoutServiceName.equals(heading)
          }
        }

        "have correctly nested headings" in {
          val headings =
            doc.select("h1, h2, h3").asScala.filterNot(h => h.hasClass("govuk-caption-l") || h.hasClass("govuk-notification-banner__title")).toList
          headings.nonEmpty mustBe true

          val levels = headings.map(h => h.tagName().drop(1).toInt)
          levels.zip(levels.drop(1)).foreach { case (previous, current) =>
            (current - previous) must be <= 1
          }
        }

        "use <strong> instead of <b>" in {
          val bTags: Elements = doc.getElementsByTag("b")

          bTags.size mustBe 0
        }

        "have an href attribute inside <a> tags" in {
          val linksWithoutHref = doc.getElementsByTag("a").asScala.filter(a => !a.hasAttr("href") || a.attr("href").isEmpty)

          linksWithoutHref.size mustBe 0
        }

        "have appropriate labels for inputs" in {
          val labelledIds = doc.getElementsByTag("label").asScala.filter(_.hasAttr("for")).map(_.attr("for")).toSet

          val inputs = doc.getElementsByTag("input").asScala.filterNot(_.attr("type").equalsIgnoreCase("hidden"))

          val inputsWithoutLabel = inputs.filterNot { input =>
            input.id().nonEmpty && labelledIds.contains(input.id())
          }
          inputsWithoutLabel.isEmpty mustBe true
        }
      }
    }
}
