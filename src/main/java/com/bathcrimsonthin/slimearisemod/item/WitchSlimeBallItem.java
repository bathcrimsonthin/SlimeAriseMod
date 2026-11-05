package com.bathcrimsonthin.slimearisemod.item;

import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.List;

public class WitchSlimeBallItem extends Item {

    public WitchSlimeBallItem(Properties properties) {
        super(properties);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity consumer) {
        ItemStack resultStack = super.finishUsingItem(stack, level, consumer);

        if (!level.isClientSide) {
            // ランダム付与の対象エフェクトリスト
            List<Holder<MobEffect>> possibleEffects = List.of(
                    MobEffects.DAMAGE_BOOST,
                    MobEffects.JUMP,
                    MobEffects.MOVEMENT_SPEED,
                    MobEffects.REGENERATION
            );

            // 1つランダムに選定して 30 秒 (600 Ticks) 付与
            Holder<MobEffect> selectedEffect = possibleEffects.get(level.getRandom().nextInt(possibleEffects.size()));
            consumer.addEffect(new MobEffectInstance(selectedEffect, 600, 0));
        }

        return resultStack;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 24; // クッキー(16 ticks(0.8秒))とパン(32 ticks(1.6秒))の中間
    }
}