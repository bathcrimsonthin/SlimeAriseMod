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

    public static void registerModEntities() {
        SlimeAriseMod.LOGGER.info("Registering Mod Entities for " + SlimeAriseMod.MOD_ID);

        // モブの属性を Fabric API に登録
        FabricDefaultAttributeRegistry.register(BLAZE_SLIME, BlazeSlimeEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(RABBIT_SLIME, RabbitSlimeEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(BOMB_SLIME, BombSlimeEntity.createAttributes());
    }
}
