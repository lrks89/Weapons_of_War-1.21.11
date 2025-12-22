package net.wowmod.mixin.client;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.wowmod.client.render.SpearAnimations;
import net.wowmod.item.custom.WeaponItem;
import net.wowmod.item.custom.enums.ActionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Injects into the humanoid model to apply spear-specific "charge" animations.
 */
@Mixin(HumanoidModel.class)
public class HumanoidModelMixin<T extends HumanoidRenderState> {

    @Inject(method = "setupAnim", at = @At("TAIL"))
    private void wowmod$applySpearChargeAnimation(T state, CallbackInfo ci) {
        HumanoidModel<T> model = (HumanoidModel<T>) (Object) this;

        // In 1.21.x, we iterate via HumanoidArm instead of boolean to match state getters
        applySpearLogic(model, state, HumanoidArm.RIGHT);
        applySpearLogic(model, state, HumanoidArm.LEFT);
    }

    private void applySpearLogic(HumanoidModel<T> model, T state, HumanoidArm arm) {
        // Correctly retrieve the item stack from the render state using the arm getter
        ItemStack stack = state.getUseItemStackForArm(arm);

        if (stack != null && stack.getItem() instanceof WeaponItem weaponItem) {
            if (weaponItem.getConfig().actionType() == ActionType.CHARGE) {
                // Check if the current arm is the one performing the 'use' action
                boolean isUsingThisArm = state.isUsingItem &&
                        state.useItemHand == (arm == state.mainArm ?
                                net.minecraft.world.InteractionHand.MAIN_HAND :
                                net.minecraft.world.InteractionHand.OFF_HAND);

                if (isUsingThisArm) {
                    SpearAnimations.thirdPersonHandUse(
                            arm == HumanoidArm.RIGHT ? model.rightArm : model.leftArm,
                            model.head, // Using head as pivot matches the vanilla spear decompile
                            arm == HumanoidArm.RIGHT,
                            stack,
                            state
                    );
                }
            }
        }
    }
}