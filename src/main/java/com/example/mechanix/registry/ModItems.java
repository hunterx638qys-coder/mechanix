package com.example.mechanix.registry;

import com.example.mechanix.MechanixMod;
import com.example.mechanix.item.AbyssBladeItem;
import com.example.mechanix.item.BonkBatItem;
import com.example.mechanix.item.CreativeWandItem;
import com.example.mechanix.item.GoblinDaggerItem;
import com.example.mechanix.item.GuideBookItem;
import com.example.mechanix.item.LifestealSwordItem;
import com.example.mechanix.item.ManaPotionItem;
import com.example.mechanix.item.OrcGreataxeItem;
import com.example.mechanix.item.ScrollItem;
import com.example.mechanix.item.StaffItem;
import com.example.mechanix.rpg.Spell;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BowlFoodItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemNameBlockItem;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, MechanixMod.MODID);

    private static Item.Properties p() {
        return new Item.Properties();
    }

    private static RegistryObject<Item> simple(String name) {
        return ITEMS.register(name, () -> new Item(p()));
    }

    private static RegistryObject<Item> epic(String name) {
        return ITEMS.register(name, () -> new Item(p().rarity(Rarity.EPIC).stacksTo(16)));
    }

    private static RegistryObject<Item> armor(String name, ModArmorMaterial material, ArmorItem.Type type) {
        return ITEMS.register(name, () -> new ArmorItem(material, type, p()));
    }

    private static RegistryObject<Item> blockItem(String name, RegistryObject<Block> block) {
        return ITEMS.register(name, () -> new BlockItem(block.get(), p()));
    }

    private static RegistryObject<Item> staff(String name, Spell spell) {
        return ITEMS.register(name, () -> new StaffItem(spell, p().durability(250).rarity(Rarity.RARE)));
    }

    // ---------- материалы ----------
    public static final RegistryObject<Item> RAW_MITHRIL = simple("raw_mithril");
    public static final RegistryObject<Item> MITHRIL_INGOT = simple("mithril_ingot");
    public static final RegistryObject<Item> RAW_STAR_IRON = simple("raw_star_iron");
    public static final RegistryObject<Item> STAR_IRON_INGOT = simple("star_iron_ingot");
    public static final RegistryObject<Item> BLOODSTONE = simple("bloodstone");
    public static final RegistryObject<Item> MANA_CRYSTAL = simple("mana_crystal");
    public static final RegistryObject<Item> GOBLIN_CROWN = epic("goblin_crown");
    public static final RegistryObject<Item> ORC_WAR_BANNER = epic("orc_war_banner");
    public static final RegistryObject<Item> LICH_CORE = epic("lich_core");

    // ---------- мифриловые инструменты ----------
    public static final RegistryObject<Item> MITHRIL_SWORD = ITEMS.register("mithril_sword", () -> new SwordItem(ModTiers.MITHRIL, 3, -2.4F, p()));
    public static final RegistryObject<Item> MITHRIL_PICKAXE = ITEMS.register("mithril_pickaxe", () -> new PickaxeItem(ModTiers.MITHRIL, 1, -2.8F, p()));
    public static final RegistryObject<Item> MITHRIL_AXE = ITEMS.register("mithril_axe", () -> new AxeItem(ModTiers.MITHRIL, 5.0F, -3.0F, p()));
    public static final RegistryObject<Item> MITHRIL_SHOVEL = ITEMS.register("mithril_shovel", () -> new ShovelItem(ModTiers.MITHRIL, 1.5F, -3.0F, p()));
    public static final RegistryObject<Item> MITHRIL_HOE = ITEMS.register("mithril_hoe", () -> new HoeItem(ModTiers.MITHRIL, -2, -1.0F, p()));

    // ---------- инструменты из звёздного железа ----------
    public static final RegistryObject<Item> STAR_IRON_SWORD = ITEMS.register("star_iron_sword", () -> new SwordItem(ModTiers.STAR_IRON, 3, -2.4F, p()));
    public static final RegistryObject<Item> STAR_IRON_PICKAXE = ITEMS.register("star_iron_pickaxe", () -> new PickaxeItem(ModTiers.STAR_IRON, 1, -2.8F, p()));
    public static final RegistryObject<Item> STAR_IRON_AXE = ITEMS.register("star_iron_axe", () -> new AxeItem(ModTiers.STAR_IRON, 5.0F, -3.0F, p()));
    public static final RegistryObject<Item> STAR_IRON_SHOVEL = ITEMS.register("star_iron_shovel", () -> new ShovelItem(ModTiers.STAR_IRON, 1.5F, -3.0F, p()));
    public static final RegistryObject<Item> STAR_IRON_HOE = ITEMS.register("star_iron_hoe", () -> new HoeItem(ModTiers.STAR_IRON, -3, 0.0F, p()));

    // ---------- броня ----------
    public static final RegistryObject<Item> MITHRIL_HELMET = armor("mithril_helmet", ModArmorMaterial.MITHRIL, ArmorItem.Type.HELMET);
    public static final RegistryObject<Item> MITHRIL_CHESTPLATE = armor("mithril_chestplate", ModArmorMaterial.MITHRIL, ArmorItem.Type.CHESTPLATE);
    public static final RegistryObject<Item> MITHRIL_LEGGINGS = armor("mithril_leggings", ModArmorMaterial.MITHRIL, ArmorItem.Type.LEGGINGS);
    public static final RegistryObject<Item> MITHRIL_BOOTS = armor("mithril_boots", ModArmorMaterial.MITHRIL, ArmorItem.Type.BOOTS);
    public static final RegistryObject<Item> STAR_IRON_HELMET = armor("star_iron_helmet", ModArmorMaterial.STAR_IRON, ArmorItem.Type.HELMET);
    public static final RegistryObject<Item> STAR_IRON_CHESTPLATE = armor("star_iron_chestplate", ModArmorMaterial.STAR_IRON, ArmorItem.Type.CHESTPLATE);
    public static final RegistryObject<Item> STAR_IRON_LEGGINGS = armor("star_iron_leggings", ModArmorMaterial.STAR_IRON, ArmorItem.Type.LEGGINGS);
    public static final RegistryObject<Item> STAR_IRON_BOOTS = armor("star_iron_boots", ModArmorMaterial.STAR_IRON, ArmorItem.Type.BOOTS);
    public static final RegistryObject<Item> BLOODSTONE_HELMET = armor("bloodstone_helmet", ModArmorMaterial.BLOODSTONE, ArmorItem.Type.HELMET);
    public static final RegistryObject<Item> BLOODSTONE_CHESTPLATE = armor("bloodstone_chestplate", ModArmorMaterial.BLOODSTONE, ArmorItem.Type.CHESTPLATE);
    public static final RegistryObject<Item> BLOODSTONE_LEGGINGS = armor("bloodstone_leggings", ModArmorMaterial.BLOODSTONE, ArmorItem.Type.LEGGINGS);
    public static final RegistryObject<Item> BLOODSTONE_BOOTS = armor("bloodstone_boots", ModArmorMaterial.BLOODSTONE, ArmorItem.Type.BOOTS);

    // ---------- особое оружие ----------
    public static final RegistryObject<Item> BLOODSTONE_BLADE = ITEMS.register("bloodstone_blade",
            () -> new LifestealSwordItem(ModTiers.BLOODSTONE, 3, -2.2F, p().rarity(Rarity.RARE)));
    public static final RegistryObject<Item> ABYSS_BLADE = ITEMS.register("abyss_blade",
            () -> new AbyssBladeItem(ModTiers.STAR_IRON, 4, -2.3F, p().rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> GOBLIN_DAGGER = ITEMS.register("goblin_dagger",
            () -> new GoblinDaggerItem(Tiers.IRON, 0, -1.2F, p().rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> ORC_GREATAXE = ITEMS.register("orc_greataxe",
            () -> new OrcGreataxeItem(ModTiers.STAR_IRON, 7.0F, -3.2F, p().rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> BONK_BAT = ITEMS.register("bonk_bat",
            () -> new BonkBatItem(Tiers.WOOD, 0, -2.0F, p().rarity(Rarity.UNCOMMON)));

    // ---------- посохи ----------
    public static final RegistryObject<Item> FIRE_STAFF = staff("fire_staff", Spell.FIREBALL);
    public static final RegistryObject<Item> FROST_STAFF = staff("frost_staff", Spell.ICE_SPIKE);
    public static final RegistryObject<Item> LIGHTNING_STAFF = staff("lightning_staff", Spell.CHAIN_LIGHTNING);
    public static final RegistryObject<Item> HEALING_STAFF = staff("healing_staff", Spell.HEAL);

    // ---------- расходники ----------
    public static final RegistryObject<Item> MANA_POTION = ITEMS.register("mana_potion",
            () -> new ManaPotionItem(40, p().stacksTo(16)));
    public static final RegistryObject<Item> GREAT_MANA_POTION = ITEMS.register("great_mana_potion",
            () -> new ManaPotionItem(120, p().stacksTo(16).rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> EXPERIENCE_TOME = ITEMS.register("experience_tome",
            () -> new ScrollItem(ScrollItem.Kind.TOME, p().stacksTo(16).rarity(Rarity.RARE)));
    public static final RegistryObject<Item> REBIRTH_SCROLL = ITEMS.register("rebirth_scroll",
            () -> new ScrollItem(ScrollItem.Kind.REBIRTH, p().stacksTo(16).rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> RESET_SCROLL = ITEMS.register("reset_scroll",
            () -> new ScrollItem(ScrollItem.Kind.RESET, p().stacksTo(16).rarity(Rarity.RARE)));

    // ---------- еда ----------
    public static final RegistryObject<Item> MOONROOT = ITEMS.register("moonroot",
            () -> new Item(p().food(new FoodProperties.Builder().nutrition(3).saturationMod(0.4F).build())));
    public static final RegistryObject<Item> MOONROOT_SEEDS = ITEMS.register("moonroot_seeds",
            () -> new ItemNameBlockItem(ModBlocks.MOONROOT_CROP.get(), p()));
    public static final RegistryObject<Item> HUNTER_STEW = ITEMS.register("hunter_stew",
            () -> new BowlFoodItem(p().stacksTo(1).food(new FoodProperties.Builder().nutrition(9).saturationMod(0.8F)
                    .effect(() -> new MobEffectInstance(MobEffects.DAMAGE_BOOST, 2400, 0), 1.0F).build())));

    // ---------- разное ----------
    public static final RegistryObject<Item> RPG_GUIDE = ITEMS.register("rpg_guide",
            () -> new GuideBookItem(p().stacksTo(1)));
    public static final RegistryObject<Item> CREATIVE_WAND = ITEMS.register("creative_wand",
            () -> new CreativeWandItem(p().stacksTo(1).rarity(Rarity.EPIC)));

    // ---------- яйца призыва ----------
    public static final RegistryObject<Item> GOBLIN_SPAWN_EGG = ITEMS.register("goblin_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.GOBLIN, 0x4A7A2E, 0x3B2A1A, p()));
    public static final RegistryObject<Item> ORC_SPAWN_EGG = ITEMS.register("orc_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.ORC, 0x2F5D2A, 0x7A1F1F, p()));
    public static final RegistryObject<Item> SKELETON_KNIGHT_SPAWN_EGG = ITEMS.register("skeleton_knight_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.SKELETON_KNIGHT, 0xC9C9C9, 0x4A4A55, p()));
    public static final RegistryObject<Item> CAVE_STALKER_SPAWN_EGG = ITEMS.register("cave_stalker_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.CAVE_STALKER, 0x2A1F3D, 0xD02020, p()));
    public static final RegistryObject<Item> GOBLIN_KING_SPAWN_EGG = ITEMS.register("goblin_king_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.GOBLIN_KING, 0x4A7A2E, 0xE5C100, p()));
    public static final RegistryObject<Item> ORC_WARLORD_SPAWN_EGG = ITEMS.register("orc_warlord_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.ORC_WARLORD, 0x1E3A1B, 0xB01010, p()));
    public static final RegistryObject<Item> LICH_SPAWN_EGG = ITEMS.register("lich_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.LICH, 0x3B1D5E, 0x7CF0FF, p()));

    // ---------- блоки ----------
    public static final RegistryObject<Item> MITHRIL_ORE = blockItem("mithril_ore", ModBlocks.MITHRIL_ORE);
    public static final RegistryObject<Item> STAR_IRON_ORE = blockItem("star_iron_ore", ModBlocks.STAR_IRON_ORE);
    public static final RegistryObject<Item> BLOODSTONE_ORE = blockItem("bloodstone_ore", ModBlocks.BLOODSTONE_ORE);
    public static final RegistryObject<Item> MANA_CRYSTAL_ORE = blockItem("mana_crystal_ore", ModBlocks.MANA_CRYSTAL_ORE);
}
