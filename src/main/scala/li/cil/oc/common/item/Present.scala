package li.cil.oc.common.item

import li.cil.oc.{Constants, OpenComputers, api}
import li.cil.oc.util.{InventoryUtils, ItemUtils}
import net.minecraft.sounds.{SoundEvents, SoundSource}
import net.minecraft.world.{InteractionResult, InteractionResultHolder}
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item.Properties
import net.minecraft.world.item.{Item, ItemStack}
import net.minecraft.world.item.crafting.RecipeManager
import net.minecraft.world.level.Level
import net.minecraftforge.common.extensions.IForgeItem

import java.util.Random
import scala.collection.mutable

class Present(props: Properties) extends Item(props) with IForgeItem with traits.SimpleItem {
//  override def fillItemCategory(tab: ItemGroup, list: NonNullList[ItemStack]) {}

  override def use(stack: ItemStack, world: Level, player: Player): InteractionResultHolder[ItemStack] = {
    if (stack.getCount > 0) {
      stack.shrink(1)
      if (!world.isClientSide) {
        world.playSound(player, player.getX, player.getY, player.getZ, SoundEvents.PLAYER_LEVELUP, SoundSource.MASTER, 0.2f, 1f)
        Present.recipeManager = world.getRecipeManager
        val present = Present.nextPresent()
        InventoryUtils.addToPlayerInventory(present, player)
      }
    }
    new InteractionResultHolder(InteractionResult.sidedSuccess(world.isClientSide), stack)
  }
}

object Present {
  private var recipeManager: RecipeManager = null

