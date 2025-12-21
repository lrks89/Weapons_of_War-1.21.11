package net.wowmod.item.custom;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

import java.util.function.Consumer;

public class WeaponItem extends Item {
    private final WeaponConfig config;

    public WeaponItem(WeaponConfig config, Properties properties) {
        // In 1.21.11 with Mojang mappings, durability is set via components in Properties
        super(properties.component(DataComponents.MAX_DAMAGE, config.durability()));
        this.config = config;
    }

    public WeaponConfig getConfig() {
        return config;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        // In 1.21.11, tooltips use a Consumer<Component> via tooltip.accept()
        tooltip.accept(Component.literal("Family: ").append(Component.literal(config.family().name())).withStyle(ChatFormatting.GRAY));
        tooltip.accept(Component.literal("Stance: ").append(Component.literal(config.stance().name())).withStyle(ChatFormatting.GRAY));

        if (config.isPiercing()) {
            tooltip.accept(Component.translatable("tooltip.wowmod.piercing").withStyle(ChatFormatting.GOLD));
        }

        super.appendHoverText(stack, context, display, tooltip, flag);
    }
}