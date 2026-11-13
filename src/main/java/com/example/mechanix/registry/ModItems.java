package com.example.mechanix.registry;

import com.example.mechanix.MechanixMod;
import com.example.mechanix.item.*;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, MechanixMod.MODID);

    public static final RegistryObject<Item> BLINK_STAFF = ITEMS.register("blink_staff",
            () -> new BlinkStaffItem(new Item.Properties().durability(128)));

    public static final RegistryObject<Item> VEIN_PICKAXE = ITEMS.register("vein_pickaxe",
            () -> new VeinPickaxeItem(Tiers.IRON, 1, -2.8F, new Item.Properties()));

    public static final RegistryObject<Item> MAGNET_AMULET = ITEMS.register("magnet_amulet",
            () -> new MagnetAmuletItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> STORM_ROD = ITEMS.register("storm_rod",
            () -> new StormRodItem(new Item.Properties().durability(25)));

    public static final RegistryObject<Item> VAMPIRE_FANG = ITEMS.register("vampire_fang",
            () -> new VampireFangItem(new Item.Properties().stacksTo(16)));

    public static final RegistryObject<Item> TIME_HOURGLASS = ITEMS.register("time_hourglass",
            () -> new TimeHourglassItem(new Item.Properties().durability(16)));

    public static final RegistryObject<Item> LAUNCH_PAD = blockItem("launch_pad", ModBlocks.LAUNCH_PAD);
    public static final RegistryObject<Item> SPEED_PAD = blockItem("speed_pad", ModBlocks.SPEED_PAD);
    public static final RegistryObject<Item> HEAL_PAD = blockItem("heal_pad", ModBlocks.HEAL_PAD);
    public static final RegistryObject<Item> FRAGILE_BLOCK = blockItem("fragile_block", ModBlocks.FRAGILE_BLOCK);

    private static RegistryObject<Item> blockItem(String name, RegistryObject<Block> block) {
        return ITEMS.register(name, () -> new DescribedBlockItem(block.get(), new Item.Properties()));
    }
}
