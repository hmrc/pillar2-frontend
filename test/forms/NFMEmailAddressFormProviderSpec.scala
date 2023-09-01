package forms

import forms.behaviours.StringFieldBehaviours
import play.api.data.FormError

class NFMEmailAddressFormProviderSpec extends StringFieldBehaviours {

  val requiredKey = "nfmEmailAddress.error.required"
  val lengthKey   = "nfmEmailAddress.error.length"
  val maxLength   = 200

  val form = new NfmEmailAddressFormProvider()()

  ".value" - {

    val fieldName = "value"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      stringsWithMaxLength(maxLength)
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
