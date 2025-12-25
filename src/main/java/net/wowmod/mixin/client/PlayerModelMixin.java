package net.wowmod.mixin.client;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.world.entity.AnimationState;
import net.wowmod.client.animation.ModAnimations;
import net.wowmod.util.IModRenderState;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;
import java.util.*;

@Mixin(HumanoidModel.class)
public class PlayerModelMixin {

    @Unique
    private ModelPart capturedRoot;

    // Cache for model parts to avoid reflection every frame
    @Unique
    private final Map<String, ModelPart> partCache = new HashMap<>();

    @Inject(method = "<init>(Lnet/minecraft/client/model/geom/ModelPart;)V", at = @At("RETURN"))
    private void init(ModelPart modelPart, CallbackInfo ci) {
        this.capturedRoot = modelPart;
        if ((Object) this instanceof PlayerModel) {
            this.setupCustomHierarchy();
            this.buildPartCache(this.capturedRoot);
        }
    }

    @Unique
    private void setupCustomHierarchy() {
        HumanoidModel<?> model = (HumanoidModel<?>) (Object) this;

        ModelPart head = model.head;
        ModelPart body = model.body;
        ModelPart leftArm = model.leftArm;
        ModelPart rightArm = model.rightArm;
        ModelPart leftLeg = model.leftLeg;
        ModelPart rightLeg = model.rightLeg;

        ModelPart controller = createPart(0, 0, 0);
        ModelPart waist = createPart(0, 12, 0);
        ModelPart neck = createPart(0, 12, 0);
        ModelPart leftShoulder = createPart(-5, 10, 0);
        ModelPart leftPivot = createPart(0, 0, 0);
        ModelPart leftHand = createPart(-1, -8, 0);
        ModelPart leftItem = createPart(0, 0, 0);
        ModelPart rightShoulder = createPart(5, 10, 0);
        ModelPart rightPivot = createPart(0, 0, 0);
        ModelPart rightHand = createPart(1, -8, 0);
        ModelPart rightItem = createPart(0, 0, 0);
        ModelPart leftHip = createPart(-2, 12, 0);
        ModelPart rightHip = createPart(2, 12, 0);

        Map<String, ModelPart> rootChildren = getChildrenMap(this.capturedRoot);

        rootChildren.remove("head");
        rootChildren.remove("body");
        rootChildren.remove("left_arm");
        rootChildren.remove("right_arm");
        rootChildren.remove("left_leg");
        rootChildren.remove("right_leg");

        moveChild(rootChildren, "hat", head);
        moveChild(rootChildren, "jacket", body);
        moveChild(rootChildren, "left_sleeve", leftArm);
        moveChild(rootChildren, "right_sleeve", rightArm);
        moveChild(rootChildren, "left_pants", leftLeg);
        moveChild(rootChildren, "right_pants", rightLeg);

        rootChildren.put("controller", controller);

        addChild(controller, "waist", waist);
        addChild(controller, "leftHip", leftHip);
        addChild(controller, "rightHip", rightHip);

        body.setPos(0, 0, 0);
        addChild(waist, "body", body);

        addChild(body, "neck", neck);

        head.setPos(0, 0, 0);
        addChild(neck, "head", head);

        addChild(body, "leftShoulder", leftShoulder);
        addChild(body, "rightShoulder", rightShoulder);

        addChild(leftShoulder, "leftPivot", leftPivot);

        leftArm.setPos(0, 0, 0);
        addChild(leftShoulder, "leftArm", leftArm);

        addChild(leftArm, "leftHand", leftHand);
        addChild(leftHand, "leftItem", leftItem);

        addChild(rightShoulder, "rightPivot", rightPivot);

        rightArm.setPos(0, 0, 0);
        addChild(rightShoulder, "rightArm", rightArm);

        addChild(rightArm, "rightHand", rightHand);
        addChild(rightHand, "rightItem", rightItem);

        leftLeg.setPos(0, 0, 0);
        addChild(leftHip, "leftLeg", leftLeg);

        rightLeg.setPos(0, 0, 0);
        addChild(rightHip, "rightLeg", rightLeg);
    }

    @Unique
    private void moveChild(Map<String, ModelPart> rootChildren, String name, ModelPart newParent) {
        if (rootChildren.containsKey(name)) {
            ModelPart part = rootChildren.remove(name);
            part.setPos(0, 0, 0);
            addChild(newParent, name, part);
        }
    }

    @Unique
    private ModelPart createPart(float x, float y, float z) {
        ModelPart part = new ModelPart(Collections.emptyList(), new HashMap<>());
        part.setPos(x, y, z);
        return part;
    }

    @Unique
    private void addChild(ModelPart parent, String name, ModelPart child) {
        getChildrenMap(parent).put(name, child);
    }

