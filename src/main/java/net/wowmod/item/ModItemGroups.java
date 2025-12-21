package net.wowmod.item;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.wowmod.WeaponsOfWar;

public class ModItemGroups {
    public static final CreativeModeTab WEAPONS_OF_WAR_GROUP = Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB,
            Identifier.fromNamespaceAndPath(WeaponsOfWar.MOD_ID, "weapons_of_war"),
            FabricItemGroup.builder()
                    .icon(() -> new ItemStack(ModItems.TEST_LONGSWORD))
                    .title(Component.translatable("itemGroup.wowmod.weapons_of_war"))
                    .displayItems((context, entries) -> {
                        // Swords
                        entries.accept(ModItems.TEST_DAGGER);
                        entries.accept(ModItems.TEST_SHORTSWORD);
                        entries.accept(ModItems.TEST_LONGSWORD);
                        entries.accept(ModItems.TEST_GREATSWORD);

                        // Axes
                        entries.accept(ModItems.TEST_CLAW);
                        entries.accept(ModItems.TEST_SHORTAXE);
                        entries.accept(ModItems.TEST_LONGAXE);
                        entries.accept(ModItems.TEST_GREATAXE);

                        // Maces
                        entries.accept(ModItems.TEST_FIST);
                        entries.accept(ModItems.TEST_SHORTMACE);
                        entries.accept(ModItems.TEST_LONGMACE);
                        entries.accept(ModItems.TEST_GREATMACE);

                        // Tridents
                        entries.accept(ModItems.TEST_TRIDENT);

                        // Spears
                        entries.accept(ModItems.TEST_SPEAR);

                        // Shield
                        entries.accept(ModItems.TEST_SHIELD);
                    })
                    .build());

    public static void registerItemGroups() {
        WeaponsOfWar.LOGGER.info("Registering Item Groups for " + WeaponsOfWar.MOD_ID);
    }
}