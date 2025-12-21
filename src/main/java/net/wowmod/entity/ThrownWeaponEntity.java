package net.wowmod.entity;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity; // Added import
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.entity.projectile.arrow.ThrownTrident;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.wowmod.WeaponsOfWar;
import org.jetbrains.annotations.Nullable;

public class ThrownWeaponEntity extends ThrownTrident implements ItemSupplier {

    private static final EntityDataAccessor<ItemStack> WEAPON_ITEM =
            SynchedEntityData.defineId(ThrownWeaponEntity.class, EntityDataSerializers.ITEM_STACK);

    // Track if we hit something, because ThrownTrident.dealtDamage is private
    private boolean hasHit = false;

    public ThrownWeaponEntity(EntityType<? extends ThrownWeaponEntity> entityType, Level level) {
        super(entityType, level);
    }

    public ThrownWeaponEntity(Level level, LivingEntity owner, ItemStack stack) {
        super(WeaponsOfWar.THROWN_WEAPON, level);
        this.setOwner(owner);
        this.setPos(owner.getX(), owner.getEyeY() - 0.1, owner.getZ());
        this.setItem(stack);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(WEAPON_ITEM, ItemStack.EMPTY);
    }

    @Override
    public ItemStack getItem() {
        return this.entityData.get(WEAPON_ITEM);
    }

    public void setItem(ItemStack stack) {
        this.entityData.set(WEAPON_ITEM, stack.copy());
    }

    @Override
    protected ItemStack getPickupItem() {
        return this.getItem();
    }

    // Capture when we hit an entity
    @Override
    protected void onHitEntity(net.minecraft.world.phys.EntityHitResult result) {
        super.onHitEntity(result);
        this.hasHit = true;
    }

    // Capture when we hit a block (via the inGround check in tick, or if we stop moving)
    // ThrownTrident sets dealtDamage=true in tick() if inGroundTime > 4. We mimic this.

    @Override
    public void tick() {
        if (this.inGroundTime > 4) {
            this.hasHit = true;
        }

        Entity owner = this.getOwner();
        int loyaltyLevel = this.getLoyaltyLevel();

        // Custom Loyalty Logic
        if (loyaltyLevel > 0 && (this.hasHit || this.isNoPhysics()) && owner != null) {
            if (!this.isAcceptibleReturnOwner()) {
                // Changed isClientSide field access to isClientSide() method call to resolve access error
                if (!this.level().isClientSide() && this.pickup == Pickup.ALLOWED) {
                    // Manual spawning to avoid resolution errors with spawnAtLocation
                    ItemStack stack = this.getPickupItem();
                    if (!stack.isEmpty()) {
                        ItemEntity itemEntity = new ItemEntity(this.level(), this.getX(), this.getY() + 0.1, this.getZ(), stack);
                        itemEntity.setDefaultPickUpDelay();
                        this.level().addFreshEntity(itemEntity);
                    }
                }
                this.discard();
            } else {
                // Determine logic for returning
                this.setNoPhysics(true);
                Vec3 vec3 = owner.getEyePosition().subtract(this.position());
                this.setPosRaw(this.getX(), this.getY() + vec3.y * 0.015D * (double)loyaltyLevel, this.getZ());

                // Changed isClientSide field access to isClientSide() method call
                if (this.level().isClientSide()) {
                    this.yOld = this.getY();
                }

                double d = 0.05D * (double)loyaltyLevel;
                this.setDeltaMovement(this.getDeltaMovement().scale(0.95D).add(vec3.normalize().scale(d)));

                if (this.clientSideReturnTridentTickCount == 0) {
                    this.playSound(SoundEvents.TRIDENT_RETURN, 10.0F, 1.0F);
                }

                ++this.clientSideReturnTridentTickCount;
            }
        }

        super.tick();
    }

    private int getLoyaltyLevel() {
        ItemStack stack = this.getItem();
        if (stack.isEmpty()) return 0;
        // 1.21 approach to getting enchantment level
        return EnchantmentHelper.getItemEnchantmentLevel(
                this.level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.LOYALTY),
                stack
        );
    }

    private boolean isAcceptibleReturnOwner() {
        Entity owner = this.getOwner();
        if (owner != null && owner.isAlive()) {
            return !(owner instanceof Player) || !owner.isSpectator();
        } else {
            return false;
        }
    }

    // Helper to check if returning (for Renderer)
    public boolean isReturning() {
        return this.isNoPhysics() && this.getLoyaltyLevel() > 0;
    }
}