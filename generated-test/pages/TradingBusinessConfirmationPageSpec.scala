package pages

import models.TradingBusinessConfirmation
import pages.behaviours.PageBehaviours

class TradingBusinessConfirmationSpec extends PageBehaviours {

  "TradingBusinessConfirmationPage" - {

    beRetrievable[TradingBusinessConfirmation](TradingBusinessConfirmationPage)

    beSettable[TradingBusinessConfirmation](TradingBusinessConfirmationPage)

    beRemovable[TradingBusinessConfirmation](TradingBusinessConfirmationPage)
  }
}