  private lazy val Presents = {
    val result = mutable.ArrayBuffer.empty[ItemStack]

    def add(name: String, weight: Int): Unit = {
      val item = api.Items.get(name)
      if (item != null) {
        val stack = item.createItemStack(1)
        // Only if it can be crafted (wasn't disabled in the config).
        if (ItemUtils.getIngredients(recipeManager, stack).nonEmpty) {
          for (i <- 0 until weight) result += stack
        }
      }
      else {
        OpenComputers.log.warn(s"Oops, trying to add '$name' as a present even though it doesn't exist!")
      }
    }

    add(Constants.ItemName.ARROW_KEYS, 520)
    add(Constants.ItemName.BUTTON_GROUP, 460)
    add(Constants.ItemName.NUMPAD, 410)
    add(Constants.ItemName.DISK, 370)
    add(Constants.ItemName.TRANSISTOR, 350)
    add(Constants.ItemName.FLOPPY, 340)
    add(Constants.ItemName.PRINTED_CIRCUIT_BOARD, 320)
    add(Constants.ItemName.CHIP_TIER_1, 290)
    add(Constants.ItemName.EEPROM, 250)
    add(Constants.ItemName.INTERWEB, 220)
    add(Constants.ItemName.CARD, 190)
    add(Constants.ItemName.ANALYZER, 170)
    add(Constants.ItemName.SIGN_UPGRADE, 150)
    add(Constants.ItemName.INVENTORY_UPGRADE, 130)
    add(Constants.ItemName.CRAFTING_UPGRADE, 110)
    add(Constants.ItemName.TANK_UPGRADE, 90)
    add(Constants.ItemName.PISTON_UPGRADE, 80)
    add(Constants.ItemName.LEASH_UPGRADE, 70)
    add(Constants.ItemName.ANGEL_UPGRADE, 55)
    add(Constants.ItemName.REDSTONE_CARD_TIER_1, 50)
    add(Constants.ItemName.RAM_TIER_1, 48)
    add(Constants.ItemName.CONTROL_UNIT, 46)
    add(Constants.ItemName.ALU, 45)
    add(Constants.ItemName.BATTERY_UPGRADE_TIER_1, 43)
    add(Constants.ItemName.NETWORK_CARD, 38)
    add(Constants.ItemName.WIRELESS_NETWORK_CARD_TIER_1, 37)
    add(Constants.ItemName.HDD_TIER_1, 36)
    add(Constants.ItemName.GENERATOR_UPGRADE, 35)
    add(Constants.ItemName.CPU_TIER_1, 31)
    add(Constants.ItemName.MICROCONTROLLER_CASE_TIER_1, 30)
    add(Constants.ItemName.DRONE_CASE_TIER_1, 25)
    add(Constants.ItemName.UPGRADE_CONTAINER_TIER_1, 23)
    add(Constants.ItemName.CARD_CONTAINER_TIER_1, 23)
    add(Constants.ItemName.GRAPHICS_CARD_TIER_1, 19)
    add(Constants.ItemName.REDSTONE_CARD_TIER_2, 17)
    add(Constants.ItemName.RAM_TIER_2, 15)
    add(Constants.ItemName.DATABASE_UPGRADE_TIER_1, 15)
    add(Constants.ItemName.CHIP_TIER_2, 15)
    add(Constants.ItemName.COMPONENT_BUS_TIER_1, 13)
    add(Constants.ItemName.BATTERY_UPGRADE_TIER_2, 12)
    add(Constants.ItemName.WIRELESS_NETWORK_CARD_TIER_2, 11)
    add(Constants.ItemName.RAM_TIER_3, 10)
    add(Constants.ItemName.SERVER_TIER_1, 10)
    add(Constants.ItemName.INTERNET_CARD, 9)
    add(Constants.ItemName.TERMINAL, 9)
    add(Constants.ItemName.SOLAR_GENERATOR_UPGRADE, 9)
    add(Constants.ItemName.HDD_TIER_2, 7)
    add(Constants.ItemName.NAVIGATION_UPGRADE, 7)
    add(Constants.ItemName.INVENTORY_CONTROLLER_UPGRADE, 7)
    add(Constants.ItemName.TANK_CONTROLLER_UPGRADE, 7)
    add(Constants.ItemName.CPU_TIER_2, 6)
    add(Constants.ItemName.MICROCONTROLLER_CASE_TIER_2, 6)
    add(Constants.ItemName.COMPONENT_BUS_TIER_2, 6)
    add(Constants.ItemName.TABLET_CASE_TIER_1, 5)
    add(Constants.ItemName.UPGRADE_CONTAINER_TIER_2, 5)
    add(Constants.ItemName.CARD_CONTAINER_TIER_2, 5)
    add(Constants.ItemName.GRAPHICS_CARD_TIER_2, 4)
    add(Constants.ItemName.RAM_TIER_4, 4)
    add(Constants.ItemName.DRONE_CASE_TIER_2, 4)
    add(Constants.ItemName.DATABASE_UPGRADE_TIER_2, 4)
    add(Constants.ItemName.SERVER_TIER_2, 4)
    add(Constants.ItemName.CHIP_TIER_3, 3)
    add(Constants.ItemName.COMPONENT_BUS_TIER_3, 3)
    add(Constants.ItemName.TRACTOR_BEAM_UPGRADE, 3)
    add(Constants.ItemName.BATTERY_UPGRADE_TIER_3, 3)
    add(Constants.ItemName.EXPERIENCE_UPGRADE, 2)
    add(Constants.ItemName.RAM_TIER_5, 2)
    add(Constants.ItemName.UPGRADE_CONTAINER_TIER_3, 2)
    add(Constants.ItemName.CARD_CONTAINER_TIER_3, 2)
    add(Constants.ItemName.TABLET_CASE_TIER_2, 1)
    add(Constants.ItemName.HDD_TIER_3, 1)
    add(Constants.ItemName.CHUNKLOADER_UPGRADE, 1)
    add(Constants.ItemName.CPU_TIER_3, 1)
    add(Constants.ItemName.GRAPHICS_CARD_TIER_3, 1)
    add(Constants.ItemName.SERVER_TIER_3, 1)
    add(Constants.ItemName.DATABASE_UPGRADE_TIER_3, 1)
    add(Constants.ItemName.RAM_TIER_6, 1)

    result.toArray
  }

  private val rng = new Random()

  private def nextPresent(): ItemStack = Presents(rng.nextInt(Presents.length)).copy()
}
