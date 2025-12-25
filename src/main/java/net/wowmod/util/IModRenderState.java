package net.wowmod.util;

import net.minecraft.world.entity.AnimationState;

public interface IModRenderState {
    void wowmod$setIdleAnimationState(AnimationState state);
    AnimationState wowmod$getIdleAnimationState();

    void wowmod$setWalkAnimationState(AnimationState state);
    AnimationState wowmod$getWalkAnimationState();

    void wowmod$setSprintAnimationState(AnimationState state);
    AnimationState wowmod$getSprintAnimationState();
}