package net.wowmod.item;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.wowmod.WeaponsOfWar;
import net.wowmod.item.custom.WeaponConfig;
import net.wowmod.item.custom.WeaponItem;
import net.wowmod.item.custom.enums.ActionType;
import net.wowmod.item.custom.enums.WeaponFamily;
import net.wowmod.item.custom.enums.WeaponStance;

import java.util.function.Function;

public class ModItems {

    // ---------- SWORDS ----------

    public static final Item TEST_DAGGER = registerItem("test_01_dagger",
            (properties) -> new WeaponItem(new WeaponConfig(
                    WeaponFamily.DAGGER, WeaponStance.ONE_HANDED,
                    8.0, 1.6, 0f, 2.5f, 2031, 1,
                    false, false, false, false,
                    ActionType.THROW, 10, 0.5f, 2000
            ), properties));

    public static final Item TEST_SHORTSWORD = registerItem("test_02_shortsword",
            (properties) -> new WeaponItem(new WeaponConfig(
                    WeaponFamily.SHORTSWORD, WeaponStance.ONE_HANDED,
                    8.0, 1.6, 0.5f, 3f, 2031, 1,
                    false, false, false, false,
                    ActionType.BLOCK, 10, 0.5f, 15
            ), properties));

    public static final Item TEST_LONGSWORD = registerItem("test_03_longsword",
            (properties) -> new WeaponItem(new WeaponConfig(
                    WeaponFamily.LONGSWORD, WeaponStance.ONE_HANDED,
                    8.0, 1.6, 1f, 3.5f, 2031, 1,
                    false, false, false, false,
                    ActionType.BLOCK, 10, 0.6f, 15
            ), properties));

    public static final Item TEST_GREATSWORD = registerItem("test_04_greatsword",
            (properties) -> new WeaponItem(new WeaponConfig(
                    WeaponFamily.GREATSWORD, WeaponStance.ONE_HANDED,
                    8.0, 1.6, 1.5f, 4f, 2031, 1,
                    false, false, false, false,
                    ActionType.BLOCK, 10, 0.7f, 15
            ), properties));

    // ---------- AXES ----------

    public static final Item TEST_CLAW = registerItem("test_11_claw",
            (properties) -> new WeaponItem(new WeaponConfig(
                    WeaponFamily.CLAW, WeaponStance.ONE_HANDED,
                    8.0, 1.6, 0f, 2.5f, 2031, 1,
                    false, false, false, false,
                    ActionType.BLOCK, 10, 0.5f, 10
            ), properties));

    public static final Item TEST_SHORTAXE = registerItem("test_12_shortaxe",
            (properties) -> new WeaponItem(new WeaponConfig(
                    WeaponFamily.SHORTAXE, WeaponStance.ONE_HANDED,
                    8.0, 1.6, 0.5f, 3f, 2031, 1,
                    false, false, false, false,
                    ActionType.BLOCK, 10, 0.5f, 5
            ), properties));

    public static final Item TEST_LONGAXE = registerItem("test_13_longaxe",
            (properties) -> new WeaponItem(new WeaponConfig(
                    WeaponFamily.LONGAXE, WeaponStance.ONE_HANDED,
                    8.0, 1.6, 1f, 3.5f, 2031, 1,
                    false, false, false, false,
                    ActionType.BLOCK, 10, 0.6f, 5
            ), properties));

    public static final Item TEST_GREATAXE = registerItem("test_14_greataxe",
            (properties) -> new WeaponItem(new WeaponConfig(
                    WeaponFamily.GREATAXE, WeaponStance.ONE_HANDED,
                    8.0, 1.6, 1.5f, 4f, 2031, 1,
                    false, false, false, false,
                    ActionType.BLOCK, 10, 0.7f, 5
            ), properties));

    // ---------- MACES ----------

    public static final Item TEST_FIST = registerItem("test_21_fist",
            (properties) -> new WeaponItem(new WeaponConfig(
                    WeaponFamily.FIST, WeaponStance.ONE_HANDED,
                    8.0, 1.6, 0f, 2.5f, 2031, 1,
                    false, false, false, false,
                    ActionType.BLOCK, 10, 0.5f, 10
            ), properties));

    public static final Item TEST_SHORTMACE = registerItem("test_22_shortmace",
            (properties) -> new WeaponItem(new WeaponConfig(
                    WeaponFamily.SHORTMACE, WeaponStance.ONE_HANDED,
                    8.0, 1.6, 0.5f, 3f, 2031, 1,
                    false, false, false, false,
                    ActionType.BLOCK, 10, 0.5f, 5
            ), properties));

    public static final Item TEST_LONGMACE = registerItem("test_23_longmace",
            (properties) -> new WeaponItem(new WeaponConfig(
                    WeaponFamily.LONGMACE, WeaponStance.ONE_HANDED,
                    8.0, 1.6, 1f, 3.5f, 2031, 1,
                    false, false, false, false,
                    ActionType.BLOCK, 10, 0.6f, 5
            ), properties));

    public static final Item TEST_GREATMACE = registerItem("test_24_greatmace",
            (properties) -> new WeaponItem(new WeaponConfig(
                    WeaponFamily.GREATMACE, WeaponStance.ONE_HANDED,
                    8.0, 1.6, 1.5f, 4f, 2031, 1,
                    false, false, false, false,
                    ActionType.BLOCK, 10, 0.7f, 5
            ), properties));

    // ---------- TRIDENTS ----------

    public static final Item TEST_TRIDENT = registerItem("test_31_trident",
            (properties) -> new WeaponItem(new WeaponConfig(
                    WeaponFamily.TRIDENT, WeaponStance.ONE_HANDED,
                    8.0, 1.6, 1f, 3.5f, 2031, 1,
                    false, false, false, false,
                    ActionType.THROW, 10, 0.5f, 5
            ), properties));

    // ---------- SPEARS ----------

    public static final Item TEST_SPEAR = registerItem("test_41_spear",
            (properties) -> new WeaponItem(new WeaponConfig(
                    WeaponFamily.SPEAR, WeaponStance.ONE_HANDED,
                    8.0, 1.6, 2f, 4.5f, 2031, 1,
                    false, false, false, false,
                    ActionType.BLOCK, 10, 0.5f, 5
            ), properties));

    // ---------- SHIELDS ----------

    public static final Item TEST_SHIELD = registerItem("test_51_shield",
            (properties) -> new WeaponItem(new WeaponConfig(
                    WeaponFamily.SHIELD, WeaponStance.ONE_HANDED,
                    8.0, 1.6, 0.5f, 3.0f, 2031, 1,
                    false, false, false, false,
                    ActionType.BLOCK, 6, 1f, 10
            ), properties));

    private static Item registerItem(String name, Function<Item.Properties, Item> factory) {
        Identifier id = Identifier.fromNamespaceAndPath(WeaponsOfWar.MOD_ID, name);
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, id);

        Item.Properties properties = new Item.Properties().setId(key);
        Item item = factory.apply(properties);

        return Registry.register(BuiltInRegistries.ITEM, key, item);
    }

    public static void registerModItems() {
        WeaponsOfWar.LOGGER.info("Registering Mod Items for " + WeaponsOfWar.MOD_ID);
    }
}