package com.bathcrimsonthin.slimearisemod.entity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public final class AlliedSlimeHelper {

    public static final int ALLY_MAX_LIFETIME_TICKS = 360; // 18秒後に弾けて消滅
    public static final int SIZE_MEDIUM = 2;
    public static final int SIZE_LARGE = 4;

    private AlliedSlimeHelper() {
    }

    public interface AlliedSlimeAccess {
        @Nullable UUID slimearisemod$getAllyOwnerUuid();
        void slimearisemod$setAllyOwnerUuid(@Nullable UUID ownerUuid);
        int slimearisemod$getAllyLifeTicks();
        void slimearisemod$setAllyLifeTicks(int ticks);
    }

    @Nullable
    public static UUID getOwnerUuid(Entity entity) {
        if (entity instanceof AlliedSlimeAccess access) {
            return access.slimearisemod$getAllyOwnerUuid();
        }
        return null;
    }

    public static boolean isAlliedSlime(Entity entity) {
        return getOwnerUuid(entity) != null;
    }

    public static boolean isAlliedTo(Entity slime, UUID playerUuid) {
        UUID owner = getOwnerUuid(slime);
        return owner != null && owner.equals(playerUuid);
    }

    public static boolean areSameAlliance(Entity a, Entity b) {
        UUID ownerA = getOwnerUuid(a);
        UUID ownerB = getOwnerUuid(b);
        return ownerA != null && ownerA.equals(ownerB);
    }

    public static boolean shouldProtectTarget(Entity attackerSlime, Entity victim) {
        UUID owner = getOwnerUuid(attackerSlime);
        if (owner == null) {
            return false;
        }
        if (victim.getUUID().equals(owner)) {
            return true;
        }
        return areSameAlliance(attackerSlime, victim);
    }

    // 指定した確率で大サイズ(4)または中サイズ(2)を召喚する
    @Nullable
    public static Slime spawnMixedAlliedSlime(ServerLevel level, double x, double y, double z,
                                              float largeChance, float modSlimeChance,
                                              double vx, double vy, double vz,
                                              UUID ownerUuid) {
        int chosenSize = (level.random.nextFloat() < largeChance) ? SIZE_LARGE : SIZE_MEDIUM;
        return spawnAlliedSlime(level, x, y, z, chosenSize, modSlimeChance, vx, vy, vz, ownerUuid);
    }

    @Nullable
    public static Slime spawnAlliedSlime(ServerLevel level, double x, double y, double z,
                                         int size, float modSlimeChance,
                                         double vx, double vy, double vz,
                                         UUID ownerUuid) {
        Slime slime = KingSlimeEntity.spawnRatioSlime(level, x, y, z, size, modSlimeChance, vx, vy, vz);
        if (slime instanceof AlliedSlimeAccess access) {
            access.slimearisemod$setAllyOwnerUuid(ownerUuid);
            access.slimearisemod$setAllyLifeTicks(0);
        }
        return slime;
    }

    public static void updateAlliedTarget(Slime slime, UUID ownerUuid) {
        if (!(slime.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        LivingEntity currentTarget = slime.getTarget();
        if (currentTarget != null && (!currentTarget.isAlive() || shouldProtectTarget(slime, currentTarget))) {
            slime.setTarget(null);
            currentTarget = null;
        }

        if (slime.getLastHurtByMob() != null && shouldProtectTarget(slime, slime.getLastHurtByMob())) {
            slime.setLastHurtByMob(null);
        }

        Player owner = serverLevel.getPlayerByUUID(ownerUuid);

        if (owner != null && owner.isAlive()) {
            LivingEntity attackerOfOwner = owner.getLastHurtByMob();
            if (isValidEnemy(slime, attackerOfOwner) && slime.distanceToSqr(attackerOfOwner) <= 256.0D) {
                slime.setTarget(attackerOfOwner);
                return;
            }

            LivingEntity attackedByOwner = owner.getLastHurtMob();
            if (isValidEnemy(slime, attackedByOwner) && slime.distanceToSqr(attackedByOwner) <= 256.0D) {
                slime.setTarget(attackedByOwner);
                return;
            }
        }

        if (currentTarget != null && currentTarget.isAlive() && slime.distanceToSqr(currentTarget) <= 256.0D) {
            return;
        }

        List<LivingEntity> hostiles = serverLevel.getEntitiesOfClass(
                LivingEntity.class,
                slime.getBoundingBox().inflate(16.0D),
                candidate -> isValidEnemy(slime, candidate) && candidate instanceof Enemy
        );
        hostiles.sort(Comparator.comparingDouble(slime::distanceToSqr));
        slime.setTarget(hostiles.isEmpty() ? null : hostiles.get(0));
    }

    private static boolean isValidEnemy(Slime slime, @Nullable LivingEntity candidate) {
        if (candidate == null || !candidate.isAlive() || candidate == slime) {
            return false;
        }
        if (candidate instanceof Player) {
            return false;
        }
        return !shouldProtectTarget(slime, candidate);
    }
}