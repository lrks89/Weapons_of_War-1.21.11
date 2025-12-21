package net.wowmod.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.wowmod.item.custom.mechanics.ParryMechanics;
import net.wowmod.util.IParryPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PlayerEntityMixin implements IParryPlayer {

    @Unique
    private long wowmod_lastParryTime = 0;

    @Override
    public void wowmod_setLastParryTime(long time) {
        this.wowmod_lastParryTime = time;
    }

    @Override
    public long wowmod_getLastParryTime() {
        return this.wowmod_lastParryTime;
    }

    // In 1.21.11 mappings, server-side damage handling is often in hurtServer.
    // We check if perfect parry conditions are met to cancel the damage completely.
    @Inject(method = "hurtServer", at = @At("HEAD"), cancellable = true)
    private void wowmod_checkParry(ServerLevel level, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        Player player = (Player) (Object) this;

        // Attempt to parry using the centralized mechanics
        if (ParryMechanics.attemptPerfectParry(player, source)) {
            // If parry was successful, return false (damage not dealt) and cancel the method execution
            cir.setReturnValue(false);
            cir.cancel();
        }
    }
}