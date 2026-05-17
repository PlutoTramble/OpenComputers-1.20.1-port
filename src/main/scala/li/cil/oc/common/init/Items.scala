package li.cil.oc.common.init

import java.util.concurrent.Callable
import li.cil.oc.Constants
import li.cil.oc.CreativeTab
import li.cil.oc.OpenComputers
import li.cil.oc.Settings
import li.cil.oc.api.detail.ItemAPI
import li.cil.oc.api.detail.ItemInfo
import li.cil.oc.api.fs.FileSystem
import li.cil.oc.common
import li.cil.oc.common.Loot
import li.cil.oc.common.Tier
import li.cil.oc.common.block.SimpleBlock
import li.cil.oc.common.item
import li.cil.oc.common.item.data.DroneData
import li.cil.oc.common.item.data.HoverBootsData
import li.cil.oc.common.item.data.MicrocontrollerData
import li.cil.oc.common.item.data.RobotData
import li.cil.oc.common.item.data.TabletData
import li.cil.oc.common.item.traits.SimpleItem
import li.cil.oc.server.machine.luac.LuaStateFactory
import net.minecraft.core.NonNullList
import net.minecraft.world.level.block.Block
import net.minecraft.world.item.{BlockItem, DyeColor, Item, ItemStack, PickaxeItem, Rarity}
import net.minecraft.world.item.Item.Properties
import net.minecraft.resources.ResourceLocation
import net.minecraftforge.registries.GameData

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

object Items extends ItemAPI {
  val descriptors = mutable.Map.empty[String, ItemInfo]

  val names = mutable.Map.empty[Any, String]

  val aliases = Map(
    "datacard" -> Constants.ItemName.DATA_CARD_TIER_1,
    "wlancard" -> Constants.ItemName.WIRELESS_NETWORK_CARD_TIER_2
  )

  override def get(name: String): ItemInfo = descriptors.get(name).orNull

  override def get(stack: ItemStack): ItemInfo = names.get(getBlockOrItem(stack)) match {
    case Some(name) => get(name)
    case _ => null
  }

  def registerBlockOnly(instance: Block, id: String): Block = {
    if (!descriptors.contains(id)) {
      instance match {
        case simple: SimpleBlock =>
          simple.setUnlocalizedName("oc." + id)
          simple.setRegistryName(OpenComputers.ID, id)
          GameData.register_impl[Block](simple)
        case _ =>
      }
      descriptors += id -> new ItemInfo {
        override def name: String = id

        override def block = instance

        override def item = null

        override def createItemStack(size: Int): ItemStack = {
          OpenComputers.log.warn(s"Attempt to get ItemStack for block ${instance} without item form")
          ItemStack.EMPTY
        }
      }
      names += instance -> id
    }
    instance
  }

  def registerBlock(instance: Block, id: String, itemProps: Properties): Block = {
    if (!descriptors.contains(id)) {
      val itemInst = instance match {
        case simple: SimpleBlock =>
          simple.setUnlocalizedName("oc." + id)
          simple.setRegistryName(OpenComputers.ID, id)
          GameData.register_impl[Block](simple)

          val item : Item = new common.block.Item(simple, itemProps)
          item.setRegistryName(OpenComputers.ID, id)
          GameData.register_impl(item)
          OpenComputers.proxy.registerModel(item, id)
          item
        case _ => null.asInstanceOf[Item]
      }
      descriptors += id -> new ItemInfo {
        override def name: String = id

        override def block = instance

        override def item = itemInst

        override def createItemStack(size: Int): ItemStack = instance match {
          case simple: SimpleBlock => simple.createItemStack(size)
          case _ => new ItemStack(instance, size)
        }
      }
      names += instance -> id
    }
    instance
  }

