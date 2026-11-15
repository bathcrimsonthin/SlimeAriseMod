package com.bathcrimsonthin.slimearisemod.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

public class SlimePuddingItem extends Item {

    public static final FoodProperties SLIME_PUDDING_FOOD = new FoodProperties.Builder()
            .nutrition(6)               // 満腹度 6（肉3個分）
            .saturationModifier(0.6F)   // 隠し満腹度 7.2
            .alwaysEdible()             // 満腹時でもデバフ解除用として食べられる
            .build();

    public SlimePuddingItem(Properties properties) {
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
                    1.15F + level.random.nextFloat() * 0.2F
            );

            // 付与中の有害なポーション効果を抽出し、ランダムに1つ解除
            List<MobEffectInstance> harmfulEffects = new ArrayList<>();
            for (MobEffectInstance effect : livingEntity.getActiveEffects()) {
                if (effect.getEffect().value().getCategory() == MobEffectCategory.HARMFUL) {
                    harmfulEffects.add(effect);
                }
            }

            if (!harmfulEffects.isEmpty()) {
                MobEffectInstance target = harmfulEffects.get(level.random.nextInt(harmfulEffects.size()));
                if (livingEntity.removeEffect(target.getEffect())) {
                    level.playSound(
                            null,
                            livingEntity.blockPosition(),
                            SoundEvents.EXPERIENCE_ORB_PICKUP,
                            SoundSource.PLAYERS,
                            0.65F,
                            1.45F
                    );
                    if (level instanceof ServerLevel serverLevel) {
                        serverLevel.sendParticles(
                                ParticleTypes.HAPPY_VILLAGER,
                                livingEntity.getX(),
                                livingEntity.getY(0.75D),
                                livingEntity.getZ(),
                                8,
                                0.35D,
                                0.35D,
                                0.35D,
                                0.05D
                        );
                    }
                }
            }
        }
        return super.finishUsingItem(stack, level, livingEntity);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(
                Component.translatable("item.slimearisemod.slime_pudding.tooltip").withStyle(ChatFormatting.GREEN)
        );
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}