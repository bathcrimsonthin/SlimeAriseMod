package com.bathcrimsonthin.slimearisemod.entity;

import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;

public final class SlamAnimationHelper {

    public static final int SLAM_TOTAL_TICKS = 18;  // 全体 0.90秒
    public static final int SLAM_IMPACT_TICK = 10;  // 残り10tick（開始0.40秒後）で地面に突き刺さる

    private SlamAnimationHelper() {
    }

    public interface SlamPlayerAccess {
        int slimearisemod$getSlamTicks();
        void slimearisemod$setSlamTicks(int ticks);
        boolean slimearisemod$isSlamMainHand();
        void slimearisemod$setSlamMainHand(boolean mainHand);
    }

    public static void startSlam(Player player, InteractionHand hand) {
        if (player instanceof SlamPlayerAccess access) {
            access.slimearisemod$setSlamMainHand(hand == InteractionHand.MAIN_HAND);
            access.slimearisemod$setSlamTicks(SLAM_TOTAL_TICKS);
        }
    }

    public static int getSlamTicks(Player player) {
        if (player instanceof SlamPlayerAccess access) {
            return access.slimearisemod$getSlamTicks();
        }
        return 0;
    }

    public static boolean isSlamming(Player player) {
        return getSlamTicks(player) > 0;
    }

    public static InteractionHand getSlamHand(Player player) {
        if (player instanceof SlamPlayerAccess access) {
            return access.slimearisemod$isSlamMainHand() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
        }
        return InteractionHand.MAIN_HAND;
    }

    public static HumanoidArm getSlamArm(Player player) {
        InteractionHand hand = getSlamHand(player);
        HumanoidArm mainArm = player.getMainArm();
        return (hand == InteractionHand.MAIN_HAND) ? mainArm : mainArm.getOpposite();
    }

    // 0.0（開始） -> 1.0（終了）の補間済み進行度
    public static float getSlamProgress(Player player, float partialTick) {
        int ticks = getSlamTicks(player);
        if (ticks <= 0) {
            return 0.0F;
        }
        float elapsed = (SLAM_TOTAL_TICKS - ticks) + partialTick;
        return Mth.clamp(elapsed / (float) SLAM_TOTAL_TICKS, 0.0F, 1.0F);
    }

    /**
     * ① 逆手持ち（刃を下向き）係数（0.0 = 順手, 1.0 = 逆手）
     */
    public static float getReverseGripFactor(float progress) {
        if (progress < 0.16F) {
            float t = progress / 0.16F;
            return t * t * (3.0F - 2.0F * t);
        } else if (progress < 0.84F) {
            return 1.0F;
        } else {
            float t = (progress - 0.84F) / 0.16F;
            return 1.0F - (t * t * (3.0F - 2.0F * t));
        }
    }

    /**
     * ② 頭上への振りかぶり係数（0.0 -> 1.0 で大きく振り上げ、振り下ろし時に 0.0 へ戻る）
     */
    public static float getWindupFactor(float progress) {
        if (progress < 0.30F) {
            float t = progress / 0.30F;
            return Mth.sin(t * (Mth.PI * 0.5F));
        } else if (progress < 0.42F) {
            float t = (progress - 0.30F) / 0.12F;
            return 1.0F - (t * t);
        }
        return 0.0F;
    }

    /**
     * ③ 体をかがめて地面へ突き立てる深さ係数（0.0 = 立位, 1.0 = 深くかがんで地面に突き刺した状態）
     */
    public static float getPlantedFactor(float progress) {
        if (progress < 0.32F) {
            return 0.0F;
        } else if (progress < 0.445F) {
            // 0.32 -> 0.445 : 一気に腰を落として地面へ突き刺す
            float t = (progress - 0.32F) / 0.125F;
            return t * t;
        } else if (progress < 0.80F) {
            // 0.445 -> 0.80 : 突き立てた姿勢を維持
            return 1.0F;
        } else {
            // 0.80 -> 1.00 : 剣を引き抜いて立ち上がる
            float t = (progress - 0.80F) / 0.20F;
            float smooth = t * t * (3.0F - 2.0F * t);
            return 1.0F - smooth;
        }
    }
}