  def registerItem(instance: Item, id: String): Item = {
    if (!descriptors.contains(id)) {
      instance match {
        case simple: SimpleItem =>
          GameData.register_impl(simple.setRegistryName(new ResourceLocation(Settings.resourceDomain, id)))
          OpenComputers.proxy.registerModel(simple, id)
        case _ =>
      }
      descriptors += id -> new ItemInfo {
        override def name: String = id

        override def block = null

        override def item: Item = instance

        override def createItemStack(size: Int): ItemStack = instance match {
          case simple: SimpleItem => simple.createItemStack(size)
          case _ => new ItemStack(instance, size)
        }
      }
      names += instance -> id
    }
    instance
  }

  def registerStack(stack: ItemStack, id: String): ItemStack = {
    val immutableStack = stack.copy()
    descriptors += id -> new ItemInfo {
      override def name: String = id

      override def block = null

      override def createItemStack(size: Int): ItemStack = {
        val copy = immutableStack.copy()
        copy.setCount(size)
        copy
      }

      override def item: Item = immutableStack.getItem
    }
    stack
  }

  private def getBlockOrItem(stack: ItemStack): Any =
    if (stack.isEmpty) null
    else stack.getItem match {
      case block: BlockItem => block.getBlock
      case item => item
    }

  // ----------------------------------------------------------------------- //

  val registeredItems: ArrayBuffer[ItemStack] = mutable.ArrayBuffer.empty[ItemStack]

  override def registerFloppy(name: String, loc: ResourceLocation, color: DyeColor, factory: Callable[FileSystem], doRecipeCycling: Boolean): ItemStack = {
    val stack = Loot.registerLootDisk(name, loc, color, factory, doRecipeCycling)

    registeredItems += stack

    stack.copy()
  }

  override def registerEEPROM(name: String, code: Array[Byte], data: Array[Byte], readonly: Boolean): ItemStack = {
    val stack = get(Constants.ItemName.EEPROM).createItemStack(1)
    val nbt = stack.getOrCreateTagElement(Settings.namespace + "data")
    if (name != null) {
      nbt.putString(Settings.namespace + "label", name.trim.take(24))
    }
    if (code != null) {
      nbt.putByteArray(Settings.namespace + "eeprom", code.take(Settings.get.eepromSize))
    }
    if (data != null) {
      nbt.putByteArray(Settings.namespace + "userdata", data.take(Settings.get.eepromDataSize))
    }
    nbt.putBoolean(Settings.namespace + "readonly", readonly)

    registeredItems += stack

    stack.copy()
  }

  // ----------------------------------------------------------------------- //

  private def safeGetStack(name: String) = Option(get(name)).map(_.createItemStack(1)).getOrElse(ItemStack.EMPTY)

  def createConfiguredDrone(): ItemStack = {
    val data = new DroneData()

    data.name = "Crecopter"
    data.tier = Tier.Four
    data.storedEnergy = Settings.get.bufferDrone.toInt
    data.components = Array(
      safeGetStack(Constants.ItemName.INVENTORY_UPGRADE),
      safeGetStack(Constants.ItemName.INVENTORY_UPGRADE),
      safeGetStack(Constants.ItemName.INVENTORY_CONTROLLER_UPGRADE),
      safeGetStack(Constants.ItemName.TANK_UPGRADE),
      safeGetStack(Constants.ItemName.TANK_CONTROLLER_UPGRADE),
      safeGetStack(Constants.ItemName.LEASH_UPGRADE),
      safeGetStack(Constants.ItemName.ANGEL_UPGRADE),

      safeGetStack(Constants.ItemName.WIRELESS_NETWORK_CARD_TIER_2),

      LuaStateFactory.setDefaultArch(safeGetStack(Constants.ItemName.CPU_TIER_3)),
      safeGetStack(Constants.ItemName.RAM_TIER_6),
      safeGetStack(Constants.ItemName.RAM_TIER_6)
    ).filter(!_.isEmpty)

    data.createItemStack()
  }

