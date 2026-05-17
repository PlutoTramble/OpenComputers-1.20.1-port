package li.cil.oc.integration.opencomputers

import li.cil.oc.Constants
import li.cil.oc.api
import li.cil.oc.api.network.EnvironmentHost
import li.cil.oc.api.driver.item.Container
import li.cil.oc.common.Slot
import li.cil.oc.common.Tier
import li.cil.oc.common.item
import net.minecraft.world.item.ItemStack

object DriverContainerCard extends Item with Container {
  override def worksWith(stack: ItemStack) = isOneOf(stack,
    api.Items.get(Constants.ItemName.CARD_CONTAINER_TIER_1),
    api.Items.get(Constants.ItemName.CARD_CONTAINER_TIER_2),
    api.Items.get(Constants.ItemName.CARD_CONTAINER_TIER_3))

  override def createEnvironment(stack: ItemStack, host: EnvironmentHost) = null

  override def slot(stack: ItemStack) = Slot.Container

  override def providedSlot(stack: ItemStack) = Slot.Card

  override def providedTier(stack: ItemStack) = tier(stack)

  override def tier(stack: ItemStack) =
    stack.getItem match {
      case container: item.UpgradeContainerCard => container.tier
      case _ => Tier.One
    }
}
