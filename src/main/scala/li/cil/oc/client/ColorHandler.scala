package li.cil.oc.client

import codechicken.lib.datagen.ItemModelProvider
import li.cil.oc.Constants
import li.cil.oc.api
import li.cil.oc.api.internal.Colored
import li.cil.oc.common.block
import li.cil.oc.util.Color
import li.cil.oc.util.ItemColorizer
import li.cil.oc.util.ItemUtils
import net.minecraft.client.color.block.BlockColor
import net.minecraft.client.color.item.ItemColors
import net.minecraft.world.level.BlockAndTintGetter
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.client.Minecraft
import net.minecraft.world.item.DyeColor
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.core.BlockPos
import net.minecraft.world.level.BlockGetter

object ColorHandler {
  def init(): Unit = {
    register((state, world, pos, tintIndex) => state.getBlock match {
      case block: block.Cable => block.colorMultiplierOverride.getOrElse(0xFFFFFFFF)
      case _ => 0xFFFFFFFF
    },
      api.Items.get(Constants.BlockName.CABLE).block())

    register((state, world, pos, tintIndex) => if (pos == null) 0xFFFFFFFF else world.getBlockEntity(pos) match {
      case colored: Colored => colored.getColor
      case _ => state.getBlock match {
        case block: block.Case => Color.rgbValues(Color.byTier(block.tier))
        case _ => 0xFFFFFFFF
      }
    },
      api.Items.get(Constants.BlockName.CASE_TIER_1).block(),
      api.Items.get(Constants.BlockName.CASE_TIER_2).block(),
      api.Items.get(Constants.BlockName.CASE_TIER_3).block(),
      api.Items.get(Constants.BlockName.CASE_CREATIVE).block())

    register((state, world, pos, tintIndex) => Color.rgbValues(state.getValue(block.ChameliumBlock.Color)),
      api.Items.get(Constants.BlockName.CHAMELIUM_BLOCK).block())

    register((state, world, pos, tintIndex) => tintIndex,
      api.Items.get(Constants.BlockName.PRINT).block())

    register((state, world, pos, tintIndex) => state.getBlock match {
      case block: block.Screen => Color.rgbValues(Color.byTier(block.tier))
      case _ => 0xFFFFFFFF
    },
      api.Items.get(Constants.BlockName.SCREEN_TIER_1).block(),
      api.Items.get(Constants.BlockName.SCREEN_TIER_2).block(),
      api.Items.get(Constants.BlockName.SCREEN_TIER_3).block())

    register(0,(stack, tintIndex) => if (ItemColorizer.hasColor(stack)) ItemColorizer.getColor(stack) else tintIndex,
      api.Items.get(Constants.BlockName.CABLE).block())

    register((stack, tintIndex) => Color.rgbValues(Color.byTier(ItemUtils.caseTier(stack))),
      api.Items.get(Constants.BlockName.CASE_TIER_1).item(),
      api.Items.get(Constants.BlockName.CASE_TIER_2).item(),
      api.Items.get(Constants.BlockName.CASE_TIER_3).item(),
      api.Items.get(Constants.BlockName.CASE_CREATIVE).item())

    register(0,(stack, tintIndex) => Color.rgbValues(DyeColor.byId(stack.getDamageValue)),
      api.Items.get(Constants.BlockName.CHAMELIUM_BLOCK).block())

    register(0,(stack, tintIndex) => tintIndex,
      api.Items.get(Constants.BlockName.SCREEN_TIER_1).block(),
      api.Items.get(Constants.BlockName.SCREEN_TIER_2).block(),
      api.Items.get(Constants.BlockName.SCREEN_TIER_3).block(),
      api.Items.get(Constants.BlockName.PRINT).block(),
      api.Items.get(Constants.BlockName.ROBOT).block())

    register((stack, tintIndex) =>
      if (tintIndex == 1) {
        if (ItemColorizer.hasColor(stack)) ItemColorizer.getColor(stack) else 0x66DD55
      } else 0xFFFFFF,
      api.Items.get(Constants.ItemName.HOVER_BOOTS).item())
  }

  def register(handler: (BlockState, BlockGetter, BlockPos, Int) => Int, blocks: Block*): Unit = {
    Minecraft.getInstance.getBlockColors.register((state, blockAndTintGetter, blockPos, tint) => handler(state, blockAndTintGetter, blockPos, tint), blocks: _*)
  }

  def register(i:Int, handler: (ItemStack, Int) => Int, blocks: Block*): Unit = {
    Minecraft.getInstance.getBlockColors.register((state, blockAndTintGetter, blockPos, tint) => handler(new ItemStack(state.getBlock.asItem()), tint), blocks: _*)
  }

  def register(handler: (ItemStack, Int) => Int, items: Item*): Unit = {
    Minecraft.getInstance.getItemColors.register((stack: ItemStack, tintIndex: Int) => handler(stack, tintIndex), items: _*)
  }
}
