package com.bathcrimsonthin.slimearisemod.item;

import com.bathcrimsonthin.slimearisemod.entity.KingSlimeEntity;
import com.bathcrimsonthin.slimearisemod.entity.ModEntities;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class SlimyCrownItem extends Item {

    public SlimyCrownItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);

        if (level.isClientSide) {
            return InteractionResultHolder.success(stack);
        }

        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResultHolder.fail(stack);
        }

        BlockPos playerPos = player.blockPosition();

        // 1. 湿地帯チェック（湿地 / マングローブの沼地）
        Holder<Biome> biome = serverLevel.getBiome(playerPos);
        boolean isSwamp = biome.is(Biomes.SWAMP) || biome.is(Biomes.MANGROVE_SWAMP);
        if (!isSwamp) {
            player.displayClientMessage(
                    Component.translatable("item.slimearisemod.slimy_crown.fail.biome").withStyle(ChatFormatting.RED),
                    true
            );
            return InteractionResultHolder.fail(stack);
        }

        // 2. 空が見える地上チェック
        if (!serverLevel.canSeeSky(playerPos)) {
            player.displayClientMessage(
                    Component.translatable("item.slimearisemod.slimy_crown.fail.sky").withStyle(ChatFormatting.RED),
                    true
            );
            return InteractionResultHolder.fail(stack);
        }

        // 3. 夜チェック
        if (!serverLevel.isNight()) {
            player.displayClientMessage(
                    Component.translatable("item.slimearisemod.slimy_crown.fail.night").withStyle(ChatFormatting.RED),
                    true
            );
            return InteractionResultHolder.fail(stack);
        }

        // 4. 満月チェック（0 = 満月）
        if (serverLevel.getMoonPhase() != 0) {
            player.displayClientMessage(
                    Component.translatable("item.slimearisemod.slimy_crown.fail.full_moon").withStyle(ChatFormatting.RED),
                    true
            );
            return InteractionResultHolder.fail(stack);
        }

        // 召喚座標の決定（プレイヤーの視線方向3ブロック先の地表、空が見えない場合はプレイヤーの足元）
        BlockPos spawnPos = findSummonPos(serverLevel, player);

        // 演出用落雷（地形炎上・アイテム焼失・ボスへの自傷を防ぐため visualOnly）
        LightningBolt lightning = EntityType.LIGHTNING_BOLT.create(serverLevel);
        if (lightning != null) {
            lightning.moveTo(Vec3.atBottomCenterOf(spawnPos));
            lightning.setVisualOnly(true);
            serverLevel.addFreshEntity(lightning);
        }

        // サウンド＆パーティクル演出
        serverLevel.playSound(null, spawnPos, SoundEvents.WITHER_SPAWN, SoundSource.HOSTILE, 0.9F, 1.1F);
        serverLevel.playSound(null, spawnPos, SoundEvents.SLIME_SQUISH, SoundSource.HOSTILE, 1.5F, 0.5F);
        serverLevel.sendParticles(
                ParticleTypes.ITEM_SLIME,
                spawnPos.getX() + 0.5D,
                spawnPos.getY() + 1.0D,
                spawnPos.getZ() + 0.5D,
                60,
                1.5D,
                1.0D,
                1.5D,
                0.15D
        );
        serverLevel.sendParticles(
                ParticleTypes.HAPPY_VILLAGER,
                spawnPos.getX() + 0.5D,
                spawnPos.getY() + 1.2D,
                spawnPos.getZ() + 0.5D,
                30,
                1.8D,
                1.2D,
                1.8D,
                0.05D
        );

        // KingSlime 召喚
        KingSlimeEntity kingSlime = ModEntities.KING_SLIME.create(serverLevel);
        if (kingSlime != null) {
            kingSlime.moveTo(
                    spawnPos.getX() + 0.5D,
                    spawnPos.getY(),
                    spawnPos.getZ() + 0.5D,
                    Mth.wrapDegrees(player.getYRot() + 180.0F),
                    0.0F
            );
            kingSlime.finalizeSpawn(
                    serverLevel,
                    serverLevel.getCurrentDifficultyAt(spawnPos),
                    MobSpawnType.MOB_SUMMONED,
                    null
            );
            serverLevel.addFreshEntity(kingSlime);
        }

        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        player.getCooldowns().addCooldown(this, 40);

        return InteractionResultHolder.consume(stack);
    }

    private BlockPos findSummonPos(ServerLevel level, Player player) {
        Vec3 look = player.getLookAngle();
        double horizontalLen = Math.sqrt(look.x * look.x + look.z * look.z);
        if (horizontalLen > 0.001D) {
            int targetX = Mth.floor(player.getX() + (look.x / horizontalLen) * 3.5D);
            int targetZ = Mth.floor(player.getZ() + (look.z / horizontalLen) * 3.5D);
            int surfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, targetX, targetZ);
            BlockPos candidate = new BlockPos(targetX, surfaceY, targetZ);
            if (Math.abs(surfaceY - player.getBlockY()) <= 4 && level.canSeeSky(candidate)) {
                return candidate;
            }
        }
        return player.blockPosition();
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(
                Component.translatable("item.slimearisemod.slimy_crown.tooltip").withStyle(ChatFormatting.GRAY)
        );
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}