package com.bathcrimsonthin.slimearisemod.entity;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectionContext;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.function.Predicate;

public final class ModEntitySpawns {

    private static final int OVERWORLD_MOD_SLIME_WEIGHT = 60;
    private static final int OVERWORLD_MOD_SLIME_MIN_COUNT = 4;
    private static final int OVERWORLD_MOD_SLIME_MAX_COUNT = 4;

    private static final int NETHER_WASTES_BLAZE_WEIGHT = 35;
    private static final int BASALT_DELTAS_BLAZE_WEIGHT = 35;
    private static final int CRIMSON_FOREST_BLAZE_WEIGHT = 10;
    private static final int SOUL_SAND_VALLEY_WITHER_WEIGHT = 30;
    private static final int WARPED_FOREST_ENDER_WEIGHT = 1;

    private static final int END_ENDER_SLIME_WEIGHT = 10;

    private ModEntitySpawns() {
    }

    /**
     * 暗さが必要なスライム用スポーン判定（Monster.checkMonsterSpawnRules 同等）
     */
    public static boolean checkSlimeDarkSpawnRules(EntityType<? extends Slime> type,
                                                   ServerLevelAccessor level,
                                                   MobSpawnType spawnType,
                                                   BlockPos pos,
                                                   RandomSource random) {
        return level.getDifficulty() != Difficulty.PEACEFUL
                && (MobSpawnType.ignoresLightRequirements(spawnType) || Monster.isDarkEnoughToSpawn(level, pos, random))
                && Mob.checkMobSpawnRules(type, level, spawnType, pos, random);
    }

    /**
     * 明るさ不問のスライム用スポーン判定（ブレイズと同じく溶岩周辺でもスポーン可能）
     */
    public static boolean checkSlimeAnyLightSpawnRules(EntityType<? extends Slime> type,
                                                       ServerLevelAccessor level,
                                                       MobSpawnType spawnType,
                                                       BlockPos pos,
                                                       RandomSource random) {
        return level.getDifficulty() != Difficulty.PEACEFUL
                && Mob.checkMobSpawnRules(type, level, spawnType, pos, random);
    }

    public static void registerSpawns() {
        registerDarkSpawnPlacement(ModEntities.WITCH_SLIME);
        registerDarkSpawnPlacement(ModEntities.STONECUTTER_SLIME);
        registerDarkSpawnPlacement(ModEntities.PUFFERFISH_SLIME);
        registerDarkSpawnPlacement(ModEntities.RABBIT_SLIME);
        registerDarkSpawnPlacement(ModEntities.BOMB_SLIME);
        registerAnyLightSpawnPlacement(ModEntities.BLAZE_SLIME); // ブレイズスライムのみ明るさ不問
        registerDarkSpawnPlacement(ModEntities.WITHER_SLIME);
        registerDarkSpawnPlacement(ModEntities.ENDER_SLIME);

        Predicate<BiomeSelectionContext> overworldBiomes = BiomeSelectors.foundInOverworld()
                .and(BiomeSelectors.excludeByKey(Biomes.MUSHROOM_FIELDS, Biomes.DEEP_DARK));

        BiomeModifications.addSpawn(overworldBiomes, MobCategory.MONSTER,
                ModEntities.WITCH_SLIME, OVERWORLD_MOD_SLIME_WEIGHT, OVERWORLD_MOD_SLIME_MIN_COUNT, OVERWORLD_MOD_SLIME_MAX_COUNT);

        BiomeModifications.addSpawn(overworldBiomes, MobCategory.MONSTER,
                ModEntities.STONECUTTER_SLIME, OVERWORLD_MOD_SLIME_WEIGHT, OVERWORLD_MOD_SLIME_MIN_COUNT, OVERWORLD_MOD_SLIME_MAX_COUNT);

        BiomeModifications.addSpawn(overworldBiomes, MobCategory.MONSTER,
                ModEntities.PUFFERFISH_SLIME, OVERWORLD_MOD_SLIME_WEIGHT, OVERWORLD_MOD_SLIME_MIN_COUNT, OVERWORLD_MOD_SLIME_MAX_COUNT);

        BiomeModifications.addSpawn(overworldBiomes, MobCategory.MONSTER,
                ModEntities.RABBIT_SLIME, OVERWORLD_MOD_SLIME_WEIGHT, OVERWORLD_MOD_SLIME_MIN_COUNT, OVERWORLD_MOD_SLIME_MAX_COUNT);

        BiomeModifications.addSpawn(overworldBiomes, MobCategory.MONSTER,
                ModEntities.BOMB_SLIME, OVERWORLD_MOD_SLIME_WEIGHT, OVERWORLD_MOD_SLIME_MIN_COUNT, OVERWORLD_MOD_SLIME_MAX_COUNT);

        BiomeModifications.addSpawn(BiomeSelectors.includeByKey(Biomes.NETHER_WASTES), MobCategory.MONSTER,
                ModEntities.BLAZE_SLIME, NETHER_WASTES_BLAZE_WEIGHT, 2, 3);
        BiomeModifications.addSpawn(BiomeSelectors.includeByKey(Biomes.BASALT_DELTAS), MobCategory.MONSTER,
                ModEntities.BLAZE_SLIME, BASALT_DELTAS_BLAZE_WEIGHT, 2, 3);
        BiomeModifications.addSpawn(BiomeSelectors.includeByKey(Biomes.CRIMSON_FOREST), MobCategory.MONSTER,
                ModEntities.BLAZE_SLIME, CRIMSON_FOREST_BLAZE_WEIGHT, 2, 3);

        BiomeModifications.addSpawn(BiomeSelectors.includeByKey(Biomes.SOUL_SAND_VALLEY), MobCategory.MONSTER,
                ModEntities.WITHER_SLIME, SOUL_SAND_VALLEY_WITHER_WEIGHT, 2, 4);

        BiomeModifications.addSpawn(overworldBiomes, MobCategory.MONSTER,
                ModEntities.ENDER_SLIME, OVERWORLD_MOD_SLIME_WEIGHT, OVERWORLD_MOD_SLIME_MIN_COUNT, OVERWORLD_MOD_SLIME_MAX_COUNT);
        BiomeModifications.addSpawn(BiomeSelectors.includeByKey(Biomes.WARPED_FOREST), MobCategory.MONSTER,
                ModEntities.ENDER_SLIME, WARPED_FOREST_ENDER_WEIGHT, 4, 4);
        BiomeModifications.addSpawn(BiomeSelectors.foundInTheEnd(), MobCategory.MONSTER,
                ModEntities.ENDER_SLIME, END_ENDER_SLIME_WEIGHT, 4, 4);
    }

    private static <T extends Slime> void registerDarkSpawnPlacement(EntityType<T> entityType) {
        SpawnPlacements.register(
                entityType,
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                ModEntitySpawns::checkSlimeDarkSpawnRules
        );
    }

    private static <T extends Slime> void registerAnyLightSpawnPlacement(EntityType<T> entityType) {
        SpawnPlacements.register(
                entityType,
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                ModEntitySpawns::checkSlimeAnyLightSpawnRules
        );
    }
}