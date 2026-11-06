package com.bathcrimsonthin.slimearisemod.entity;

import com.bathcrimsonthin.slimearisemod.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

public class WitchSlimeEntity extends Slime {

    private static final EntityDataAccessor<Integer> EFFECT_COLOR =
            SynchedEntityData.defineId(WitchSlimeEntity.class, EntityDataSerializers.INT);

    private Holder<MobEffect> storedDebuff = null;
    private int storedDebuffTimer = 0;
    private Holder<MobEffect> currentBrewedEffect = null;
    private boolean wasOnGround = false;

    public WitchSlimeEntity(EntityType<? extends Slime> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(EFFECT_COLOR, 0);
    }

    @Override
    protected ParticleOptions getParticleType() {
        int tintedColor = this.getTintedColor();
        if (tintedColor != -1) {
            return ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, tintedColor);
        }
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

    // 死亡時・撃破時の処理（親スライムの分裂に伴うエフェクト引き継ぎ）
    @Override
    public void remove(Entity.RemovalReason reason) {
        int currentSize = this.getSize();
        Holder<MobEffect> parentEffect = this.currentBrewedEffect != null ? this.currentBrewedEffect : this.storedDebuff;

        // super.remove により標準のバニラ分裂処理（子スライムの生成）が実行される
        super.remove(reason);

        // 撃破による分裂かつ、親がポーション醸造状態であった場合
        if (reason == Entity.RemovalReason.KILLED && currentSize > 1 && parentEffect != null && !this.level().isClientSide) {
            // スポーンした周囲の子スライム（未着色状態）を取得
            List<WitchSlimeEntity> children = this.level().getEntitiesOfClass(
                    WitchSlimeEntity.class,
                    this.getBoundingBox().inflate(2.0D),
                    child -> child != this && child.isAlive() && child.getEffectColor() == 0
            );

            // 子スライムの総数の半数（例: 2匹中1匹、4匹中2匹、3匹中2匹）のみに引き継がせる
            int inheritCount = (int) Math.ceil(children.size() / 2.0);
            for (int i = 0; i < inheritCount && i < children.size(); i++) {
                children.get(i).applyInternalPotion(parentEffect, false);
            }
        }
    }

