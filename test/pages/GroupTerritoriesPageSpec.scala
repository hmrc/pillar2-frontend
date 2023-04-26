package pages

import models.GroupTerritories
import pages.behaviours.PageBehaviours

class GroupTerritoriesSpec extends PageBehaviours {

  "GroupTerritories" - {

    beRetrievable[GroupTerritories](GroupTerritoriesPage)

    beSettable[GroupTerritories](GroupTerritoriesPage)

    beRemovable[GroupTerritories](GroupTerritoriesPage)
  }
}
