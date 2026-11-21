package com.bathcrimsonthin.slimearisemod.item;

import com.bathcrimsonthin.slimearisemod.entity.SlimeSolventProjectileEntity;
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

import java.util.List;

public class SlimeSolventItem extends Item implements ProjectileItem {

    public SlimeSolventItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        level.playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                SoundEvents.SPLASH_POTION_THROW,
                SoundSource.NEUTRAL,
                0.5F,
                0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F)
        );

        player.getCooldowns().addCooldown(this, 10);

        if (!level.isClientSide) {
            SlimeSolventProjectileEntity projectile = new SlimeSolventProjectileEntity(level, player);
            projectile.setItem(itemStack);
            projectile.shootFromRotation(player, player.getXRot(), player.getYRot(), -20.0F, 0.5F, 1.0F);
            level.addFreshEntity(projectile);
        }

        player.awardStat(Stats.ITEM_USED.get(this));
        itemStack.consume(1, player);
        return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("tooltip.slimearisemod.slime_solvent.1").withStyle(ChatFormatting.GRAY));
//        tooltipComponents.add(Component.translatable("tooltip.slimearisemod.slime_solvent.2").withStyle(ChatFormatting.DARK_AQUA));
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }

    @Override
    public Projectile asProjectile(Level level, Position pos, ItemStack stack, Direction direction) {
        SlimeSolventProjectileEntity projectile = new SlimeSolventProjectileEntity(level, pos.x(), pos.y(), pos.z());
        projectile.setItem(stack);
        return projectile;
    }

    @Override
    public DispenseConfig createDispenseConfig() {
        return DispenseConfig.builder()
                .uncertainty(DispenseConfig.DEFAULT.uncertainty() * 0.5F)
                .power(DispenseConfig.DEFAULT.power() * 1.25F)
                .build();
    }
}