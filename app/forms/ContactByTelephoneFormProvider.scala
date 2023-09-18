/*
 * Copyright 2023 HM Revenue & Customs
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

package forms

import javax.inject.Inject
import forms.mappings.Mappings
import models.fm.ContactNFMByTelephone
import models.subscription.ContactByTelephone
import play.api.data.Form

class ContactByTelephoneFormProvider @Inject() extends Mappings {

  def apply(userName: String): Form[ContactByTelephone] =
    Form(
      "value" -> enumerable[ContactByTelephone]("contactByTelephone.error.required", args = Seq(userName))
    )
}
