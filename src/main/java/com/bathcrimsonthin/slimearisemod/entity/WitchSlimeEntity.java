package com.bathcrimsonthin.slimearisemod.entity;

import com.bathcrimsonthin.slimearisemod.item.ModItems;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.List;

public class WitchSlimeEntity extends Slime {

    public WitchSlimeEntity(EntityType<? extends Slime> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes();
    }

    // スライムの跳躍時・破砕時に WitchSlimeBall の見た目の粒子を使用
    @Override
    protected ParticleOptions getParticleType() {
        return new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(ModItems.WITCH_SLIME_BALL));
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(3, new WitchSlimePotionGoal(this));
    }

    @Override
    public void setSize(int size, boolean resetHealth) {
        super.setSize(size, resetHealth);
        double doubleHealth = (double) (size * size * 2);
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(doubleHealth);
        if (resetHealth) {
            this.setHealth((float) doubleHealth);
        }
    }

    // HARMFUL（デバフ）ポーション効果を無効化
    @Override
    public boolean canBeAffected(MobEffectInstance effect) {
        if (effect.getEffect().value().getCategory() == MobEffectCategory.HARMFUL) {
            return false;
        }
        return super.canBeAffected(effect);
    }

    // ウィッチと同等の魔法・耐性ダメージ判定 (85%軽減)
    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.is(DamageTypeTags.WITCH_RESISTANT_TO)) {
            amount *= 0.15F;
        }
        return super.hurt(source, amount);
    }

    // 向いている前方に少しずらして残留ポーション雲を生成
    public void spawnLingeringCloud(Holder<MobEffect> effect) {
        // 最小サイズの場合はポーション雲を出さない
        if (this.isTiny()) {
            return;
        }

        Vec3 look = this.getLookAngle();
        // スライムのサイズに応じて前方向へのずらし距離を計算 (中: 2.2m, 大: 3.2m 前方)
        double forwardOffset = 1.2D + (0.5D * (double) this.getSize());

        double spawnX = this.getX() + look.x * forwardOffset;
        double spawnY = this.getY();
        double spawnZ = this.getZ() + look.z * forwardOffset;

        AreaEffectCloud cloud = new AreaEffectCloud(this.level(), spawnX, spawnY, spawnZ);
        cloud.setOwner(this);

        cloud.setRadius(3.0F);
        cloud.setDuration(80); // 雲の消滅時間 (4.0秒)
        cloud.setRadiusPerTick(-cloud.getRadius() / (float) cloud.getDuration());

        // 付与エフェクトの持続時間 (150 ticks = 7.5秒間)
        cloud.addEffect(new MobEffectInstance(effect, 150, 0));

        this.level().addFreshEntity(cloud);
    }

    static class WitchSlimePotionGoal extends Goal {
        private final WitchSlimeEntity slime;
        private int cooldown = 0;

        public WitchSlimePotionGoal(WitchSlimeEntity slime) {
            this.slime = slime;
            this.setFlags(EnumSet.noneOf(Goal.Flag.class));
        }

        @Override
        public boolean canUse() {
            // 最小サイズ (getSize() <= 1) の場合は Goal 自体を起動させない
            if (this.slime.isTiny()) {
                return false;
            }

            if (this.cooldown > 0) {
                this.cooldown--;
                return false;
            }
            return this.slime.isAlive() && (
                    this.slime.getTarget() != null ||
                            this.slime.getHealth() < this.slime.getMaxHealth() * 0.5F ||
                            this.slime.isOnFire() ||
                            this.slime.isInWater()
            );
        }

        @Override
        public void start() {
            Holder<MobEffect> selectedEffect = evaluateSituation();
            if (selectedEffect != null) {
                this.slime.spawnLingeringCloud(selectedEffect);
            }
            // 発生までのインターバル (約 1.5 ~ 2.5 秒)
            this.cooldown = 30 + this.slime.getRandom().nextInt(20);
        }

        private Holder<MobEffect> evaluateSituation() {
            // 1. 環境緊急回避 (最優先)
            if (this.slime.isInWater()) {
                return MobEffects.WATER_BREATHING;
            }
            if (this.slime.isOnFire()) {
                return MobEffects.FIRE_RESISTANCE;
            }

            LivingEntity target = this.slime.getTarget();

            // 2. ターゲットとの戦闘時
            if (target != null && target.isAlive()) {
                double distanceSq = this.slime.distanceToSqr(target);
                // 前方に生成される雲がヒットする範囲 (約7.0ブロック以内 = 49.0D)
                double effectiveCloudRangeSq = 49.0D;

                if (distanceSq <= effectiveCloudRangeSq) {
                    // 【中・近距離】前方の雲が届くため、デバフを中心に放出

                    // HP 50% 未満時は 30% の確率で回復・ピンチバフを織り交ぜる
                    if (this.slime.getHealth() < this.slime.getMaxHealth() * 0.5F && this.slime.getRandom().nextFloat() < 0.3F) {
                        List<Holder<MobEffect>> lowHpBuffs = List.of(
                                MobEffects.REGENERATION,
                                MobEffects.OOZING
                        );
                        return lowHpBuffs.get(this.slime.getRandom().nextInt(lowHpBuffs.size()));
                    }

                    // 80% でデバフ攻撃、20% で自己バフ
                    if (this.slime.getRandom().nextFloat() < 0.8F) {
                        List<Holder<MobEffect>> debuffs = List.of(
                                MobEffects.HARM,
                                MobEffects.POISON,
                                MobEffects.MOVEMENT_SLOWDOWN,
                                MobEffects.WEAKNESS
                        );
                        return debuffs.get(this.slime.getRandom().nextInt(debuffs.size()));
                    } else {
                        List<Holder<MobEffect>> selfBuffs = List.of(
                                MobEffects.JUMP,
                                MobEffects.DAMAGE_BOOST,
                                MobEffects.MOVEMENT_SPEED
                        );
                        return selfBuffs.get(this.slime.getRandom().nextInt(selfBuffs.size()));
                    }
                } else {
                    // 【遠距離】デバフが届かない間は、接近用の移動・攻撃バフのみ使用
                    List<Holder<MobEffect>> approachBuffs = List.of(
                            MobEffects.JUMP,
                            MobEffects.MOVEMENT_SPEED,
                            MobEffects.DAMAGE_BOOST
                    );
                    return approachBuffs.get(this.slime.getRandom().nextInt(approachBuffs.size()));
                }
            }

            // 3. ターゲット不在かつHP低下時
            if (this.slime.getHealth() < this.slime.getMaxHealth() * 0.5F) {
                List<Holder<MobEffect>> outOfCombatBuffs = List.of(
                        MobEffects.REGENERATION,
                        MobEffects.OOZING,
                        MobEffects.WEAVING
                );
                return outOfCombatBuffs.get(this.slime.getRandom().nextInt(outOfCombatBuffs.size()));
            }

            return null;
        }
    }
}