  def createConfiguredMicrocontroller(): ItemStack = {
    val data = new MicrocontrollerData()

    data.tier = Tier.Four
    data.storedEnergy = Settings.get.bufferMicrocontroller.toInt
    data.components = Array(
      safeGetStack(Constants.ItemName.SIGN_UPGRADE),
      safeGetStack(Constants.ItemName.PISTON_UPGRADE),

      safeGetStack(Constants.ItemName.REDSTONE_CARD_TIER_2),
      safeGetStack(Constants.ItemName.WIRELESS_NETWORK_CARD_TIER_2),

      LuaStateFactory.setDefaultArch(safeGetStack(Constants.ItemName.CPU_TIER_3)),
      safeGetStack(Constants.ItemName.RAM_TIER_6),
      safeGetStack(Constants.ItemName.RAM_TIER_6)
    ).filter(!_.isEmpty)

    data.createItemStack()
  }

  def createConfiguredRobot(): ItemStack = {
    val data = new RobotData()

    data.name = "Creatix"
    data.tier = Tier.Four
    data.robotEnergy = Settings.get.bufferRobot.toInt
    data.totalEnergy = data.robotEnergy
    data.components = Array(
      safeGetStack(Constants.BlockName.SCREEN_TIER_1),
      safeGetStack(Constants.BlockName.KEYBOARD),
      safeGetStack(Constants.BlockName.GEOLYZER),
      safeGetStack(Constants.ItemName.INVENTORY_UPGRADE),
      safeGetStack(Constants.ItemName.INVENTORY_UPGRADE),
      safeGetStack(Constants.ItemName.INVENTORY_UPGRADE),
      safeGetStack(Constants.ItemName.INVENTORY_CONTROLLER_UPGRADE),
      safeGetStack(Constants.ItemName.TANK_UPGRADE),
      safeGetStack(Constants.ItemName.TANK_CONTROLLER_UPGRADE),
      safeGetStack(Constants.ItemName.CRAFTING_UPGRADE),
      safeGetStack(Constants.ItemName.HOVER_UPGRADE_TIER_2),
      safeGetStack(Constants.ItemName.ANGEL_UPGRADE),
      safeGetStack(Constants.ItemName.TRADING_UPGRADE),
      safeGetStack(Constants.ItemName.EXPERIENCE_UPGRADE),

      safeGetStack(Constants.ItemName.GRAPHICS_CARD_TIER_3),
      safeGetStack(Constants.ItemName.REDSTONE_CARD_TIER_2),
      safeGetStack(Constants.ItemName.WIRELESS_NETWORK_CARD_TIER_2),
      safeGetStack(Constants.ItemName.INTERNET_CARD),

      LuaStateFactory.setDefaultArch(safeGetStack(Constants.ItemName.CPU_TIER_3)),
      safeGetStack(Constants.ItemName.RAM_TIER_6),
      safeGetStack(Constants.ItemName.RAM_TIER_6),

      safeGetStack(Constants.ItemName.LUA_BIOS),
      safeGetStack(Constants.ItemName.OPEN_OS),
      safeGetStack(Constants.ItemName.HDD_TIER_3)
    ).filter(!_.isEmpty)
    data.containers = Array(
      safeGetStack(Constants.ItemName.CARD_CONTAINER_TIER_3),
      safeGetStack(Constants.ItemName.UPGRADE_CONTAINER_TIER_3),
      safeGetStack(Constants.BlockName.DISK_DRIVE)
    ).filter(!_.isEmpty)

    data.createItemStack()
  }

