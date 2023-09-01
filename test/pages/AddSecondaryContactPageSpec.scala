package pages

import pages.behaviours.PageBehaviours

class AddSecondaryContactPageSpec extends PageBehaviours {

  "AddSecondaryContactPage" - {

    beRetrievable[Boolean](AddSecondaryContactPage)

    beSettable[Boolean](AddSecondaryContactPage)

    beRemovable[Boolean](AddSecondaryContactPage)
  }
}
