package net.wowmod;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.server.packs.PackType;
import net.wowmod.client.animation.ModAnimations;
import net.wowmod.client.renderer.ThrownWeaponRenderer;
import net.wowmod.entity.ThrownWeaponEntity;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

@Environment(EnvType.CLIENT)
public class WeaponsOfWarClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Fix: Register the listener to load animations when resources are ready (avoids NPE crash)
        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new ModAnimations());

        // Register Renderers
        EntityRendererRegistry.register(WeaponsOfWar.THROWN_WEAPON, ThrownWeaponRenderer::new);
    }
}