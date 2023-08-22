package pages

import pages.behaviours.PageBehaviours


class NFMEmailAddressPageSpec extends PageBehaviours {

  "NFMEmailAddressPage" - {

    beRetrievable[String](NFMEmailAddressPage)

    beSettable[String](NFMEmailAddressPage)

    beRemovable[String](NFMEmailAddressPage)
  }
}
