package li.cil.oc.integration.opencomputers

import li.cil.oc.Constants
import li.cil.oc.api
import li.cil.oc.api.driver.EnvironmentProvider
import li.cil.oc.api.network.Environment
import li.cil.oc.common
import li.cil.oc.common.tileentity
import li.cil.oc.integration.util.BundledRedstone
import li.cil.oc.server.component
import li.cil.oc.server.machine.Machine
import net.minecraft.world.level.block.Block
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.{BlockItem, ItemStack}

/**
 * Provide static environment lookup for blocks that are components.
 * This allows showing their documentation in NEI, for example. Not
 * all blocks are present here, because some also serve as upgrades
 * and therefore have item drivers.
 */
object EnvironmentProviderBlocks extends EnvironmentProvider {
  override def getEnvironment(stack: ItemStack): Class[_] = stack.getItem match {
    case block: BlockItem if block.getBlock != null =>
      if (isOneOf(block.getBlock, Constants.BlockName.ASSEMBLER)) classOf[tileentity.Assembler]
      else if (isOneOf(block.getBlock, Constants.BlockName.CASE_TIER_1, Constants.BlockName.CASE_TIER_2, Constants.BlockName.CASE_TIER_3, Constants.BlockName.CASE_CREATIVE, Constants.BlockName.MICROCONTROLLER)) classOf[Machine]
      else if (isOneOf(block.getBlock, Constants.BlockName.HOLOGRAM_TIER_1, Constants.BlockName.HOLOGRAM_TIER_2)) classOf[tileentity.Hologram]
      else if (isOneOf(block.getBlock, Constants.BlockName.PRINTER)) classOf[tileentity.Printer]
      else if (isOneOf(block.getBlock, Constants.BlockName.RELAY)) classOf[tileentity.Relay]
      else if (isOneOf(block.getBlock, Constants.BlockName.REDSTONE)) if (BundledRedstone.isAvailable) classOf[component.Redstone.Bundled] else classOf[component.Redstone.Vanilla]
      else if (isOneOf(block.getBlock, Constants.BlockName.SCREEN_TIER_1)) classOf[common.component.TextBuffer]: Class[_ <: Environment]
      else if (isOneOf(block.getBlock, Constants.BlockName.SCREEN_TIER_2, Constants.BlockName.SCREEN_TIER_3)) classOf[common.component.Screen]
      else if (isOneOf(block.getBlock, Constants.BlockName.ROBOT)) classOf[component.Robot]: Class[_ <: Environment]
      else if (isOneOf(block.getBlock, Constants.BlockName.WAYPOINT)) classOf[tileentity.Waypoint]: Class[_ <: Environment]
      else null
    case _ =>
      if (api.Items.get(stack) == api.Items.get(Constants.ItemName.DRONE)) classOf[component.Drone]: Class[_ <: Environment]
      else null
  }

  private def isOneOf(block: Block, names: String*) = names.exists(api.Items.get(_).block == block)
}
