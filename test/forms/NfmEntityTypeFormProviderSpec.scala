package forms

import forms.behaviours.OptionFieldBehaviours
import models.NfmEntityType
import play.api.data.FormError

class NfmEntityTypeFormProviderSpec extends OptionFieldBehaviours {

  val form = new NfmEntityTypeFormProvider()()

  ".value" - {

    val fieldName   = "value"
    val requiredKey = "nfmEntityType.error.required"

    behave like optionsField[NfmEntityType](
      form,
      fieldName,
      validValues = NfmEntityType.values,
      invalidError = FormError(fieldName, "error.invalid")
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
