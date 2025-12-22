package net.wowmod.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.wowmod.entity.ThrownWeaponEntity;
import net.wowmod.item.custom.WeaponItem;
import net.wowmod.item.custom.enums.WeaponFamily;

public class ThrownWeaponRenderer extends EntityRenderer<ThrownWeaponEntity, ThrownWeaponRenderer.ThrownWeaponState> {
    private final ItemModelResolver itemModelResolver;
    private final float scale;
    private final boolean fullBright;

    public ThrownWeaponRenderer(EntityRendererProvider.Context context) {
        this(context, 1.0F, false);
    }

    public ThrownWeaponRenderer(EntityRendererProvider.Context context, float scale, boolean fullBright) {
        super(context);
        this.itemModelResolver = context.getItemModelResolver();
        this.scale = scale;
        this.fullBright = fullBright;
    }

    @Override
    public ThrownWeaponState createRenderState() {
        return new ThrownWeaponState();
    }

    @Override
    public void extractRenderState(ThrownWeaponEntity entity, ThrownWeaponState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);

        ItemStack stack = entity.getItem();

        if (stack.getItem() instanceof WeaponItem weaponItem) {
            state.family = weaponItem.getConfig().family();
        } else {
            state.family = null;
        }

        this.itemModelResolver.updateForNonLiving(state.itemRenderState, stack, ItemDisplayContext.FIXED, entity);

        state.xRot = Mth.lerp(partialTicks, entity.xRotO, entity.getXRot());
        state.yRot = Mth.lerp(partialTicks, entity.yRotO, entity.getYRot());
        state.shakeTime = (float)entity.shakeTime - partialTicks;

        // Capture returning state (no longer used for spin, but kept for potential future use)
        state.isReturning = entity.isReturning();

        if (this.entityRenderDispatcher.camera != null) {
            double distSqr = this.entityRenderDispatcher.distanceToSqr(entity);
            state.isInvisible = entity.tickCount < 2 && distSqr < 12.25D;
        }
    }

    @Override
    public void submit(ThrownWeaponState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState) {
        super.submit(state, poseStack, submitNodeCollector, cameraRenderState);

        if (state.isInvisible || state.itemRenderState.isEmpty()) {
            return;
        }

        poseStack.pushPose();

        // 1. Rotation logic - REMOVED the 'if (state.isReturning)' block
        // Always use standard flight rotation (pitch/yaw)
        poseStack.mulPose(Axis.YP.rotationDegrees(state.yRot - 90.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(state.xRot));

        // 2. Shake logic
        if (state.shakeTime > 0.0F) {
            float f = -Mth.sin(state.shakeTime * 3.0F) * state.shakeTime;
            poseStack.mulPose(Axis.ZP.rotationDegrees(f));
        }

        // 3. Depth Adjustment
        double offset = 0.0D;
        if (state.family != null) {
            switch (state.family) {
                case DAGGER, FIST, CLAW:
                    offset = -0.1D;
                    break;
                case SHORTSWORD, SHORTAXE, SHORTMACE:
                    offset = -0.2D;
                    break;
                case LONGSWORD, LONGAXE, LONGMACE:
                    offset = -0.3D;
                    break;
                case GREATSWORD, GREATAXE, GREATMACE:
                    offset = -0.4D;
                    break;
                case SPEAR, TRIDENT:
                    offset = -0.5D;
                    break;
                default:
                    offset = -0.0D;
            }
        }

        // Re-enabled depth offset during return since it doesn't spin anymore
        poseStack.translate(offset, 0.0D, 0.0D);

        // 4. Scale
        poseStack.scale(this.scale, this.scale, this.scale);

        // 5. Texture Correction
        float rotation = -135.0F;
        poseStack.mulPose(Axis.ZP.rotationDegrees(rotation));

        state.itemRenderState.submit(
                poseStack,
                submitNodeCollector,
                this.fullBright ? 15728880 : state.lightCoords,
                OverlayTexture.NO_OVERLAY,
                0
        );

        poseStack.popPose();
    }

    public static class ThrownWeaponState extends EntityRenderState {
        public final ItemStackRenderState itemRenderState = new ItemStackRenderState();
        public float xRot;
        public float yRot;
        public float shakeTime;
        public boolean isInvisible;
        public WeaponFamily family;
        public boolean isReturning;
    }
}