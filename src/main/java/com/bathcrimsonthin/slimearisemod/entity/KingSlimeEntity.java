package com.bathcrimsonthin.slimearisemod.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

public class KingSlimeEntity extends Slime {

    public static final int STATE_NORMAL = 0;
    public static final int STATE_ABSORBING = 1;
    public static final int STATE_GIANT = 2;

    private static final int NORMAL_SIZE = 2;
    private static final int GIANT_SIZE = 18;
    private static final float MAX_MAIN_HP = 100.0F;
    private static final float MAX_GIANT_HP = 50.0F;

    private static final float GIANT_PROJECTILE_DAMAGE_MULTIPLIER = 0.60F; // 巨大アーマー中は飛翔体ダメージ40%軽減
    private static final float RANGED_COUNTER_CHANCE = 0.50F;              // 遠距離被弾カウンターの発生確率（50%）
    private static final int RANGED_COUNTER_COOLDOWN_TICKS = 80;           // 遠距離被弾カウンターのクールダウン（4.0秒）
    private static final double RANGED_COUNTER_MIN_DIST_SQR = 64.0D;       // 遠距離判定距離（8ブロック以上）
    private static final int HIGH_LEAP_INTERVAL = 3;                       // 巨大形態で大ジャンプ落下を行う間隔（3回に1回）
    private static final int HIGH_LEAP_CHARGE_DURATION = 20;               // 大ジャンプ前の予備動作タメ時間（1.0秒）
    private static final double MAX_HIGH_LEAP_RANGE = 22.0D;               // 大ジャンプの最大水平射程

    private static final int MIN_ABSORB_COUNT = 4;        // 周囲にいない場合の最低補充数
    private static final int MAX_ABSORB_COUNT = 20;       // 周囲に多数いる場合の最大吸収数
    private static final int ABSORB_DURATION_TICKS = 80;  // 吸収シーケンス時間（4.0秒）

    private static final EntityDataAccessor<Integer> DATA_KING_STATE =
            SynchedEntityData.defineId(KingSlimeEntity.class, EntityDataSerializers.INT);

    private static final List<Supplier<EntityType<? extends Slime>>> MOD_SLIME_TYPES = List.of(
            () -> ModEntities.BLAZE_SLIME,
            () -> ModEntities.RABBIT_SLIME,
            () -> ModEntities.BOMB_SLIME,
            () -> ModEntities.WITCH_SLIME,
            () -> ModEntities.ENDER_SLIME,
            () -> ModEntities.PUFFERFISH_SLIME,
            () -> ModEntities.STONECUTTER_SLIME,
            () -> ModEntities.WITHER_SLIME
    );

    private final ServerBossEvent mainBossEvent = new ServerBossEvent(
            this.getDisplayName(),
            BossEvent.BossBarColor.GREEN,
            BossEvent.BossBarOverlay.PROGRESS
    );

    private final ServerBossEvent giantBossEvent = new ServerBossEvent(
            Component.translatable("entity.slimearisemod.king_slime.giant_armor"),
            BossEvent.BossBarColor.PURPLE,
            BossEvent.BossBarOverlay.NOTCHED_10
    );

    private float giantArmorHp = MAX_GIANT_HP;
    private boolean hasGiantOnce = false;
    private float normalDamageTaken = 0.0F;
    private int normalElapsedTicks = 0;
    private int summonCooldown = 120;

    private int rangedCounterCooldown = 0;
    private int giantJumpCounter = 0;
    private int leapChargeTicks = 0;
    private boolean isPerformingHighLeap = false;
    private int highLeapTicks = 0;
    private double leapTargetX = 0.0D;
    private double leapTargetY = 0.0D;
    private double leapTargetZ = 0.0D;
    private int contactDamageGraceTicks = 0;
    private boolean droppedCeilingJellyThisJump = false;

    private int absorbTimer = 0;
    private final List<UUID> absorbingSlimeIds = new ArrayList<>();
    private boolean wasOnGroundLastTick = true;

