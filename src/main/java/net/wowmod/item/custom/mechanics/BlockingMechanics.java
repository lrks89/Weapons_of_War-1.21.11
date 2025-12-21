package net.wowmod.item.custom.mechanics;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.component.BlocksAttacks;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;
import net.wowmod.WeaponsOfWar;
import net.wowmod.item.custom.WeaponConfig;
import net.wowmod.item.custom.enums.ActionType;
import net.wowmod.item.custom.enums.WeaponFamily; // Import WeaponFamily
import net.wowmod.item.custom.enums.WeaponStance;

import java.util.List;
import java.util.Optional;

public class BlockingMechanics {

    public static void applyComponents(WeaponConfig config, Item.Properties properties) {
        if (config.actionType() == ActionType.BLOCK) {
            float mitigationFactor = config.blockMitigation();

            // Select sound based on Weapon Family
            SoundEvent blockSound = getBlockSoundForFamily(config.family());

            // Re-enabled the sound in the component so it plays automatically on block
            properties.component(DataComponents.BLOCKS_ATTACKS, new BlocksAttacks(
                    0.0F, // blockDelaySeconds
                    1.0F, // disableCooldownScale
                    List.of(new BlocksAttacks.DamageReduction(90.0F, Optional.empty(), 0.0F, mitigationFactor)),
                    BlocksAttacks.ItemDamageFunction.DEFAULT,
                    Optional.empty(),
                    Optional.of(Holder.direct(blockSound)), // Enabled default sound
                    Optional.empty()
            ));
        }
    }

    // Helper method to determine the block sound
    private static SoundEvent getBlockSoundForFamily(WeaponFamily family) {
        // SoundEvents fields are Holder<SoundEvent>, so we use .value() to get the raw SoundEvent
        switch (family) {
            case SHIELD:
                return SoundEvents.SHIELD_BLOCK.value();
            case DAGGER:
            case SHORTSWORD:
            case LONGSWORD:
            case GREATSWORD:
            case SPEAR:
            case TRIDENT:
                return SoundEvents.ANVIL_PLACE;
            case SHORTAXE:
            case LONGAXE:
            case GREATAXE:
            case CLAW:
                return SoundEvents.ANVIL_PLACE;
            case SHORTMACE:
            case LONGMACE:
            case GREATMACE:
            case FIST:
                // Heavy impact for blunt weapons
                return SoundEvents.ANVIL_LAND;
            default:
                // Fallback
                return SoundEvents.SHIELD_BLOCK.value();
        }
    }

    // Updated to play sounds manually with custom pitch/volume per family
    // This method is available for manual calls (e.g. from mixins) but not used by BlocksAttacks directly.
    public static void playBlockSound(Player player, ServerLevel level, WeaponFamily family) {
        float pitch = 0.8F + level.random.nextFloat() * 0.2F;

        switch (family) {
            case SHORTAXE, LONGAXE, GREATAXE, CLAW ->
                    level.playSound(null, player.getX(), player.getY(), player.getZ(),
                            SoundEvents.ANVIL_PLACE, SoundSource.PLAYERS, 0.8F, pitch);

            case SHORTMACE, LONGMACE, GREATMACE, FIST ->
                    level.playSound(null, player.getX(), player.getY(), player.getZ(),
                            SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 0.8F, pitch);

            case SHIELD ->
                    level.playSound(null, player.getX(), player.getY(), player.getZ(),
                            SoundEvents.SHIELD_BLOCK, SoundSource.PLAYERS, 1.0F, 1.2F + level.random.nextFloat() * 0.1F);

            default ->
                    level.playSound(null, player.getX(), player.getY(), player.getZ(),
                            SoundEvents.ANVIL_PLACE, SoundSource.PLAYERS, 1.0F, 1.3F + level.random.nextFloat() * 0.1F);
        }
    }

    public static void addTooltipAttributes(WeaponConfig config, ItemAttributeModifiers.Builder builder) {
        if (config.actionType() == ActionType.BLOCK) {
            int mitigationPercent = (int) (config.blockMitigation() * 100);
            double parryWindowSeconds = config.parryWindow() / 20.0;

            builder.add(
                    Attributes.ENTITY_INTERACTION_RANGE,
                    new AttributeModifier(
                            Identifier.fromNamespaceAndPath(WeaponsOfWar.MOD_ID, "weapon_block_mitigation"),
                            0.0,
                            AttributeModifier.Operation.ADD_VALUE
                    ),
                    EquipmentSlotGroup.MAINHAND,
                    ItemAttributeModifiers.Display.override(
                            Component.literal(" ")
                                    .append(Component.literal(mitigationPercent + "% Block Mitigation"))
                                    .withStyle(ChatFormatting.DARK_BLUE)
                    )
            );

            builder.add(
                    Attributes.ENTITY_INTERACTION_RANGE,
                    new AttributeModifier(
                            Identifier.fromNamespaceAndPath(WeaponsOfWar.MOD_ID, "weapon_parry_window"),
                            0.0,
                            AttributeModifier.Operation.ADD_VALUE
                    ),
                    EquipmentSlotGroup.MAINHAND,
                    ItemAttributeModifiers.Display.override(
                            Component.literal(" ")
                                    .append(Component.literal(String.format("%.1fs Parry Window", parryWindowSeconds)))
                                    .withStyle(ChatFormatting.DARK_BLUE)
                    )
            );
        }
    }

    public static InteractionResult handleUse(WeaponConfig config, Level level, Player player, InteractionHand hand) {
        if (config.actionType() != ActionType.BLOCK) {
            return InteractionResult.PASS;
        }

        ItemStack itemStack = player.getItemInHand(hand);

        if (player.getCooldowns().isOnCooldown(itemStack)) {
            return InteractionResult.FAIL;
        }

        if (hand == InteractionHand.MAIN_HAND && config.stance() == WeaponStance.ONE_HANDED) {
            ItemStack offhandStack = player.getItemInHand(InteractionHand.OFF_HAND);
            if (offhandStack.getItem() instanceof ShieldItem) {
                return InteractionResult.PASS;
            }
        }

        // Start blocking
        player.startUsingItem(hand);

        // Initialize Parry Timer
        if (!level.isClientSide()) {
            ParryMechanics.initializeParry(player);

            if (config.abilityCooldown() > 0) {
                player.getCooldowns().addCooldown(itemStack, config.abilityCooldown());
            }
        }

        return InteractionResult.CONSUME;
    }

    public static ItemUseAnimation getUseAnimation(WeaponConfig config) {
        if (config.actionType() == ActionType.BLOCK) {
            return ItemUseAnimation.BLOCK;
        }
        return null;
    }

    public static int getUseDuration(WeaponConfig config) {
        if (config.actionType() == ActionType.BLOCK) {
            return 72000;
        }
        return 0;
    }
}