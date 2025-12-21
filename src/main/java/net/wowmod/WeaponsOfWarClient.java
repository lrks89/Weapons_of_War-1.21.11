package net.wowmod;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.wowmod.client.renderer.ThrownWeaponRenderer;

public class WeaponsOfWarClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // CHANGED: Use our custom ThrownWeaponRenderer instead of vanilla ThrownItemRenderer
        // This ensures the projectile rotates to face its direction of travel (like a trident)
        // instead of always facing the player (like a snowball).
        EntityRendererRegistry.register(WeaponsOfWar.THROWN_WEAPON, ThrownWeaponRenderer::new);
    }
}