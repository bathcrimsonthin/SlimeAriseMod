package com.bathcrimsonthin.slimearisemod.item;

import com.bathcrimsonthin.slimearisemod.advancement.ModCriteriaTriggers;
import com.bathcrimsonthin.slimearisemod.entity.AlliedSlimeHelper;
import com.bathcrimsonthin.slimearisemod.entity.ModEntities;
import com.bathcrimsonthin.slimearisemod.entity.SlamAnimationHelper;
import com.bathcrimsonthin.slimearisemod.entity.SlimeFieldEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.UUID;

public class SlimySwordItem extends SwordItem {

    private static final int DOMAIN_COOLDOWN_TICKS = 300; // 15秒
    private static final int DOMAIN_DURABILITY_COST = 20;
    private static final int DOMAIN_FIELD_LIFETIME = 300; // 15秒

    public static final Tier SLIMY_TIER = new Tier() {
        @Override
        public int getUses() {
            return 1561; // ダイヤモンドの剣と同じ耐久力
        }

        @Override
        public float getSpeed() {
            return 8.0F;
        }

        @Override
        public float getAttackDamageBonus() {
            return 3.0F; // createAttributes(SLIMY_TIER, 6, -2.4F) と合算して合計攻撃力 10.0
        }

        @Override
        public TagKey<Block> getIncorrectBlocksForDrops() {
            return BlockTags.INCORRECT_FOR_DIAMOND_TOOL;
        }

        @Override
        public int getEnchantmentValue() {
            return 15;
        }

        @Override
        public Ingredient getRepairIngredient() {
            return Ingredient.of(Items.SLIME_BLOCK);
        }
    };

    public SlimySwordItem(Properties properties) {
        super(SLIMY_TIER, properties);
    }

