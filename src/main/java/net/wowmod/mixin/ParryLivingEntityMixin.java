package net.wowmod.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.wowmod.item.custom.mechanics.ParryMechanics;
import net.wowmod.item.custom.WeaponItem;
import net.wowmod.item.custom.enums.ActionType;
import net.wowmod.util.IParryStunnedEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class ParryLivingEntityMixin implements IParryStunnedEntity {

    @Unique
    private int wowmod_parriedStunTicks = 0;

    @Override
    public int wowmod_getStunTicks() {
        return this.wowmod_parriedStunTicks;
    }

    @Override
    public void wowmod_setStunTicks(int ticks) {
        this.wowmod_parriedStunTicks = ticks;
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void wowmod_tickStun(CallbackInfo ci) {
        if (this.wowmod_parriedStunTicks > 0) {
            this.wowmod_parriedStunTicks--;

            // Delegate logic to ParryMechanics
            LivingEntity entity = (LivingEntity) (Object) this;
            ParryMechanics.tickStunnedEntity(entity, this.wowmod_parriedStunTicks);
        }
    }

    // Increases damage taken if the entity is stunned (Counter Attack Mechanic)
    @ModifyVariable(method = "hurtServer", at = @At("HEAD"), argsOnly = true)
    private float wowmod_modifyDamageTaken(float amount, ServerLevel level, DamageSource source) {
        // Delegate calculation to ParryMechanics
        return ParryMechanics.modifyDamageTaken((LivingEntity)(Object)this, amount, source, this.wowmod_parriedStunTicks);
    }

    // FIX: Initialize Parry Timer when the blocking action actually starts
    @Inject(method = "startUsingItem", at = @At("HEAD"))
    private void wowmod_onStartUsingItem(InteractionHand hand, CallbackInfo ci) {
        if ((Object)this instanceof Player player) {
            ItemStack stack = player.getItemInHand(hand);
            if (stack.getItem() instanceof WeaponItem weaponItem) {
                if (weaponItem.getConfig().actionType() == ActionType.BLOCK) {
                    ParryMechanics.initializeParry(player);
                }
            }
        }
    }

    // Ensures the engine recognizes our WeaponItem as 'blocking' even if vanilla checks fall short
    @Inject(method = "isBlocking", at = @At("HEAD"), cancellable = true)
    private void wowmod_checkCustomBlock(CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof Player player) {
            if (player.isUsingItem()) {
                ItemStack stack = player.getUseItem();
                if (stack.getItem() instanceof WeaponItem) {
                    if (stack.getUseAnimation() == ItemUseAnimation.BLOCK) {
                        cir.setReturnValue(true);
                    }
                }
            }
        }
    }

    // Prevents knockback while blocking
    @Inject(method = "knockback", at = @At("HEAD"), cancellable = true)
    private void wowmod_cancelDefensiveKnockback(double strength, double x, double z, CallbackInfo ci) {
        if ((Object) this instanceof Player player) {
            if (player.isBlocking()) {
                ci.cancel();
            }
        }
    }
}