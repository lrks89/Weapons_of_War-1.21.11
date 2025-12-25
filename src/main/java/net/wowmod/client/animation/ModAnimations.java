package net.wowmod.client.animation;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.joml.Vector3f;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.*;

/**
 * Handles dynamic loading of Bedrock/Blockbench JSON animations.
 * Implements resource reload listener to load safely when resources are available.
 */
@Environment(EnvType.CLIENT)
public class ModAnimations implements SimpleSynchronousResourceReloadListener {

    // Initialize with dummy empty animations to prevent NPEs before first load
    public static Animation IDLE = new Animation(0, false);
    public static Animation WALK = new Animation(0, false);
    public static Animation SPRINT = new Animation(0, false);

    @Override
    public Identifier getFabricId() {
        return Identifier.fromNamespaceAndPath("wowmod", "animations");
    }

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        IDLE = load(resourceManager, "wowmod", "animations/player_animations/idle_default.json", "idle_default");
        WALK = load(resourceManager, "wowmod", "animations/player_animations/walking_default.json", "walking_default");
        SPRINT = load(resourceManager, "wowmod", "animations/player_animations/sprinting_default.json", "sprinting_default");
        System.out.println("WowMod: Animations reloaded successfully.");
    }

    private static Animation load(ResourceManager resourceManager, String namespace, String path, String animationName) {
        Identifier location = Identifier.fromNamespaceAndPath(namespace, path);
        try {
            Optional<Resource> resource = resourceManager.getResource(location);
            if (resource.isPresent()) {
                try (Reader reader = new InputStreamReader(resource.get().open())) {
                    JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
                    return parseAnimation(json, animationName);
                }
            } else {
                System.err.println("WowMod: Could not find animation file: " + location);
            }
        } catch (Exception e) {
            System.err.println("WowMod: Failed to load animation: " + location);
            e.printStackTrace();
        }
        return new Animation(0, false);
    }

    private static Animation parseAnimation(JsonObject json, String name) {
        if (!json.has("animations")) return new Animation(0, false);
        JsonObject animationsObj = json.getAsJsonObject("animations");

        if (!animationsObj.has(name)) {
            System.err.println("WowMod: Animation '" + name + "' not found in JSON.");
            return new Animation(0, false);
        }

        JsonObject animNode = animationsObj.getAsJsonObject(name);
        float length = animNode.has("animation_length") ? animNode.get("animation_length").getAsFloat() : 0.0f;
        boolean loop = animNode.has("loop") && animNode.get("loop").getAsBoolean();

        Animation animation = new Animation(length, loop);

        if (animNode.has("bones")) {
            JsonObject bones = animNode.getAsJsonObject("bones");
            for (String boneName : bones.keySet()) {
                JsonObject boneData = bones.getAsJsonObject(boneName);
                parseBoneModifier(animation, boneName, boneData);
            }
        }

        return animation;
    }

    private static void parseBoneModifier(Animation animation, String boneName, JsonObject boneData) {
        BoneModifier modifier = new BoneModifier();

        if (boneData.has("rotation")) {
            parseChannel(boneData.get("rotation"), modifier.rotation, true);
        }
        if (boneData.has("position")) {
            parseChannel(boneData.get("position"), modifier.position, false);
        }
        if (boneData.has("scale")) {
            parseChannel(boneData.get("scale"), modifier.scale, false);
        }

        animation.bones.put(boneName, modifier);
    }

    private static void parseChannel(JsonElement element, List<Keyframe> keyframes, boolean isRotation) {
        if (element.isJsonObject()) {
            JsonObject map = element.getAsJsonObject();
            for (String timeStr : map.keySet()) {
                try {
                    float time = Float.parseFloat(timeStr);
                    Vector3f vec = parseVector(map.get(timeStr));
                    if (isRotation) {
                        vec.set(
                                (float) Math.toRadians(vec.x),
                                (float) Math.toRadians(vec.y),
                                (float) Math.toRadians(vec.z)
                        );
                    }
                    keyframes.add(new Keyframe(time, vec));
                } catch (NumberFormatException e) {
                    System.err.println("Invalid keyframe time: " + timeStr);
                }
            }
        } else if (element.isJsonArray()) {
            Vector3f vec = parseVector(element);
            if (isRotation) {
                vec.set(
                        (float) Math.toRadians(vec.x),
                        (float) Math.toRadians(vec.y),
                        (float) Math.toRadians(vec.z)
                );
            }
            keyframes.add(new Keyframe(0.0f, vec));
        }

        keyframes.sort((k1, k2) -> Float.compare(k1.time, k2.time));
    }

    private static Vector3f parseVector(JsonElement element) {
        if (element.isJsonArray()) {
            JsonArray arr = element.getAsJsonArray();
            if (arr.size() >= 3) {
                return new Vector3f(
                        arr.get(0).getAsFloat(),
                        arr.get(1).getAsFloat(),
                        arr.get(2).getAsFloat()
                );
            }
        }
        return new Vector3f(0, 0, 0);
    }

    // ==========================================
    // Custom Animation Classes
    // ==========================================

    public static class Animation {
        public final float length;
        public final boolean loop;
        public final Map<String, BoneModifier> bones = new HashMap<>();

        public Animation(float length, boolean loop) {
            this.length = length;
            this.loop = loop;
        }
    }

    public static class BoneModifier {
        public final List<Keyframe> rotation = new ArrayList<>();
        public final List<Keyframe> position = new ArrayList<>();
        public final List<Keyframe> scale = new ArrayList<>();
    }

    public static class Keyframe {
        public final float time;
        public final Vector3f data;

        public Keyframe(float time, Vector3f data) {
            this.time = time;
            this.data = data;
        }
    }
}