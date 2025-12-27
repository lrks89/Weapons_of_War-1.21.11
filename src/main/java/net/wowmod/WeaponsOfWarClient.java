package net.wowmod;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.wowmod.client.renderer.ThrownWeaponRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

@Environment(EnvType.CLIENT)
public class WeaponsOfWarClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {

        // Register Renderers
        EntityRendererRegistry.register(WeaponsOfWar.THROWN_WEAPON, ThrownWeaponRenderer::new);
    }
}