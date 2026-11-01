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

    // --- 火球攻撃 & 追尾 Goal ---
    static class BlazeSlimeFireballGoal extends Goal {
        private final BlazeSlimeEntity slime;
        private int attackStep;
        private int attackTime;
        private int lastSeen;

        public BlazeSlimeFireballGoal(BlazeSlimeEntity slime) {
            this.slime = slime;
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

            // 1. スライム本体の向きをターゲットへ向ける
            this.slime.lookAt(target, 10.0F, 10.0F);

            // 2. SlimeMoveControl へターゲットの方向と追尾（攻撃的跳躍）フラグを渡す
            if (this.slime.getMoveControl() instanceof Slime.SlimeMoveControl slimeMoveControl) {
                slimeMoveControl.setDirection(this.slime.getYRot(), this.slime.isDealsDamage());
            }

            double distanceSq = this.slime.distanceToSqr(target);

            if (distanceSq < 256.0D && this.lastSeen < 40) {
                if (this.attackTime <= 0) {
                    ++this.attackStep;

                    if (this.attackStep == 1) {
                        this.attackTime = 60;
                        this.slime.setCharged(true);
                    } else if (this.attackStep <= 4) {
                        this.attackTime = 6;
                    } else {
                        this.attackTime = 100;
                        this.attackStep = 0;
                        this.slime.setCharged(false);
                    }

                    if (this.attackStep > 1) {
                        if (!this.slime.level().isClientSide()) {
                            EntityDimensions dimensions = this.slime.getDimensions(this.slime.getPose());
                            double width = dimensions.width();
                            double height = dimensions.height();

                            Vec3 look = this.slime.getLookAngle();
                            double mouthX = this.slime.getX() + look.x * (width * 0.5D + 0.1D);
                            double mouthY = this.slime.getY() + height * 0.5D;
                            double mouthZ = this.slime.getZ() + look.z * (width * 0.5D + 0.1D);

                            double dx = target.getX() - mouthX;
                            double dy = target.getY(0.5D) - mouthY;
                            double dz = target.getZ() - mouthZ;

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
