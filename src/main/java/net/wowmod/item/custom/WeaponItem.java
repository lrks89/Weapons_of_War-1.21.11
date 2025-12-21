package net.wowmod.item.custom;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.*;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.wowmod.WeaponsOfWar;
import net.wowmod.item.enums.ActionType;
import net.wowmod.item.enums.WeaponStance;
import net.minecraft.world.item.ItemUseAnimation;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class WeaponItem extends Item {
    private final WeaponConfig config;

    // Unique ID for the range modifier to prevent conflicts
    private static final Identifier RANGE_MODIFIER_ID = Identifier.fromNamespaceAndPath(WeaponsOfWar.MOD_ID, "weapon_range");

    public WeaponItem(WeaponConfig config, Properties properties) {
        // In 1.21.11, we inject attribute modifiers directly into the item components.
        // We also add the AttackRange component to handle minimum range logic.
        super(applyWeaponComponents(config, properties));
        this.config = config;
    }

    private static Properties applyWeaponComponents(WeaponConfig config, Properties properties) {
        properties.component(DataComponents.MAX_DAMAGE, config.durability())
                .component(DataComponents.ATTACK_RANGE, new AttackRange(config.minRange(), config.maxRange(), config.minRange(), config.maxRange(), 0.3F, 1.0F))
                .component(DataComponents.ATTRIBUTE_MODIFIERS, createAttributes(config))
                .component(DataComponents.WEAPON, new Weapon(config.durabilityPerHit()));

        // Enable actual damage reduction if the action type is BLOCK
        if (config.actionType() == ActionType.BLOCK) {
            // resolveBlockedDamage uses base and factor.
            // factor represents the PROPORTION of damage to BLOCK.
            // If config.blockMitigation() is 1.0 (100%), we want factor to be 1.0 so 100% of damage is blocked.
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

        return properties;
    }

    private static ItemAttributeModifiers createAttributes(WeaponConfig config) {
        ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();

        // 1. Attack Damage
        builder.add(
                Attributes.ATTACK_DAMAGE,
                new AttributeModifier(
                        Item.BASE_ATTACK_DAMAGE_ID,
                        config.attackDamage() - 1.0,
                        AttributeModifier.Operation.ADD_VALUE
                ),
                EquipmentSlotGroup.MAINHAND
        );

        // 2. Attack Speed
        builder.add(
                Attributes.ATTACK_SPEED,
                new AttributeModifier(
                        Item.BASE_ATTACK_SPEED_ID,
                        config.attackSpeed() - 4.0,
                        AttributeModifier.Operation.ADD_VALUE
                ),
                EquipmentSlotGroup.MAINHAND
        );

        // 3. Attack Range (Reach)
        builder.add(
                Attributes.ENTITY_INTERACTION_RANGE,
                new AttributeModifier(
                        RANGE_MODIFIER_ID,
                        (double) config.maxRange() - 3.0,
                        AttributeModifier.Operation.ADD_VALUE
                ),
                EquipmentSlotGroup.MAINHAND,
                ItemAttributeModifiers.Display.override(
                        Component.literal(" ")
                                .append(Component.literal(config.maxRange() + " Attack Range"))
                                .withStyle(ChatFormatting.DARK_GREEN)
                )
        );

        // 4. Block & Parry Info (Displayed as attributes to appear at the bottom)
        if (config.actionType() == ActionType.BLOCK) {
            int mitigationPercent = (int) (config.blockMitigation() * 100);
            double parryWindowSeconds = config.parryWindow() / 20.0;

            // Block Mitigation
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
                                    .withStyle(ChatFormatting.DARK_BLUE)
                    )
            );

            // Parry Window
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
                                    .withStyle(ChatFormatting.DARK_BLUE)
                    )
            );
        }

        return builder.build();
    }

    public WeaponConfig getConfig() {
        return config;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        if (config.actionType() == ActionType.BLOCK) {
            if (player.getCooldowns().isOnCooldown(itemStack)) {
                return InteractionResult.FAIL;
            }

            if (hand == InteractionHand.MAIN_HAND && config.stance() == WeaponStance.ONE_HANDED) {
                ItemStack offhandStack = player.getItemInHand(InteractionHand.OFF_HAND);
                if (offhandStack.getItem() instanceof ShieldItem) {
                    return InteractionResult.PASS;
                }
            }

            player.startUsingItem(hand);

            if (!level.isClientSide() && config.abilityCooldown() > 0) {
                player.getCooldowns().addCooldown(itemStack, config.abilityCooldown());
            }

            return InteractionResult.CONSUME;
        }

        return super.use(level, player, hand);
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        if (config.actionType() == ActionType.BLOCK) {
            return ItemUseAnimation.BLOCK;
        }
        return super.getUseAnimation(stack);
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        if (config.actionType() == ActionType.BLOCK) {
            return 72000;
        }
        return super.getUseDuration(stack, entity);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        tooltip.accept(Component.literal("Family: ").append(Component.literal(config.family().name())).withStyle(ChatFormatting.GRAY));
        tooltip.accept(Component.literal("Stance: ").append(Component.literal(config.stance().name())).withStyle(ChatFormatting.GRAY));

        if (config.isPiercing()) {
            tooltip.accept(Component.translatable("tooltip.wowmod.piercing").withStyle(ChatFormatting.GOLD));
        }

        super.appendHoverText(stack, context, display, tooltip, flag);
    }
}