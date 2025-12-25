package net.wowmod.mixin.client;

import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.KeyframeAnimation;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.world.entity.AnimationState;
import net.wowmod.client.animation.ModAnimations;
import net.wowmod.util.IModRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Mixin(HumanoidModel.class)
public class PlayerModelMixin {

    @Unique
    private ModelPart capturedRoot;

    // Capture the root part from the constructor and setup the custom hierarchy
    @Inject(method = "<init>(Lnet/minecraft/client/model/geom/ModelPart;)V", at = @At("RETURN"))
    private void init(ModelPart modelPart, CallbackInfo ci) {
        this.capturedRoot = modelPart;
        // CRITICAL FIX: Only apply hierarchy changes to the PlayerModel.
        // This prevents crashes with ArmorStandModel, ZombieModel, etc. which also extend HumanoidModel.
        if ((Object) this instanceof PlayerModel) {
            this.setupCustomHierarchy();
        }
    }

    @Unique
    private void setupCustomHierarchy() {
        HumanoidModel<?> model = (HumanoidModel<?>) (Object) this;

        // 1. Get references to standard parts
        ModelPart head = model.head;
        ModelPart body = model.body;
        ModelPart leftArm = model.leftArm;
        ModelPart rightArm = model.rightArm;
        ModelPart leftLeg = model.leftLeg;
        ModelPart rightLeg = model.rightLeg;

        // 2. Create new custom bones
        // Controller (Root)
        ModelPart controller = createPart(0, 0, 0);

        // Waist (0, 12, 0) - Child of Controller
        ModelPart waist = createPart(0, 12, 0);

        // Neck (0, 24, 0) - Child of Body
        // Relative to Body (0, 0, 0 in waist space, 0, 12, 0 absolute):
        ModelPart neck = createPart(0, 12, 0);

        // Left Shoulder (-5, 22, 0) - Child of Body
        // Relative to Body (0, 0, 0 in waist space): (-5, 10, 0)
        ModelPart leftShoulder = createPart(-5, 10, 0);

        // Left Pivot (Shares pivot with Shoulder) - Child of Left Shoulder
        ModelPart leftPivot = createPart(0, 0, 0);

        // Left Hand (-6, 14, 0) - Child of Left Arm
        // Relative to Arm: (-6 - -5, 14 - 22, 0) = (-1, -8, 0)
        ModelPart leftHand = createPart(-1, -8, 0);

        // Left Item (-6, 14, 0) - Child of Left Hand
        ModelPart leftItem = createPart(0, 0, 0);

        // Right Shoulder (5, 22, 0) - Child of Body
        // Relative to Body (0, 0, 0 in waist space): (5, 10, 0)
        ModelPart rightShoulder = createPart(5, 10, 0);

        // Right Pivot (Shares pivot) - Child of Right Shoulder
        ModelPart rightPivot = createPart(0, 0, 0);

        // Right Hand (6, 14, 0) - Child of Right Arm
        // Relative to Arm: (6 - 5, 14 - 22, 0) = (1, -8, 0)
        ModelPart rightHand = createPart(1, -8, 0);

        // Right Item (6, 14, 0) - Child of Right Hand
        ModelPart rightItem = createPart(0, 0, 0);

        // Left Hip (-2, 12, 0) - Child of Controller
        // Relative to Controller (0, 0, 0): (-2, 12, 0)
        ModelPart leftHip = createPart(-2, 12, 0);

        // Right Hip (2, 12, 0) - Child of Controller
        // Relative to Controller (0, 0, 0): (2, 12, 0)
        ModelPart rightHip = createPart(2, 12, 0);

        // 3. Assemble the Hierarchy
        Map<String, ModelPart> rootChildren = getChildrenMap(this.capturedRoot);

        // Remove standard parts from root so we can move them.
        // We do NOT use clear() anymore, to preserve other parts (like Cloak, Ears, etc).
        rootChildren.remove("head");
        rootChildren.remove("body");
        rootChildren.remove("left_arm");
        rootChildren.remove("right_arm");
        rootChildren.remove("left_leg");
        rootChildren.remove("right_leg");

        // Also handle Player Skin Layers (Hat, Jacket, Sleeves, Pants)
        // We move them to follow the respective body parts.
        moveChild(rootChildren, "hat", head);
        moveChild(rootChildren, "jacket", body);
        moveChild(rootChildren, "left_sleeve", leftArm);
        moveChild(rootChildren, "right_sleeve", rightArm);
        moveChild(rootChildren, "left_pants", leftLeg);
        moveChild(rootChildren, "right_pants", rightLeg);

        // Build Tree:
        // Root -> Controller
        rootChildren.put("controller", controller);

        // Controller -> Waist, LeftHip, RightHip
        addChild(controller, "waist", waist);
        addChild(controller, "leftHip", leftHip);
        addChild(controller, "rightHip", rightHip);

        // Waist -> Body
        body.setPos(0, 0, 0);
        addChild(waist, "body", body);

        // Body -> Neck
        addChild(body, "neck", neck);

        // Neck -> Head
        head.setPos(0, 0, 0);
        addChild(neck, "head", head);

        // Body -> Shoulders
        addChild(body, "leftShoulder", leftShoulder);
        addChild(body, "rightShoulder", rightShoulder);

        // Left Shoulder -> Left Pivot, Left Arm
        addChild(leftShoulder, "leftPivot", leftPivot);

        // Left Arm
        leftArm.setPos(0, 0, 0);
        addChild(leftShoulder, "leftArm", leftArm);

        // Left Arm -> Left Hand -> Left Item
        addChild(leftArm, "leftHand", leftHand);
        addChild(leftHand, "leftItem", leftItem);

        // Right Shoulder -> Right Pivot, Right Arm
        addChild(rightShoulder, "rightPivot", rightPivot);

        // Right Arm
        rightArm.setPos(0, 0, 0);
        addChild(rightShoulder, "rightArm", rightArm);

        // Right Arm -> Right Hand -> Right Item
        addChild(rightArm, "rightHand", rightHand);
        addChild(rightHand, "rightItem", rightItem);

        // Hips -> Legs
        leftLeg.setPos(0, 0, 0);
        addChild(leftHip, "leftLeg", leftLeg);

        rightLeg.setPos(0, 0, 0);
        addChild(rightHip, "rightLeg", rightLeg);
    }

