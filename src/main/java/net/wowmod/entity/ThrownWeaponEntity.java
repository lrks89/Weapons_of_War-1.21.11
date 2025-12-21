package net.wowmod.entity;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.entity.projectile.arrow.ThrownTrident;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.wowmod.WeaponsOfWar;

// EXTENDS ThrownTrident to keep physics/loyalty.
// IMPLEMENTS ItemSupplier (Mojang mapping for FlyingItemEntity) to allow item rendering.
public class ThrownWeaponEntity extends ThrownTrident implements ItemSupplier {

    // Define a data parameter to sync the ItemStack to the client for rendering
    private static final EntityDataAccessor<ItemStack> WEAPON_ITEM =
            SynchedEntityData.defineId(ThrownWeaponEntity.class, EntityDataSerializers.ITEM_STACK);

    public ThrownWeaponEntity(EntityType<? extends ThrownWeaponEntity> entityType, Level level) {
        super(entityType, level);
    }

    public ThrownWeaponEntity(Level level, LivingEntity owner, ItemStack stack) {
        // Use our custom EntityType, not EntityType.TRIDENT
        super(WeaponsOfWar.THROWN_WEAPON, level);
        this.setOwner(owner);
        this.setPos(owner.getX(), owner.getEyeY() - 0.1, owner.getZ());
        this.setItem(stack);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        // Register the ItemStack data parameter
        builder.define(WEAPON_ITEM, ItemStack.EMPTY);
    }

    // Required by ItemSupplier interface.
    // The renderer calls this to know what item to draw.
    @Override
    public ItemStack getItem() {
        return this.entityData.get(WEAPON_ITEM);
    }

    public void setItem(ItemStack stack) {
        this.entityData.set(WEAPON_ITEM, stack.copy());
    }

    // Override getPickupItem to ensure the player gets the WEAPON back, not a trident.
    @Override
    protected ItemStack getPickupItem() {
        return this.getItem();
    }
}