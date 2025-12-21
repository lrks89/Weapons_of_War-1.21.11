package net.wowmod.item.custom.mechanics;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.ThrownTrident;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.level.Level;
import net.wowmod.item.custom.WeaponConfig;
import net.wowmod.item.custom.enums.ActionType;

public class ThrowingMechanics {

    public static InteractionResult handleUse(WeaponConfig config, Level level, Player player, InteractionHand hand) {
        if (config.actionType() != ActionType.THROW) {
            return InteractionResult.PASS;
        }

        ItemStack itemStack = player.getItemInHand(hand);

        // Don't allow throwing if broken
        if (itemStack.getDamageValue() >= itemStack.getMaxDamage() - 1) {
            return InteractionResult.FAIL;
        }

        player.startUsingItem(hand);
        return InteractionResult.CONSUME;
    }

    public static void releaseUsing(WeaponConfig config, ItemStack stack, Level level, LivingEntity entity, int timeCharged) {
        if (config.actionType() != ActionType.THROW) {
            return;
        }

        if (entity instanceof Player player) {
            // Corrected: getUseDuration belongs to ItemStack, not Player
            int i = stack.getUseDuration(entity) - timeCharged;
            if (i < 10) { // Minimum charge time (same as Trident)
                return;
            }

            // Corrected: isClientSide is a private field, must use the method isClientSide()
            if (!level.isClientSide()) {
                // Corrected: LivingEntity.getSlotForHand is missing in these mappings, manually map InteractionHand to EquipmentSlot
                EquipmentSlot slot = player.getUsedItemHand() == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
                stack.hurtAndBreak(1, player, slot);

                // Create the projectile.
                // Note: The stack passed here allows the entity to retain enchantments (like Loyalty).
                ThrownTrident tridentEntity = new ThrownTrident(level, player, stack);

                // Setup throw physics (pitch, yaw, roll, speed, divergence)
                tridentEntity.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 2.5F, 1.0F);

                if (player.getAbilities().instabuild) {
                    // Corrected: Access Pickup enum via ThrownTrident to avoid importing AbstractArrow
                    tridentEntity.pickup = ThrownTrident.Pickup.CREATIVE_ONLY;
                }

                level.addFreshEntity(tridentEntity);

                // Play Throw Sound
                // Corrected: Uses coordinates explicitly and extracts .value() from the SoundEvent Holder
                level.playSound(null, tridentEntity.getX(), tridentEntity.getY(), tridentEntity.getZ(), SoundEvents.TRIDENT_THROW.value(), SoundSource.PLAYERS, 1.0F, 1.0F);

                if (!player.getAbilities().instabuild) {
                    player.getInventory().removeItem(stack);
                }
            }

            player.awardStat(Stats.ITEM_USED.get(stack.getItem()));
        }
    }

    public static ItemUseAnimation getUseAnimation(WeaponConfig config) {
        if (config.actionType() == ActionType.THROW) {
            return ItemUseAnimation.SPEAR;
        }
        return null;
    }

    public static int getUseDuration(WeaponConfig config) {
        if (config.actionType() == ActionType.THROW) {
            return 72000;
        }
        return 0;
    }
}