    // 最小サイズ（isTiny）で死亡した際のみポーション入り WitchSlimeBall をドロップ
    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource damageSource, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);

        if (!this.isTiny()) {
            return;
        }

        Holder<MobEffect> activeEffect = this.currentBrewedEffect != null ? this.currentBrewedEffect : this.storedDebuff;

        if (activeEffect != null) {
            ItemStack stack = new ItemStack(ModItems.WITCH_SLIME_BALL);
            MobEffectInstance effectInstance = new MobEffectInstance(activeEffect, 300, 0);

            PotionContents contents = new PotionContents(
                    Optional.empty(),
                    Optional.of(activeEffect.value().getColor()),
                    List.of(effectInstance)
            );

            stack.set(DataComponents.POTION_CONTENTS, contents);
            this.spawnAtLocation(stack);
        }
    }

    @Override
    public boolean canBeAffected(MobEffectInstance effect) {
        if (effect.getEffect().value().getCategory() == MobEffectCategory.HARMFUL) {
            return false;
        }
        return super.canBeAffected(effect);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.is(DamageTypeTags.WITCH_RESISTANT_TO)) {
            amount *= 0.15F;
        }
        return super.hurt(source, amount);
    }

    // --- 体内醸造処理 ---

    public void applyInternalPotion(Holder<MobEffect> effect) {
        applyInternalPotion(effect, true);
    }

    public void applyInternalPotion(Holder<MobEffect> effect, boolean playSound) {
        // 小スライム（isTiny）でも親からの引き継ぎを可能にするためガードを解除
        this.currentBrewedEffect = effect;
        int color = 0xFF000000 | effect.value().getColor();
        MobEffectCategory category = effect.value().getCategory();

        if (category == MobEffectCategory.HARMFUL) {
            this.storedDebuff = effect;
            this.storedDebuffTimer = 300;
            this.entityData.set(EFFECT_COLOR, color);
        } else {
            this.addEffect(new MobEffectInstance(effect, 300, 0, false, false, true));
            this.storedDebuff = null;
            this.storedDebuffTimer = 0;
            this.entityData.set(EFFECT_COLOR, color);
        }

        if (playSound) {
            this.playSound(SoundEvents.BREWING_STAND_BREW, 1.0F, 1.2F + this.random.nextFloat() * 0.3F);
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide) {
            if (this.storedDebuffTimer > 0) {
                this.storedDebuffTimer--;
                if (this.storedDebuffTimer == 0) {
                    this.storedDebuff = null;
                    this.currentBrewedEffect = null;
                    if (this.getActiveEffects().isEmpty()) {
                        this.entityData.set(EFFECT_COLOR, 0);
                    }
                }
            } else if (this.getActiveEffects().isEmpty()) {
                this.currentBrewedEffect = null;
                this.entityData.set(EFFECT_COLOR, 0);
            }

            boolean currentlyOnGround = this.onGround();
            if (currentlyOnGround && !this.wasOnGround) {
                this.onLandingSplash();
            }
            this.wasOnGround = currentlyOnGround;
        }

        if (this.level().isClientSide && !this.isInvisible()) {
            int tintedColor = this.getTintedColor();
            if (tintedColor != -1) {
                for (int i = 0; i < 2; i++) {
                    double px = this.getX() + (this.random.nextDouble() - 0.5D) * this.getBbWidth();
                    double py = this.getY() + 0.1D + this.random.nextDouble() * this.getBbHeight();
                    double pz = this.getZ() + (this.random.nextDouble() - 0.5D) * this.getBbWidth();

                    this.level().addParticle(
                            ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, tintedColor),
                            px, py, pz,
                            0.0D, 0.0D, 0.0D
                    );
                }
            }
        }
    }

    private void onLandingSplash() {
        if (this.isTiny() || this.currentBrewedEffect == null) {
            return;
        }

        AreaEffectCloud cloud = new AreaEffectCloud(this.level(), this.getX(), this.getY(), this.getZ());
        cloud.setOwner(this);
        cloud.setRadius(0.8F + 0.3F * (float) this.getSize());
        cloud.setDuration(40);
        cloud.setRadiusPerTick(-cloud.getRadius() / (float) cloud.getDuration());

        cloud.addEffect(new MobEffectInstance(this.currentBrewedEffect, 100, 0));
        this.level().addFreshEntity(cloud);

        this.playSound(SoundEvents.SLIME_SQUISH, 0.6F, 1.4F);
    }

    @Override
    protected void dealDamage(LivingEntity target) {
        if (this.isAlive()) {
            int size = this.getSize();
            boolean willHit = this.distanceToSqr(target) < 0.6D * (double) size * 0.6D * (double) size && this.hasLineOfSight(target);

            super.dealDamage(target);

            if (willHit && target.isAlive() && this.storedDebuff != null && this.storedDebuffTimer > 0) {
                target.addEffect(new MobEffectInstance(this.storedDebuff, 100, 0));
            }
        }
    }

    public int getEffectColor() {
        return this.entityData.get(EFFECT_COLOR);
    }

    public int getTintedColor() {
        int color = this.entityData.get(EFFECT_COLOR);
        if (color == 0) return -1;

        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;

        r = (int) (r * 0.7F + 255 * 0.3F);
        g = (int) (g * 0.7F + 255 * 0.3F);
        b = (int) (b * 0.7F + 255 * 0.3F);

        return (0xFF << 24) | (r << 16) | (g << 8) | b;
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
            // 小スライム自身が能動的に新規醸造を開始するのを防ぐ
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
                this.slime.applyInternalPotion(selectedEffect);
            }
            this.cooldown = 60 + this.slime.getRandom().nextInt(40);
        }

        private Holder<MobEffect> evaluateSituation() {
            if (this.slime.isInWater() && !this.slime.hasEffect(MobEffects.WATER_BREATHING)) {
                return MobEffects.WATER_BREATHING;
            }
            if (this.slime.isOnFire() && !this.slime.hasEffect(MobEffects.FIRE_RESISTANCE)) {
                return MobEffects.FIRE_RESISTANCE;
            }

            LivingEntity target = this.slime.getTarget();

            if (target != null && target.isAlive()) {
                if (this.slime.getHealth() < this.slime.getMaxHealth() * 0.4F && this.slime.getRandom().nextFloat() < 0.3F) {
                    List<Holder<MobEffect>> lowHpBuffs = List.of(
                            MobEffects.REGENERATION,
                            MobEffects.OOZING
//                            MobEffects.WEAVING
                    );
                    return lowHpBuffs.get(this.slime.getRandom().nextInt(lowHpBuffs.size()));
                }

                if (this.slime.getRandom().nextFloat() < 0.6F) {
                    List<Holder<MobEffect>> debuffs = List.of(
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
            }

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

    // 死亡時イベント（全サイズ共通で OOZING / WEAVING の死亡効果を発動）
    @Override
    public void die(DamageSource damageSource) {
        if (!this.level().isClientSide) {
            Holder<MobEffect> activeEffect = this.currentBrewedEffect != null ? this.currentBrewedEffect : this.storedDebuff;

            if (activeEffect != null) {
                // 1. OOZING（粘着）: 死亡時に周囲に小スライムを2匹追加生成
                if (activeEffect.equals(MobEffects.OOZING)) {
                    for (int i = 0; i < 2; i++) {
                        WitchSlimeEntity extraSlime = (WitchSlimeEntity) this.getType().create(this.level());
                        if (extraSlime != null) {
                            extraSlime.setSize(1, true);
                            extraSlime.moveTo(
                                    this.getX() + (this.random.nextDouble() - 0.5D) * (double) this.getBbWidth(),
                                    this.getY() + 0.5D,
                                    this.getZ() + (this.random.nextDouble() - 0.5D) * (double) this.getBbWidth(),
                                    this.random.nextFloat() * 360.0F,
                                    0.0F
                            );
                            this.level().addFreshEntity(extraSlime);
                        }
                    }
                }
//                // 2. WEAVING（織網）: 死亡時に足元・周囲の空気ブロックをクモの巣に変換
//                else if (activeEffect.equals(MobEffects.WEAVING)) {
//                    BlockPos center = this.blockPosition();
//                    for (BlockPos pos : BlockPos.betweenClosed(center.offset(-1, 0, -1), center.offset(1, 1, 1))) {
//                        if (this.level().getBlockState(pos).isAir()) {
//                            this.level().setBlockAndUpdate(pos, Blocks.COBWEB.defaultBlockState());
//                        }
//                    }
//                }
            }
        }

        super.die(damageSource);
    }
}