package com.bathcrimsonthin.slimearisemod.client;

import com.bathcrimsonthin.slimearisemod.entity.SlamAnimationHelper;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public final class SlamCameraShake {

    private static long shakeEndTimeMs = 0L;
    private static long shakeDurationMs = 450L;
    private static float baseStrength = 0.0F;

    private SlamCameraShake() {
    }

    public static void trigger(float strength) {
        shakeDurationMs = 450L;
        shakeEndTimeMs = System.currentTimeMillis() + shakeDurationMs;
        baseStrength = Math.max(baseStrength, strength);
    }

    public static float getCurrentStrength() {
        long now = System.currentTimeMillis();
        if (now >= shakeEndTimeMs || baseStrength <= 0.0F) {
            baseStrength = 0.0F;
            return 0.0F;
        }
        float ratio = (float) (shakeEndTimeMs - now) / (float) shakeDurationMs;
        return baseStrength * (ratio * ratio);
    }

    public static float getTimeSeconds() {
        return (System.currentTimeMillis() % 100000L) / 1000.0F;
    }

    public static float getYawOffset() {
        float s = getCurrentStrength();
        if (s <= 0.0F) return 0.0F;
        float t = getTimeSeconds();
        return Mth.sin(t * 78.0F) * 2.4F * s;
    }

    public static float getPitchOffset() {
        float s = getCurrentStrength();
        if (s <= 0.0F) return 0.0F;
        float t = getTimeSeconds();
        return (Mth.cos(t * 92.0F) * 2.8F + 1.2F) * s;
    }

    public static float getVerticalOffset() {
        float s = getCurrentStrength();
        if (s <= 0.0F) return 0.0F;
        float t = getTimeSeconds();
        return Mth.sin(t * 85.0F) * 0.14F * s;
    }

    // 一人称で体をかがめて突き立てるときのカメラ沈み込み量
    public static float getCrouchDipOffset(Entity cameraEntity, float partialTick) {
        if (cameraEntity instanceof Player player && SlamAnimationHelper.isSlamming(player)) {
            float progress = SlamAnimationHelper.getSlamProgress(player, partialTick);
            float planted = SlamAnimationHelper.getPlantedFactor(progress);
            return -0.36F * planted;
        }
        return 0.0F;
    }
}