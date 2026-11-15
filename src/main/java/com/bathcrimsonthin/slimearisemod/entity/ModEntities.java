package com.bathcrimsonthin.slimearisemod.entity;

import com.bathcrimsonthin.slimearisemod.SlimeAriseMod;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.monster.Slime;

public class ModEntities {

    public static final EntityType<BlazeSlimeEntity> BLAZE_SLIME = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            ResourceLocation.fromNamespaceAndPath(SlimeAriseMod.MOD_ID, "blaze_slime"),
            EntityType.Builder.of(BlazeSlimeEntity::new, MobCategory.MONSTER)
                    .sized(0.5202f, 0.5202f)
                    .eyeHeight(0.325f)
                    .spawnDimensionsScale(4.0f)
                    .clientTrackingRange(10)
                    .fireImmune() // 炎・溶岩耐性
                    .build("blaze_slime")
    );

    public static final EntityType<RabbitSlimeEntity> RABBIT_SLIME = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            ResourceLocation.fromNamespaceAndPath(SlimeAriseMod.MOD_ID, "rabbit_slime"),
            EntityType.Builder.of(RabbitSlimeEntity::new, MobCategory.MONSTER)
                    .sized(0.5202F, 0.5202F)
                    .eyeHeight(0.325f)
                    .spawnDimensionsScale(4.0f)
                    .clientTrackingRange(10)
                    .build("rabbit_slime")
    );

    public static final EntityType<BombSlimeEntity> BOMB_SLIME = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            ResourceLocation.fromNamespaceAndPath(SlimeAriseMod.MOD_ID, "bomb_slime"),
            EntityType.Builder.of(BombSlimeEntity::new, MobCategory.MONSTER)
                    .sized(0.5202F, 0.5202F)
                    .eyeHeight(0.325f)
                    .spawnDimensionsScale(4.0f)
                    .clientTrackingRange(10)
                    .build("bomb_slime")
    );

    public static final EntityType<WitchSlimeEntity> WITCH_SLIME = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            ResourceLocation.fromNamespaceAndPath(SlimeAriseMod.MOD_ID, "witch_slime"),
            EntityType.Builder.of(WitchSlimeEntity::new, MobCategory.MONSTER)
                    .sized(0.5202F, 0.5202F)
                    .eyeHeight(0.325F)
                    .spawnDimensionsScale(4.0F)
                    .clientTrackingRange(10)
                    .build("witch_slime")
    );

    public static final EntityType<EnderSlimeEntity> ENDER_SLIME = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            ResourceLocation.fromNamespaceAndPath(SlimeAriseMod.MOD_ID, "ender_slime"),
            EntityType.Builder.of(EnderSlimeEntity::new, MobCategory.MONSTER)
                    .sized(0.5202F, 1.0404F)       // 横幅 0.5202F, 高さ 1.0404F (縦2倍)
                    .eyeHeight(0.845F)              // 上部の目の位置に合わせた視線高さ
                    .spawnDimensionsScale(4.0F)     // 24.0F → 4.0F に修正（Size 4 スポーン時の当たり判定倍率）
                    .clientTrackingRange(10)        // クライアント同期の追跡距離
                    .build("ender_slime")
    );

    public static final EntityType<ThrownEnderSlimeBall> ENDER_SLIME_BALL = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            ResourceLocation.fromNamespaceAndPath(SlimeAriseMod.MOD_ID, "ender_slime_ball"),
            EntityType.Builder.<ThrownEnderSlimeBall>of(ThrownEnderSlimeBall::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .clientTrackingRange(4)
                    .updateInterval(10)
                    .build("ender_slime_ball")
    );

    public static final EntityType<StonecutterSlimeEntity> STONECUTTER_SLIME = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            ResourceLocation.fromNamespaceAndPath(SlimeAriseMod.MOD_ID, "stonecutter_slime"),
            EntityType.Builder.of(StonecutterSlimeEntity::new, MobCategory.MONSTER)
                    .sized(0.5202F, 0.5202F)
                    .eyeHeight(0.325F)
                    .spawnDimensionsScale(4.0F)
                    .clientTrackingRange(10)
                    .build("stonecutter_slime")
    );

    public static final EntityType<PufferfishSlimeEntity> PUFFERFISH_SLIME = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            ResourceLocation.fromNamespaceAndPath(SlimeAriseMod.MOD_ID, "pufferfish_slime"),
            EntityType.Builder.of(PufferfishSlimeEntity::new, MobCategory.MONSTER)
                    .sized(0.5202F, 0.5202F)
                    .eyeHeight(0.325F)
                    .spawnDimensionsScale(4.0F)
                    .clientTrackingRange(10)
                    .build("pufferfish_slime")
    );

    public static final EntityType<WitherSlimeEntity> WITHER_SLIME = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            ResourceLocation.fromNamespaceAndPath(SlimeAriseMod.MOD_ID, "wither_slime"),
            EntityType.Builder.of(WitherSlimeEntity::new, MobCategory.MONSTER)
                    .fireImmune()
                    .sized(0.5202F, 0.5202F)
                    .eyeHeight(0.325F)
                    .spawnDimensionsScale(4.0F)
                    .clientTrackingRange(10)
                    .build("wither_slime")
    );

    public static final EntityType<KingSlimeEntity> KING_SLIME = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            ResourceLocation.fromNamespaceAndPath(SlimeAriseMod.MOD_ID, "king_slime"),
            EntityType.Builder.of(KingSlimeEntity::new, MobCategory.MONSTER)
                    .sized(0.5202F, 0.5202F)
                    .eyeHeight(0.325F)
                    .spawnDimensionsScale(8.0F)
                    .clientTrackingRange(12)
                    .build("king_slime")
    );

    public static final EntityType<SlimeFieldEntity> SLIME_FIELD = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            ResourceLocation.fromNamespaceAndPath(SlimeAriseMod.MOD_ID, "slime_field"),
            EntityType.Builder.of(SlimeFieldEntity::new, MobCategory.MISC)
                    .sized(6.4F, 0.1F)
                    .clientTrackingRange(10)
                    .updateInterval(2)
                    .build("slime_field")
    );

    public static final EntityType<SlimeArrowEntity> SLIME_ARROW = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            ResourceLocation.fromNamespaceAndPath(SlimeAriseMod.MOD_ID, "slime_arrow"),
            EntityType.Builder.<SlimeArrowEntity>of(SlimeArrowEntity::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .clientTrackingRange(4)
                    .updateInterval(20)
                    .build("slime_arrow")
    );

    public static final EntityType<StickyTorchProjectileEntity> STICKY_TORCH_PROJECTILE = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            ResourceLocation.fromNamespaceAndPath(SlimeAriseMod.MOD_ID, "sticky_torch_projectile"),
            EntityType.Builder.<StickyTorchProjectileEntity>of(StickyTorchProjectileEntity::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .clientTrackingRange(4)
                    .updateInterval(10)
                    .build("sticky_torch_projectile")
    );

    public static void registerModEntities() {
        SlimeAriseMod.LOGGER.info("Registering Mod Entities for " + SlimeAriseMod.MOD_ID);

        // モブの属性を Fabric API に登録
        FabricDefaultAttributeRegistry.register(BLAZE_SLIME, BlazeSlimeEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(RABBIT_SLIME, RabbitSlimeEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(BOMB_SLIME, BombSlimeEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(WITCH_SLIME, WitchSlimeEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(ENDER_SLIME, EnderSlimeEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(PUFFERFISH_SLIME, PufferfishSlimeEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(STONECUTTER_SLIME, StonecutterSlimeEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(WITHER_SLIME, WitherSlimeEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(KING_SLIME, KingSlimeEntity.createAttributes());
    }
}
