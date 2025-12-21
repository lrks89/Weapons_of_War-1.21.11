package net.wowmod;

import net.fabricmc.api.ModInitializer;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.wowmod.entity.ThrownWeaponEntity;
import net.wowmod.item.ModItemGroups;
import net.wowmod.item.ModItems;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WeaponsOfWar implements ModInitializer {
    public static final String MOD_ID = "wowmod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    // Create the Identifier and ResourceKey explicitly
    public static final Identifier THROWN_WEAPON_ID = Identifier.tryParse(MOD_ID + ":thrown_weapon");
    public static final ResourceKey<EntityType<?>> THROWN_WEAPON_KEY = ResourceKey.create(Registries.ENTITY_TYPE, THROWN_WEAPON_ID);

    // Register the Custom Entity Type
    public static final EntityType<ThrownWeaponEntity> THROWN_WEAPON = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            THROWN_WEAPON_ID,
            EntityType.Builder.<ThrownWeaponEntity>of(ThrownWeaponEntity::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    // Fix: Pass the ResourceKey to build() instead of the String ID
                    .build(THROWN_WEAPON_KEY)
    );

    @Override
    public void onInitialize() {
        ModItemGroups.registerItemGroups();
        ModItems.registerModItems();

        LOGGER.info("Registering Entities for " + MOD_ID);
    }
}