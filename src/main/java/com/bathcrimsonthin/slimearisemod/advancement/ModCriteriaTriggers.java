package com.bathcrimsonthin.slimearisemod.advancement;

import com.bathcrimsonthin.slimearisemod.SlimeAriseMod;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Items;

public final class ModCriteriaTriggers {

    public static final WitchLingeringPotionTrigger WITCH_LINGERING_POTION = CriteriaTriggers.register(
            ResourceLocation.fromNamespaceAndPath(SlimeAriseMod.MOD_ID, "witch_lingering_potion").toString(),
            new WitchLingeringPotionTrigger()
    );

    public static final EatWitherSlimeBallTrigger EAT_WITHER_SLIME_BALL = CriteriaTriggers.register(
            ResourceLocation.fromNamespaceAndPath(SlimeAriseMod.MOD_ID, "eat_wither_slime_ball").toString(),
            new EatWitherSlimeBallTrigger()
    );

    public static final ThrowEnderSlimeBallTrigger THROW_ENDER_SLIME_BALL = CriteriaTriggers.register(
            ResourceLocation.fromNamespaceAndPath(SlimeAriseMod.MOD_ID, "throw_ender_slime_ball").toString(),
            new ThrowEnderSlimeBallTrigger()
    );

    public static final UseSlimySwordTrigger USE_SLIMY_SWORD = CriteriaTriggers.register(
            ResourceLocation.fromNamespaceAndPath(SlimeAriseMod.MOD_ID, "use_slimy_sword").toString(),
            new UseSlimySwordTrigger()
    );

    private ModCriteriaTriggers() {
    }

    public static void registerTriggers() {
        // クラスロードを発火させてトリガーを登録する
    }

    public static void onEatWitherSlimeBall(LivingEntity entity) {
        if (entity instanceof ServerPlayer serverPlayer
                && serverPlayer.getItemBySlot(EquipmentSlot.HEAD).is(Items.WITHER_SKELETON_SKULL)) {
            EAT_WITHER_SLIME_BALL.trigger(serverPlayer);
        }
    }

    public static void onThrowEnderSlimeBall(LivingEntity entity) {
        if (entity instanceof ServerPlayer serverPlayer) {
            THROW_ENDER_SLIME_BALL.trigger(serverPlayer);
        }
    }

    public static void onUseSlimySword(LivingEntity entity) {
        if (entity instanceof ServerPlayer serverPlayer) {
            USE_SLIMY_SWORD.trigger(serverPlayer);
        }
    }
}