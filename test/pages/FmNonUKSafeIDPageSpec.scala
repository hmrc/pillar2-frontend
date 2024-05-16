package pages

import pages.behaviours.PageBehaviours

class FmNonUKSafeIDPageSpec extends PageBehaviours {

  "FmNonUKSafeIDPage" - {

    beRetrievable[String](FmNonUKSafeIDPage)

    beSettable[String](FmNonUKSafeIDPage)

    beRemovable[String](FmNonUKSafeIDPage)
  }
}
