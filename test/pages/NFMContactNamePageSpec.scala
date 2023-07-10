package pages

import pages.behaviours.PageBehaviours

class NFMContactNamePageSpec extends PageBehaviours {

  "NFMContactNamePage" - {

    beRetrievable[String](NFMContactNamePage)

    beSettable[String](NFMContactNamePage)

    beRemovable[String](NFMContactNamePage)
  }
}
