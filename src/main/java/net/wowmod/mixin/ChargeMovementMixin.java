package net.wowmod.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.wowmod.item.custom.WeaponItem;
import net.wowmod.item.custom.enums.ActionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to prevent the movement speed penalty while using a weapon with the CHARGE ActionType.
 * This targets the movement input processing to ensure full speed during a charge.
 */
@Mixin(LivingEntity.class)
public abstract class ChargeMovementMixin {

    @Shadow public abstract ItemStack getUseItem();
    @Shadow public abstract boolean isUsingItem();
    @Shadow public float xxa;
    @Shadow public float zza;

    /**
     * Minecraft's movement logic (specifically in Player.applyInput) multiplies movement
     * by 0.2 when an item is in use. We inject after applyInput() to restore full speed.
     */
    @Inject(
            method = "aiStep",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;applyInput()V", shift = At.Shift.AFTER)
    )
    private void wowmod$restoreChargeMovementSpeed(CallbackInfo ci) {
        // Only apply to Players as they are the ones subjected to the usage penalty
        if ((Object)this instanceof Player) {
            if (this.isUsingItem()) {
                ItemStack itemStack = this.getUseItem();

                if (itemStack.getItem() instanceof WeaponItem weaponItem) {
                    if (weaponItem.getConfig().actionType() == ActionType.CHARGE) {
                        // Restore the 0.2x multiplier applied by vanilla usage logic (1 / 0.2 = 5)
                        this.xxa *= 5.0F;
                        this.zza *= 5.0F;
                    }
                }
            }
        }
    }
}