    public KingSlimeEntity(EntityType<? extends Slime> entityType, Level level) {
        super(entityType, level);
        this.giantBossEvent.setVisible(false);
        this.setSize(NORMAL_SIZE, true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, MAX_MAIN_HP)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.5D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_KING_STATE, STATE_NORMAL);
    }

    @Override
    public void setSize(int size, boolean resetHealth) {
        super.setSize(size, false);

        AttributeInstance maxHealthAttr = this.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealthAttr != null) {
            maxHealthAttr.setBaseValue(MAX_MAIN_HP);
        }

        // バニラのSize=2スライムと同等の移動速度(0.4)
        AttributeInstance moveSpeedAttr = this.getAttribute(Attributes.MOVEMENT_SPEED);
        if (moveSpeedAttr != null) {
            moveSpeedAttr.setBaseValue(0.2D + 0.1D * NORMAL_SIZE);
        }

        // バニラのSize=2スライムと同等の攻撃力(2.0)
        AttributeInstance attackDamageAttr = this.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attackDamageAttr != null) {
            attackDamageAttr.setBaseValue(size >= GIANT_SIZE ? 4.0D : 2.0D);
        }

        if (resetHealth) {
            this.setHealth(this.getMaxHealth());
        }
    }

    public int getKingState() {
        return this.entityData.get(DATA_KING_STATE);
    }

    public void setKingState(int state) {
        this.entityData.set(DATA_KING_STATE, state);
    }

    public boolean isGiant() {
        return this.getKingState() == STATE_GIANT;
    }

    public boolean isAbsorbing() {
        return this.getKingState() == STATE_ABSORBING;
    }

    public boolean isChargingHighLeap() {
        return this.leapChargeTicks > 0;
    }

    public boolean isBelowHalfHealth() {
        return this.getHealth() < this.getMaxHealth() * 0.5F;
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                        MobSpawnType spawnType, @Nullable SpawnGroupData spawnGroupData) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData);
        this.setSize(NORMAL_SIZE, true);
        this.setKingState(STATE_NORMAL);
        return data;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new KingSlimeFloatGoal(this));
        this.goalSelector.addGoal(2, new KingSlimeDirectionalGoal(this));
        this.goalSelector.addGoal(3, new KingSlimeRandomDirectionGoal(this));
        this.goalSelector.addGoal(5, new KingSlimeKeepOnJumpingGoal(this));
        // 巨大化による視線高さの変化で見失わないよう mustSee = false に設定
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, false));
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide && this.level() instanceof ServerLevel serverLevel && this.isAlive()) {
            if (this.rangedCounterCooldown > 0) {
                this.rangedCounterCooldown--;
            }
            if (this.contactDamageGraceTicks > 0) {
                this.contactDamageGraceTicks--;
            }

            int state = this.getKingState();

            if (state == STATE_NORMAL) {
                this.tickNormalState(serverLevel);
            } else if (state == STATE_ABSORBING) {
                this.tickAbsorbingState(serverLevel);
            } else if (state == STATE_GIANT) {
                this.tickGiantState(serverLevel);
            }

            if (this.tickCount % 10 == 0) {
                this.preventSubordinateFriendlyFire(serverLevel);
            }

            this.mainBossEvent.setProgress(this.getHealth() / this.getMaxHealth());
            this.giantBossEvent.setVisible(state == STATE_GIANT);
            if (state == STATE_GIANT) {
                this.giantBossEvent.setProgress(Mth.clamp(this.giantArmorHp / MAX_GIANT_HP, 0.0F, 1.0F));
            }
        }

        this.wasOnGroundLastTick = this.onGround();
    }

    private void preventSubordinateFriendlyFire(ServerLevel serverLevel) {
        List<Slime> nearbySlimes = serverLevel.getEntitiesOfClass(
                Slime.class,
                this.getBoundingBox().inflate(24.0D),
                s -> s.isAlive() && !(s instanceof KingSlimeEntity)
        );
        for (Slime slime : nearbySlimes) {
            if (slime.getTarget() instanceof Slime) {
                slime.setTarget(null);
            }
            if (slime.getLastHurtByMob() instanceof Slime) {
                slime.setLastHurtByMob(null);
            }
        }
    }

    private void tickNormalState(ServerLevel serverLevel) {
        this.normalElapsedTicks++;

        if (this.summonCooldown > 0) {
            this.summonCooldown--;
        } else {
            List<Slime> nearby = serverLevel.getEntitiesOfClass(
                    Slime.class,
                    this.getBoundingBox().inflate(16.0D),
                    s -> s.isAlive() && !s.isTiny() && !(s instanceof KingSlimeEntity)
            );
            if (nearby.size() < 12) {
                int count = 2 + this.random.nextInt(2);
                for (int i = 0; i < count; i++) {
                    double angle = this.random.nextDouble() * Math.PI * 2.0D;
                    double dist = 2.0D + this.random.nextDouble() * 1.5D;
                    spawnRatioSlime(
                            serverLevel,
                            this.getX() + Math.cos(angle) * dist,
                            this.getY() + 0.2D,
                            this.getZ() + Math.sin(angle) * dist,
                            2,
                            0.25F,
                            Math.cos(angle) * 0.2D,
                            0.25D,
                            Math.sin(angle) * 0.2D
                    );
                }
                this.playSound(SoundEvents.SLIME_SQUISH, 1.0F, 0.8F);
            }
            this.summonCooldown = 160 + this.random.nextInt(60);
        }

        boolean shouldTransform = false;
        if (!this.hasGiantOnce) {
            Player nearestPlayer = serverLevel.getNearestPlayer(this, 10.0D);
            if (nearestPlayer != null && !nearestPlayer.isCreative() && !nearestPlayer.isSpectator()) {
                shouldTransform = true;
            }
        } else {
            if (this.normalDamageTaken >= 20.0F || this.normalElapsedTicks >= 300) {
                shouldTransform = true;
            }
        }

        if (shouldTransform) {
            this.beginAbsorption(serverLevel);
        }
    }

    // 周囲のスライムを近い順に最大20匹まで集め、4匹未満なら召喚して吸収シーケンスを開始する
    private void beginAbsorption(ServerLevel serverLevel) {
        List<Slime> candidates = serverLevel.getEntitiesOfClass(
                Slime.class,
                this.getBoundingBox().inflate(18.0D),
                s -> s.isAlive() && !(s instanceof KingSlimeEntity)
        );
        candidates.sort(Comparator.comparingDouble(this::distanceToSqr));

        this.absorbingSlimeIds.clear();
        for (Slime slime : candidates) {
            if (this.absorbingSlimeIds.size() >= MAX_ABSORB_COUNT) {
                break;
            }
            this.absorbingSlimeIds.add(slime.getUUID());
        }

        int missing = MIN_ABSORB_COUNT - this.absorbingSlimeIds.size();
        for (int i = 0; i < missing; i++) {
            double angle = (Math.PI * 2.0D / Math.max(1, missing)) * i;
            Slime summoned = spawnRatioSlime(
                    serverLevel,
                    this.getX() + Math.cos(angle) * 3.5D,
                    this.getY() + 0.3D,
                    this.getZ() + Math.sin(angle) * 3.5D,
                    2,
                    0.25F,
                    0.0D,
                    0.3D,
                    0.0D
            );
            if (summoned != null) {
                this.absorbingSlimeIds.add(summoned.getUUID());
            }
        }

        this.absorbTimer = ABSORB_DURATION_TICKS;
        this.setKingState(STATE_ABSORBING);
        this.playSound(SoundEvents.EVOKER_PREPARE_SUMMON, 1.3F, 0.9F);
        this.playSound(SoundEvents.SLIME_SQUISH, 1.4F, 0.55F);
    }

    // 4.0秒かけて周囲からスライムとエネルギーを吸い込む予兆演出
    private void tickAbsorbingState(ServerLevel serverLevel) {
        this.absorbTimer--;
        float progress = 1.0F - ((float) this.absorbTimer / ABSORB_DURATION_TICKS);
        Vec3 center = this.position().add(0.0D, this.getBbHeight() * 0.65D, 0.0D);

        if (this.absorbTimer % 16 == 0) {
            this.playSound(SoundEvents.SLIME_SQUISH, 1.2F, 0.6F + progress * 0.7F);
        }

        for (int i = 0; i < 4; i++) {
            double angle = this.random.nextDouble() * Math.PI * 2.0D;
            double radius = (1.1D - progress * 0.7D) * 2.8D;
            double px = this.getX() + Math.cos(angle) * radius;
            double py = this.getY() + 0.2D + this.random.nextDouble() * 1.5D;
            double pz = this.getZ() + Math.sin(angle) * radius;
            serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, px, py, pz, 1, 0.0D, 0.05D, 0.0D, 0.0D);
        }

        double pullFactor = 0.045D + 0.20D * (progress * progress);

        for (UUID uuid : this.absorbingSlimeIds) {
            Entity entity = serverLevel.getEntity(uuid);
            if (entity instanceof Slime slime && slime.isAlive()) {
                slime.setNoAi(true);
                slime.setNoGravity(true);

                Vec3 diff = center.subtract(slime.position());
                double dist = diff.length();
                Vec3 pull = (dist > 0.6D) ? diff.scale(pullFactor) : diff.scale(0.15D);
                slime.setDeltaMovement(pull);
                slime.hurtMarked = true;

                if (this.absorbTimer % 2 == 0) {
                    serverLevel.sendParticles(
                            ParticleTypes.ITEM_SLIME,
                            slime.getX(),
                            slime.getY(0.5D),
                            slime.getZ(),
                            2,
                            0.12D,
                            0.12D,
                            0.12D,
                            0.04D
                    );
                }
            }
        }

        if (this.absorbTimer <= 0) {
            for (UUID uuid : this.absorbingSlimeIds) {
                Entity entity = serverLevel.getEntity(uuid);
                if (entity != null && entity.isAlive()) {
                    entity.discard();
                }
            }
            this.absorbingSlimeIds.clear();
            this.transformToGiant(serverLevel);
        }
    }

    private void transformToGiant(ServerLevel serverLevel) {
        this.setKingState(STATE_GIANT);
        this.setSize(GIANT_SIZE, false);
        this.giantArmorHp = MAX_GIANT_HP;
        this.hasGiantOnce = true;
        this.giantJumpCounter = 0;
        this.leapChargeTicks = 0;
        this.isPerformingHighLeap = false;
        this.highLeapTicks = 0;
        this.contactDamageGraceTicks = 0;
        this.droppedCeilingJellyThisJump = false;

        this.playSound(SoundEvents.WITHER_SPAWN, 0.8F, 1.2F);
        this.playSound(SoundEvents.SLIME_SQUISH, 1.5F, 0.5F);
        serverLevel.sendParticles(
                ParticleTypes.EXPLOSION,
                this.getX(),
                this.getY(0.5D),
                this.getZ(),
                6,
                1.2D,
                1.2D,
                1.2D,
                0.0D
        );
    }

    private void tickGiantState(ServerLevel serverLevel) {
        // 1. 大ジャンプ前の予備動作（1.0秒タメ）処理
        if (this.leapChargeTicks > 0) {
            this.leapChargeTicks--;
            Vec3 currentMotion = this.getDeltaMovement();
            this.setDeltaMovement(0.0D, Math.min(0.0D, currentMotion.y), 0.0D);

            // 足元にエネルギーが集まるチャージ演出
            double chargeRadius = this.getBbWidth() * 0.58D;
            for (int i = 0; i < 6; i++) {
                double angle = this.random.nextDouble() * Math.PI * 2.0D;
                double px = this.getX() + Math.cos(angle) * chargeRadius;
                double pz = this.getZ() + Math.sin(angle) * chargeRadius;
                serverLevel.sendParticles(ParticleTypes.ITEM_SLIME, px, this.getY() + 0.2D, pz, 1, 0.0D, 0.12D, 0.0D, 0.04D);
                if (i % 2 == 0) {
                    serverLevel.sendParticles(ParticleTypes.CLOUD, px, this.getY() + 0.15D, pz, 1, 0.0D, 0.05D, 0.0D, 0.01D);
                }
            }

            if (this.leapChargeTicks == 10) {
                this.playSound(SoundEvents.SLIME_SQUISH, 1.5F, 0.5F);
            }

            // タメ完了の瞬間にターゲット座標をロックオンして大ジャンプ発射
            if (this.leapChargeTicks <= 0) {
                this.executeHighLeap(serverLevel);
            }
            return;
        }

        // 2. 大ジャンプ中の空中軌道制御（跳躍瞬間のロックオン座標へ定点放物線移動＋着地予測リング表示）
        if (this.isPerformingHighLeap && !this.onGround()) {
            this.highLeapTicks++;
            Vec3 motion = this.getDeltaMovement();

            if (this.highLeapTicks <= 34) {
                double dx = this.leapTargetX - this.getX();
                double dz = this.leapTargetZ - this.getZ();
                double remDist = Math.sqrt(dx * dx + dz * dz);

                int remainingTravelTicks = Math.max(4, 36 - this.highLeapTicks);
                double desiredSpeed = Math.min(1.05D, remDist / remainingTravelTicks);
                double targetVx = (remDist > 0.05D) ? (dx / remDist) * desiredSpeed : 0.0D;
                double targetVz = (remDist > 0.05D) ? (dz / remDist) * desiredSpeed : 0.0D;

                double nextVx = Mth.lerp(0.32D, motion.x, targetVx);
                double nextVz = Mth.lerp(0.32D, motion.z, targetVz);
                // 頂点付近（14〜26tick）で一瞬フワッと滞空させ、合計約2.3秒の回避猶予を確保
                double nextVy = (this.highLeapTicks >= 14 && this.highLeapTicks <= 26)
                        ? motion.y + 0.024D
                        : motion.y;
                this.setDeltaMovement(nextVx, nextVy, nextVz);
            } else {
                // 目標真上を通過後は水平移動を抑え、真下へ急降下
                double diveVy = Math.min(motion.y - 0.05D, -0.45D);
                this.setDeltaMovement(motion.x * 0.65D, diveVy, motion.z * 0.65D);
            }

            // 着地予定地点の地面に危険エリアを示す円形リングパーティクルを表示
            this.spawnLandingWarningParticles(serverLevel);

            if (this.highLeapTicks % 2 == 0) {
                serverLevel.sendParticles(
                        ParticleTypes.ITEM_SLIME,
                        this.getX(),
                        this.getY() + 0.5D,
                        this.getZ(),
                        8,
                        this.getBbWidth() * 0.3D,
                        0.3D,
                        this.getBbWidth() * 0.3D,
                        0.05D
                );
            }

            if (this.highLeapTicks > 120 || this.isInWater() || this.isInLava()) {
                this.isPerformingHighLeap = false;
                this.highLeapTicks = 0;
            }
        }

        // 3. ジャンプ中に天井に頭をぶつけた場合、衝撃で剥がれたゼリーを真下に落として屋根ハメを防ぐ
        if (!this.onGround() && this.verticalCollision && this.getDeltaMovement().y <= 0.05D && !this.droppedCeilingJellyThisJump) {
            this.droppedCeilingJellyThisJump = true;
            if (this.isPerformingHighLeap) {
                this.leapTargetX = this.getX();
                this.leapTargetZ = this.getZ();
                this.leapTargetY = this.findGroundY(serverLevel, this.leapTargetX, this.getY(), this.leapTargetZ);
            }
            this.dropCeilingSlimeField(serverLevel);
        }

        // 4. 着地時の処理
        if (!this.wasOnGroundLastTick && this.onGround()) {
            List<Slime> underfootSlimes = serverLevel.getEntitiesOfClass(
                    Slime.class,
                    this.getBoundingBox().inflate(3.0D, 1.5D, 3.0D),
                    s -> s.isAlive() && !(s instanceof KingSlimeEntity)
            );
            for (Slime slime : underfootSlimes) {
                double dx = slime.getX() - this.getX();
                double dz = slime.getZ() - this.getZ();
                double dist = Math.sqrt(dx * dx + dz * dz);
                if (dist < 0.05D) {
                    double randAngle = this.random.nextDouble() * Math.PI * 2.0D;
                    dx = Math.cos(randAngle);
                    dz = Math.sin(randAngle);
                } else {
                    dx /= dist;
                    dz /= dist;
                }
                double knockStrength = 0.85D + this.random.nextDouble() * 0.35D;
                slime.setDeltaMovement(dx * knockStrength, 0.45D + this.random.nextDouble() * 0.15D, dz * knockStrength);
                slime.hurtMarked = true;
            }

            // 大ジャンプ落下からの着地時は円形衝撃波を発生させる
            if (this.isPerformingHighLeap) {
                this.performHighLeapLandingShockwave(serverLevel);
                this.isPerformingHighLeap = false;
                this.highLeapTicks = 0;
            }

            int deployedCount = 0;
            int maxPerLanding = 4;

            if (this.trySpawnFieldOnEmptySpace(serverLevel, this.getX(), this.getY(), this.getZ())) {
                deployedCount++;
            }

            int directions = 6;
            double baseAngle = this.random.nextDouble() * Math.PI * 2.0D;
            double footStepRadius = this.getBbWidth() * 0.48D;

            for (int i = 0; i < directions && deployedCount < maxPerLanding; i++) {
                double angle = baseAngle + (Math.PI * 2.0D / directions) * i;
                double fx = this.getX() + Math.cos(angle) * footStepRadius;
                double fz = this.getZ() + Math.sin(angle) * footStepRadius;
                double fy = this.findGroundY(serverLevel, fx, this.getY(), fz);

                if (this.trySpawnFieldOnEmptySpace(serverLevel, fx, fy, fz)) {
                    deployedCount++;
                }
            }
        }
    }

    private void executeHighLeap(ServerLevel serverLevel) {
        LivingEntity target = this.getTarget();
        if (target != null && target.isAlive()) {
            double rawDx = target.getX() - this.getX();
            double rawDz = target.getZ() - this.getZ();
            double rawDist = Math.sqrt(rawDx * rawDx + rawDz * rawDz);

            if (rawDist > MAX_HIGH_LEAP_RANGE) {
                double scale = MAX_HIGH_LEAP_RANGE / rawDist;
                this.leapTargetX = this.getX() + rawDx * scale;
                this.leapTargetZ = this.getZ() + rawDz * scale;
            } else {
                this.leapTargetX = target.getX();
                this.leapTargetZ = target.getZ();
            }
            this.leapTargetY = this.findGroundY(serverLevel, this.leapTargetX, target.getY(), this.leapTargetZ);
        } else {
            this.leapTargetX = this.getX();
            this.leapTargetZ = this.getZ();
            this.leapTargetY = this.getY();
        }

        double dx = this.leapTargetX - this.getX();
        double dz = this.leapTargetZ - this.getZ();
        double horizDist = Math.sqrt(dx * dx + dz * dz);

        double jumpVy = 1.65D; // 到達高度 約16〜18ブロック
        double initialSpeed = (horizDist > 0.05D) ? Math.min(1.05D, horizDist / 36.0D) : 0.0D;
        double vx = (horizDist > 0.05D) ? (dx / horizDist) * initialSpeed : 0.0D;
        double vz = (horizDist > 0.05D) ? (dz / horizDist) * initialSpeed : 0.0D;

        this.setDeltaMovement(vx, jumpVy, vz);
        this.hasImpulse = true;
        this.isPerformingHighLeap = true;
        this.highLeapTicks = 0;
        this.droppedCeilingJellyThisJump = false;

        this.playSound(SoundEvents.SLIME_JUMP, 1.9F, 0.45F);
        serverLevel.sendParticles(
                ParticleTypes.ITEM_SLIME,
                this.getX(),
                this.getY() + 0.3D,
                this.getZ(),
                32,
                this.getBbWidth() * 0.4D,
                0.25D,
                this.getBbWidth() * 0.4D,
                0.16D
        );
    }

    private void spawnLandingWarningParticles(ServerLevel serverLevel) {
        double shockRadius = this.getBbWidth() * 0.52D;
        int ringPoints = 18;
        double rotOffset = this.highLeapTicks * 0.12D;

        for (int i = 0; i < ringPoints; i++) {
            double angle = rotOffset + (Math.PI * 2.0D / ringPoints) * i;
            double px = this.leapTargetX + Math.cos(angle) * shockRadius;
            double pz = this.leapTargetZ + Math.sin(angle) * shockRadius;
            serverLevel.sendParticles(
                    ParticleTypes.ITEM_SLIME,
                    px,
                    this.leapTargetY + 0.12D,
                    pz,
                    1,
                    0.0D,
                    0.02D,
                    0.0D,
                    0.0D
            );
        }

        if (this.highLeapTicks % 2 == 0) {
            serverLevel.sendParticles(
                    ParticleTypes.COMPOSTER,
                    this.leapTargetX,
                    this.leapTargetY + 0.18D,
                    this.leapTargetZ,
                    3,
                    0.35D,
                    0.05D,
                    0.35D,
                    0.0D
            );
        }
    }

    private void dropCeilingSlimeField(ServerLevel serverLevel) {
        double groundY = this.findGroundY(serverLevel, this.getX(), this.getY(), this.getZ());
        this.spawnFieldDirect(serverLevel, this.getX(), groundY, this.getZ());

        if (this.isBelowHalfHealth() || this.isPerformingHighLeap) {
            double angle = this.random.nextDouble() * Math.PI * 2.0D;
            double offsetDist = 2.2D + this.random.nextDouble() * 1.2D;
            double sx = this.getX() + Math.cos(angle) * offsetDist;
            double sz = this.getZ() + Math.sin(angle) * offsetDist;
            double sy = this.findGroundY(serverLevel, sx, this.getY(), sz);
            this.trySpawnFieldOnEmptySpace(serverLevel, sx, sy, sz);
        }

        this.playSound(SoundEvents.SLIME_SQUISH, 1.5F, 0.65F);
        serverLevel.sendParticles(
                ParticleTypes.ITEM_SLIME,
                this.getX(),
                this.getY() + this.getBbHeight(),
                this.getZ(),
                18,
                this.getBbWidth() * 0.3D,
                0.2D,
                this.getBbWidth() * 0.3D,
                0.08D
        );
    }

    private void performHighLeapLandingShockwave(ServerLevel serverLevel) {
        this.playSound(SoundEvents.SLIME_SQUISH, 1.8F, 0.45F);
        this.playSound(SoundEvents.GENERIC_EXPLODE.value(), 0.75F, 1.35F);

        // 本体サイズとほぼ同じ半径の円形判定（予測リングと一致）
        double shockRadius = this.getBbWidth() * 0.52D;
        double shockRadiusSqr = shockRadius * shockRadius;

        // 着地時に巨体へめり込んで連続接触ダメージを受けるのを防ぐ猶予時間（0.6秒）
        this.contactDamageGraceTicks = 12;

        serverLevel.sendParticles(
                ParticleTypes.ITEM_SLIME,
                this.getX(),
                this.getY() + 0.25D,
                this.getZ(),
                40,
                shockRadius * 0.65D,
                0.2D,
                shockRadius * 0.65D,
                0.15D
        );
        serverLevel.sendParticles(
                ParticleTypes.POOF,
                this.getX(),
                this.getY() + 0.2D,
                this.getZ(),
                16,
                shockRadius * 0.55D,
                0.15D,
                shockRadius * 0.55D,
                0.06D
        );

        AABB searchBox = this.getBoundingBox().inflate(1.5D, 1.5D, 1.5D);
        List<LivingEntity> targets = serverLevel.getEntitiesOfClass(
                LivingEntity.class,
                searchBox,
                e -> e.isAlive() && e != this && !(e instanceof Slime)
        );

        for (LivingEntity target : targets) {
            if (target instanceof Player player && (player.isCreative() || player.isSpectator())) {
                continue;
            }
            if (Math.abs(target.getY() - this.getY()) > 2.5D) {
                continue;
            }

            double dx = target.getX() - this.getX();
            double dz = target.getZ() - this.getZ();
            double distSqr = dx * dx + dz * dz;
            if (distSqr > shockRadiusSqr) {
                continue;
            }

            double dist = Math.sqrt(distSqr);
            double pushDirX;
            double pushDirZ;
            if (dist < 0.05D) {
                double randAngle = this.random.nextDouble() * Math.PI * 2.0D;
                pushDirX = Math.cos(randAngle);
                pushDirZ = Math.sin(randAngle);
            } else {
                pushDirX = dx / dist;
                pushDirZ = dz / dist;
            }

            // 盾ガード判定が正しく働くようターゲット方向を向いてダメージを与える
            this.lookAt(target, 180.0F, 30.0F);
            target.hurt(this.damageSources().mobAttack(this), 6.0F);

            // 巨体の外周まで押し出して連続接触を防ぐ
            double pushStrength = target.isBlocking() ? 0.95D : 1.25D;
            target.setDeltaMovement(pushDirX * pushStrength, 0.38D, pushDirZ * pushStrength);
            target.hurtMarked = true;
        }
    }

    private boolean trySpawnFieldOnEmptySpace(ServerLevel serverLevel, double x, double y, double z) {
        double minSpacing = SlimeFieldEntity.FIELD_RADIUS * 1.85D;
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

        this.spawnFieldDirect(serverLevel, x, y, z);
        return true;
    }

    private void spawnFieldDirect(ServerLevel serverLevel, double x, double y, double z) {
        SlimeFieldEntity field = new SlimeFieldEntity(ModEntities.SLIME_FIELD, serverLevel);
        field.moveTo(x, y + 0.01D, z, 0.0F, 0.0F);
        field.setCasterKing(this);
        serverLevel.addFreshEntity(field);
    }

    private double findGroundY(ServerLevel serverLevel, double x, double startY, double z) {
        int bx = Mth.floor(x);
        int bz = Mth.floor(z);
        int topY = Mth.floor(startY) + 3;
        int bottomY = Mth.floor(startY) - 4;

        for (int by = topY; by >= bottomY; by--) {
            BlockPos pos = new BlockPos(bx, by, bz);
            if (!serverLevel.getBlockState(pos).getCollisionShape(serverLevel, pos).isEmpty()) {
                return by + 1.0D;
            }
        }
        return startY;
    }

    // 吸収中・大ジャンプタメ中・大ジャンプ着地直後は本体接触ダメージを発生させない
    @Override
    protected boolean isDealsDamage() {
        if (this.isAbsorbing() || this.isChargingHighLeap() || this.contactDamageGraceTicks > 0) {
            return false;
        }
        return super.isDealsDamage();
    }

    // 吸収シーケンス中の完全無敵 ＆ 壁埋まりによる窒息ダメージ無効化
    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        if (source.is(DamageTypes.IN_WALL)) {
            return true;
        }
        if (this.isAbsorbing() && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return true;
        }
        return super.isInvulnerableTo(source);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.isInvulnerableTo(source)) {
            return false;
        }
        if (source.getEntity() instanceof Slime || source.getDirectEntity() instanceof Slime) {
            return false;
        }
        if (source.is(DamageTypeTags.IS_EXPLOSION) && (source.getEntity() == null || source.getEntity() instanceof Slime)) {
            return false;
        }
        return super.hurt(source, amount);
    }

    @Override
    public boolean canBeAffected(MobEffectInstance effect) {
        if (effect.getEffect().value().getCategory() == MobEffectCategory.HARMFUL) {
            return false;
        }
        return super.canBeAffected(effect);
    }

    @Override
    protected void actuallyHurt(DamageSource damageSource, float damageAmount) {
        if (this.isInvulnerableTo(damageSource)) {
            return;
        }

        boolean isSolventAttack = damageSource.getDirectEntity() instanceof SlimeSolventProjectileEntity;
        boolean isProjectileAttack = !isSolventAttack && (damageSource.is(DamageTypeTags.IS_PROJECTILE)
                || damageSource.getDirectEntity() instanceof Projectile);

        // 巨大形態（紫ゲージ展開中）は分厚いゼリー装甲により飛翔体ダメージを40%軽減する（粘液溶解液は軽減しない）
        float effectiveDamage = damageAmount;
        if (this.isGiant() && isProjectileAttack) {
            effectiveDamage *= GIANT_PROJECTILE_DAMAGE_MULTIPLIER;
        }

        // 遠距離攻撃（飛翔体または8ブロック以上離れた位置からの攻撃）に対するスライムフィールド展開カウンター（50%確率）
        if (effectiveDamage > 0.0F
                && this.rangedCounterCooldown <= 0
                && this.level() instanceof ServerLevel serverLevel
                && damageSource.getEntity() instanceof LivingEntity attacker
                && !(attacker instanceof Slime)) {
            boolean isFarAttack = this.distanceToSqr(attacker) >= RANGED_COUNTER_MIN_DIST_SQR;
            if ((isProjectileAttack || isFarAttack) && this.random.nextFloat() < RANGED_COUNTER_CHANCE) {
                this.triggerRangedCounter(serverLevel, attacker);
            }
        }

        if (this.isGiant()) {
            this.giantArmorHp -= effectiveDamage;
            if (this.giantArmorHp <= 0.0F) {
                this.giantArmorHp = 0.0F;
                this.revertFromGiant(isSolventAttack);
            }
            return;
        }

        this.normalDamageTaken += effectiveDamage;
        super.actuallyHurt(damageSource, effectiveDamage);
    }

    private void triggerRangedCounter(ServerLevel serverLevel, LivingEntity attacker) {
        this.rangedCounterCooldown = RANGED_COUNTER_COOLDOWN_TICKS;

        double ax = attacker.getX();
        double az = attacker.getZ();
        double ay = this.findGroundY(serverLevel, ax, attacker.getY(), az);
        this.spawnFieldDirect(serverLevel, ax, ay, az);

        // HP半分以下では攻撃者のすぐ近くにもう1つ追加展開する
        if (this.isBelowHalfHealth()) {
            double angle = this.random.nextDouble() * Math.PI * 2.0D;
            double dist = 2.4D + this.random.nextDouble() * 1.0D;
            double sx = ax + Math.cos(angle) * dist;
            double sz = az + Math.sin(angle) * dist;
            double sy = this.findGroundY(serverLevel, sx, attacker.getY(), sz);
            this.trySpawnFieldOnEmptySpace(serverLevel, sx, sy, sz);
        }

        this.playSound(SoundEvents.SLIME_SQUISH, 1.2F, 1.15F);
        serverLevel.sendParticles(
                ParticleTypes.ITEM_SLIME,
                ax,
                ay + 0.15D,
                az,
                16,
                0.7D,
                0.15D,
                0.7D,
                0.08D
        );
    }

    private void revertFromGiant(boolean dissolvedBySolvent) {
        if (this.level() instanceof ServerLevel serverLevel) {
            // 粘液溶解液で巨大アーマーを削り切った場合はばら撒くスライム数を大幅に減らす（通常12〜16匹 → 6〜8匹）
            int spawnCount = dissolvedBySolvent
                    ? (6 + this.random.nextInt(3))
                    : (12 + this.random.nextInt(5));
            float modSlimeChance = (this.getHealth() >= this.getMaxHealth() * 0.5F) ? 0.25F : 0.75F;

            for (int i = 0; i < spawnCount; i++) {
                double angle = (Math.PI * 2.0D / spawnCount) * i + (this.random.nextDouble() - 0.5D) * 0.3D;
                double speed = 0.35D + this.random.nextDouble() * 0.25D;

                spawnRatioSlime(
                        serverLevel,
                        this.getX() + Math.cos(angle) * 3.0D,
                        this.getY() + 0.5D,
                        this.getZ() + Math.sin(angle) * 3.0D,
                        2,
                        modSlimeChance,
                        Math.cos(angle) * speed,
                        0.35D + this.random.nextDouble() * 0.2D,
                        Math.sin(angle) * speed
                );
            }

            serverLevel.sendParticles(
                    ParticleTypes.EXPLOSION_EMITTER,
                    this.getX(),
                    this.getY(0.5D),
                    this.getZ(),
                    1,
                    0.0D,
                    0.0D,
                    0.0D,
                    0.0D
            );

            if (dissolvedBySolvent) {
                this.playSound(SoundEvents.FIRE_EXTINGUISH, 1.0F, 1.35F);
                serverLevel.sendParticles(
                        ParticleTypes.BUBBLE_POP,
                        this.getX(),
                        this.getY(0.5D),
                        this.getZ(),
                        35,
                        2.5D,
                        1.5D,
                        2.5D,
                        0.08D
                );
                serverLevel.sendParticles(
                        ParticleTypes.CLOUD,
                        this.getX(),
                        this.getY(0.5D),
                        this.getZ(),
                        20,
                        2.0D,
                        1.0D,
                        2.0D,
                        0.05D
                );
            }
        }

        this.setKingState(STATE_NORMAL);
        this.setSize(NORMAL_SIZE, false);
        this.normalDamageTaken = 0.0F;
        this.normalElapsedTicks = 0;
        this.leapChargeTicks = 0;
        this.isPerformingHighLeap = false;
        this.highLeapTicks = 0;
        this.contactDamageGraceTicks = 0;
        this.droppedCeilingJellyThisJump = false;
        this.playSound(SoundEvents.SLIME_SQUISH, 1.5F, 1.1F);
    }

    @Nullable
    public static Slime spawnRatioSlime(ServerLevel level, double x, double y, double z,
                                        int size, float modSlimeChance,
                                        double vx, double vy, double vz) {
        RandomSource random = level.getRandom();
        EntityType<? extends Slime> chosenType;

        if (random.nextFloat() < modSlimeChance) {
            Supplier<EntityType<? extends Slime>> supplier =
                    MOD_SLIME_TYPES.get(random.nextInt(MOD_SLIME_TYPES.size()));
            chosenType = supplier.get();
        } else {
            chosenType = EntityType.SLIME;
        }

        Slime slime = chosenType.create(level);
        if (slime != null) {
            slime.setSize(size, true);
            slime.moveTo(x, y, z, random.nextFloat() * 360.0F, 0.0F);
            slime.setDeltaMovement(vx, vy, vz);
            level.addFreshEntity(slime);
        }
        return slime;
    }

    // 通常形態ではバニラと同じジャンプ力、巨大形態では3回に1回予備動作（1.0秒タメ）を挟んで大ジャンプを行う
    @Override
    public void jumpFromGround() {
        if (!this.isGiant()) {
            super.jumpFromGround();
            return;
        }

        if (this.leapChargeTicks > 0) {
            return;
        }

        this.droppedCeilingJellyThisJump = false;
        this.giantJumpCounter++;

        LivingEntity target = this.getTarget();
        if (target != null && target.isAlive() && this.giantJumpCounter % HIGH_LEAP_INTERVAL == 0) {
            this.leapChargeTicks = HIGH_LEAP_CHARGE_DURATION;
            this.playSound(SoundEvents.SLIME_SQUISH, 1.4F, 0.6F);
            this.playSound(SoundEvents.PISTON_CONTRACT, 1.0F, 0.85F);
            return;
        }

        this.isPerformingHighLeap = false;
        this.highLeapTicks = 0;
        Vec3 motion = this.getDeltaMovement();
        this.setDeltaMovement(motion.x, (double) (this.getJumpPower() * 1.45F), motion.z);
        this.hasImpulse = true;
    }

    @Override
    public void remove(Entity.RemovalReason reason) {
        if (this.isDeadOrDying()) {
            this.setSize(1, false);
        }
        super.remove(reason);
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    public void setCustomName(@Nullable Component name) {
        super.setCustomName(name);
        this.mainBossEvent.setName(this.getDisplayName());
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        this.mainBossEvent.addPlayer(player);
        this.giantBossEvent.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        this.mainBossEvent.removePlayer(player);
        this.giantBossEvent.removePlayer(player);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("KingState", this.getKingState());
        tag.putFloat("GiantArmorHp", this.giantArmorHp);
        tag.putBoolean("HasGiantOnce", this.hasGiantOnce);
        tag.putFloat("NormalDamageTaken", this.normalDamageTaken);
        tag.putInt("NormalElapsedTicks", this.normalElapsedTicks);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (this.hasCustomName()) {
            this.mainBossEvent.setName(this.getDisplayName());
        }
        if (tag.contains("KingState")) {
            this.setKingState(tag.getInt("KingState"));
        }
        if (tag.contains("GiantArmorHp")) {
            this.giantArmorHp = tag.getFloat("GiantArmorHp");
        }
        if (tag.contains("HasGiantOnce")) {
            this.hasGiantOnce = tag.getBoolean("HasGiantOnce");
        }
        if (tag.contains("NormalDamageTaken")) {
            this.normalDamageTaken = tag.getFloat("NormalDamageTaken");
        }
        if (tag.contains("NormalElapsedTicks")) {
            this.normalElapsedTicks = tag.getInt("NormalElapsedTicks");
        }
    }

    // --- KingSlime専用 AI Goal 群 ---

    private static class KingSlimeFloatGoal extends Goal {
        private final KingSlimeEntity slime;

        public KingSlimeFloatGoal(KingSlimeEntity slime) {
            this.slime = slime;
            this.setFlags(EnumSet.of(Flag.JUMP, Flag.MOVE));
            slime.getNavigation().setCanFloat(true);
        }

        @Override
        public boolean canUse() {
            return (this.slime.isInWater() || this.slime.isInLava()) && this.slime.getMoveControl() instanceof SlimeMoveControl;
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
            if (this.slime.getMoveControl() instanceof SlimeMoveControl moveControl) {
                moveControl.setWantedMovement(1.2D);
            }
        }
    }

    private static class KingSlimeDirectionalGoal extends Goal {
        private final KingSlimeEntity slime;
        private int growTiredTimer;

        public KingSlimeDirectionalGoal(KingSlimeEntity slime) {
            this.slime = slime;
            this.setFlags(EnumSet.of(Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (this.slime.isAbsorbing()) {
                return false;
            }
            LivingEntity target = this.slime.getTarget();
            if (target == null || !target.isAlive()) {
                return false;
            }
            return this.slime.canAttack(target) && this.slime.getMoveControl() instanceof SlimeMoveControl;
        }

        @Override
        public void start() {
            this.growTiredTimer = reducedTickDelay(300);
            super.start();
        }

        @Override
        public boolean canContinueToUse() {
            if (this.slime.isAbsorbing()) {
                return false;
            }
            LivingEntity target = this.slime.getTarget();
            if (target == null || !target.isAlive()) {
                return false;
            }
            return this.slime.canAttack(target) && --this.growTiredTimer > 0;
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            LivingEntity target = this.slime.getTarget();
            if (target != null && this.slime.getMoveControl() instanceof SlimeMoveControl moveControl) {
                if (this.slime.isGiant()) {
                    this.slime.lookAt(target, 30.0F, 30.0F);
                    moveControl.setDirection(this.slime.getYRot(), !this.slime.isChargingHighLeap());
                } else {
                    double dx = this.slime.getX() - target.getX();
                    double dz = this.slime.getZ() - target.getZ();
                    float fleeYaw = (float) (Mth.atan2(dz, dx) * (180.0D / Math.PI)) - 90.0F;
                    this.slime.setYRot(fleeYaw);
                    this.slime.yHeadRot = fleeYaw;
                    this.slime.yBodyRot = fleeYaw;
                    moveControl.setDirection(fleeYaw, false);
                }
            }
        }
    }

    private static class KingSlimeRandomDirectionGoal extends Goal {
        private final KingSlimeEntity slime;
        private float chosenDegrees;
        private int nextRandomizeTime;

        public KingSlimeRandomDirectionGoal(KingSlimeEntity slime) {
            this.slime = slime;
            this.setFlags(EnumSet.of(Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            return !this.slime.isAbsorbing()
                    && !this.slime.isChargingHighLeap()
                    && this.slime.getTarget() == null
                    && (this.slime.onGround() || this.slime.isInWater() || this.slime.isInLava() || this.slime.hasEffect(MobEffects.LEVITATION))
                    && this.slime.getMoveControl() instanceof SlimeMoveControl;
        }

        @Override
        public void tick() {
            if (--this.nextRandomizeTime <= 0) {
                this.nextRandomizeTime = this.adjustedTickDelay(40 + this.slime.getRandom().nextInt(60));
                this.chosenDegrees = (float) this.slime.getRandom().nextInt(360);
            }
            if (this.slime.getMoveControl() instanceof SlimeMoveControl moveControl) {
                moveControl.setDirection(this.chosenDegrees, false);
            }
        }
    }

    private static class KingSlimeKeepOnJumpingGoal extends Goal {
        private final KingSlimeEntity slime;

        public KingSlimeKeepOnJumpingGoal(KingSlimeEntity slime) {
            this.slime = slime;
            this.setFlags(EnumSet.of(Flag.JUMP, Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            return !this.slime.isAbsorbing() && !this.slime.isChargingHighLeap() && !this.slime.isPassenger();
        }

        @Override
        public void tick() {
            if (this.slime.getMoveControl() instanceof SlimeMoveControl moveControl) {
                moveControl.setWantedMovement(1.0D);
            }
        }
    }

    // プレイヤーから離れても自然デスポーンしないようにする
    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    // 時間経過（noActionTime）によるデスポーン判定を無効化する
    @Override
    public boolean requiresCustomPersistence() {
        return true;
    }
}