    @Unique
    @SuppressWarnings("unchecked")
    private Map<String, ModelPart> getChildrenMap(ModelPart part) {
        try {
            for (Field field : ModelPart.class.getDeclaredFields()) {
                if (Map.class.isAssignableFrom(field.getType())) {
                    field.setAccessible(true);
                    return (Map<String, ModelPart>) field.get(part);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("WowMod: Could not find children map in ModelPart", e);
        }
        return new HashMap<>();
    }

    // Recursively find and cache all parts for fast lookup during animation
    @Unique
    private void buildPartCache(ModelPart part) {
        Map<String, ModelPart> children = getChildrenMap(part);
        for (Map.Entry<String, ModelPart> entry : children.entrySet()) {
            this.partCache.put(entry.getKey(), entry.getValue());
            buildPartCache(entry.getValue());
        }
    }

    @Inject(method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/HumanoidRenderState;)V", at = @At("RETURN"))
    private void setupCustomAnimations(HumanoidRenderState state, CallbackInfo ci) {
        if ((Object)this instanceof PlayerModel && state instanceof IModRenderState modState) {
            float time = state.ageInTicks;

            this.animate(modState.wowmod$getIdleAnimationState(), ModAnimations.IDLE, time);
            this.animate(modState.wowmod$getWalkAnimationState(), ModAnimations.WALK, time);
            this.animate(modState.wowmod$getSprintAnimationState(), ModAnimations.SPRINT, time);
        }
    }

    @Unique
    private void animate(AnimationState animationState, ModAnimations.Animation animation, float ageInTicks) {
        if (animationState == null || !animationState.isStarted() || animation == null) return;

        // Calculate animation time in seconds (assuming 20 ticks per second)
        float accumulatedTime = animationState.getTimeInMillis(ageInTicks) / 1000.0f;

        // Handle looping
        if (animation.loop && animation.length > 0) {
            accumulatedTime %= animation.length;
        } else if (accumulatedTime > animation.length) {
            // Clamp to end if not looping
            accumulatedTime = animation.length;
        }

        // Apply transformations for each bone
        for (Map.Entry<String, ModAnimations.BoneModifier> entry : animation.bones.entrySet()) {
            String boneName = entry.getKey();
            ModAnimations.BoneModifier modifier = entry.getValue();

            // Find the ModelPart using our cache (or fallback to capturedRoot children if cache missed)
            ModelPart part = this.partCache.get(boneName);
            if (part == null) {
                // Try to match standard parts if names differ slightly, otherwise skip
                if (boneName.equals("body")) part = ((HumanoidModel)(Object)this).body;
                else if (boneName.equals("head")) part = ((HumanoidModel)(Object)this).head;
                else continue;
            }

            if (!modifier.rotation.isEmpty()) {
                Vector3f rot = interpolate(modifier.rotation, accumulatedTime);
                // Add to existing rotation (Vanilla convention) or set?
                // Usually for total overhaul animations, we want to SET relative to base pose.
                // But mixins run AFTER vanilla setupAnim, so we are adding to vanilla pose.
                // However, standard Bedrock animations are absolute.
                // Let's add the values.
                part.xRot += rot.x;
                part.yRot += rot.y;
                part.zRot += rot.z;
            }

            if (!modifier.position.isEmpty()) {
                Vector3f pos = interpolate(modifier.position, accumulatedTime);
                part.x += pos.x;
                part.y += pos.y; // Y is inverted in Blockbench vs Minecraft sometimes, be careful
                part.z += pos.z;
            }

            if (!modifier.scale.isEmpty()) {
                Vector3f scale = interpolate(modifier.scale, accumulatedTime);
                // Scaling is tricky on ModelParts in 1.21, usually requires scaling the matrix stack in render
                // ModelPart has xScale, yScale, zScale fields in some versions, or simply doesn't support it easily.
                // Ignoring scale for now as standard HumanoidModel parts don't always support dynamic scaling well without more mixins.
                part.xScale = scale.x;
                part.yScale = scale.y;
                part.zScale = scale.z;
            }
        }
    }

    @Unique
    private Vector3f interpolate(List<ModAnimations.Keyframe> keyframes, float time) {
        if (keyframes.isEmpty()) return new Vector3f(0, 0, 0);
        if (keyframes.size() == 1) return keyframes.get(0).data;

        // Find the keyframes surrounding the current time
        ModAnimations.Keyframe previous = keyframes.get(0);
        ModAnimations.Keyframe next = keyframes.get(0);

        for (ModAnimations.Keyframe frame : keyframes) {
            if (frame.time > time) {
                next = frame;
                break;
            }
            previous = frame;
        }

        if (previous == next) return previous.data;

        float delta = next.time - previous.time;
        if (delta <= 0) return previous.data;

        float t = (time - previous.time) / delta;
        // Clamp t between 0 and 1
        t = Math.max(0, Math.min(1, t));

        // Linear interpolation
        float x = previous.data.x + (next.data.x - previous.data.x) * t;
        float y = previous.data.y + (next.data.y - previous.data.y) * t;
        float z = previous.data.z + (next.data.z - previous.data.z) * t;

        return new Vector3f(x, y, z);
    }
}