  def createConfiguredTablet(): ItemStack = {
    val data = new TabletData()

    data.tier = Tier.Four
    data.energy = Settings.get.bufferTablet
    data.maxEnergy = data.energy
    data.items = Array(
      safeGetStack(Constants.BlockName.SCREEN_TIER_1),
      safeGetStack(Constants.BlockName.KEYBOARD),

      safeGetStack(Constants.ItemName.SIGN_UPGRADE),
      safeGetStack(Constants.ItemName.PISTON_UPGRADE),
      safeGetStack(Constants.BlockName.GEOLYZER),
      safeGetStack(Constants.ItemName.NAVIGATION_UPGRADE),
      safeGetStack(Constants.ItemName.ANALYZER),

      safeGetStack(Constants.ItemName.GRAPHICS_CARD_TIER_2),
      safeGetStack(Constants.ItemName.REDSTONE_CARD_TIER_2),
      safeGetStack(Constants.ItemName.WIRELESS_NETWORK_CARD_TIER_2),

      LuaStateFactory.setDefaultArch(safeGetStack(Constants.ItemName.CPU_TIER_3)),
      safeGetStack(Constants.ItemName.RAM_TIER_6),
      safeGetStack(Constants.ItemName.RAM_TIER_6),

      safeGetStack(Constants.ItemName.LUA_BIOS),
      safeGetStack(Constants.ItemName.HDD_TIER_3)
    ).padTo(32, ItemStack.EMPTY)
    data.items(31) = safeGetStack(Constants.ItemName.OPEN_OS)
    data.container = safeGetStack(Constants.BlockName.DISK_DRIVE)

    data.createItemStack()
  }

  def createChargedHoverBoots(): ItemStack = {
    val data = new HoverBootsData()
    data.charge = Settings.get.bufferHoverBoots

    data.createItemStack()
  }

  // ----------------------------------------------------------------------- //

  private def defaultProps = new Properties().tab(CreativeTab)

  def init() {
    initMaterials()
    initTools()
    initComponents()
    initCards()
    initUpgrades()
    initStorage()
    initSpecial()

    // Register aliases.
    for ((k, v) <- aliases) {
      descriptors.getOrElseUpdate(k, descriptors(v))
    }
  }

  // Crafting materials.
  private def initMaterials(): Unit = {
    registerItem(new item.CuttingWire(defaultProps), Constants.ItemName.CUTTING_WIRE)
    registerItem(new item.Acid(defaultProps), Constants.ItemName.ACID)
    registerItem(new item.RawCircuitBoard(defaultProps), Constants.ItemName.RAW_CIRCUIT_BOARD)
    registerItem(new item.CircuitBoard(defaultProps), Constants.ItemName.CIRCUIT_BOARD)
    registerItem(new item.PrintedCircuitBoard(defaultProps), Constants.ItemName.PRINTED_CIRCUIT_BOARD)
    registerItem(new item.CardBase(defaultProps), Constants.ItemName.CARD)
    registerItem(new item.Transistor(defaultProps), Constants.ItemName.TRANSISTOR)
    registerItem(new item.Microchip(defaultProps, Tier.One), Constants.ItemName.CHIP_TIER_1)
    registerItem(new item.Microchip(defaultProps.rarity(Rarity.UNCOMMON), Tier.Two), Constants.ItemName.CHIP_TIER_2)
    registerItem(new item.Microchip(defaultProps.rarity(Rarity.RARE), Tier.Three), Constants.ItemName.CHIP_TIER_3)
    registerItem(new item.ALU(defaultProps), Constants.ItemName.ALU)
    registerItem(new item.ControlUnit(defaultProps), Constants.ItemName.CONTROL_UNIT)
    registerItem(new item.Disk(defaultProps), Constants.ItemName.DISK)
    registerItem(new item.Interweb(defaultProps), Constants.ItemName.INTERWEB)
    registerItem(new item.ButtonGroup(defaultProps), Constants.ItemName.BUTTON_GROUP)
    registerItem(new item.ArrowKeys(defaultProps), Constants.ItemName.ARROW_KEYS)
    registerItem(new item.NumPad(defaultProps), Constants.ItemName.NUMPAD)

    registerItem(new item.TabletCase(defaultProps, Tier.One), Constants.ItemName.TABLET_CASE_TIER_1)
    registerItem(new item.TabletCase(defaultProps.rarity(Rarity.UNCOMMON), Tier.Two), Constants.ItemName.TABLET_CASE_TIER_2)
    registerItem(new item.TabletCase(defaultProps.rarity(Rarity.EPIC), Tier.Four), Constants.ItemName.TABLET_CASE_CREATIVE)
    registerItem(new item.MicrocontrollerCase(defaultProps, Tier.One), Constants.ItemName.MICROCONTROLLER_CASE_TIER_1)
    registerItem(new item.MicrocontrollerCase(defaultProps.rarity(Rarity.UNCOMMON), Tier.Two), Constants.ItemName.MICROCONTROLLER_CASE_TIER_2)
    registerItem(new item.MicrocontrollerCase(defaultProps.rarity(Rarity.EPIC), Tier.Four), Constants.ItemName.MICROCONTROLLER_CASE_CREATIVE)
    registerItem(new item.DroneCase(defaultProps, Tier.One), Constants.ItemName.DRONE_CASE_TIER_1)
    registerItem(new item.DroneCase(defaultProps.rarity(Rarity.UNCOMMON), Tier.Two), Constants.ItemName.DRONE_CASE_TIER_2)
    registerItem(new item.DroneCase(defaultProps.rarity(Rarity.EPIC), Tier.Four), Constants.ItemName.DRONE_CASE_CREATIVE)

    registerItem(new item.InkCartridgeEmpty(defaultProps.stacksTo(1)), Constants.ItemName.INK_CARTRIDGE_EMPTY)
    registerItem(new item.InkCartridge(defaultProps.stacksTo(1).craftRemainder(get(Constants.ItemName.INK_CARTRIDGE_EMPTY).item)), Constants.ItemName.INK_CARTRIDGE)
    registerItem(new item.Chamelium(defaultProps), Constants.ItemName.CHAMELIUM)

    registerItem(new item.DiamondChip(defaultProps), Constants.ItemName.DIAMOND_CHIP)
  }

