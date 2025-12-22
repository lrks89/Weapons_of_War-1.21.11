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

/**
 * Handles the logic for weapons with the CHARGE action type.
 * Utilizes the Vanilla KineticWeapon component for velocity-based damage.
 */
public class ChargingMechanics {

    /**
     * Applies the KineticWeapon component during item registration.
     */
    public static void applyComponents(WeaponConfig config, Item.Properties properties) {
        if (config.actionType() == ActionType.CHARGE) {
            // Define standard charge conditions (e.g., dismount, knockback, damage)
            // Using standard vanilla-like values for a kinetic weapon
            properties.component(DataComponents.KINETIC_WEAPON, new KineticWeapon(
                    10, // contactCooldownTicks
                    10, // delayTicks
                    Optional.empty(), // dismount
                    Optional.empty(), // knockback
                    KineticWeapon.Condition.ofAttackerSpeed(20, 0.2f), // damage conditions
                    0.5f, // forwardMovement
                    1.5f, // damageMultiplier
                    Optional.empty(), // sound
                    Optional.empty()  // hitSound
            ));
        }
    }

    /**
     * Adds tooltip information for the charging mechanic.
     */
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
                                    .append(Component.literal("Kinetic Charge Action"))
                                    .withStyle(ChatFormatting.BLUE)
                    )
            );
        }
    }

    /**
     * Called when the player right-clicks with a charging weapon.
     */
    public static InteractionResult use(Level world, Player user, InteractionHand hand) {
        ItemStack itemStack = user.getItemInHand(hand);

        // Ensure the item has the kinetic weapon component to function
        if (itemStack.has(DataComponents.KINETIC_WEAPON)) {
            user.startUsingItem(hand);
            return InteractionResult.CONSUME;
        }

        return InteractionResult.PASS;
    }

    /**
     * Called every tick while the item is being used (charged).
     * This triggers the vanilla KineticWeapon collision checks.
     */
    public static void usageTick(Level world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        if (!world.isClientSide()) {
            KineticWeapon kineticWeapon = stack.get(DataComponents.KINETIC_WEAPON);
            if (kineticWeapon != null) {
                // Determine slot (Primary hand usually)
                EquipmentSlot slot = user.getUsedItemHand() == InteractionHand.MAIN_HAND
                        ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;

                // Trigger vanilla damage/collision logic
                kineticWeapon.damageEntities(stack, remainingUseTicks, user, slot);
            }
        }
    }
}