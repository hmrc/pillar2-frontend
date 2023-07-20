package pages

import pages.behaviours.PageBehaviours

class NfmNameRegistrationControllerPageSpec extends PageBehaviours {

  "NfmNameRegistrationControllerPage" - {

    beRetrievable[String](NfmNameRegistrationControllerPage)

    beSettable[String](NfmNameRegistrationControllerPage)

    beRemovable[String](NfmNameRegistrationControllerPage)
  }
}