  // All kinds of tools.
  private def initTools(): Unit = {
    registerItem(new item.Analyzer(defaultProps), Constants.ItemName.ANALYZER)
    registerItem(new item.Debugger(defaultProps), Constants.ItemName.DEBUGGER)
    registerItem(new item.Terminal(defaultProps.stacksTo(1)), Constants.ItemName.TERMINAL)
    registerItem(new item.TexturePicker(defaultProps), Constants.ItemName.TEXTURE_PICKER)
    registerItem(new item.Manual(defaultProps), Constants.ItemName.MANUAL)
    registerItem(new item.Wrench(defaultProps.stacksTo(1)), Constants.ItemName.WRENCH)

    // 1.5.11
    // FIXME : Causes server crashes, disabled as temp fix
    //registerItem(new item.HoverBoots(defaultProps.stacksTo(1).rarity(Rarity.UNCOMMON).setNoRepair()), Constants.ItemName.HOVER_BOOTS)

    // 1.5.18
    registerItem(new item.Nanomachines(defaultProps.rarity(Rarity.UNCOMMON)), Constants.ItemName.NANOMACHINES)
  }

  // General purpose components.
  private def initComponents(): Unit = {
    registerItem(new item.CPU(defaultProps, Tier.One), Constants.ItemName.CPU_TIER_1)
    registerItem(new item.CPU(defaultProps.rarity(Rarity.UNCOMMON), Tier.Two), Constants.ItemName.CPU_TIER_2)
    registerItem(new item.CPU(defaultProps.rarity(Rarity.RARE), Tier.Three), Constants.ItemName.CPU_TIER_3)

    registerItem(new item.ComponentBus(defaultProps, Tier.One), Constants.ItemName.COMPONENT_BUS_TIER_1)
    registerItem(new item.ComponentBus(defaultProps.rarity(Rarity.UNCOMMON), Tier.Two), Constants.ItemName.COMPONENT_BUS_TIER_2)
    registerItem(new item.ComponentBus(defaultProps.rarity(Rarity.RARE), Tier.Three), Constants.ItemName.COMPONENT_BUS_TIER_3)

    registerItem(new item.Memory(defaultProps, Tier.One), Constants.ItemName.RAM_TIER_1)
    registerItem(new item.Memory(defaultProps, Tier.Two), Constants.ItemName.RAM_TIER_2)
    registerItem(new item.Memory(defaultProps.rarity(Rarity.UNCOMMON), Tier.Three), Constants.ItemName.RAM_TIER_3)
    registerItem(new item.Memory(defaultProps.rarity(Rarity.UNCOMMON), Tier.Four), Constants.ItemName.RAM_TIER_4)
    registerItem(new item.Memory(defaultProps.rarity(Rarity.RARE), Tier.Five), Constants.ItemName.RAM_TIER_5)
    registerItem(new item.Memory(defaultProps.rarity(Rarity.RARE), Tier.Six), Constants.ItemName.RAM_TIER_6)

    registerItem(new item.Server(defaultProps.stacksTo(1).rarity(Rarity.EPIC), Tier.Four), Constants.ItemName.SERVER_CREATIVE)
    registerItem(new item.Server(defaultProps.stacksTo(1), Tier.One), Constants.ItemName.SERVER_TIER_1)
    registerItem(new item.Server(defaultProps.stacksTo(1).rarity(Rarity.UNCOMMON), Tier.Two), Constants.ItemName.SERVER_TIER_2)
    registerItem(new item.Server(defaultProps.stacksTo(1).rarity(Rarity.RARE), Tier.Three), Constants.ItemName.SERVER_TIER_3)

    // 1.5.10
    registerItem(new item.APU(defaultProps.rarity(Rarity.UNCOMMON), Tier.One), Constants.ItemName.APU_TIER_1)
    registerItem(new item.APU(defaultProps.rarity(Rarity.RARE), Tier.Two), Constants.ItemName.APU_TIER_2)

    // 1.5.12
    registerItem(new item.APU(defaultProps.rarity(Rarity.EPIC), Tier.Three), Constants.ItemName.APU_CREATIVE)

    // 1.6
    registerItem(new item.TerminalServer(defaultProps.stacksTo(1)), Constants.ItemName.TERMINAL_SERVER)
    registerItem(new item.DiskDriveMountable(defaultProps.stacksTo(1)), Constants.ItemName.DISK_DRIVE_MOUNTABLE)
  }

