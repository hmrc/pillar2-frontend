package pages

import models.NfmEntityType
import pages.behaviours.PageBehaviours

class NfmEntityTypeSpec extends PageBehaviours {

  "NfmEntityTypePage" - {

    beRetrievable[NfmEntityType](NfmEntityTypePage)

    beSettable[NfmEntityType](NfmEntityTypePage)

    beRemovable[NfmEntityType](NfmEntityTypePage)
  }
}
