package li.cil.oc.common.init

import li.cil.oc.Constants
import li.cil.oc.CreativeTab
import li.cil.oc.Settings
import li.cil.oc.common.Tier
import li.cil.oc.common.block._
import net.minecraft.world.level.block.state.BlockBehaviour.Properties
import net.minecraft.world.level.material.Material
import net.minecraft.world.item.{Item, Rarity}

object Blocks {
  def init() {
    def defaultProps = Properties.of(Material.METAL).strength(2, 5)
    def defaultItemProps = new Item.Properties().tab(CreativeTab)
    Items.registerBlock(new Adapter(defaultProps), Constants.BlockName.ADAPTER, defaultItemProps)
    Items.registerBlock(new Assembler(defaultProps), Constants.BlockName.ASSEMBLER, defaultItemProps)
    Items.registerBlock(new Cable(defaultProps), Constants.BlockName.CABLE, defaultItemProps)
    Items.registerBlock(new Capacitor(defaultProps), Constants.BlockName.CAPACITOR, defaultItemProps)
    Items.registerBlock(new Case(defaultProps, Tier.One), Constants.BlockName.CASE_TIER_1, defaultItemProps)
    Items.registerBlock(new Case(defaultProps, Tier.Three), Constants.BlockName.CASE_TIER_3, defaultItemProps.rarity(Rarity.RARE))
    Items.registerBlock(new Case(defaultProps, Tier.Two), Constants.BlockName.CASE_TIER_2, defaultItemProps.rarity(Rarity.UNCOMMON))
    Items.registerBlock(new ChameliumBlock(Properties.of(Material.STONE).strength(2, 5)), Constants.BlockName.CHAMELIUM_BLOCK, defaultItemProps)
    Items.registerBlock(new Charger(defaultProps), Constants.BlockName.CHARGER, defaultItemProps)
    Items.registerBlock(new Disassembler(defaultProps), Constants.BlockName.DISASSEMBLER, defaultItemProps)
    Items.registerBlock(new DiskDrive(defaultProps), Constants.BlockName.DISK_DRIVE, defaultItemProps)
    Items.registerBlock(new Geolyzer(defaultProps), Constants.BlockName.GEOLYZER, defaultItemProps)
    Items.registerBlock(new Hologram(defaultProps, Tier.One), Constants.BlockName.HOLOGRAM_TIER_1, defaultItemProps)
    Items.registerBlock(new Hologram(defaultProps, Tier.Two), Constants.BlockName.HOLOGRAM_TIER_2, defaultItemProps.rarity(Rarity.UNCOMMON))
    Items.registerBlock(new Keyboard(Properties.of(Material.STONE).strength(2, 5).noOcclusion), Constants.BlockName.KEYBOARD, defaultItemProps)
    Items.registerBlock(new MotionSensor(defaultProps), Constants.BlockName.MOTION_SENSOR, defaultItemProps)
    Items.registerBlock(new PowerConverter(defaultProps), Constants.BlockName.POWER_CONVERTER,
      new Item.Properties().tab(if (!Settings.get.ignorePower) CreativeTab else null))
    Items.registerBlock(new PowerDistributor(defaultProps), Constants.BlockName.POWER_DISTRIBUTOR, defaultItemProps)
    Items.registerBlock(new Printer(defaultProps), Constants.BlockName.PRINTER, defaultItemProps)
    Items.registerBlock(new Raid(defaultProps), Constants.BlockName.RAID, defaultItemProps)
    Items.registerBlock(new Redstone(defaultProps), Constants.BlockName.REDSTONE, defaultItemProps)
    Items.registerBlock(new Relay(defaultProps), Constants.BlockName.RELAY, defaultItemProps)
    Items.registerBlock(new Screen(defaultProps, Tier.One), Constants.BlockName.SCREEN_TIER_1, defaultItemProps)
    Items.registerBlock(new Screen(defaultProps, Tier.Three), Constants.BlockName.SCREEN_TIER_3, defaultItemProps.rarity(Rarity.RARE))
    Items.registerBlock(new Screen(defaultProps, Tier.Two), Constants.BlockName.SCREEN_TIER_2, defaultItemProps.rarity(Rarity.UNCOMMON))
    Items.registerBlock(new Rack(defaultProps), Constants.BlockName.RACK, defaultItemProps)
    Items.registerBlock(new Waypoint(defaultProps), Constants.BlockName.WAYPOINT, defaultItemProps)

    Items.registerBlock(new Case(defaultProps, Tier.Four), Constants.BlockName.CASE_CREATIVE, defaultItemProps.rarity(Rarity.EPIC))
    Items.registerBlock(new Microcontroller(defaultProps), Constants.BlockName.MICROCONTROLLER, new Item.Properties())
    Items.registerBlock(new Print(Properties.of(Material.METAL).strength(1, 5).noOcclusion.dynamicShape), Constants.BlockName.PRINT, new Item.Properties())
    Items.registerBlockOnly(new RobotAfterimage(Properties.of(Material.AIR).noCollission.instabreak.noOcclusion.dynamicShape), Constants.BlockName.ROBOT_AFTER_IMAGE)
    Items.registerBlock(new RobotProxy(defaultProps.noOcclusion.dynamicShape), Constants.BlockName.ROBOT, new Item.Properties())

    // v1.5.10
    Items.registerBlock(new FakeEndstone(Properties.of(Material.STONE).strength(3, 15)), Constants.BlockName.ENDSTONE, defaultItemProps)

    // v1.5.14
    Items.registerBlock(new NetSplitter(defaultProps), Constants.BlockName.NET_SPLITTER, defaultItemProps)

    // v1.5.16
    Items.registerBlock(new Transposer(defaultProps), Constants.BlockName.TRANSPOSER, defaultItemProps)

    // v1.7.2
    Items.registerBlock(new CarpetedCapacitor(defaultProps), Constants.BlockName.CARPETED_CAPACITOR, defaultItemProps)
  }
}
