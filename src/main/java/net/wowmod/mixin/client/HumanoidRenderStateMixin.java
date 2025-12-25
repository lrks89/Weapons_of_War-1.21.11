package net.wowmod.mixin.client;

import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.world.entity.AnimationState;
import net.wowmod.util.IModRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(HumanoidRenderState.class)
public class HumanoidRenderStateMixin implements IModRenderState {
    @Unique private AnimationState idleAnimationState;
    @Unique private AnimationState walkAnimationState;
    @Unique private AnimationState sprintAnimationState;

    @Override
    public void wowmod$setIdleAnimationState(AnimationState state) {
        this.idleAnimationState = state;
    }

    @Override
    public AnimationState wowmod$getIdleAnimationState() {
        return this.idleAnimationState;
    }

    @Override
    public void wowmod$setWalkAnimationState(AnimationState state) {
        this.walkAnimationState = state;
    }

    @Override
    public AnimationState wowmod$getWalkAnimationState() {
        return this.walkAnimationState;
    }

    @Override
    public void wowmod$setSprintAnimationState(AnimationState state) {
        this.sprintAnimationState = state;
    }

    @Override
    public AnimationState wowmod$getSprintAnimationState() {
        return this.sprintAnimationState;
    }
}