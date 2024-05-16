package pages

import pages.behaviours.PageBehaviours

class UpeNonUKSafeIDPageSpec extends PageBehaviours {

  "UpeNonUKSafeIDPage" - {

    beRetrievable[String](UpeNonUKSafeIDPage)

    beSettable[String](UpeNonUKSafeIDPage)

    beRemovable[String](UpeNonUKSafeIDPage)
  }
}
