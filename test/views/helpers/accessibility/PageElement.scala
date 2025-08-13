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

import java.util

trait PageElement {
  def text:    String
  def classes: util.Set[String]
}

object PageElement {
  case class Title(
    text:    String,
    classes: util.Set[String]
  ) extends PageElement

  case class H1(
    text:    String,
    classes: util.Set[String]
  ) extends PageElement
      with Heading

  case class H2(
    text:    String,
    classes: util.Set[String]
  ) extends PageElement
      with Heading

  case class H3(
    text:    String,
    classes: util.Set[String]
  ) extends PageElement
      with Heading

  case class H4(
    text:    String,
    classes: util.Set[String]
  ) extends PageElement
      with Heading

  case class P(
    text:    String,
    classes: util.Set[String]
  ) extends PageElement

  case class A(
    text:    String,
    classes: util.Set[String]
  ) extends PageElement

}
