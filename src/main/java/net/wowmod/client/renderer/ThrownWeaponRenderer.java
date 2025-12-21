package net.wowmod.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
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
import net.wowmod.entity.ThrownWeaponEntity;

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

        // Populate the ItemStackRenderState with model data
        this.itemModelResolver.updateForNonLiving(state.itemRenderState, entity.getItem(), ItemDisplayContext.FIXED, entity);

        // Capture interpolated rotation
        state.xRot = Mth.lerp(partialTicks, entity.xRotO, entity.getXRot());
        state.yRot = Mth.lerp(partialTicks, entity.yRotO, entity.getYRot());

        state.shakeTime = (float)entity.shakeTime - partialTicks;

        // Culling logic
        if (this.entityRenderDispatcher.camera != null) {
            double distSqr = this.entityRenderDispatcher.distanceToSqr(entity);
            state.isInvisible = entity.tickCount < 2 && distSqr < 12.25D;
        }
    }

    @Override
    public void submit(ThrownWeaponState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState) {
        // Handle name tags and leashes from super
        super.submit(state, poseStack, submitNodeCollector, cameraRenderState);

        if (state.isInvisible || state.itemRenderState.isEmpty()) {
            return;
        }

        poseStack.pushPose();

        // 1. Rotation logic
        poseStack.mulPose(Axis.YP.rotationDegrees(state.yRot - 90.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(state.xRot));

        // 2. Adjust position to center the item
        if (state.shakeTime > 0.0F) {
            float f = -Mth.sin(state.shakeTime * 3.0F) * state.shakeTime;
            poseStack.mulPose(Axis.ZP.rotationDegrees(f));
        }

        // 3. Scale and Orient
        poseStack.scale(this.scale, this.scale, this.scale);

        // Rotate 45 degrees on Z to make the item "point" forward
        poseStack.mulPose(Axis.ZP.rotationDegrees(-45.0F));

        // Render using the submit method in ItemStackRenderState
        // This avoids accessing private fields of ItemStackRenderState
        state.itemRenderState.submit(
                poseStack,
                submitNodeCollector,
                this.fullBright ? 15728880 : state.lightCoords,
                OverlayTexture.NO_OVERLAY,
                0 // Seed
        );

        poseStack.popPose();
    }

    // Inner class to hold the rendering state
    public static class ThrownWeaponState extends EntityRenderState {
        public final ItemStackRenderState itemRenderState = new ItemStackRenderState();
        public float xRot;
        public float yRot;
        public float shakeTime;
        public boolean isInvisible;
    }
}