  // Card components.
  private def initCards(): Unit = {
    registerItem(new item.DebugCard(defaultProps), Constants.ItemName.DEBUG_CARD)
    registerItem(new item.GraphicsCard(defaultProps, Tier.One), Constants.ItemName.GRAPHICS_CARD_TIER_1)
    registerItem(new item.GraphicsCard(defaultProps.rarity(Rarity.UNCOMMON), Tier.Two), Constants.ItemName.GRAPHICS_CARD_TIER_2)
    registerItem(new item.GraphicsCard(defaultProps.rarity(Rarity.RARE), Tier.Three), Constants.ItemName.GRAPHICS_CARD_TIER_3)
    registerItem(new item.RedstoneCard(defaultProps, Tier.One), Constants.ItemName.REDSTONE_CARD_TIER_1)
    registerItem(new item.RedstoneCard(defaultProps.rarity(Rarity.UNCOMMON), Tier.Two), Constants.ItemName.REDSTONE_CARD_TIER_2)
    registerItem(new item.NetworkCard(defaultProps), Constants.ItemName.NETWORK_CARD)
    registerItem(new item.WirelessNetworkCard(defaultProps.rarity(Rarity.UNCOMMON), Tier.Two), Constants.ItemName.WIRELESS_NETWORK_CARD_TIER_2)
    registerItem(new item.InternetCard(defaultProps.rarity(Rarity.UNCOMMON)), Constants.ItemName.INTERNET_CARD)
    registerItem(new item.LinkedCard(defaultProps.rarity(Rarity.RARE)), Constants.ItemName.LINKED_CARD)

    // 1.5.13
    registerItem(new item.DataCard(defaultProps, Tier.One), Constants.ItemName.DATA_CARD_TIER_1)

    // 1.5.15
    registerItem(new item.DataCard(defaultProps.rarity(Rarity.UNCOMMON), Tier.Two), Constants.ItemName.DATA_CARD_TIER_2)
    registerItem(new item.DataCard(defaultProps.rarity(Rarity.RARE), Tier.Three), Constants.ItemName.DATA_CARD_TIER_3)
  }

