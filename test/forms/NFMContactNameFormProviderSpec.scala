package forms

import forms.behaviours.StringFieldBehaviours
import play.api.data.FormError

class NFMContactNameFormProviderSpec extends StringFieldBehaviours {

  val requiredKey = "nFMContactName.error.required"
  val lengthKey   = "nFMContactName.error.length"
  val maxLength   = NFM

  val form = new NFMContactNameFormProvider()()

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
