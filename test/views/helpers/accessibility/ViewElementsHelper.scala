/*
 * Copyright 2025 HM Revenue & Customs
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

package views.helpers.accessibility

import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import views.helpers.accessibility
import views.helpers.accessibility.PageElement._

import java.util
import scala.collection.mutable
import scala.jdk.CollectionConverters._

object ViewElementsHelper {

  def getPageHeadings(view: Document): Elements = view.select("h1, h2, h3, h4, h5")

  def getPageElements(view: Document): Elements = view.select("title, h1, h2, h3, h4, p")

  def getPageElementsMap(view: Document): mutable.Seq[PageElement] = getPageElements(view).asScala.map { element =>
    val elementTag:     String           = element.tagName()
    val elementClasses: util.Set[String] = element.classNames()
    val elementText:    String           = element.text()

    elementTag match {
      case "title" => Title(elementText, elementClasses)
      case "h1"    => H1(elementText, elementClasses)
      case "h2"    => H2(elementText, elementClasses)
      case "h3"    => H3(elementText, elementClasses)
      case "h4"    => H4(elementText, elementClasses)
      case "h4"    => H4(elementText, elementClasses)
      case "p"     => P(elementText, elementClasses)
      case "a"     => A(elementText, elementClasses)
      case _       => throw new IllegalAccessError(s"Unsupported heading tag: $elementTag")
    }
  }

  def getPageHeadingsMap(view: Document): mutable.Seq[accessibility.Heading] = getPageHeadings(view).asScala.map { element =>
    val headingTag:     String           = element.tagName()
    val headingText:    String           = element.text()
    val headingClasses: util.Set[String] = element.classNames()

    headingTag match {
      case "h1" => H1(headingText, headingClasses)
      case "h2" => H2(headingText, headingClasses)
      case "h3" => H3(headingText, headingClasses)
      case "h4" => H4(headingText, headingClasses)
      case _    => throw new IllegalAccessError(s"Unsupported heading tag: $headingTag")
    }
  }

}