  // Upgrade components.
  private def initUpgrades(): Unit = {
    registerItem(new item.UpgradeAngel(defaultProps.rarity(Rarity.UNCOMMON)), Constants.ItemName.ANGEL_UPGRADE)
    registerItem(new item.UpgradeBattery(defaultProps, Tier.One), Constants.ItemName.BATTERY_UPGRADE_TIER_1)
    registerItem(new item.UpgradeBattery(defaultProps.rarity(Rarity.UNCOMMON), Tier.Two), Constants.ItemName.BATTERY_UPGRADE_TIER_2)
    registerItem(new item.UpgradeBattery(defaultProps.rarity(Rarity.RARE), Tier.Three), Constants.ItemName.BATTERY_UPGRADE_TIER_3)
    registerItem(new item.UpgradeChunkloader(defaultProps.rarity(Rarity.RARE)), Constants.ItemName.CHUNKLOADER_UPGRADE)
    registerItem(new item.UpgradeContainerCard(defaultProps, Tier.One), Constants.ItemName.CARD_CONTAINER_TIER_1)
    registerItem(new item.UpgradeContainerCard(defaultProps.rarity(Rarity.UNCOMMON), Tier.Two), Constants.ItemName.CARD_CONTAINER_TIER_2)
    registerItem(new item.UpgradeContainerCard(defaultProps.rarity(Rarity.RARE), Tier.Three), Constants.ItemName.CARD_CONTAINER_TIER_3)
    registerItem(new item.UpgradeContainerUpgrade(defaultProps, Tier.One), Constants.ItemName.UPGRADE_CONTAINER_TIER_1)
    registerItem(new item.UpgradeContainerUpgrade(defaultProps.rarity(Rarity.UNCOMMON), Tier.Two), Constants.ItemName.UPGRADE_CONTAINER_TIER_2)
    registerItem(new item.UpgradeContainerUpgrade(defaultProps.rarity(Rarity.RARE), Tier.Three), Constants.ItemName.UPGRADE_CONTAINER_TIER_3)
    registerItem(new item.UpgradeCrafting(defaultProps.rarity(Rarity.UNCOMMON)), Constants.ItemName.CRAFTING_UPGRADE)
    registerItem(new item.UpgradeDatabase(defaultProps, Tier.One), Constants.ItemName.DATABASE_UPGRADE_TIER_1)
    registerItem(new item.UpgradeDatabase(defaultProps.rarity(Rarity.UNCOMMON), Tier.Two), Constants.ItemName.DATABASE_UPGRADE_TIER_2)
    registerItem(new item.UpgradeDatabase(defaultProps.rarity(Rarity.RARE), Tier.Three), Constants.ItemName.DATABASE_UPGRADE_TIER_3)
    registerItem(new item.UpgradeExperience(defaultProps.rarity(Rarity.RARE)), Constants.ItemName.EXPERIENCE_UPGRADE)
    registerItem(new item.UpgradeGenerator(defaultProps.rarity(Rarity.UNCOMMON)), Constants.ItemName.GENERATOR_UPGRADE)
    registerItem(new item.UpgradeInventory(defaultProps), Constants.ItemName.INVENTORY_UPGRADE)
    registerItem(new item.UpgradeInventoryController(defaultProps.rarity(Rarity.UNCOMMON)), Constants.ItemName.INVENTORY_CONTROLLER_UPGRADE)
    registerItem(new item.UpgradeNavigation(defaultProps.rarity(Rarity.UNCOMMON)), Constants.ItemName.NAVIGATION_UPGRADE)
    registerItem(new item.UpgradePiston(defaultProps.rarity(Rarity.UNCOMMON)), Constants.ItemName.PISTON_UPGRADE)
    registerItem(new item.UpgradeSign(defaultProps.rarity(Rarity.UNCOMMON)), Constants.ItemName.SIGN_UPGRADE)
    registerItem(new item.UpgradeSolarGenerator(defaultProps.rarity(Rarity.UNCOMMON)), Constants.ItemName.SOLAR_GENERATOR_UPGRADE)
    registerItem(new item.UpgradeTank(defaultProps), Constants.ItemName.TANK_UPGRADE)
    registerItem(new item.UpgradeTankController(defaultProps.rarity(Rarity.UNCOMMON)), Constants.ItemName.TANK_CONTROLLER_UPGRADE)
    registerItem(new item.UpgradeTractorBeam(defaultProps.rarity(Rarity.RARE)), Constants.ItemName.TRACTOR_BEAM_UPGRADE)
    registerItem(new item.UpgradeLeash(defaultProps), Constants.ItemName.LEASH_UPGRADE)

    // 1.5.8
    registerItem(new item.UpgradeHover(defaultProps, Tier.One), Constants.ItemName.HOVER_UPGRADE_TIER_1)
    registerItem(new item.UpgradeHover(defaultProps.rarity(Rarity.UNCOMMON), Tier.Two), Constants.ItemName.HOVER_UPGRADE_TIER_2)

    // 1.6
    registerItem(new item.UpgradeTrading(defaultProps.rarity(Rarity.UNCOMMON)), Constants.ItemName.TRADING_UPGRADE)
    registerItem(new item.UpgradeMF(defaultProps.rarity(Rarity.RARE)), Constants.ItemName.MFU)

    // 1.7.2
    registerItem(new item.WirelessNetworkCard(defaultProps, Tier.One), Constants.ItemName.WIRELESS_NETWORK_CARD_TIER_1)
    registerItem(new item.ComponentBus(defaultProps.rarity(Rarity.EPIC), Tier.Four), Constants.ItemName.COMPONENT_BUS_CREATIVE)

    // 1.8
    registerItem(new item.UpgradeStickyPiston(defaultProps), Constants.ItemName.STICKY_PISTON_UPGRADE)
  }

