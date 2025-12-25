package net.wowmod.mixin.client;

import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.Avatar;
import net.wowmod.util.IModAnimationPlayer;
import net.wowmod.util.IModRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AvatarRenderer.class)
public class PlayerRendererMixin {

    // The method signature uses the erased type of the generic parameter, which is 'Avatar'
    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/Avatar;Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;F)V", at = @At("TAIL"))
    private void extractCustomAnimations(Avatar avatar, AvatarRenderState state, float partialTick, CallbackInfo ci) {
        if (avatar instanceof IModAnimationPlayer animPlayer && state instanceof IModRenderState modState) {
            modState.wowmod$setIdleAnimationState(animPlayer.wowmod$getIdleAnimationState());
            modState.wowmod$setWalkAnimationState(animPlayer.wowmod$getWalkAnimationState());
            modState.wowmod$setSprintAnimationState(animPlayer.wowmod$getSprintAnimationState());
        }
    }
}