    @Override
    public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
        return repair.is(Items.SLIME_BLOCK) || super.isValidRepairItem(toRepair, repair);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }
        return tryStartSlam(context.getLevel(), player, context.getHand()).getResult();
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        return tryStartSlam(level, player, usedHand);
    }

    private InteractionResultHolder<ItemStack> tryStartSlam(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (player.getCooldowns().isOnCooldown(this) || SlamAnimationHelper.isSlamming(player)) {
            return InteractionResultHolder.pass(stack);
        }

        double groundY = findGroundY(level, player.getX(), player.getY(), player.getZ());
        if (!player.onGround() && (player.getY() - groundY) > 2.0D) {
            if (!level.isClientSide) {
                player.displayClientMessage(
                        Component.translatable("item.slimearisemod.slimy_sword.fail.air").withStyle(ChatFormatting.RED),
                        true
                );
            }
            return InteractionResultHolder.fail(stack);
        }

        if (level.isClientSide) {
            return InteractionResultHolder.success(stack);
        }

        // 突き立てモーション開始（移動固定・Sync同期）
        SlamAnimationHelper.startSlam(player, hand);

        // 振り下ろし風切り音
        level.playSound(null, player.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.0F, 0.65F);

        if (!player.getAbilities().instabuild) {
            stack.hurtAndBreak(DOMAIN_DURABILITY_COST, player, LivingEntity.getSlotForHand(hand));
        }
        player.getCooldowns().addCooldown(this, DOMAIN_COOLDOWN_TICKS);

        ModCriteriaTriggers.onUseSlimySword(player);
        return InteractionResultHolder.consume(stack);
    }

    // 剣が地面に突き刺さった瞬間（Impactフレーム）にサーバー側で呼ばれる
    public static void executeSlamImpact(ServerLevel serverLevel, Player player) {
        UUID ownerUuid = player.getUUID();

        // プレイヤーの少し前方（剣先が刺さる位置）を衝撃の中心にする
        Vec3 look = Vec3.directionFromRotation(0.0F, player.getYRot());
        double centerX = player.getX() + look.x * 0.65D;
        double centerZ = player.getZ() + look.z * 0.65D;
        double groundY = findGroundY(serverLevel, centerX, player.getY(), centerZ);

        // ① 放射状にスライムフィールドを展開（中央1枚 ＋ 周囲最大3枚）
        int deployedFields = 0;
        if (tryDeployOwnedField(serverLevel, centerX, groundY, centerZ, ownerUuid)) {
            deployedFields++;
        }

        int radialDirections = 5;
        double baseAngle = serverLevel.random.nextDouble() * Math.PI * 2.0D;
        double ringRadius = 4.6D;

        for (int i = 0; i < radialDirections && deployedFields < 4; i++) {
            double angle = baseAngle + (Math.PI * 2.0D / radialDirections) * i;
            double fx = centerX + Math.cos(angle) * ringRadius;
            double fz = centerZ + Math.sin(angle) * ringRadius;
            double fy = findGroundY(serverLevel, fx, groundY, fz);

            if (tryDeployOwnedField(serverLevel, fx, fy, fz, ownerUuid)) {
                deployedFields++;
            }
        }

        // 味方スライムを4体召喚（1/2バニラ・1/2レアスライム、大サイズと中サイズをミックス）
        int initialSummonCount = 4;
        for (int i = 0; i < initialSummonCount; i++) {
            double angle = (Math.PI * 2.0D / initialSummonCount) * i + 0.4D;
            int size = (i == 0 || serverLevel.random.nextFloat() < 0.5F)
                    ? AlliedSlimeHelper.SIZE_LARGE
                    : AlliedSlimeHelper.SIZE_MEDIUM;

            // 大サイズは当たり判定が広いため少し外側に配置
            double dist = (size >= AlliedSlimeHelper.SIZE_LARGE) ? 2.9D : 2.3D;
            double sx = centerX + Math.cos(angle) * dist;
            double sz = centerZ + Math.sin(angle) * dist;
            double sy = findGroundY(serverLevel, sx, groundY, sz);

            AlliedSlimeHelper.spawnAlliedSlime(
                    serverLevel,
                    sx,
                    sy + 0.25D,
                    sz,
                    size,
                    0.5F, // 1/2 レアスライム、1/2 バニラスライム
                    Math.cos(angle) * 0.25D,
                    0.3D,
                    Math.sin(angle) * 0.25D,
                    ownerUuid
            );
        }

        // 剣先の地面ブロック破片パーティクル
        BlockPos impactBlockPos = BlockPos.containing(centerX, groundY - 0.2D, centerZ);
        BlockState groundState = serverLevel.getBlockState(impactBlockPos);
        if (groundState.isAir()) {
            groundState = Blocks.SLIME_BLOCK.defaultBlockState();
        }
        serverLevel.sendParticles(
                new BlockParticleOption(ParticleTypes.BLOCK, groundState),
                centerX,
                groundY + 0.1D,
                centerZ,
                28,
                0.45D,
                0.25D,
                0.45D,
                0.2D
        );

        // 放射状衝撃波パーティクル
        for (int ring = 1; ring <= 3; ring++) {
            double r = ring * 1.8D;
            int points = ring * 18;
            for (int p = 0; p < points; p++) {
                double a = (Math.PI * 2.0D / points) * p;
                double px = centerX + Math.cos(a) * r;
                double pz = centerZ + Math.sin(a) * r;
                serverLevel.sendParticles(ParticleTypes.ITEM_SLIME, px, groundY + 0.12D, pz, 1, 0.04D, 0.18D, 0.04D, 0.08D);
                if (ring == 1) {
                    serverLevel.sendParticles(ParticleTypes.CLOUD, px, groundY + 0.08D, pz, 1, 0.05D, 0.04D, 0.05D, 0.03D);
                }
                if (ring == 3 && p % 2 == 0) {
                    serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, px, groundY + 0.3D, pz, 1, 0.0D, 0.1D, 0.0D, 0.0D);
                }
            }
        }

        // 突き立て衝撃音＋領域展開音
        BlockPos soundPos = BlockPos.containing(centerX, groundY, centerZ);
        serverLevel.playSound(null, soundPos, SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 0.75F, 0.55F);
        serverLevel.playSound(null, soundPos, SoundEvents.SLIME_SQUISH, SoundSource.PLAYERS, 1.5F, 0.5F);
        serverLevel.playSound(null, soundPos, SoundEvents.TRIDENT_THUNDER.value(), SoundSource.PLAYERS, 0.55F, 1.45F);
        serverLevel.playSound(null, soundPos, SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 0.9F, 1.35F);
    }

    private static boolean tryDeployOwnedField(ServerLevel serverLevel, double x, double y, double z, UUID ownerUuid) {
        double minSpacing = SlimeFieldEntity.FIELD_RADIUS * 1.8D;
        AABB searchBox = new AABB(
                x - minSpacing, y - 1.5D, z - minSpacing,
                x + minSpacing, y + 1.5D, z + minSpacing
        );

        List<SlimeFieldEntity> existingFields = serverLevel.getEntitiesOfClass(SlimeFieldEntity.class, searchBox);
        for (SlimeFieldEntity existing : existingFields) {
            double dx = existing.getX() - x;
            double dz = existing.getZ() - z;
            if (dx * dx + dz * dz < minSpacing * minSpacing) {
                return false;
            }
        }

        SlimeFieldEntity field = new SlimeFieldEntity(ModEntities.SLIME_FIELD, serverLevel);
        field.moveTo(x, y + 0.01D, z, 0.0F, 0.0F);
        field.setOwnerUuid(ownerUuid);
        field.setMaxLifetime(DOMAIN_FIELD_LIFETIME);
        serverLevel.addFreshEntity(field);
        return true;
    }

    private static double findGroundY(Level level, double x, double startY, double z) {
        int bx = Mth.floor(x);
        int bz = Mth.floor(z);
        int topY = Mth.floor(startY) + 2;
        int bottomY = Mth.floor(startY) - 3;

        for (int by = topY; by >= bottomY; by--) {
            BlockPos pos = new BlockPos(bx, by, bz);
            if (!level.getBlockState(pos).getCollisionShape(level, pos).isEmpty()) {
                return by + 1.0D;
            }
        }
        return startY;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(
                Component.translatable("item.slimearisemod.slimy_sword.tooltip").withStyle(ChatFormatting.GREEN)
        );
        tooltipComponents.add(
                Component.translatable("item.slimearisemod.slimy_sword.tooltip2").withStyle(ChatFormatting.GRAY)
        );
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}