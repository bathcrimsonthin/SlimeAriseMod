package com.bathcrimsonthin.slimearisemod.entity;

import com.bathcrimsonthin.slimearisemod.item.ModItems;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;

public class BombSlimeEntity extends Slime {
    private static final EntityDataAccessor<Integer> DATA_SWELL_DIR =
            SynchedEntityData.defineId(BombSlimeEntity.class, EntityDataSerializers.INT);

    private int oldSwell;
    private int swell;
    private final int maxSwell = 30; // 1.5秒で爆発

    public BombSlimeEntity(EntityType<? extends Slime> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes(){
        return Monster.createMonsterAttributes();
    }

    @Override
    protected @NotNull ParticleOptions getParticleType() {
        // ダミーアイテムの見た目を持ったアイテム破砕パーティクルを生成
        return new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(ModItems.DUMMY_BOMB_SLIME_BALL));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_SWELL_DIR, -1);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        // 接近時にカウントダウンを開始するAIを追加
        this.goalSelector.addGoal(2, new BombSlimeSwellGoal(this));
    }

    @Override
    public void tick() {
        if (this.isAlive()) {
            this.oldSwell = this.swell;
            int dir = this.getSwellDir();

            if (dir > 0 && this.swell == 0) {
                this.playSound(SoundEvents.CREEPER_PRIMED, 1.0F, 0.5F);
            }

            this.swell += dir;

            if (this.swell < 0) {
                this.swell = 0;
            }

            if (this.swell >= this.maxSwell) {
                this.swell = this.maxSwell;
                this.explode();
            }
        }
        super.tick();
    }

    // 爆発処理：サイズに応じた爆発力 (小: 1.0F, 中: 2.0F, 大: 4.0F)
    private void explode() {
        if (!this.level().isClientSide) {
            float power = (float) this.getSize();
            this.level().explode(this, this.getX(), this.getY(), this.getZ(), power, Level.ExplosionInteraction.MOB);
            this.discard();
        }
    }

    public int getSwellDir() {
        return this.entityData.get(DATA_SWELL_DIR);
    }

    public void setSwellDir(int dir) {
        this.entityData.set(DATA_SWELL_DIR, dir);
    }

    // レンダラーの白色点滅アニメーション補間計算
    public float getSwelling(float partialTicks) {
        return Mth.lerp(partialTicks, (float) this.oldSwell, (float) this.swell) / (float) (this.maxSwell - 2);
    }

    // --- 近接爆発用 AI Goal ---
    static class BombSlimeSwellGoal extends Goal {
        private final BombSlimeEntity slime;
        private LivingEntity target;

        public BombSlimeSwellGoal(BombSlimeEntity slime) {
            this.slime = slime;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            LivingEntity livingEntity = this.slime.getTarget();
            return this.slime.getSwellDir() > 0 || (livingEntity != null && this.slime.distanceToSqr(livingEntity) < 9.0D);
        }

        @Override
        public void start() {
            this.slime.getNavigation().stop();
            this.target = this.slime.getTarget();
        }

        @Override
        public void stop() {
            this.target = null;
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            if (this.target == null) {
                this.slime.setSwellDir(-1);
            } else if (this.slime.distanceToSqr(this.target) > 49.0D) {
                this.slime.setSwellDir(-1);
            } else if (!this.slime.getSensing().hasLineOfSight(this.target)) {
                this.slime.setSwellDir(-1);
            } else {
                this.slime.setSwellDir(1);
            }
        }
    }
}