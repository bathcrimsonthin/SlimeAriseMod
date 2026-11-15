package com.bathcrimsonthin.slimearisemod.item;

import com.bathcrimsonthin.slimearisemod.entity.StickyTorchProjectileEntity;
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
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import java.util.List;

public class StickyTorchItem extends BlockItem implements ProjectileItem {

    public StickyTorchItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);

        level.playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                SoundEvents.SNOWBALL_THROW,
                SoundSource.PLAYERS,
                0.6F,
                0.85F / (level.getRandom().nextFloat() * 0.4F + 0.8F)
        );
        level.playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                SoundEvents.SLIME_SQUISH_SMALL,
                SoundSource.PLAYERS,
                0.45F,
                1.35F
        );

        if (!level.isClientSide) {
            StickyTorchProjectileEntity projectile = new StickyTorchProjectileEntity(level, player);
            projectile.setItem(stack.copyWithCount(1));
            projectile.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.55F, 1.0F);
            level.addFreshEntity(projectile);
        }

        player.awardStat(Stats.ITEM_USED.get(this));
        player.getCooldowns().addCooldown(this, 4); // 0.2秒クールダウンで連続投擲可能
        stack.consume(1, player);

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public Projectile asProjectile(Level level, Position pos, ItemStack stack, Direction direction) {
        StickyTorchProjectileEntity projectile = new StickyTorchProjectileEntity(level, pos.x(), pos.y(), pos.z());
        projectile.setItem(stack.copyWithCount(1));
        return projectile;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(
                Component.translatable("item.slimearisemod.sticky_torch.tooltip").withStyle(ChatFormatting.GREEN)
        );
        tooltipComponents.add(
                Component.translatable("item.slimearisemod.sticky_torch.tooltip2").withStyle(ChatFormatting.GRAY)
        );
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}