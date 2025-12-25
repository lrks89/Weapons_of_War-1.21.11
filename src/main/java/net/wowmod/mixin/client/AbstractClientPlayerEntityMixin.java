package net.wowmod.mixin.client;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.wowmod.item.custom.WeaponItem;
import net.wowmod.util.IModAnimationPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractClientPlayer.class)
public abstract class AbstractClientPlayerEntityMixin extends Player implements IModAnimationPlayer {

    @Unique
    private final AnimationState idleAnimationState = new AnimationState();
    @Unique
    private final AnimationState walkAnimationState = new AnimationState();
    @Unique
    private final AnimationState sprintAnimationState = new AnimationState();

    public AbstractClientPlayerEntityMixin(Level level, GameProfile gameProfile) {
        super(level, gameProfile);
    }

    @Override
    public AnimationState wowmod$getIdleAnimationState() {
        return this.idleAnimationState;
    }

    @Override
    public AnimationState wowmod$getWalkAnimationState() {
        return this.walkAnimationState;
    }

    @Override
    public AnimationState wowmod$getSprintAnimationState() {
        return this.sprintAnimationState;
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void tickAnimations(CallbackInfo ci) {
        // Removed the check for WeaponItem so animations play all the time

        // Logic to determine which animation to play
        boolean isMoving = this.getDeltaMovement().horizontalDistanceSqr() > 0.00001;
        boolean isSprinting = this.isSprinting();

        if (isMoving) {
            if (isSprinting) {
                this.idleAnimationState.stop();
                this.walkAnimationState.stop();
                this.sprintAnimationState.startIfStopped(this.tickCount);
            } else {
                this.idleAnimationState.stop();
                this.sprintAnimationState.stop();
                this.walkAnimationState.startIfStopped(this.tickCount);
            }
        } else {
            this.walkAnimationState.stop();
            this.sprintAnimationState.stop();
            this.idleAnimationState.startIfStopped(this.tickCount);
        }
    }
}