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
import net.minecraft.world.item.ItemUseAnimation;
import net.wowmod.item.custom.enums.ActionType;
import net.wowmod.item.custom.mechanics.BlockingMechanics;
import net.wowmod.item.custom.mechanics.ChargingMechanics;
import net.wowmod.item.custom.mechanics.ThrowingMechanics;

import java.util.function.Consumer;

public class WeaponItem extends Item {
    private final WeaponConfig config;

    // Unique ID for the range modifier to prevent conflicts
    private static final Identifier RANGE_MODIFIER_ID = Identifier.fromNamespaceAndPath(WeaponsOfWar.MOD_ID, "weapon_range");

    public WeaponItem(WeaponConfig config, Properties properties) {
        // Apply base stats and delegate component application to mechanics classes
        super(applyWeaponComponents(config, properties));
        this.config = config;
    }

    private static Properties applyWeaponComponents(WeaponConfig config, Properties properties) {
        // Core components
        properties.stacksTo(1) // Make weapons unstackable
                .component(DataComponents.MAX_DAMAGE, config.durability())
                .component(DataComponents.ATTACK_RANGE, new AttackRange(config.minRange(), config.maxRange(), config.minRange(), config.maxRange(), 0.3F, 1.0F))
                .component(DataComponents.ATTRIBUTE_MODIFIERS, createAttributes(config))
                .component(DataComponents.WEAPON, new Weapon(config.durabilityPerHit()));

        // Delegate blocking logic
        BlockingMechanics.applyComponents(config, properties);

        // Delegate charging logic (Adds KineticWeapon component)
        ChargingMechanics.applyComponents(config, properties);

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

        // 4. Delegate Tooltips for specialized mechanics
        BlockingMechanics.addTooltipAttributes(config, builder);
        ChargingMechanics.addTooltipAttributes(config, builder);

        return builder.build();
    }

    public WeaponConfig getConfig() {
        return config;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        // 1. Try Blocking
        InteractionResult blockResult = BlockingMechanics.handleUse(config, level, player, hand);
        if (blockResult != InteractionResult.PASS) {
            return blockResult;
        }

        // 2. Try Throwing
        InteractionResult throwResult = ThrowingMechanics.handleUse(config, level, player, hand);
        if (throwResult != InteractionResult.PASS) {
            return throwResult;
        }

        // 3. Try Charging
        InteractionResult chargeResult = ChargingMechanics.use(level, player, hand);
        if (chargeResult != InteractionResult.PASS) {
            return chargeResult;
        }

        return super.use(level, player, hand);
    }

    @Override
    public boolean releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeCharged) {
        // Handle Throw release
        ThrowingMechanics.releaseUsing(config, stack, level, entity, timeCharged);

        return super.releaseUsing(stack, level, entity, timeCharged);
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        // Check Blocking Animation
        ItemUseAnimation animation = BlockingMechanics.getUseAnimation(config);
        if (animation != null) return animation;

        // Check Throwing Animation
        animation = ThrowingMechanics.getUseAnimation(config);
        if (animation != null) return animation;

        // Check Charging Animation
        if (config.actionType() == ActionType.CHARGE) {
            return ItemUseAnimation.SPEAR;
        }

        return super.getUseAnimation(stack);
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        // Check Blocking Duration
        int duration = BlockingMechanics.getUseDuration(config);
        if (duration > 0) return duration;

        // Check Throwing Duration
        duration = ThrowingMechanics.getUseDuration(config);
        if (duration > 0) return duration;

        // Check Charging Duration
        if (config.actionType() == ActionType.CHARGE) {
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