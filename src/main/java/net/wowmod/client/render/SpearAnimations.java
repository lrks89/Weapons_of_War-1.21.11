package net.wowmod.client.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.Ease;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.KineticWeapon;

/**
 * Handles the mathematical transformations for spear-like weapons.
 * Recreated from vanilla logic to support steady-then-unstable charging poses.
 */
@Environment(EnvType.CLIENT)
public class SpearAnimations {

    static float progress(float f, float g, float h) {
        return Mth.clamp(Mth.inverseLerp(f, g, h), 0.0F, 1.0F);
    }

    public static <T extends HumanoidRenderState> void thirdPersonHandUse(ModelPart arm, ModelPart body, boolean isMainHand, ItemStack stack, T state) {
        int i = isMainHand ? 1 : -1;

        // Base pose: pointed forward
        arm.yRot = -0.1F * (float)i + body.yRot;
        arm.xRot = (-(float)Math.PI / 2F) + body.xRot + 0.8F;

        if (state.isFallFlying || state.swimAmount > 0.0F) {
            arm.xRot -= 0.9599311F;
        }

        arm.yRot = ((float)Math.PI / 180F) * Mth.clamp((180F / (float)Math.PI) * arm.yRot, -60.0F, 60.0F);
        arm.xRot = ((float)Math.PI / 180F) * Mth.clamp((180F / (float)Math.PI) * arm.xRot, -120.0F, 30.0F);

        // Apply Sway and Instability
        if (state.ticksUsingItem > 0.0F && (!state.isUsingItem || state.useItemHand == (isMainHand ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND))) {
            KineticWeapon kineticWeapon = stack.get(DataComponents.KINETIC_WEAPON);
            if (kineticWeapon != null) {
                UseParams useParams = UseParams.fromKineticWeapon(kineticWeapon, state.ticksUsingItem);

                // Rotations based on the calculated sway
                arm.yRot += (float)(-i) * useParams.swayScaleFast() * ((float)Math.PI / 180F) * useParams.swayIntensity() * 1.0F;
                arm.zRot += (float)(-i) * useParams.swayScaleSlow() * ((float)Math.PI / 180F) * useParams.swayIntensity() * 0.5F;

                // Vertical jitter/lowering
                arm.xRot += ((float)Math.PI / 180F) * (
                        -40.0F * useParams.raiseProgressStart() +
                                30.0F * useParams.raiseProgressMiddle() +
                                -20.0F * useParams.raiseProgressEnd() +
                                20.0F * useParams.lowerProgress() +
                                10.0F * useParams.raiseBackProgress() +
                                0.6F * useParams.swayScaleSlow() * useParams.swayIntensity()
                );
            }
        }
    }

    /**
     * Parameters that define the transition from steady to unstable.
     */
    @Environment(EnvType.CLIENT)
    public record UseParams(float raiseProgress, float raiseProgressStart, float raiseProgressMiddle, float raiseProgressEnd, float swayProgress, float lowerProgress, float raiseBackProgress, float swayIntensity, float swayScaleSlow, float swayScaleFast) {
        public static UseParams fromKineticWeapon(KineticWeapon kineticWeapon, float ticks) {
            int delay = kineticWeapon.delayTicks();

            // Fix: kineticWeapon components return Optional<KineticWeapon.Condition>.
            // We need to map to maxDurationTicks or default to 0.
            int maxDur = kineticWeapon.damageConditions()
                    .map(KineticWeapon.Condition::maxDurationTicks)
                    .orElse(0);

            // Instability starts 40 ticks before the end
            int startInstability = Math.max(0, maxDur - 40);
            int startDrop = Math.max(0, maxDur - 10);

            float g = SpearAnimations.progress(ticks, 0.0F, (float)delay);
            float h = SpearAnimations.progress(g, 0.0F, 0.5F);
            float o = SpearAnimations.progress(g, 0.5F, 0.8F);
            float p = SpearAnimations.progress(g, 0.8F, 1.0F);

            float q = SpearAnimations.progress(ticks, (float)startInstability, (float)startDrop);
            float r = Ease.outCubic(Ease.inOutElastic(SpearAnimations.progress(ticks - 20.0F, (float)startDrop, (float)maxDur)));
            float s = SpearAnimations.progress(ticks, (float)(maxDur - 5), (float)maxDur);

            // t is the "Shake Intensity"
            float t = 2.0F * Ease.outCirc(q) - 2.0F * Ease.inCirc(s);

            // Sine waves for the jitter motion
            float u = Mth.sin((float)(ticks * 19.0F * (Math.PI / 180F))) * t;
            float v = Mth.sin((float)(ticks * 30.0F * (Math.PI / 180F))) * t;

            return new UseParams(g, h, o, p, q, r, s, t, u, v);
        }
    }
}