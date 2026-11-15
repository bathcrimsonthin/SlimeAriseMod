package com.bathcrimsonthin.slimearisemod.item;

import com.bathcrimsonthin.slimearisemod.entity.SlimeBottleProjectileEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class SlimeBottleItem extends Item implements ProjectileItem {

    public SlimeBottleItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("item.slimearisemod.slime_bottle.tooltip.1").withStyle(ChatFormatting.GREEN));
        tooltipComponents.add(Component.translatable("item.slimearisemod.slime_bottle.tooltip.2").withStyle(ChatFormatting.GRAY));
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);

        level.playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                SoundEvents.SPLASH_POTION_THROW,
                SoundSource.PLAYERS,
                0.5F,
                0.4F / (level.random.nextFloat() * 0.4F + 0.8F)
        );

        if (!level.isClientSide) {
            SlimeBottleProjectileEntity projectile = new SlimeBottleProjectileEntity(level, player);
            projectile.setItem(stack);
            projectile.shootFromRotation(player, player.getXRot(), player.getYRot(), -10.0F, 1.3F, 0.5F);

            // プレイヤーが落下中の場合、下向き速度を上乗せして必ずプレイヤーより先に着弾させる
            Vec3 playerMotion = player.getDeltaMovement();
            if (playerMotion.y < 0.0D) {
                projectile.setDeltaMovement(projectile.getDeltaMovement().add(0.0D, playerMotion.y, 0.0D));
            }

            level.addFreshEntity(projectile);
        }

        player.awardStat(Stats.ITEM_USED.get(this));
        stack.consume(1, player);
        player.getCooldowns().addCooldown(this, 10);

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public Projectile asProjectile(Level level, Position pos, ItemStack stack, Direction direction) {
        SlimeBottleProjectileEntity projectile = new SlimeBottleProjectileEntity(level, pos.x(), pos.y(), pos.z());
        projectile.setItem(stack);
        return projectile;
    }
}