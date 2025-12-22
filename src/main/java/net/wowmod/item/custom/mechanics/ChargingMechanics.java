package net.wowmod.item.custom.mechanics;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.KineticWeapon;
import net.minecraft.world.level.Level;
import net.wowmod.WeaponsOfWar;
import net.wowmod.item.custom.WeaponConfig;
import net.wowmod.item.custom.enums.ActionType;

import java.util.Optional;

public class ChargingMechanics {

    public static void applyComponents(WeaponConfig config, Item.Properties properties) {
        if (config.actionType() == ActionType.CHARGE) {
            // UPDATED: We set the maxDurationTicks to 200 (10 seconds).
            // The animation logic in SpearAnimations will see this and keep the weapon
            // steady until approximately tick 160 (8 seconds), then start shaking.
            properties.component(DataComponents.KINETIC_WEAPON, new KineticWeapon(
                    10, // contactCooldownTicks
                    10, // delayTicks
                    Optional.empty(),
                    Optional.empty(),
                    KineticWeapon.Condition.ofAttackerSpeed(200, 0.2f), // 200 ticks duration
                    0.5f,
                    1.5f,
                    Optional.empty(),
                    Optional.empty()
            ));
        }
    }

    public static void addTooltipAttributes(WeaponConfig config, ItemAttributeModifiers.Builder builder) {
        if (config.actionType() == ActionType.CHARGE) {
            builder.add(
                    Attributes.ENTITY_INTERACTION_RANGE,
                    new AttributeModifier(
                            Identifier.fromNamespaceAndPath(WeaponsOfWar.MOD_ID, "weapon_charge_ability"),
                            0.0,
                            AttributeModifier.Operation.ADD_VALUE
                    ),
                    EquipmentSlotGroup.MAINHAND,
                    ItemAttributeModifiers.Display.override(
                            Component.literal(" ")
                                    .append(Component.translatable("tooltip.wowmod.charge_action"))
                                    .withStyle(ChatFormatting.BLUE)
                    )
            );
        }
    }

    public static InteractionResult use(Level world, Player user, InteractionHand hand) {
        ItemStack itemStack = user.getItemInHand(hand);
        if (itemStack.has(DataComponents.KINETIC_WEAPON)) {
            user.startUsingItem(hand);
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    public static void usageTick(Level world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        if (!world.isClientSide()) {
            KineticWeapon kineticWeapon = stack.get(DataComponents.KINETIC_WEAPON);
            if (kineticWeapon != null) {
                EquipmentSlot slot = user.getUsedItemHand() == InteractionHand.MAIN_HAND
                        ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
                kineticWeapon.damageEntities(stack, remainingUseTicks, user, slot);
            }
        }
    }
}