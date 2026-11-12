package com.bathcrimsonthin.slimearisemod.entity;

import com.bathcrimsonthin.slimearisemod.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.Optional;

public class EnderSlimeEntity extends Slime {

    private static final EntityDataAccessor<Optional<BlockState>> DATA_CARRY_STATE =
            SynchedEntityData.defineId(EnderSlimeEntity.class, EntityDataSerializers.OPTIONAL_BLOCK_STATE);

    private static final EntityDataAccessor<Boolean> DATA_CREEPY =
            SynchedEntityData.defineId(EnderSlimeEntity.class, EntityDataSerializers.BOOLEAN);

    public EnderSlimeEntity(EntityType<? extends Slime> entityType, Level level) {
        super(entityType, level);
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
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new EnderSlimeAttackGoal(this));
        this.goalSelector.addGoal(3, new EnderSlimeDragonBreathGoal(this));
        this.goalSelector.addGoal(4, new EnderSlimeRandomDirectionGoal(this));
        this.goalSelector.addGoal(5, new EnderSlimeKeepOnJumpingGoal(this));
        this.goalSelector.addGoal(10, new EnderSlimePlaceBlockGoal(this));
        this.goalSelector.addGoal(11, new EnderSlimeTakeBlockGoal(this));

        this.targetSelector.addGoal(1, new EnderSlimeLookForPlayerGoal(this));
        this.targetSelector.addGoal(2, new EnderSlimeHurtByTargetGoal(this));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Endermite.class, true, false));
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
            // 水・雨でのダメージ ＋ テレポート判定
            if (this.isInWaterOrRain()) {
                this.hurt(this.damageSources().drown(), 1.0F);
                this.teleportRandomly();
            }

            // 直射日光回避テレポート
            if (this.level().isDay()) {
                float brightness = this.getLightLevelDependentMagicValue();
                if (brightness > 0.5F && this.level().canSeeSky(this.blockPosition()) && this.random.nextFloat() * 30.0F < (brightness - 0.4F) * 2.0F) {
                    this.setTarget(null);
                    this.teleportRandomly();
                }
            }

            // 自身が生成したブレス雲（AreaEffectCloud）の残留時間を 3秒（60 ticks）に短縮・調整
            this.level().getEntitiesOfClass(AreaEffectCloud.class, this.getBoundingBox().inflate(64.0D),
                    cloud -> cloud.getOwner() == this && cloud.getDuration() > 60
            ).forEach(cloud -> {
                cloud.setDuration(300); // 残留時間を 15秒 に設定
                cloud.setRadiusPerTick(-cloud.getRadius() / 60.0F); // 時間経過とともに円滑に収縮
            });
        }
    }

    @Override
    protected @NotNull ParticleOptions getParticleType() {
        // ダミーアイテムの見た目を持ったアイテム破砕パーティクルを生成
        return new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(ModItems.DUMMY_ENDER_SLIME_BALL));
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
            for (int i = 0; i < 64; ++i) {
                if (this.teleportRandomly()) {
                    return true;
                }
            }
            return super.hurt(source, amount);
        }
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
            mutablePos.move(0, -1, 0);
        }

        BlockState state = this.level().getBlockState(mutablePos);
        boolean blocksMotion = state.blocksMotion();
        boolean isWater = state.getFluidState().is(FluidTags.WATER);

        if (blocksMotion && !isWater) {
            Vec3 oldPos = this.position();
            boolean success = this.randomTeleport(x, mutablePos.getY() + 1.0D, z, true);
            if (success) {
                this.level().gameEvent(GameEvent.TELEPORT, oldPos, GameEvent.Context.of(this));
                if (!this.isSilent()) {
                    this.level().playSound(null, oldPos.x, oldPos.y, oldPos.z, SoundEvents.ENDERMAN_TELEPORT, this.getSoundSource(), 1.0F, 1.0F);
                    this.playSound(SoundEvents.ENDERMAN_TELEPORT, 1.0F, 1.0F);
                }
                return true;
            }
        }
        return false;
    }

    public boolean isLookingAtMe(Player player) {
        ItemStack headStack = player.getInventory().armor.get(3);
        if (headStack.is(Items.CARVED_PUMPKIN)) {
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

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        BlockState carriedState = this.getCarriedBlock();
        if (carriedState != null) {
            compound.put("carriedBlockState", NbtUtils.writeBlockState(carriedState));
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains("carriedBlockState", 10)) {
            BlockState state = NbtUtils.readBlockState(this.level().holderLookup(Registries.BLOCK), compound.getCompound("carriedBlockState"));
            if (state.isAir()) {
                state = null;
            }
            this.setCarriedBlock(state);
        }
    }

    // --- Inner AI Goals ---

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

            if (--this.attackCooldown <= 0) {
                this.attackCooldown = 80 + this.slime.getRandom().nextInt(60);

                Vec3 look = this.slime.getViewVector(1.0F);
                double spawnOffset = (double) this.slime.getSize() * 0.5D + 0.6D;
                double spawnX = this.slime.getX() + look.x * spawnOffset;
                double spawnY = this.slime.getEyeY();
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
            if (target == null || !target.isAlive()) {
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
            if (target == null || !target.isAlive()) {
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
                this.slime.lookAt(target, 30.0F, 30.0F);
                if (this.slime.getMoveControl() instanceof Slime.SlimeMoveControl moveControl) {
                    moveControl.setDirection(this.slime.getYRot(), true);
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
                this.nextRandomizeTime = 40 + this.slime.getRandom().nextInt(60);
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
        private Player pendingTarget;
        private int stareTimer;
        private int targetChangeTime;

        public EnderSlimeLookForPlayerGoal(EnderSlimeEntity enderSlime) {
            super(enderSlime, Player.class, false);
            this.enderSlime = enderSlime;
        }

        @Override
        public boolean canUse() {
            Player player = this.enderSlime.level().getNearestPlayer(this.enderSlime, this.getFollowDistance());
            if (player != null && this.enderSlime.isLookingAtMe(player)) {
                this.pendingTarget = player;
                return true;
            }
            return false;
        }

        @Override
        public void start() {
            this.stareTimer = 5;
            this.targetChangeTime = 0;
            this.enderSlime.setCreepy(true);
            this.enderSlime.playSound(SoundEvents.ENDERMAN_STARE, 1.0F, 1.0F);
        }

        @Override
        public void stop() {
            this.pendingTarget = null;
            this.enderSlime.setCreepy(false);
            super.stop();
        }

        @Override
        public boolean canContinueToUse() {
            if (this.pendingTarget != null) {
                if (!this.enderSlime.isLookingAtMe(this.pendingTarget)) {
                    return false;
                } else {
                    this.enderSlime.lookAt(this.pendingTarget, 30.0F, 30.0F);
                    return true;
                }
            }
            return this.target != null && this.target.isAlive() ? true : super.canContinueToUse();
        }

        @Override
        public void tick() {
            if (this.pendingTarget != null) {
                this.enderSlime.lookAt(this.pendingTarget, 30.0F, 30.0F);
                if (--this.stareTimer <= 0) {
                    this.target = this.pendingTarget;
                    this.enderSlime.setTarget(this.pendingTarget);
                    this.pendingTarget = null;
                }
            } else {
                if (this.target != null && !this.enderSlime.isPassenger()) {
                    if (this.enderSlime.isLookingAtMe((Player) this.target)) {
                        if (this.target.distanceToSqr(this.enderSlime) < 16.0D) {
                            this.enderSlime.teleportRandomly();
                        }
                        this.targetChangeTime = 0;
                    } else if (this.target.distanceToSqr(this.enderSlime) > 256.0D && this.targetChangeTime++ >= 30 && this.enderSlime.teleportTowards(this.target)) {
                        this.targetChangeTime = 0;
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
            return this.slime.getCarriedBlock() == null && this.slime.getRandom().nextInt(200) == 0;
        }

        @Override
        public void start() {
            BlockPos pos = this.slime.blockPosition().below();
            BlockState state = this.slime.level().getBlockState(pos);
            if (state.is(BlockTags.ENDERMAN_HOLDABLE)) {
                this.slime.setCarriedBlock(state);
                this.slime.level().setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
                this.slime.playSound(SoundEvents.ENDERMAN_SCREAM, 0.5F, 1.5F);
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
            return this.slime.getCarriedBlock() != null && this.slime.getRandom().nextInt(2000) == 0;
        }

        @Override
        public void start() {
            BlockPos pos = this.slime.blockPosition();
            BlockState current = this.slime.level().getBlockState(pos);
            BlockState below = this.slime.level().getBlockState(pos.below());

            if (current.isAir() && below.blocksMotion()) {
                this.slime.level().setBlockAndUpdate(pos, this.slime.getCarriedBlock());
                this.slime.setCarriedBlock(null);
            }
        }
    }
}