package pages

import models.repayments.NonUKBank
import org.scalacheck.ScalacheckShapeless.derivedArbitrary
import pages.behaviours.PageBehaviours

class NonUKBankPageSpec extends PageBehaviours {

  "NonUKBankPage" - {

    beRetrievable[NonUKBank](NonUKBankPage)

    beSettable[NonUKBank](NonUKBankPage)

    beRemovable[NonUKBank](NonUKBankPage)
  }
}
