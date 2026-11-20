package com.bathcrimsonthin.slimearisemod.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class SlimeFieldEntity extends Entity {

    public static final float FIELD_RADIUS = 2.4F;
    public static final int MAX_LIFETIME = 200;

    private static final EntityDataAccessor<Integer> DATA_MAX_LIFETIME =
            SynchedEntityData.defineId(SlimeFieldEntity.class, EntityDataSerializers.INT);

    @Nullable
    private UUID ownerUuid = null;
    @Nullable
    private UUID casterKingUuid = null;
    private float modSlimeChance = 0.25F;
    private int spawnCooldown = 20;

    public SlimeFieldEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = true;
    }

    public void setOwnerUuid(@Nullable UUID ownerUuid) {
        this.ownerUuid = ownerUuid;
        if (ownerUuid != null && this.tickCount == 0) {
            this.spawnCooldown = 45;
        }
    }

    @Nullable
    public UUID getOwnerUuid() {
        return this.ownerUuid;
    }

    public void setCasterKing(KingSlimeEntity king) {
        this.casterKingUuid = king.getUUID();
        this.modSlimeChance = (king.getHealth() >= king.getMaxHealth() * 0.5F) ? 0.25F : 0.75F;
    }

    public void setModSlimeChance(float modSlimeChance) {
        this.modSlimeChance = modSlimeChance;
    }

    public int getMaxLifetime() {
        return this.entityData.get(DATA_MAX_LIFETIME);
    }

    public void setMaxLifetime(int maxLifetime) {
        this.entityData.set(DATA_MAX_LIFETIME, maxLifetime);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_MAX_LIFETIME, MAX_LIFETIME);
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide) {
            return;
        }

        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        if (this.tickCount >= this.getMaxLifetime()) {
            this.discard();
            return;
        }

        // フィールド上の粘液パーティクル
        if (this.tickCount % 3 == 0) {
            for (int i = 0; i < 4; i++) {
                double angle = this.random.nextDouble() * Math.PI * 2.0D;
                double dist = Math.sqrt(this.random.nextDouble()) * FIELD_RADIUS;
                double px = this.getX() + Math.cos(angle) * dist;
                double pz = this.getZ() + Math.sin(angle) * dist;
                serverLevel.sendParticles(ParticleTypes.ITEM_SLIME, px, this.getY() + 0.05D, pz, 1, 0.0D, 0.02D, 0.0D, 0.0D);
            }
        }

        AABB area = new AABB(
                this.getX() - FIELD_RADIUS, this.getY() - 0.5D, this.getZ() - FIELD_RADIUS,
                this.getX() + FIELD_RADIUS, this.getY() + 1.5D, this.getZ() + FIELD_RADIUS
        );

        Player ownerPlayer = (this.ownerUuid != null) ? serverLevel.getPlayerByUUID(this.ownerUuid) : null;
        List<LivingEntity> entities = serverLevel.getEntitiesOfClass(LivingEntity.class, area);

        for (LivingEntity entity : entities) {
            double dx = entity.getX() - this.getX();
            double dz = entity.getZ() - this.getZ();
            if (dx * dx + dz * dz > FIELD_RADIUS * FIELD_RADIUS) {
                continue;
            }

            // パッシブ「王の吸収」：領域内で敵または味方スライムが倒れるとオーナーが回復
            if (this.ownerUuid != null && ownerPlayer != null && ownerPlayer.isAlive()
                    && !entity.isAlive() && entity.deathTime == 1 && entity != ownerPlayer) {
                if (ownerPlayer.getHealth() < ownerPlayer.getMaxHealth()) {
                    ownerPlayer.heal(1.5F);
                } else {
                    float currentAbsorb = ownerPlayer.getAbsorptionAmount();
                    if (currentAbsorb < 8.0F) {
                        ownerPlayer.setAbsorptionAmount(Math.min(8.0F, currentAbsorb + 1.5F));
                    }
                }
                serverLevel.sendParticles(
                        ParticleTypes.HEART,
                        ownerPlayer.getX(),
                        ownerPlayer.getY(1.0D) + 0.3D,
                        ownerPlayer.getZ(),
                        2,
                        0.25D,
                        0.2D,
                        0.25D,
                        0.05D
                );
                continue;
            }

            if (!entity.isAlive()) {
                continue;
            }

            // オーナー（展開したプレイヤー）への「王の特権」バフ
            if (this.ownerUuid != null && entity.getUUID().equals(this.ownerUuid)) {
                entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 15, 0, true, false));
                entity.addEffect(new MobEffectInstance(MobEffects.JUMP, 15, 1, true, false));
                continue;
            }

            // スライムへの効果（味方スライムには移動速度上昇、それ以外のスライムは無視）
            if (entity instanceof Slime slime) {
                if (this.ownerUuid != null && AlliedSlimeHelper.isAlliedTo(slime, this.ownerUuid)) {
                    slime.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 15, 0, true, false));
                }
                continue;
            }

            if (entity instanceof Player player && (player.isCreative() || player.isSpectator())) {
                continue;
            }

            // 敵対対象には強力な粘液鈍足
            entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 15, 2, true, false));
        }

        // 湧く直前の予兆パーティクル
        if (this.spawnCooldown > 0 && this.spawnCooldown <= 15 && this.spawnCooldown % 3 == 0) {
            serverLevel.sendParticles(
                    ParticleTypes.ITEM_SLIME,
                    this.getX(),
                    this.getY() + 0.15D,
                    this.getZ(),
                    4,
                    0.45D,
                    0.1D,
                    0.45D,
                    0.06D
            );
        }

        // フィールドからのスライム湧き
        if (this.spawnCooldown > 0) {
            this.spawnCooldown--;
        } else {
            // 小サイズスライムが戦場に溜まりすぎている場合は古いものから自然消散させ、湧きを阻害しない
            this.cleanupExcessTinySlimes(serverLevel);

            if (this.ownerUuid != null) {
                List<Slime> nearbyCombatSlimes = serverLevel.getEntitiesOfClass(
                        Slime.class,
                        this.getBoundingBox().inflate(14.0D),
                        s -> s.isAlive() && !s.isTiny() && !(s instanceof KingSlimeEntity)
                );
                if (nearbyCombatSlimes.size() < 10) {
                    double angle = this.random.nextDouble() * Math.PI * 2.0D;
                    double dist = this.random.nextDouble() * (FIELD_RADIUS * 0.65D);
                    double sx = this.getX() + Math.cos(angle) * dist;
                    double sz = this.getZ() + Math.sin(angle) * dist;

                    AlliedSlimeHelper.spawnMixedAlliedSlime(
                            serverLevel, sx, this.getY() + 0.2D, sz,
                            0.35F, 0.5F, 0.0D, 0.25D, 0.0D, this.ownerUuid
                    );
                    this.playSound(SoundEvents.SLIME_SQUISH, 0.8F, 1.1F);
                }
                this.spawnCooldown = 80 + this.random.nextInt(40);
            } else {
                // キングスライム等による敵対フィールド：小サイズを完全に無視し、戦闘圏内（近傍4m・広域14m）のみで判定
                List<Slime> localCombatSlimes = serverLevel.getEntitiesOfClass(
                        Slime.class,
                        this.getBoundingBox().inflate(4.0D),
                        s -> s.isAlive() && !s.isTiny() && !(s instanceof KingSlimeEntity)
                );
                List<Slime> wideCombatSlimes = serverLevel.getEntitiesOfClass(
                        Slime.class,
                        this.getBoundingBox().inflate(14.0D),
                        s -> s.isAlive() && !s.isTiny() && !(s instanceof KingSlimeEntity)
                );

                if (localCombatSlimes.size() < 3 && wideCombatSlimes.size() < 16) {
                    double angle = this.random.nextDouble() * Math.PI * 2.0D;
                    double dist = 0.4D + this.random.nextDouble() * (FIELD_RADIUS * 0.6D);
                    double sx = this.getX() + Math.cos(angle) * dist;
                    double sz = this.getZ() + Math.sin(angle) * dist;

                    float currentModChance = this.modSlimeChance;
                    if (this.casterKingUuid != null
                            && serverLevel.getEntity(this.casterKingUuid) instanceof KingSlimeEntity king
                            && king.isAlive()) {
                        currentModChance = (king.getHealth() >= king.getMaxHealth() * 0.5F) ? 0.25F : 0.75F;
                        this.modSlimeChance = currentModChance;
                    }

                    KingSlimeEntity.spawnRatioSlime(
                            serverLevel, sx, this.getY() + 0.35D, sz,
                            2, currentModChance,
                            Math.cos(angle) * 0.12D, 0.32D, Math.sin(angle) * 0.12D
                    );
                    serverLevel.sendParticles(
                            ParticleTypes.ITEM_SLIME,
                            sx, this.getY() + 0.25D, sz,
                            10, 0.3D, 0.2D, 0.3D, 0.08D
                    );
                    this.playSound(SoundEvents.SLIME_SQUISH, 0.9F, 1.1F);
                }
                this.spawnCooldown = 45 + this.random.nextInt(20);
            }
        }
    }

    private void cleanupExcessTinySlimes(ServerLevel serverLevel) {
        List<Slime> tinySlimes = serverLevel.getEntitiesOfClass(
                Slime.class,
                this.getBoundingBox().inflate(24.0D),
                s -> s.isAlive() && s.isTiny() && !(s instanceof KingSlimeEntity)
        );
        int excess = tinySlimes.size() - 35;
        if (excess > 0) {
            tinySlimes.sort(Comparator.comparingInt((Slime s) -> s.tickCount).reversed());
            for (int i = 0; i < excess; i++) {
                Slime oldTiny = tinySlimes.get(i);
                serverLevel.sendParticles(
                        ParticleTypes.ITEM_SLIME,
                        oldTiny.getX(), oldTiny.getY(0.5D), oldTiny.getZ(),
                        4, 0.15D, 0.15D, 0.15D, 0.02D
                );
                oldTiny.discard();
            }
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.hasUUID("OwnerUUID")) {
            this.ownerUuid = tag.getUUID("OwnerUUID");
        }
        if (tag.hasUUID("CasterKingUUID")) {
            this.casterKingUuid = tag.getUUID("CasterKingUUID");
        }
        if (tag.contains("ModSlimeChance")) {
            this.modSlimeChance = tag.getFloat("ModSlimeChance");
        }
        if (tag.contains("MaxLifetime")) {
            this.setMaxLifetime(tag.getInt("MaxLifetime"));
        }
        if (tag.contains("SpawnCooldown")) {
            this.spawnCooldown = tag.getInt("SpawnCooldown");
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        if (this.ownerUuid != null) {
            tag.putUUID("OwnerUUID", this.ownerUuid);
        }
        if (this.casterKingUuid != null) {
            tag.putUUID("CasterKingUUID", this.casterKingUuid);
        }
        tag.putFloat("ModSlimeChance", this.modSlimeChance);
        tag.putInt("MaxLifetime", this.getMaxLifetime());
        tag.putInt("SpawnCooldown", this.spawnCooldown);
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }
}