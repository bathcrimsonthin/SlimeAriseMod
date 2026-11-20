package com.bathcrimsonthin.slimearisemod.entity;

import com.bathcrimsonthin.slimearisemod.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.ResetUniversalAngerTargetGoal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.monster.Endermite;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.DragonFireball;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.providers.VanillaEnchantmentProviders;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

public class EnderSlimeEntity extends Slime implements NeutralMob {

    private static final int DELAY_BETWEEN_CREEPY_STARE_SOUND = 400;
    private static final int MIN_DEAGGRESSION_TIME = 600;

    private static final EntityDataAccessor<Optional<BlockState>> DATA_CARRY_STATE =
            SynchedEntityData.defineId(EnderSlimeEntity.class, EntityDataSerializers.OPTIONAL_BLOCK_STATE);

    private static final EntityDataAccessor<Boolean> DATA_CREEPY =
            SynchedEntityData.defineId(EnderSlimeEntity.class, EntityDataSerializers.BOOLEAN);

    private static final EntityDataAccessor<Boolean> DATA_STARED_AT =
            SynchedEntityData.defineId(EnderSlimeEntity.class, EntityDataSerializers.BOOLEAN);

    private static final UniformInt PERSISTENT_ANGER_TIME = TimeUtil.rangeOfSeconds(20, 39);

    private int lastStareSound = Integer.MIN_VALUE;
    private int targetChangeTime;
    private int remainingPersistentAngerTime;
    @Nullable
    private UUID persistentAngerTarget;

