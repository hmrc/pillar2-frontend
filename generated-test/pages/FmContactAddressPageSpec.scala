package pages

import pages.behaviours.PageBehaviours


class FmContactAddressPageSpec extends PageBehaviours {

  "FmContactAddressPage" - {

    beRetrievable[String](FmContactAddressPage)

    beSettable[String](FmContactAddressPage)

    beRemovable[String](FmContactAddressPage)
  }
}
