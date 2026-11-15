package com.bathcrimsonthin.slimearisemod.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class SlimeGummyItem extends Item {

    public static final FoodProperties SLIME_GUMMY_FOOD = new FoodProperties.Builder()
            .nutrition(4)               // 満腹度 4（肉2個分）
            .saturationModifier(0.6F)   // 隠し満腹度 4.8
            .fast()                     // 通常の2倍の速度（0.8秒）で食べられる
            .alwaysEdible()             // 満腹時でもつまみ食い可能
            .effect(new MobEffectInstance(MobEffects.JUMP, 80, 0), 1.0F) // 跳躍力上昇 I（4秒）
            .build();

    public SlimeGummyItem(Properties properties) {
        super(properties);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
        if (!level.isClientSide) {
            level.playSound(
                    null,
                    livingEntity.blockPosition(),
                    SoundEvents.SLIME_SQUISH_SMALL,
                    SoundSource.PLAYERS,
                    0.75F,
                    1.25F + level.random.nextFloat() * 0.2F
            );
        }
        return super.finishUsingItem(stack, level, livingEntity);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(
                Component.translatable("item.slimearisemod.slime_gummy.tooltip").withStyle(ChatFormatting.GREEN)
        );
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}