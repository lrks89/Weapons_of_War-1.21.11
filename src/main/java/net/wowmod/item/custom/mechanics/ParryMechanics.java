package net.wowmod.item.custom.mechanics;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.wowmod.item.custom.WeaponConfig;
import net.wowmod.item.custom.WeaponItem;
import net.wowmod.item.custom.enums.WeaponFamily;
import net.wowmod.util.IParryPlayer;
import net.wowmod.util.IParryStunnedEntity;

public class ParryMechanics {

    private static final int PARRIED_STUN_DURATION = 60; // 3 seconds (60 ticks)
    private static final float KNOCKBACK_STRENGTH_SHIELD = 1.5F;
    private static final float KNOCKBACK_STRENGTH_WEAPON = 0.5F;

    public static void initializeParry(Player player) {
        if (player instanceof IParryPlayer parryPlayer) {
            long time = player.level().getGameTime();
            parryPlayer.wowmod_setLastParryTime(time);
            // System.out.println("DEBUG: Parry Initialized at GameTime: " + time);
        }
    }

    /**
     * Handles the visual and physical effects of being stunned.
     * Called every tick for stunned entities.
     */
    public static void tickStunnedEntity(LivingEntity entity, int stunTicks) {
        if (!entity.level().isClientSide()) {
            ServerLevel serverLevel = (ServerLevel) entity.level();

            // 1. Visuals: Crit particles to indicate vulnerability (Counter Attack Window)
            if (stunTicks % 5 == 0) {
                serverLevel.sendParticles(ParticleTypes.CRIT,
                        entity.getX(), entity.getEyeY() + 0.5, entity.getZ(),
                        1, 0.2, 0.2, 0.2, 0.1);
            }

            // 2. Physics: Freeze movement (X/Z) but allow Gravity (Y)
            Vec3 currentVel = entity.getDeltaMovement();
            entity.setDeltaMovement(0, currentVel.y, 0);
        }
    }

    /**
     * Calculates the modified damage if the target is stunned.
     */
    public static float modifyDamageTaken(LivingEntity entity, float amount, DamageSource source, int stunTicks) {
        if (stunTicks > 0 && source.getEntity() instanceof Player) {
            return amount * 1.5F;
        }
        return amount;
    }

    public static boolean attemptPerfectParry(Player player, DamageSource source) {
        // System.out.println("DEBUG: Attempting Parry...");

        if (!player.isBlocking()) {
            return false;
        }

        if (source.is(DamageTypeTags.BYPASSES_SHIELD)) {
            return false;
        }

        ItemStack activeStack = player.getUseItem();

        if (activeStack.getItem() instanceof WeaponItem weaponItem) {
            WeaponConfig config = weaponItem.getConfig();

            if (config.parryWindow() > 0) {
                if (player instanceof IParryPlayer parryPlayer) {
                    long gameTime = player.level().getGameTime();
                    long startTime = parryPlayer.wowmod_getLastParryTime();
                    long timeDelta = gameTime - startTime;

                    // System.out.println("DEBUG: Time Delta: " + timeDelta + " (Window: " + config.parryWindow() + ")");

                    if (timeDelta >= 0 && timeDelta <= config.parryWindow()) {
                        // System.out.println("DEBUG: SUCCESS! Parry executing.");
                        if (!player.level().isClientSide()) {
                            ServerLevel serverLevel = (ServerLevel) player.level();
                            handleParryEffects(player, serverLevel, source, config);
                        }
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static void handleParryEffects(Player player, ServerLevel level, DamageSource source, WeaponConfig config) {
        Entity attacker = source.getEntity();
        WeaponFamily family = config.family();
        boolean isShield = family == WeaponFamily.SHIELD;

        playParrySound(player, level, family);

        // Updated to END_ROD for a white sparkle effect
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                player.getX(), player.getY(1.2), player.getZ(),
                15, 0.5, 0.5, 0.5, 0.1);

        if (attacker instanceof LivingEntity livingAttacker && !(source.getDirectEntity() instanceof Projectile)) {

            if (livingAttacker instanceof IParryStunnedEntity stunnedAttacker) {
                stunnedAttacker.wowmod_setStunTicks(PARRIED_STUN_DURATION);
            }

            double strength = isShield ? KNOCKBACK_STRENGTH_SHIELD : KNOCKBACK_STRENGTH_WEAPON;
            double dx = livingAttacker.getX() - player.getX();
            double dz = livingAttacker.getZ() - player.getZ();

            double dist = Math.sqrt(dx * dx + dz * dz);
            if (dist > 0.001) {
                livingAttacker.knockback(strength, -dx, -dz);
            }
        }
    }

    private static void playParrySound(Player player, ServerLevel level, WeaponFamily family) {
        float pitch = 0.8F + level.random.nextFloat() * 0.2F;

        switch (family) {
            case SHORTAXE, LONGAXE, GREATAXE, CLAW ->
                    level.playSound(null, player.getX(), player.getY(), player.getZ(),
                            SoundEvents.ANVIL_PLACE, SoundSource.PLAYERS, 1.0F, 1.0F);

            case SHORTMACE, LONGMACE, GREATMACE, FIST ->
                    level.playSound(null, player.getX(), player.getY(), player.getZ(),
                            SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 1.0F, 1.0F);

            case SHIELD ->
                    level.playSound(null, player.getX(), player.getY(), player.getZ(),
                            SoundEvents.SHIELD_BLOCK, SoundSource.PLAYERS, 1.0F, 1.2F + level.random.nextFloat() * 0.1F);

            default ->
                    level.playSound(null, player.getX(), player.getY(), player.getZ(),
                            SoundEvents.ANVIL_PLACE, SoundSource.PLAYERS, 1.0F, 1.2F + level.random.nextFloat() * 0.1F);
        }
    }
}