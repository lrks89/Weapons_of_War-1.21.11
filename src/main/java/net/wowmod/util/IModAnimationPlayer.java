package net.wowmod.util;

import net.minecraft.world.entity.AnimationState;

public interface IModAnimationPlayer {
    AnimationState wowmod$getIdleAnimationState();
    AnimationState wowmod$getWalkAnimationState();
    AnimationState wowmod$getSprintAnimationState();
}