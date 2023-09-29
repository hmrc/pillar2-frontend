package pages

import pages.behaviours.PageBehaviours

class SubscriptionAddressPageSpec extends PageBehaviours {

  "SubscriptionAddressPage" - {

    beRetrievable[String](SubscriptionAddressPage)

    beSettable[String](SubscriptionAddressPage)

    beRemovable[String](SubscriptionAddressPage)
  }
}
