package com.bathcrimsonthin.slimearisemod.item;

import com.bathcrimsonthin.slimearisemod.advancement.ModCriteriaTriggers;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.WitherSkull;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class WitherSlimeBallItem extends Item {

    public static final FoodProperties WITHER_SLIME_BALL_FOOD = new FoodProperties.Builder()
            .nutrition(0)
            .saturationModifier(0.0F)
            .alwaysEdible()
            .build();

    public WitherSlimeBallItem(Properties properties) {
        super(properties.food(WITHER_SLIME_BALL_FOOD));
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 24;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
        ItemStack resultStack = super.finishUsingItem(stack, level, livingEntity);

        if (!level.isClientSide) {
            ItemStack headStack = livingEntity.getItemBySlot(EquipmentSlot.HEAD);

            if (headStack.is(Items.WITHER_SKELETON_SKULL)) {
                // ウィザースケルトンの頭を被っている場合：視線方向へウィザースカルを発射
                Vec3 lookVec = livingEntity.getLookAngle();
                double spawnX = livingEntity.getX() + lookVec.x * 0.6D;
                double spawnY = livingEntity.getEyeY() - 0.1D + lookVec.y * 0.6D;
                double spawnZ = livingEntity.getZ() + lookVec.z * 0.6D;

                WitherSkull skull = new WitherSkull(level, livingEntity, lookVec);
                skull.setOwner(livingEntity);
                skull.setPosRaw(spawnX, spawnY, spawnZ);

                level.addFreshEntity(skull);
                level.playSound(
                        null,
                        livingEntity.getX(),
                        livingEntity.getY(),
                        livingEntity.getZ(),
                        SoundEvents.WITHER_SHOOT,
                        SoundSource.PLAYERS,
                        1.0F,
                        (level.random.nextFloat() - level.random.nextFloat()) * 0.2F + 1.0F
                );

                if (level instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.SMOKE, spawnX, spawnY, spawnZ, 8, 0.1D, 0.1D, 0.1D, 0.05D);
                }
                ModCriteriaTriggers.onEatWitherSlimeBall(livingEntity);
            } else {
                // 被っていない場合：ウィザー状態を付与（10秒）
                livingEntity.addEffect(new MobEffectInstance(MobEffects.WITHER, 200, 0));
            }
        }

        return resultStack;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(
                Component.translatable("item.slimearisemod.wither_slime_ball.tooltip")
                        .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC)
        );
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}