    @Unique
    private void moveChild(Map<String, ModelPart> rootChildren, String name, ModelPart newParent) {
        if (rootChildren.containsKey(name)) {
            ModelPart part = rootChildren.remove(name);
            // Reset position to 0,0,0 relative to new parent if it was an overlay
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

    @Inject(method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/HumanoidRenderState;)V", at = @At("RETURN"))
    private void setupCustomAnimations(HumanoidRenderState state, CallbackInfo ci) {
        // Only animate if this is a PlayerModel AND the state supports our animations
        if ((Object)this instanceof PlayerModel && state instanceof IModRenderState modState) {
            float time = state.ageInTicks;

            this.animate(modState.wowmod$getIdleAnimationState(), ModAnimations.IDLE, time, this.capturedRoot);
            this.animate(modState.wowmod$getWalkAnimationState(), ModAnimations.WALK, time, this.capturedRoot);
            this.animate(modState.wowmod$getSprintAnimationState(), ModAnimations.SPRINT, time, this.capturedRoot);
        }
    }

    @Unique
    private void animate(AnimationState animationState, AnimationDefinition animationDefinition, float ageInTicks, ModelPart root) {
        if (root == null || animationState == null) return;

        animationState.ifStarted((state) -> {
            KeyframeAnimation animation = animationDefinition.bake(root);
            animation.apply(state, ageInTicks, 1.0F);
        });
    }
}