package net.wowmod.item.custom;

import net.wowmod.item.custom.enums.ActionType;
import net.wowmod.item.custom.enums.WeaponFamily;
import net.wowmod.item.custom.enums.WeaponStance;

public record WeaponConfig(
        WeaponFamily family,
        WeaponStance stance,

        // Base Stats
        double attackDamage,
        double attackSpeed,
        float minRange,
        float maxRange,

        // Durability
        int durability,
        int durabilityPerHit,

        // Mechanics
        boolean isKinetic,
        boolean disablesShields,
        boolean isPiercing,
        boolean isSweeping,

        // Abilities & Right Click
        ActionType actionType,
        int abilityCooldown,
        float blockMitigation,
        int parryWindow
) {
}