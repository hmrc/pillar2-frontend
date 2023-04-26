package forms

import forms.behaviours.OptionFieldBehaviours
import models.GroupTerritories
import play.api.data.FormError

class GroupTerritoriesFormProviderSpec extends OptionFieldBehaviours {

  val form = new GroupTerritoriesFormProvider()()

  ".value" - {

    val fieldName   = "value"
    val requiredKey = "groupTerritories.error.required"

    behave like optionsField[GroupTerritories](
      form,
      fieldName,
      validValues = GroupTerritories.values,
      invalidError = FormError(fieldName, "error.invalid")
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
