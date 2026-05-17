package li.cil.oc

import li.cil.oc.util.ItemUtils

object SConstants {

  object BlockName {
    def Case(tier: Int): String = ItemUtils.caseNameWithTierSuffix("case", tier)
  }

  object ItemName {
    def DroneCase(tier: Int): String = ItemUtils.caseNameWithTierSuffix("dronecase", tier)

    def MicrocontrollerCase(tier: Int): String = ItemUtils.caseNameWithTierSuffix("microcontrollercase", tier)

    def TabletCase(tier: Int): String = ItemUtils.caseNameWithTierSuffix("tabletcase", tier)
  }
}
