package net.wowmod;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;

public class WeaponsOfWarClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Register the renderer.
        // ThrownItemRenderer is the Mojang mapping for FlyingItemEntityRenderer.
        // It renders the entity as a flat item (like a snowball or potion).
        EntityRendererRegistry.register(WeaponsOfWar.THROWN_WEAPON, ThrownItemRenderer::new);
    }
}