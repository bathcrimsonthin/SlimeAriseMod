package com.bathcrimsonthin.slimearisemod.item;

import com.bathcrimsonthin.slimearisemod.mixin.AreaEffectCloudAccessor;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;

public class WitchSlimeBallItem extends Item {

    public WitchSlimeBallItem(Properties properties) {
        super(properties
                .stacksTo(64)
                .food(new FoodProperties.Builder()
                        .nutrition(0)
                        .saturationModifier(0.0F)
                        .alwaysEdible()
                        .build())
        );
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 24;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.EAT;
    }

    // 他の LivingEntity（Mob やプレイヤー）を右クリックした際の付与処理
    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity interactionTarget, InteractionHand hand) {
        PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);

        // 効果入りの WitchSlimeBall を持っている場合
        if (contents != null && contents.hasEffects()) {
            if (!player.level().isClientSide) {
                // クリックした対象エンティティへエフェクトを付与
                contents.forEachEffect(effect -> interactionTarget.addEffect(new MobEffectInstance(effect)));

                // 塗布音の再生
                player.level().playSound(
                        null,
                        interactionTarget.getX(),
                        interactionTarget.getY(),
                        interactionTarget.getZ(),
                        SoundEvents.SLIME_SQUISH,
                        SoundSource.PLAYERS,
                        1.0F,
                        1.0F
                );

                // クリエイティブモード以外なら1個消費
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
            }

            return InteractionResult.sidedSuccess(player.level().isClientSide);
        }

        return InteractionResult.PASS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack heldStack = player.getItemInHand(hand);

        // 手持のボールが無色（未充填）の場合のみ、ポーション雲の吸い取りを試みる
        if (!heldStack.has(DataComponents.POTION_CONTENTS)) {
            AreaEffectCloud cloud = findTargetCloud(level, player);
            if (cloud != null) {
                PotionContents cloudContents = ((AreaEffectCloudAccessor) cloud).getPotionContents();

                if (cloudContents.hasEffects()) {
                    ItemStack filledBall = new ItemStack(this);
                    filledBall.set(DataComponents.POTION_CONTENTS, cloudContents);

                    if (!level.isClientSide) {
                        level.playSound(null, cloud.getX(), cloud.getY(), cloud.getZ(),
                                SoundEvents.BOTTLE_FILL, SoundSource.PLAYERS, 1.0F, 1.2F);
                        cloud.discard();
                    }

                    ItemStack resultStack = ItemUtils.createFilledResult(heldStack, player, filledBall);
                    return InteractionResultHolder.sidedSuccess(resultStack, level.isClientSide());
                }
            }
        }

        return super.use(level, player, hand);
    }

    private AreaEffectCloud findTargetCloud(Level level, Player player) {
        double reach = 4.5D;
        Vec3 eyePos = player.getEyePosition(1.0F);
        Vec3 lookVec = player.getViewVector(1.0F);
        Vec3 reachVec = eyePos.add(lookVec.scale(reach));

        AABB searchBox = player.getBoundingBox().expandTowards(lookVec.scale(reach)).inflate(1.5D);
        List<AreaEffectCloud> clouds = level.getEntitiesOfClass(AreaEffectCloud.class, searchBox, AreaEffectCloud::isAlive);

        AreaEffectCloud closestCloud = null;
        double closestDistanceSq = reach * reach;

        for (AreaEffectCloud cloud : clouds) {
            AABB cloudBox = cloud.getBoundingBox().inflate(0.8D, 0.8D, 0.8D);
            Optional<Vec3> hit = cloudBox.clip(eyePos, reachVec);

            if (hit.isPresent() || cloudBox.contains(eyePos)) {
                double distSq = eyePos.distanceToSqr(cloud.position());
                if (distSq < closestDistanceSq) {
                    closestDistanceSq = distSq;
                    closestCloud = cloud;
                }
            }
        }

        return closestCloud;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);

        ItemStack result = super.finishUsingItem(stack, level, entity);

        if (!level.isClientSide && contents != null) {
            contents.forEachEffect(effect -> entity.addEffect(new MobEffectInstance(effect)));
        }

        return result;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
        if (contents != null) {
            contents.addPotionTooltip(tooltip::add, 1.0F, context.tickRate());
        }
    }
}