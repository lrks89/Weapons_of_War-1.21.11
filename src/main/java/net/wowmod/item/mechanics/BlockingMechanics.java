package net.wowmod.item.mechanics;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.component.BlocksAttacks;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;
import net.wowmod.WeaponsOfWar;
import net.wowmod.item.custom.WeaponConfig;
import net.wowmod.item.enums.ActionType;
import net.wowmod.item.enums.WeaponStance;
import net.minecraft.world.item.Item;

import java.util.List;
import java.util.Optional;

public class BlockingMechanics {

    /**
     * Applies the BLOCKS_ATTACKS component if the weapon is configured to block.
     */
    public static void applyComponents(WeaponConfig config, Item.Properties properties) {
        if (config.actionType() == ActionType.BLOCK) {
            // Factor represents the proportion of damage to BLOCK.
            // 1.0f means 100% mitigation.
            float mitigationFactor = config.blockMitigation();

            properties.component(DataComponents.BLOCKS_ATTACKS, new BlocksAttacks(
                    0.0F, // blockDelaySeconds
                    1.0F, // disableCooldownScale
                    List.of(new BlocksAttacks.DamageReduction(90.0F, Optional.empty(), 0.0F, mitigationFactor)),
                    BlocksAttacks.ItemDamageFunction.DEFAULT,
                    Optional.empty(),
                    Optional.empty(),
                    Optional.empty()
            ));
        }
    }

    /**
     * Adds visual attributes for Block Mitigation and Parry Window to the tooltip.
     */
    public static void addTooltipAttributes(WeaponConfig config, ItemAttributeModifiers.Builder builder) {
        if (config.actionType() == ActionType.BLOCK) {
            int mitigationPercent = (int) (config.blockMitigation() * 100);
            double parryWindowSeconds = config.parryWindow() / 20.0;

            // Block Mitigation Attribute
            builder.add(
                    Attributes.ENTITY_INTERACTION_RANGE, // Attached to Range to group at bottom
                    new AttributeModifier(
                            Identifier.fromNamespaceAndPath(WeaponsOfWar.MOD_ID, "weapon_block_mitigation"),
                            0.0,
                            AttributeModifier.Operation.ADD_VALUE
                    ),
                    EquipmentSlotGroup.MAINHAND,
                    ItemAttributeModifiers.Display.override(
                            Component.literal(" ")
                                    .append(Component.literal(mitigationPercent + "% Block Mitigation"))
                                    .withStyle(ChatFormatting.GREEN)
                    )
            );

            // Parry Window Attribute
            builder.add(
                    Attributes.ENTITY_INTERACTION_RANGE, // Attached to Range to group at bottom
                    new AttributeModifier(
                            Identifier.fromNamespaceAndPath(WeaponsOfWar.MOD_ID, "weapon_parry_window"),
                            0.0,
                            AttributeModifier.Operation.ADD_VALUE
                    ),
                    EquipmentSlotGroup.MAINHAND,
                    ItemAttributeModifiers.Display.override(
                            Component.literal(" ")
                                    .append(Component.literal(String.format("%.1fs Parry Window", parryWindowSeconds)))
                                    .withStyle(ChatFormatting.YELLOW)
                    )
            );
        }
    }

    /**
     * Handles the item use action for blocking.
     * Returns InteractionResult.PASS if blocking logic shouldn't apply (delegating to super),
     * otherwise returns CONSUME or FAIL.
     */
    public static InteractionResult handleUse(WeaponConfig config, Level level, Player player, InteractionHand hand) {
        if (config.actionType() != ActionType.BLOCK) {
            return InteractionResult.PASS;
        }

        ItemStack itemStack = player.getItemInHand(hand);

        // Fail if item is on cooldown (e.g. shield break / parry stun)
        if (player.getCooldowns().isOnCooldown(itemStack)) {
            return InteractionResult.FAIL;
        }

        // Stance Logic: If One-Handed, prioritize Shield in offhand.
        // If the player has a shield in offhand, let the shield handle the block (return PASS).
        if (hand == InteractionHand.MAIN_HAND && config.stance() == WeaponStance.ONE_HANDED) {
            ItemStack offhandStack = player.getItemInHand(InteractionHand.OFF_HAND);
            if (offhandStack.getItem() instanceof ShieldItem) {
                return InteractionResult.PASS;
            }
        }

        // Start the blocking action
        player.startUsingItem(hand);

        // Apply Ability Cooldown (Parry Window / Recovery)
        if (!level.isClientSide() && config.abilityCooldown() > 0) {
            player.getCooldowns().addCooldown(itemStack, config.abilityCooldown());
        }

        return InteractionResult.CONSUME;
    }

    public static ItemUseAnimation getUseAnimation(WeaponConfig config) {
        if (config.actionType() == ActionType.BLOCK) {
            return ItemUseAnimation.BLOCK;
        }
        return null; // Return null to indicate fallback to default
    }

    public static int getUseDuration(WeaponConfig config) {
        if (config.actionType() == ActionType.BLOCK) {
            return 72000;
        }
        return 0; // Return 0 to indicate fallback
    }
}