  // Storage media of all kinds.
  private def initStorage(): Unit = {
    registerItem(new item.EEPROM(defaultProps), Constants.ItemName.EEPROM)
    registerItem(new item.FloppyDisk(defaultProps), Constants.ItemName.FLOPPY)
    registerItem(new item.HardDiskDrive(defaultProps, Tier.One), Constants.ItemName.HDD_TIER_1)
    registerItem(new item.HardDiskDrive(defaultProps.rarity(Rarity.UNCOMMON), Tier.Two), Constants.ItemName.HDD_TIER_2)
    registerItem(new item.HardDiskDrive(defaultProps.rarity(Rarity.RARE), Tier.Three), Constants.ItemName.HDD_TIER_3)

    val luaBios = {
      val code = new Array[Byte](4 * 1024)
      val count = OpenComputers.getClass.getResourceAsStream(Settings.scriptPath + "bios.lua").read(code)
      registerEEPROM("EEPROM (Lua BIOS)", code.take(count), null, readonly = false)
    }
    registerStack(luaBios, Constants.ItemName.LUA_BIOS)

  }

  // Special purpose items that don't fit into any other category.
  private def initSpecial(): Unit = {
    registerItem(new item.Tablet(defaultProps.stacksTo(1)), Constants.ItemName.TABLET)
    registerItem(new item.Drone(defaultProps), Constants.ItemName.DRONE)
    registerItem(new item.Present(defaultProps), Constants.ItemName.PRESENT)
  }

  def decorateCreativeTab(list: NonNullList[ItemStack]) {
    list.add(Items.createConfiguredDrone())
    list.add(Items.createConfiguredMicrocontroller())
    list.add(Items.createConfiguredRobot())
    list.add(Items.createConfiguredTablet())
    Loot.disksForClient.foreach(list.add)
    registeredItems.foreach(list.add)
  }
}
