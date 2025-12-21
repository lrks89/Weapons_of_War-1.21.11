package net.wowmod.item.custom;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.AttackRange;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.TooltipDisplay;
import net.wowmod.WeaponsOfWar;

import java.util.function.Consumer;

public class WeaponItem extends Item {
    private final WeaponConfig config;

    // Unique ID for the range modifier to prevent conflicts
    private static final Identifier RANGE_MODIFIER_ID = Identifier.fromNamespaceAndPath(WeaponsOfWar.MOD_ID, "weapon_range");

    public WeaponItem(WeaponConfig config, Properties properties) {
        // In 1.21.11, we inject attribute modifiers directly into the item components.
        // This ensures tooltips and stats work automatically.
        // We also add the AttackRange component to handle minimum range logic.
        super(properties
                .component(DataComponents.MAX_DAMAGE, config.durability())
                .component(DataComponents.ATTACK_RANGE, new AttackRange(config.minRange(), config.maxRange(), config.minRange(), config.maxRange(), 0.3F, 1.0F))
                .component(DataComponents.ATTRIBUTE_MODIFIERS, createAttributes(config)));
        this.config = config;
    }

    private static ItemAttributeModifiers createAttributes(WeaponConfig config) {
        ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();

        // 1. Attack Damage
        // Base player damage is 1.0. We add (config - 1) to reach the total target damage.
        // e.g., if config is 8.0, we add 7.0. Total = 1.0 (base) + 7.0 (item) = 8.0.
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
        // Base player speed is 4.0. We add (config - 4.0).
        // e.g., if config is 1.6, we add -2.4. Total = 4.0 + (-2.4) = 1.6.
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
        // Base player reach is 3.0. We add (maxRange - 3.0).
        // e.g., if maxRange is 5.0, we add 2.0.
        builder.add(
                Attributes.ENTITY_INTERACTION_RANGE,
                new AttributeModifier(
                        RANGE_MODIFIER_ID,
                        (double) config.maxRange() - 3.0,
                        AttributeModifier.Operation.ADD_VALUE
                ),
                EquipmentSlotGroup.MAINHAND
        );

        return builder.build();
    }

    public WeaponConfig getConfig() {
        return config;
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