    public EnderSlimeEntity(EntityType<? extends Slime> entityType, Level level) {
        super(entityType, level);
        this.setPathfindingMalus(PathType.WATER, -1.0F);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MOVEMENT_SPEED, 0.3D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_CARRY_STATE, Optional.empty());
        builder.define(DATA_CREEPY, false);
        builder.define(DATA_STARED_AT, false);
    }

    @Override
    public void setSize(int size, boolean resetHealth) {
        super.setSize(size, resetHealth);
        double maxHealth = (double) (size * size * 4);
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(maxHealth);
        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue((double) (size * 2));

        if (resetHealth) {
            this.setHealth((float) maxHealth);
        }
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new EnderSlimeFloatGoal(this));
        this.goalSelector.addGoal(2, new EnderSlimeAttackGoal(this));
        this.goalSelector.addGoal(3, new EnderSlimeDragonBreathGoal(this));
        this.goalSelector.addGoal(4, new EnderSlimeRandomDirectionGoal(this));
        this.goalSelector.addGoal(5, new EnderSlimeKeepOnJumpingGoal(this));
        this.goalSelector.addGoal(10, new EnderSlimePlaceBlockGoal(this));
        this.goalSelector.addGoal(11, new EnderSlimeTakeBlockGoal(this));

        this.targetSelector.addGoal(1, new EnderSlimeLookForPlayerGoal(this, this::isAngryAt));
        this.targetSelector.addGoal(2, new EnderSlimeHurtByTargetGoal(this));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Endermite.class, true, false));
        this.targetSelector.addGoal(4, new ResetUniversalAngerTargetGoal<>(this, false));
    }

    @Override
    public void setTarget(@Nullable LivingEntity livingEntity) {
        super.setTarget(livingEntity);
        if (livingEntity == null) {
            this.targetChangeTime = 0;
            this.entityData.set(DATA_CREEPY, false);
            this.entityData.set(DATA_STARED_AT, false);
        } else {
            this.targetChangeTime = this.tickCount;
            this.entityData.set(DATA_CREEPY, true);
        }
    }

    @Override
    public void startPersistentAngerTimer() {
        this.setRemainingPersistentAngerTime(PERSISTENT_ANGER_TIME.sample(this.random));
    }

    @Override
    public void setRemainingPersistentAngerTime(int remainingPersistentAngerTime) {
        this.remainingPersistentAngerTime = remainingPersistentAngerTime;
    }

    @Override
    public int getRemainingPersistentAngerTime() {
        return this.remainingPersistentAngerTime;
    }

    @Override
    public void setPersistentAngerTarget(@Nullable UUID persistentAngerTarget) {
        this.persistentAngerTarget = persistentAngerTarget;
    }

    @Override
    @Nullable
    public UUID getPersistentAngerTarget() {
        return this.persistentAngerTarget;
    }

    public void playStareSound() {
        if (this.tickCount >= this.lastStareSound + DELAY_BETWEEN_CREEPY_STARE_SOUND) {
            this.lastStareSound = this.tickCount;
            if (!this.isSilent()) {
                this.level().playLocalSound(this.getX(), this.getEyeY(), this.getZ(), SoundEvents.ENDERMAN_STARE, this.getSoundSource(), 2.5F, 1.0F, false);
            }
        }
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        if (DATA_CREEPY.equals(key) && this.hasBeenStaredAt() && this.level().isClientSide) {
            this.playStareSound();
        }
        super.onSyncedDataUpdated(key);
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide) {
            for (int i = 0; i < 2; ++i) {
                this.level().addParticle(
                        ParticleTypes.PORTAL,
                        this.getRandomX(0.5D),
                        this.getRandomY() - 0.25D,
                        this.getRandomZ(0.5D),
                        (this.random.nextDouble() - 0.5D) * 2.0D,
                        -this.random.nextDouble(),
                        (this.random.nextDouble() - 0.5D) * 2.0D
                );
            }
        } else {
            this.updatePersistentAnger((ServerLevel) this.level(), true);

            // 水・雨でのダメージ ＋ テレポート判定
            if (this.isInWaterOrRain()) {
                this.hurt(this.damageSources().drown(), 1.0F);
                this.teleportRandomly();
            }

            // 直射日光回避テレポート（日陰・地下を優先して退避する）
            if (this.level().isDay() && this.tickCount >= this.targetChangeTime + MIN_DEAGGRESSION_TIME) {
                float brightness = this.getLightLevelDependentMagicValue();
                if (brightness > 0.5F && this.level().canSeeSky(this.blockPosition()) && this.random.nextFloat() * 15.0F < (brightness - 0.4F) * 2.0F) {
                    this.setTarget(null);
                    this.teleportToShade();
                }
            }

            // 自身が生成したブレス雲（AreaEffectCloud）の残留時間を短縮・調整
            this.level().getEntitiesOfClass(AreaEffectCloud.class, this.getBoundingBox().inflate(64.0D),
                    cloud -> cloud.getOwner() == this && cloud.getDuration() > 60
            ).forEach(cloud -> {
                cloud.setDuration(100); // 残留時間を 10秒 に設定
//                cloud.setRadiusPerTick(-cloud.getRadius() / 60.0F); // 時間経過とともに円滑に収縮
            });
        }
    }

    @Override
    public boolean isSensitiveToWater() {
        return true;
    }

    @Override
    protected @NotNull ParticleOptions getParticleType() {
        // ダミーアイテムの見た目を持ったアイテム破砕パーティクルを生成
        return new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(ModItems.ENDER_SLIME_BALL));
    }

    @Override
    public void push(Entity entity) {
        super.push(entity);
        // バニラ Slime#push は IronGolem のみ対象のため、敵対対象の Endermite にも接触ダメージを発生させる
        if (entity instanceof Endermite && this.isDealsDamage()) {
            this.dealDamage((LivingEntity) entity);
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.isInvulnerableTo(source)) {
            return false;
        }

        if (source.is(DamageTypes.DRAGON_BREATH)) {
            return false;
        }
        if (source.getDirectEntity() instanceof AreaEffectCloud cloud && cloud.getOwner() == this) {
            return false;
        }
        if (source.getDirectEntity() instanceof DragonFireball fireball && fireball.getOwner() == this) {
            return false;
        }

        boolean isPotion = source.getDirectEntity() instanceof ThrownPotion;
        if (!source.is(DamageTypeTags.IS_PROJECTILE) && !isPotion) {
            boolean result = super.hurt(source, amount);
            if (!this.level().isClientSide() && !(source.getEntity() instanceof LivingEntity) && this.random.nextInt(10) != 0) {
                this.teleportRandomly();
            }
            return result;
        } else {
            boolean waterDamage = isPotion && this.hurtWithCleanWater(source, (ThrownPotion) source.getDirectEntity(), amount);

            for (int i = 0; i < 64; ++i) {
                if (this.teleportRandomly()) {
                    return true;
                }
            }
            return waterDamage;
        }
    }

    private boolean hurtWithCleanWater(DamageSource source, ThrownPotion thrownPotion, float amount) {
        ItemStack itemStack = thrownPotion.getItem();
        PotionContents potionContents = itemStack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
        return potionContents.is(Potions.WATER) && super.hurt(source, amount);
    }

    public boolean teleportToShade() {
        if (!this.level().isClientSide() && this.isAlive()) {
            // 最大16回試行し、空が見えない場所（洞窟・木陰・屋根の下）を優先してテレポートする
            for (int i = 0; i < 16; ++i) {
                double x = this.getX() + (this.random.nextDouble() - 0.5D) * 64.0D;
                // 地下洞窟へ入りやすいよう、下方向（-40 〜 +16）にバイアスをかける
                double y = this.getY() + (double) (this.random.nextInt(56) - 40);
                double z = this.getZ() + (this.random.nextDouble() - 0.5D) * 64.0D;
                if (this.teleportIfShaded(x, y, z)) {
                    return true;
                }
            }
            // 周囲に日陰や洞窟が見つからない場合（開けた平原・砂漠など）は通常のランダムテレポートを行う
            return this.teleportRandomly();
        }
        return false;
    }

    private boolean teleportIfShaded(double x, double y, double z) {
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos(x, y, z);
        while (mutablePos.getY() > this.level().getMinBuildHeight() && !this.level().getBlockState(mutablePos).blocksMotion()) {
            mutablePos.move(Direction.DOWN);
        }

        BlockState state = this.level().getBlockState(mutablePos);
        boolean blocksMotion = state.blocksMotion();
        boolean isWater = state.getFluidState().is(FluidTags.WATER);

        if (blocksMotion && !isWater) {
            BlockPos landingPos = mutablePos.above();
            if (!this.level().canSeeSky(landingPos)) {
                return this.teleport(x, landingPos.getY(), z);
            }
        }
        return false;
    }

    public boolean teleportRandomly() {
        if (!this.level().isClientSide() && this.isAlive()) {
            double x = this.getX() + (this.random.nextDouble() - 0.5D) * 64.0D;
            double y = this.getY() + (double) (this.random.nextInt(64) - 32);
            double z = this.getZ() + (this.random.nextDouble() - 0.5D) * 64.0D;
            return this.teleport(x, y, z);
        }
        return false;
    }

    public boolean teleportTowards(Entity target) {
        Vec3 diff = new Vec3(this.getX() - target.getX(), this.getY(0.5D) - target.getEyeY(), this.getZ() - target.getZ()).normalize();
        double x = this.getX() + (this.random.nextDouble() - 0.5D) * 8.0D - diff.x * 16.0D;
        double y = this.getY() + (double) (this.random.nextInt(16) - 8) - diff.y * 16.0D;
        double z = this.getZ() + (this.random.nextDouble() - 0.5D) * 8.0D - diff.z * 16.0D;
        return this.teleport(x, y, z);
    }

    private boolean teleport(double x, double y, double z) {
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos(x, y, z);
        while (mutablePos.getY() > this.level().getMinBuildHeight() && !this.level().getBlockState(mutablePos).blocksMotion()) {
            mutablePos.move(Direction.DOWN);
        }

        BlockState state = this.level().getBlockState(mutablePos);
        boolean blocksMotion = state.blocksMotion();
        boolean isWater = state.getFluidState().is(FluidTags.WATER);

        if (blocksMotion && !isWater) {
            Vec3 oldPos = this.position();
            boolean success = this.randomTeleport(x, y, z, true);
            if (success) {
                this.level().gameEvent(GameEvent.TELEPORT, oldPos, GameEvent.Context.of(this));
                if (!this.isSilent()) {
                    this.level().playSound(null, this.xo, this.yo, this.zo, SoundEvents.ENDERMAN_TELEPORT, this.getSoundSource(), 1.0F, 1.0F);
                    this.playSound(SoundEvents.ENDERMAN_TELEPORT, 1.0F, 1.0F);
                }
            }
            return success;
        }
        return false;
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return this.isCreepy() ? SoundEvents.ENDERMAN_SCREAM : SoundEvents.ENDERMAN_AMBIENT;
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel serverLevel, DamageSource damageSource, boolean recentlyHit) {
        super.dropCustomDeathLoot(serverLevel, damageSource, recentlyHit);
        BlockState carriedState = this.getCarriedBlock();
        if (carriedState != null) {
            ItemStack itemStack = new ItemStack(Items.DIAMOND_AXE);
            EnchantmentHelper.enchantItemFromProvider(itemStack, serverLevel.registryAccess(), VanillaEnchantmentProviders.ENDERMAN_LOOT_DROP, serverLevel.getCurrentDifficultyAt(this.blockPosition()), this.getRandom());
            LootParams.Builder builder = new LootParams.Builder(serverLevel)
                    .withParameter(LootContextParams.ORIGIN, this.position())
                    .withParameter(LootContextParams.TOOL, itemStack)
                    .withOptionalParameter(LootContextParams.THIS_ENTITY, this);

            for (ItemStack drop : carriedState.getDrops(builder)) {
                this.spawnAtLocation(drop);
            }
        }
    }

    public boolean isLookingAtMe(Player player) {
        ItemStack headStack = player.getInventory().armor.get(3);
        if (headStack.is(Blocks.CARVED_PUMPKIN.asItem())) {
            return false;
        }

        Vec3 lookVec = player.getViewVector(1.0F).normalize();
        Vec3 toEntityVec = new Vec3(this.getX() - player.getX(), this.getEyeY() - player.getEyeY(), this.getZ() - player.getZ());
        double distance = toEntityVec.length();
        toEntityVec = toEntityVec.normalize();

        double dot = lookVec.dot(toEntityVec);
        return dot > 1.0D - 0.025D / distance && player.hasLineOfSight(this);
    }

    public @Nullable BlockState getCarriedBlock() {
        return this.entityData.get(DATA_CARRY_STATE).orElse(null);
    }

    public void setCarriedBlock(@Nullable BlockState state) {
        this.entityData.set(DATA_CARRY_STATE, Optional.ofNullable(state));
    }

    public boolean isCreepy() {
        return this.entityData.get(DATA_CREEPY);
    }

    public void setCreepy(boolean creepy) {
        this.entityData.set(DATA_CREEPY, creepy);
    }

    public boolean hasBeenStaredAt() {
        return this.entityData.get(DATA_STARED_AT);
    }

    public void setBeingStaredAt() {
        this.entityData.set(DATA_STARED_AT, true);
    }

    @Override
    public boolean requiresCustomPersistence() {
        return super.requiresCustomPersistence() || this.getCarriedBlock() != null;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        BlockState carriedState = this.getCarriedBlock();
        if (carriedState != null) {
            compound.put("carriedBlockState", NbtUtils.writeBlockState(carriedState));
        }
        this.addPersistentAngerSaveData(compound);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        BlockState state = null;
        if (compound.contains("carriedBlockState", 10)) {
            state = NbtUtils.readBlockState(this.level().holderLookup(Registries.BLOCK), compound.getCompound("carriedBlockState"));
            if (state.isAir()) {
                state = null;
            }
        }
        this.setCarriedBlock(state);
        this.readPersistentAngerSaveData(this.level(), compound);
    }

    // --- Inner AI Goals ---

    static class EnderSlimeFloatGoal extends Goal {
        private final EnderSlimeEntity slime;

        public EnderSlimeFloatGoal(EnderSlimeEntity slime) {
            this.slime = slime;
            this.setFlags(EnumSet.of(Goal.Flag.JUMP, Goal.Flag.MOVE));
            slime.getNavigation().setCanFloat(true);
        }

        @Override
        public boolean canUse() {
            return (this.slime.isInWater() || this.slime.isInLava()) && this.slime.getMoveControl() instanceof Slime.SlimeMoveControl;
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            if (this.slime.getRandom().nextFloat() < 0.8F) {
                this.slime.getJumpControl().jump();
            }
            if (this.slime.getMoveControl() instanceof Slime.SlimeMoveControl moveControl) {
                moveControl.setWantedMovement(1.2D);
            }
        }
    }

    static class EnderSlimeDragonBreathGoal extends Goal {
        private final EnderSlimeEntity slime;
        private int attackCooldown;

        public EnderSlimeDragonBreathGoal(EnderSlimeEntity slime) {
            this.slime = slime;
            this.setFlags(EnumSet.noneOf(Goal.Flag.class));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = this.slime.getTarget();
            return target != null && target.isAlive() && !this.slime.isTiny() && this.slime.distanceToSqr(target) <= 256.0D;
        }

        @Override
        public boolean canContinueToUse() {
            LivingEntity target = this.slime.getTarget();
            return target != null && target.isAlive() && !this.slime.isTiny() && this.slime.distanceToSqr(target) <= 256.0D;
        }

        @Override
        public void start() {
            this.attackCooldown = 20;
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            LivingEntity target = this.slime.getTarget();
            if (target == null) {
                return;
            }

            if (this.slime.hasLineOfSight(target) && --this.attackCooldown <= 0) {
                this.attackCooldown = 80 + this.slime.getRandom().nextInt(60);

                Vec3 look = this.slime.getViewVector(1.0F);
                double spawnOffset = (double) this.slime.getSize() * 0.5D + 0.6D;
                double spawnX = this.slime.getX() + look.x * spawnOffset;
                // モデル座標の口中心 (Y=13.5F / 16.0F -> 地上から 10.5 / 16.0 = 0.65625) に合わせて射出
                double spawnY = this.slime.getY() + 0.65625D * (double) this.slime.getSize();
                double spawnZ = this.slime.getZ() + look.z * spawnOffset;

                double dX = target.getX() - spawnX;
                double dY = target.getY(0.5D) - spawnY;
                double dZ = target.getZ() - spawnZ;
                Vec3 accel = new Vec3(dX, dY, dZ).normalize().scale(0.2D);

                DragonFireball fireball = new DragonFireball(this.slime.level(), this.slime, accel);
                fireball.setPos(spawnX, spawnY, spawnZ);
                this.slime.level().addFreshEntity(fireball);

                this.slime.playSound(SoundEvents.ENDER_DRAGON_GROWL, 0.7F, 1.2F / (this.slime.getRandom().nextFloat() * 0.2F + 0.9F));
            }
        }
    }

    static class EnderSlimeAttackGoal extends Goal {
        private final EnderSlimeEntity slime;
        private int growTiredOfGrowing;

        public EnderSlimeAttackGoal(EnderSlimeEntity slime) {
            this.slime = slime;
            this.setFlags(EnumSet.of(Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = this.slime.getTarget();
            if (target == null || !target.isAlive() || !this.slime.canAttack(target)) {
                return false;
            }
            return this.slime.getMoveControl() instanceof Slime.SlimeMoveControl;
        }

        @Override
        public void start() {
            this.growTiredOfGrowing = 300;
            super.start();
        }

        @Override
        public boolean canContinueToUse() {
            LivingEntity target = this.slime.getTarget();
            if (target == null || !target.isAlive() || !this.slime.canAttack(target)) {
                return false;
            }
            return --this.growTiredOfGrowing > 0;
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            LivingEntity target = this.slime.getTarget();
            if (target != null) {
                this.slime.lookAt(target, 10.0F, 10.0F);
                if (this.slime.getMoveControl() instanceof Slime.SlimeMoveControl moveControl) {
                    moveControl.setDirection(this.slime.getYRot(), this.slime.isDealsDamage());
                }
            }
        }
    }

    static class EnderSlimeRandomDirectionGoal extends Goal {
        private final EnderSlimeEntity slime;
        private float chosenDegrees;
        private int nextRandomizeTime;

        public EnderSlimeRandomDirectionGoal(EnderSlimeEntity slime) {
            this.slime = slime;
            this.setFlags(EnumSet.of(Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            return this.slime.getTarget() == null && !this.slime.isCreepy() && (this.slime.onGround() || this.slime.isInWater() || this.slime.isInLava() || this.slime.hasEffect(MobEffects.LEVITATION));
        }

        @Override
        public void tick() {
            if (--this.nextRandomizeTime <= 0) {
                this.nextRandomizeTime = this.adjustedTickDelay(40 + this.slime.getRandom().nextInt(60));
                this.chosenDegrees = (float) this.slime.getRandom().nextInt(360);
            }

            if (this.slime.getMoveControl() instanceof Slime.SlimeMoveControl moveControl) {
                moveControl.setDirection(this.chosenDegrees, false);
            }
        }
    }

    static class EnderSlimeKeepOnJumpingGoal extends Goal {
        private final EnderSlimeEntity slime;

        public EnderSlimeKeepOnJumpingGoal(EnderSlimeEntity slime) {
            this.slime = slime;
            this.setFlags(EnumSet.of(Goal.Flag.JUMP, Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            return !this.slime.isPassenger();
        }

        @Override
        public void tick() {
            if (this.slime.getMoveControl() instanceof Slime.SlimeMoveControl moveControl) {
                moveControl.setWantedMovement(1.0D);
            }
        }
    }

    static class EnderSlimeLookForPlayerGoal extends NearestAttackableTargetGoal<Player> {
        private final EnderSlimeEntity enderSlime;
        @Nullable
        private Player pendingTarget;
        private int aggroTime;
        private int teleportTime;
        private final TargetingConditions startAggroTargetConditions;
        private final TargetingConditions continueAggroTargetConditions = TargetingConditions.forCombat().ignoreLineOfSight();
        private final Predicate<LivingEntity> isAngerInducing;

        public EnderSlimeLookForPlayerGoal(EnderSlimeEntity enderSlime, @Nullable Predicate<LivingEntity> predicate) {
            super(enderSlime, Player.class, 10, false, false, predicate);
            this.enderSlime = enderSlime;
            this.isAngerInducing = livingEntity -> (enderSlime.isLookingAtMe((Player) livingEntity) || enderSlime.isAngryAt(livingEntity)) && !enderSlime.hasIndirectPassenger(livingEntity);
            this.startAggroTargetConditions = TargetingConditions.forCombat().range(this.getFollowDistance()).selector(this.isAngerInducing);
        }

        @Override
        public boolean canUse() {
            this.pendingTarget = this.enderSlime.level().getNearestPlayer(this.startAggroTargetConditions, this.enderSlime);
            return this.pendingTarget != null;
        }

        @Override
        public void start() {
            this.aggroTime = this.adjustedTickDelay(5);
            this.teleportTime = 0;
            this.enderSlime.setBeingStaredAt();
            this.enderSlime.setCreepy(true);
        }

        @Override
        public void stop() {
            this.pendingTarget = null;
            if (this.enderSlime.getTarget() == null) {
                this.enderSlime.setCreepy(false);
            }
            super.stop();
        }

        @Override
        public boolean canContinueToUse() {
            if (this.pendingTarget != null) {
                if (!this.isAngerInducing.test(this.pendingTarget)) {
                    return false;
                } else {
                    this.enderSlime.lookAt(this.pendingTarget, 10.0F, 10.0F);
                    return true;
                }
            } else {
                if (this.target != null) {
                    if (this.enderSlime.hasIndirectPassenger(this.target)) {
                        return false;
                    }
                    if (this.continueAggroTargetConditions.test(this.enderSlime, this.target)) {
                        return true;
                    }
                }
                return super.canContinueToUse();
            }
        }

        @Override
        public void tick() {
            if (this.enderSlime.getTarget() == null) {
                super.setTarget(null);
            }

            if (this.pendingTarget != null) {
                this.enderSlime.lookAt(this.pendingTarget, 10.0F, 10.0F);
                if (--this.aggroTime <= 0) {
                    this.target = this.pendingTarget;
                    this.pendingTarget = null;
                    super.start();
                }
            } else {
                if (this.target != null && !this.enderSlime.isPassenger()) {
                    if (this.enderSlime.isLookingAtMe((Player) this.target)) {
                        if (this.target.distanceToSqr(this.enderSlime) < 16.0D) {
                            this.enderSlime.teleportRandomly();
                        }
                        this.teleportTime = 0;
                    } else if (this.target.distanceToSqr(this.enderSlime) > 256.0D && this.teleportTime++ >= this.adjustedTickDelay(30) && this.enderSlime.teleportTowards(this.target)) {
                        this.teleportTime = 0;
                    }
                }
                super.tick();
            }
        }
    }

    static class EnderSlimeHurtByTargetGoal extends TargetGoal {
        private int lastHurtTimestamp;

        public EnderSlimeHurtByTargetGoal(EnderSlimeEntity slime) {
            super(slime, true);
        }

        @Override
        public boolean canUse() {
            int currentTimestamp = this.mob.getLastHurtByMobTimestamp();
            LivingEntity attacker = this.mob.getLastHurtByMob();
            if (currentTimestamp != this.lastHurtTimestamp && attacker != null) {
                return this.canAttack(attacker, TargetingConditions.DEFAULT);
            }
            return false;
        }

        @Override
        public void start() {
            this.mob.setTarget(this.mob.getLastHurtByMob());
            this.lastHurtTimestamp = this.mob.getLastHurtByMobTimestamp();
            super.start();
        }
    }

    static class EnderSlimeTakeBlockGoal extends Goal {
        private final EnderSlimeEntity slime;

        public EnderSlimeTakeBlockGoal(EnderSlimeEntity slime) {
            this.slime = slime;
        }

        @Override
        public boolean canUse() {
            if (!com.bathcrimsonthin.slimearisemod.config.SlimeAriseConfig.get().behavior.allowEnderSlimeBlockGriefing) {
                return false;
            }
            if (this.slime.level().isDay()) {
                return false;
            } else if (this.slime.getCarriedBlock() != null) {
                return false;
            } else if (!this.slime.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
                return false;
            } else {
                return this.slime.getRandom().nextInt(reducedTickDelay(20)) == 0;
            }
        }

        @Override
        public void tick() {
            RandomSource randomSource = this.slime.getRandom();
            Level level = this.slime.level();
            int i = Mth.floor(this.slime.getX() - 2.0D + randomSource.nextDouble() * 4.0D);
            int j = Mth.floor(this.slime.getY() + randomSource.nextDouble() * 3.0D);
            int k = Mth.floor(this.slime.getZ() - 2.0D + randomSource.nextDouble() * 4.0D);
            BlockPos blockPos = new BlockPos(i, j, k);
            BlockState blockState = level.getBlockState(blockPos);
            Vec3 vec3 = new Vec3((double) this.slime.getBlockX() + 0.5D, (double) j + 0.5D, (double) this.slime.getBlockZ() + 0.5D);
            Vec3 vec32 = new Vec3((double) i + 0.5D, (double) j + 0.5D, (double) k + 0.5D);
            BlockHitResult blockHitResult = level.clip(new ClipContext(vec3, vec32, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, this.slime));
            boolean hitTargetBlock = blockHitResult.getBlockPos().equals(blockPos);
            if (blockState.is(BlockTags.ENDERMAN_HOLDABLE) && hitTargetBlock) {
                level.removeBlock(blockPos, false);
                level.gameEvent(GameEvent.BLOCK_DESTROY, blockPos, GameEvent.Context.of(this.slime, blockState));
                this.slime.setCarriedBlock(blockState.getBlock().defaultBlockState());
            }
        }
    }

    static class EnderSlimePlaceBlockGoal extends Goal {
        private final EnderSlimeEntity slime;

        public EnderSlimePlaceBlockGoal(EnderSlimeEntity slime) {
            this.slime = slime;
        }

        @Override
        public boolean canUse() {
            if (this.slime.getCarriedBlock() == null) {
                return false;
            } else if (!this.slime.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
                return false;
            } else {
                return this.slime.getRandom().nextInt(reducedTickDelay(2000)) == 0;
            }
        }

        @Override
        public void tick() {
            RandomSource randomSource = this.slime.getRandom();
            Level level = this.slime.level();
            int i = Mth.floor(this.slime.getX() - 1.0D + randomSource.nextDouble() * 2.0D);
            int j = Mth.floor(this.slime.getY() + randomSource.nextDouble() * 2.0D);
            int k = Mth.floor(this.slime.getZ() - 1.0D + randomSource.nextDouble() * 2.0D);
            BlockPos blockPos = new BlockPos(i, j, k);
            BlockState blockState = level.getBlockState(blockPos);
            BlockPos blockPosBelow = blockPos.below();
            BlockState blockStateBelow = level.getBlockState(blockPosBelow);
            BlockState carriedState = this.slime.getCarriedBlock();
            if (carriedState != null) {
                carriedState = Block.updateFromNeighbourShapes(carriedState, this.slime.level(), blockPos);
                if (this.canPlaceBlock(level, blockPos, carriedState, blockState, blockStateBelow, blockPosBelow)) {
                    level.setBlock(blockPos, carriedState, 3);
                    level.gameEvent(GameEvent.BLOCK_PLACE, blockPos, GameEvent.Context.of(this.slime, carriedState));
                    this.slime.setCarriedBlock(null);
                }
            }
        }

        private boolean canPlaceBlock(Level level, BlockPos blockPos, BlockState carriedState, BlockState targetState, BlockState belowState, BlockPos belowPos) {
            return targetState.isAir()
                    && !carriedState.isAir()
                    && !carriedState.is(Blocks.BEDROCK)
                    && belowState.isCollisionShapeFullBlock(level, belowPos)
                    && carriedState.canSurvive(level, blockPos)
                    && level.getEntities(this.slime, AABB.unitCubeFromLowerCorner(Vec3.atLowerCornerOf(blockPos))).isEmpty();
        }
    }
}