package com.bathcrimsonthin.slimearisemod.entity;

import com.bathcrimsonthin.slimearisemod.particle.ModParticles;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;

public class BlazeSlimeEntity extends Slime {

    // サーバー・クライアント間でチャージ状態を同期するアクセサリー
    private static final EntityDataAccessor<Boolean> ID_CHARGED =
            SynchedEntityData.defineId(BlazeSlimeEntity.class, EntityDataSerializers.BOOLEAN);

    // 火球攻撃のクールダウンタイマー (tick単位)
    private int fireballCooldown = 0;

    public BlazeSlimeEntity(EntityType<? extends Slime> entityType, Level level) {
        super(entityType, level);
        // AIがマグマや炎を障害物として回避しないように経路探索AIへ設定
        this.setPathfindingMalus(PathType.LAVA, 0.0F);
        this.setPathfindingMalus(PathType.DAMAGE_FIRE, 0.0F);
        this.setPathfindingMalus(PathType.DANGER_FIRE, 0.0F);
    }

    public static AttributeSupplier.Builder createAttributes(){
        return Monster.createMonsterAttributes();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        // 初期値falseでチャージ状態を初期化
        builder.define(ID_CHARGED, false);
    }

    public boolean isCharged(){
        return this.entityData.get(ID_CHARGED);
    }

    public void setCharged(boolean charged){
        this.entityData.set(ID_CHARGED, charged);
    }

    @Override
    public void tick() {
        super.tick();

        // サーバー側でクールダウンのカウントダウン
        if (!this.level().isClientSide() && this.fireballCooldown > 0) {
            this.fireballCooldown--;
        }

        // クライアント側勝チャージ中のみ
        if (this.level().isClientSide && this.isCharged()) {
            // ポーズに応じた幅と高さを取得
            EntityDimensions dimensions = this.getDimensions(this.getPose());
            double width = dimensions.width();
            double height = dimensions.height();

            // 内部に炎・煙パーティクルを発生
            for (int i = 0; i < 6; i++) {
                double px = this.getX() + (this.random.nextDouble() - 0.5D) * width * 1.2D;
                double py = this.getY() + this.random.nextDouble() * height * 1.2D;
                double pz = this.getZ() + (this.random.nextDouble() - 0.5D) * width * 1.2D;

                // 炎と煙を混ぜ合わせて予備動作を強調
                this.level().addParticle(ParticleTypes.FLAME, px, py, pz, 0.0D, 0.04D, 0.0D);
                if (this.random.nextInt(3) == 0) {
                    this.level().addParticle(ParticleTypes.SMOKE, px, py, pz, 0.0D, 0.02D, 0.0D);
                }
            }
        }
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        // 優先度 1 で火球 Goal を追加 (LOOK フラグのみのため跳躍 Goal と並行して動作する)
        this.goalSelector.addGoal(1, new BlazeSlimeFireballGoal(this));
    }

    // --- ブレイズと同等仕様の火球攻撃 Goal ---
    static class BlazeSlimeFireballGoal extends Goal {
        private final BlazeSlimeEntity slime;
        private int attackStep;    // 発射した火球の段階 (1〜3)
        private int attackTime;    // タイマー (チャージ・連射間隔・クールダウンを制御)
        private int lastSeen;

        public BlazeSlimeFireballGoal(BlazeSlimeEntity slime) {
            this.slime = slime;
            // LOOK のみを指定し SlimeKeepOnJumpingGoal(JUMP, MOVE) と同時に動作させる
            this.setFlags(EnumSet.of(Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = this.slime.getTarget();
            return target != null && target.isAlive() && this.slime.canAttack(target) && !this.slime.isTiny();
        }

        @Override
        public void start() {
            this.attackStep = 0;
            this.attackTime = 0;
            this.lastSeen = 0;
        }

        @Override
        public void stop() {
            this.slime.setCharged(false);
            this.lastSeen = 0;
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            --this.attackTime;
            LivingEntity target = this.slime.getTarget();
            if (target == null) return;

            boolean hasLineOfSight = this.slime.getSensing().hasLineOfSight(target);
            if (hasLineOfSight) {
                this.lastSeen = 0;
            } else {
                ++this.lastSeen;
            }

            double distanceSq = this.slime.distanceToSqr(target);

            // ターゲットを注視（移動しつつ向きを合わせる）
            this.slime.getLookControl().setLookAt(target, 30.0F, 30.0F);

            // 射程範囲内 (16ブロック以内) かつ 視線が通っている場合
            if (distanceSq < 256.0D && this.lastSeen < 40) {

                // タイマーが0になったタイミングで処理を開始
                if (this.attackTime <= 0) {
                    ++this.attackStep;

                    if (this.attackStep == 1) {
                        // 段階1: チャージ開始 (バニラブレイズ基準: 60 ticks = 3秒間)
                        this.attackTime = 60;
                        this.slime.setCharged(true);
                    } else if (this.attackStep <= 4) {
                        // 段階2〜4: 火球発射 (6 ticks = 0.3秒間隔で計3発)
                        this.attackTime = 6;
                    } else {
                        // 段階5: 発射完了、クールダウンへ (バニラブレイズ基準: 100 ticks = 5秒間)
                        this.attackTime = 100;
                        this.attackStep = 0;
                        this.slime.setCharged(false);
                    }

                    // 火球を生成して発射（第2〜4段階でそれぞれ1発ずつ、計3発）
                    if (this.attackStep > 1) {
                        if (!this.slime.level().isClientSide()) {
                            EntityDimensions dimensions = this.slime.getDimensions(this.slime.getPose());
                            double width = dimensions.width();
                            double height = dimensions.height();

                            // 1. 口元（体の前面・高さ中央）の座標を計算
                            Vec3 look = this.slime.getLookAngle();
                            double mouthX = this.slime.getX() + look.x * (width * 0.5D + 0.1D);
                            double mouthY = this.slime.getY() + height * 0.5D; // 口元の高さ
                            double mouthZ = this.slime.getZ() + look.z * (width * 0.5D + 0.1D);

                            // 2. ターゲットの中心へ向けたベクトルを計算 (バニラブレイズの弾速・精度計算)
                            double dx = target.getX() - mouthX;
                            double dy = target.getY(0.5D) - mouthY;
                            double dz = target.getZ() - mouthZ;

                            // 3. 火球の生成と射出
                            SmallFireball fireball = new SmallFireball(
                                    this.slime.level(),
                                    this.slime,
                                    new Vec3(dx, dy, dz)
                            );
                            fireball.setPos(mouthX, mouthY, mouthZ);

                            this.slime.level().addFreshEntity(fireball);
                            this.slime.playSound(SoundEvents.BLAZE_SHOOT, 1.0F, 1.0F);
                        }
                    }
                }
            } else if (this.attackStep > 0) {
                // ターゲットを見失った場合はチャージを解除
                this.attackStep = 0;
                this.slime.setCharged(false);
            }
        }
    }

    @Override
    public boolean isSensitiveToWater() {
        return true; // 水や雨に触れた際にダメージを受ける
    }

    @Override
    public void setSize(int size, boolean resetHealth) {
        super.setSize(size, resetHealth);
        // ヒットボックスの寸法を即座に最新の size に合わせて再計算する
        this.refreshDimensions();
    }

    @Override
    protected @NotNull ParticleOptions getParticleType() {
        return ModParticles.BLAZE_SLIME_PARTICLE;
    }
}
