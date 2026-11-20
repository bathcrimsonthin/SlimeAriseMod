package com.bathcrimsonthin.slimearisemod.entity;

import com.bathcrimsonthin.slimearisemod.SlimeAriseMod;
import com.bathcrimsonthin.slimearisemod.config.SlimeAriseConfig;
import com.bathcrimsonthin.slimearisemod.mixin.NaturalSpawnerAccessor;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectionContext;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.biome.v1.ModificationPhase;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

public final class ModEntitySpawns {

    private static final ThreadLocal<Boolean> IS_BUILDING_STACK = ThreadLocal.withInitial(() -> false);

    private ModEntitySpawns() {
    }

    /**
     * 基準となるスポーン重みに全体スケールを適用する（元が1以上の場合は最低1を保証、0以下は0）
     */
    public static int scaleWeight(int baseWeight) {
        if (baseWeight <= 0) {
            return 0;
        }
        double scale = SlimeAriseConfig.get().globalSpawnWeightScale;
        if (scale <= 0.0D) {
            return 0;
        }
        return Math.max(1, (int) Math.round(baseWeight * scale));
    }

    private static void addScaledSpawn(Predicate<BiomeSelectionContext> selector,
                                       EntityType<? extends Slime> entityType,
                                       SlimeAriseConfig.SpawnEntry entry) {
        if (entry == null || entry.weight <= 0) {
            return;
        }
        int scaledWeight = scaleWeight(entry.weight);
        if (scaledWeight <= 0) {
            return;
        }
        int min = Math.max(1, entry.minGroupSize);
        int max = Math.max(min, entry.maxGroupSize);
        BiomeModifications.addSpawn(selector, MobCategory.MONSTER, entityType, scaledWeight, min, max);
    }

    // --- スポーン判定ルール群 ---

    /**
     * 暗所湧き判定（明るさ上限・Y座標・ディメンション制限を考慮）
     */
    public static boolean checkSlimeDarkSpawnRules(EntityType<? extends Slime> type,
                                                   ServerLevelAccessor level,
                                                   MobSpawnType spawnType,
                                                   BlockPos pos,
                                                   RandomSource random) {
        if (level.getDifficulty() == Difficulty.PEACEFUL) {
            return false;
        }
        if (!checkOverworldEnvironmentLimits(level, spawnType, pos)) {
            return false;
        }
        if (!MobSpawnType.ignoresLightRequirements(spawnType) && !isDarkEnoughForConfig(level, pos, random)) {
            return false;
        }
        return Mob.checkMobSpawnRules(type, level, spawnType, pos, random);
    }

    /**
     * オーバーワールド用スポーン判定（Configの overworldSpawnMode に応じて分岐）
     */
    @SuppressWarnings("unchecked")
    public static boolean checkOverworldSlimeSpawnRules(EntityType<? extends Slime> type,
                                                        ServerLevelAccessor level,
                                                        MobSpawnType spawnType,
                                                        BlockPos pos,
                                                        RandomSource random) {
        SlimeAriseConfig.OverworldSpawnMode mode = SlimeAriseConfig.get().overworldSpawnMode;
        if (mode == SlimeAriseConfig.OverworldSpawnMode.DISABLED) {
            return false;
        }
        if (!checkOverworldEnvironmentLimits(level, spawnType, pos)) {
            return false;
        }
        if (mode == SlimeAriseConfig.OverworldSpawnMode.VANILLA) {
            return Slime.checkSlimeSpawnRules((EntityType<Slime>) type, level, spawnType, pos, random);
        }
        return checkSlimeDarkSpawnRules(type, level, spawnType, pos, random);
    }

    /**
     * ネザー暗所系スライム（WitherSlime, EnderSlime）の判定（ネザーモードに応じて分岐）
     */
    public static boolean checkNetherDarkOrMagmaSlimeRules(EntityType<? extends Slime> type,
                                                           ServerLevelAccessor level,
                                                           MobSpawnType spawnType,
                                                           BlockPos pos,
                                                           RandomSource random) {
        if (level.getLevel().dimension() == Level.NETHER
                && SlimeAriseConfig.get().netherSpawnMode == SlimeAriseConfig.NetherSpawnMode.MAGMA_CUBE_LIKE) {
            return checkSlimeAnyLightSpawnRules(type, level, spawnType, pos, random);
        }
        if (type == ModEntities.ENDER_SLIME
                && level.getLevel().dimension() != Level.NETHER
                && level.getLevel().dimension() != Level.END) {
            return checkOverworldSlimeSpawnRules(type, level, spawnType, pos, random);
        }
        return checkSlimeDarkSpawnRules(type, level, spawnType, pos, random);
    }

    /**
     * 明るさ不問のスライム用スポーン判定（ブレイズスライム / マグマキューブ準拠モード用）
     */
    public static boolean checkSlimeAnyLightSpawnRules(EntityType<? extends Slime> type,
                                                       ServerLevelAccessor level,
                                                       MobSpawnType spawnType,
                                                       BlockPos pos,
                                                       RandomSource random) {
        return level.getDifficulty() != Difficulty.PEACEFUL
                && Mob.checkMobSpawnRules(type, level, spawnType, pos, random);
    }

    private static boolean checkOverworldEnvironmentLimits(ServerLevelAccessor level, MobSpawnType spawnType, BlockPos pos) {
        if (spawnType == MobSpawnType.SPAWNER || spawnType == MobSpawnType.COMMAND || spawnType == MobSpawnType.SPAWN_EGG) {
            return true;
        }
        SlimeAriseConfig config = SlimeAriseConfig.get();
        ResourceLocation dimId = level.getLevel().dimension().location();

        if (!dimId.equals(Level.NETHER.location()) && !dimId.equals(Level.END.location())) {
            if (!config.allowedOverworldDimensions.isEmpty()
                    && !config.allowedOverworldDimensions.contains(dimId.toString())) {
                return false;
            }
            if (pos.getY() < config.overworldMinY || pos.getY() > config.overworldMaxY) {
                return false;
            }
        }
        return true;
    }

    private static boolean isDarkEnoughForConfig(ServerLevelAccessor level, BlockPos pos, RandomSource random) {
        int maxLight = SlimeAriseConfig.get().darkSpawnMaxLightLevel;
        if (maxLight <= 0) {
            return Monster.isDarkEnoughToSpawn(level, pos, random);
        }
        if (level.getBrightness(LightLayer.SKY, pos) > random.nextInt(32)) {
            return false;
        }
        int blockLight = level.getBrightness(LightLayer.BLOCK, pos);
        return blockLight <= maxLight;
    }

    // --- スポーン登録 ---

    public static void registerSpawns() {
        SlimeAriseConfig config = SlimeAriseConfig.get();

        // 1. SpawnPlacement 登録
        registerOverworldSpawnPlacement(ModEntities.WITCH_SLIME);
        registerOverworldSpawnPlacement(ModEntities.STONECUTTER_SLIME);
        registerOverworldSpawnPlacement(ModEntities.PUFFERFISH_SLIME);
        registerOverworldSpawnPlacement(ModEntities.RABBIT_SLIME);
        registerOverworldSpawnPlacement(ModEntities.BOMB_SLIME);

        registerAnyLightSpawnPlacement(ModEntities.BLAZE_SLIME);
        registerNetherHybridSpawnPlacement(ModEntities.WITHER_SLIME);
        registerNetherHybridSpawnPlacement(ModEntities.ENDER_SLIME);

        // 2. オーバーワールドのバイオームスポーン登録
        registerOverworldBiomeSpawns(config);

        // 3. ネザーのバイオームスポーン登録
        registerNetherBiomeSpawns(config);

        // 4. エンドのバイオームスポーン登録
        if (config.endSpawnMode == SlimeAriseConfig.EndSpawnMode.ENABLED) {
            addScaledSpawn(BiomeSelectors.foundInTheEnd(), ModEntities.ENDER_SLIME, config.end.enderSlime);
        }
    }

    private static void registerOverworldBiomeSpawns(SlimeAriseConfig config) {
        Set<ResourceKey<Biome>> blacklistKeys = parseBiomeKeys(config.blacklistedBiomes);
        Set<ResourceKey<Biome>> whitelistKeys = parseBiomeKeys(config.whitelistedOverworldBiomes);

        Predicate<BiomeSelectionContext> targetSelector = context -> {
            ResourceKey<Biome> key = context.getBiomeKey();
            if (blacklistKeys.contains(key)) {
                return false;
            }
            if (!whitelistKeys.isEmpty() && whitelistKeys.contains(key)) {
                return true;
            }
            return BiomeSelectors.foundInOverworld().test(context);
        };

        // 標準スライム（minecraft:slime）の既存スポーンを置き換え
        BiomeModifications.create(ResourceLocation.fromNamespaceAndPath(SlimeAriseMod.MOD_ID, "configure_vanilla_slime"))
                .add(ModificationPhase.REMOVALS, BiomeSelectors.foundInOverworld(), context ->
                        context.getSpawnSettings().removeSpawnsOfEntityType(EntityType.SLIME));

        if (config.overworldSpawnMode == SlimeAriseConfig.OverworldSpawnMode.DISABLED) {
            return;
        }

        SlimeAriseConfig.SpawnEntry vanillaEntry = config.overworld.vanillaSlime;
        if (vanillaEntry != null && vanillaEntry.weight > 0) {
            int vanillaWeight = (config.overworldSpawnMode == SlimeAriseConfig.OverworldSpawnMode.VANILLA)
                    ? vanillaEntry.weight
                    : scaleWeight(vanillaEntry.weight);
            if (vanillaWeight > 0) {
                int min = Math.max(1, vanillaEntry.minGroupSize);
                int max = Math.max(min, vanillaEntry.maxGroupSize);
                BiomeModifications.addSpawn(targetSelector, MobCategory.MONSTER, EntityType.SLIME, vanillaWeight, min, max);
            }
        }

        addScaledSpawn(targetSelector, ModEntities.WITCH_SLIME, config.overworld.witchSlime);
        addScaledSpawn(targetSelector, ModEntities.STONECUTTER_SLIME, config.overworld.stonecutterSlime);
        addScaledSpawn(targetSelector, ModEntities.PUFFERFISH_SLIME, config.overworld.pufferfishSlime);
        addScaledSpawn(targetSelector, ModEntities.RABBIT_SLIME, config.overworld.rabbitSlime);
        addScaledSpawn(targetSelector, ModEntities.BOMB_SLIME, config.overworld.bombSlime);
        addScaledSpawn(targetSelector, ModEntities.ENDER_SLIME, config.overworld.enderSlime);
    }

    private static void registerNetherBiomeSpawns(SlimeAriseConfig config) {
        if (config.netherSpawnMode == SlimeAriseConfig.NetherSpawnMode.DISABLED) {
            return;
        }

        if (config.netherSpawnMode == SlimeAriseConfig.NetherSpawnMode.MAGMA_CUBE_LIKE) {
            Predicate<BiomeSelectionContext> netherAll = BiomeSelectors.foundInTheNether();
            addScaledSpawn(netherAll, ModEntities.BLAZE_SLIME, config.netherMagmaCubeLike.blazeSlime);
            addScaledSpawn(netherAll, ModEntities.WITHER_SLIME, config.netherMagmaCubeLike.witherSlime);
            addScaledSpawn(netherAll, ModEntities.ENDER_SLIME, config.netherMagmaCubeLike.enderSlime);
            return;
        }

        // BIOME_AND_FORTRESS（デフォルト・現状維持）
        SlimeAriseConfig.NetherBiomeSpawns nether = config.netherBiomeAndFortress;
        addScaledSpawn(BiomeSelectors.includeByKey(Biomes.NETHER_WASTES), ModEntities.BLAZE_SLIME, nether.blazeSlimeNetherWastes);
        addScaledSpawn(BiomeSelectors.includeByKey(Biomes.BASALT_DELTAS), ModEntities.BLAZE_SLIME, nether.blazeSlimeBasaltDeltas);
        addScaledSpawn(BiomeSelectors.includeByKey(Biomes.CRIMSON_FOREST), ModEntities.BLAZE_SLIME, nether.blazeSlimeCrimsonForest);
        addScaledSpawn(BiomeSelectors.includeByKey(Biomes.SOUL_SAND_VALLEY), ModEntities.WITHER_SLIME, nether.witherSlimeSoulSandValley);
        addScaledSpawn(BiomeSelectors.includeByKey(Biomes.WARPED_FOREST), ModEntities.ENDER_SLIME, nether.enderSlimeWarpedForest);
    }

    private static Set<ResourceKey<Biome>> parseBiomeKeys(List<String> idStrings) {
        Set<ResourceKey<Biome>> keys = new HashSet<>();
        for (String idStr : idStrings) {
            ResourceLocation loc = ResourceLocation.tryParse(idStr);
            if (loc != null) {
                keys.add(ResourceKey.create(Registries.BIOME, loc));
            }
        }
        return keys;
    }

    private static <T extends Slime> void registerOverworldSpawnPlacement(EntityType<T> entityType) {
        SpawnPlacements.register(
                entityType,
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                ModEntitySpawns::checkOverworldSlimeSpawnRules
        );
    }

    private static <T extends Slime> void registerNetherHybridSpawnPlacement(EntityType<T> entityType) {
        SpawnPlacements.register(
                entityType,
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                ModEntitySpawns::checkNetherDarkOrMagmaSlimeRules
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

    // --- 中サイズスライムのスタッキング（段積み）処理 ---

    private static Set<EntityType<?>> getStackableSlimeTypes() {
        return Set.of(
                EntityType.SLIME,
                ModEntities.WITCH_SLIME,
                ModEntities.STONECUTTER_SLIME,
                ModEntities.PUFFERFISH_SLIME,
                ModEntities.RABBIT_SLIME,
                ModEntities.BOMB_SLIME,
                ModEntities.BLAZE_SLIME,
                ModEntities.WITHER_SLIME,
                ModEntities.ENDER_SLIME
        );
    }

    // 頭上にパーツがついているため最上段にのみ配置するスライム
    private static Set<EntityType<?>> getTopOnlySlimeTypes() {
        return Set.of(
                ModEntities.WITCH_SLIME,
                ModEntities.STONECUTTER_SLIME,
                ModEntities.RABBIT_SLIME
        );
    }

    public static boolean isStackableSlimeType(EntityType<?> type) {
        return getStackableSlimeTypes().contains(type);
    }

    public static boolean isTopOnlySlimeType(EntityType<?> type) {
        return getTopOnlySlimeTypes().contains(type);
    }

    /**
     * 2体のエンティティが同じ騎乗スタック（乗っている・乗られている関係）に属しているか判定する
     */
    public static boolean isSameSlimeStack(@Nullable Entity first, @Nullable Entity second) {
        if (first == null || second == null || first == second) {
            return false;
        }
        return first.isPassengerOfSameVehicle(second);
    }

    /**
     * 自然スポーン時、十分なスペースがあれば中サイズのスライム2～3匹を積み重ねてスポーンさせる
     * （WitchSlime, StonecutterSlime, RabbitSlime は必ず最上段に配置）
     */
    public static void tryApplySlimeStack(Slime baseSlime, ServerLevelAccessor levelAccessor, MobSpawnType spawnType) {
        if (IS_BUILDING_STACK.get()) {
            return;
        }
        if (spawnType != MobSpawnType.NATURAL && spawnType != MobSpawnType.CHUNK_GENERATION) {
            return;
        }
        if (!isStackableSlimeType(baseSlime.getType()) || !(levelAccessor instanceof ServerLevel serverLevel)) {
            return;
        }

        SlimeAriseConfig.StackingSettings stackConfig = SlimeAriseConfig.get().stacking;
        if (stackConfig.stackSpawnChance <= 0.0D || stackConfig.maxStackCount < 2) {
            return;
        }

        RandomSource random = serverLevel.getRandom();
        if (stackConfig.stackSpawnChance < 1.0D && random.nextDouble() >= stackConfig.stackSpawnChance) {
            return;
        }

        int minCount = Math.max(2, stackConfig.minStackCount);
        int maxCount = Math.max(minCount, stackConfig.maxStackCount);
        int stackSize = Math.max(1, stackConfig.stackSlimeSize);
        double extraChance = stackConfig.additionalStackChance != null ? stackConfig.additionalStackChance : 0.15D;

        // 最低段数(minCount)から開始し、1段増えるごとに additionalStackChance の確率で段数を増やす
        int desiredCount = minCount;
        while (desiredCount < maxCount && random.nextDouble() < extraChance) {
            desiredCount++;
        }

        int actualCount = findAvailableStackCount(serverLevel, baseSlime.getX(), baseSlime.getY(), baseSlime.getZ(), desiredCount, stackSize);
        if (actualCount < minCount) {
            return;
        }

        BlockPos spawnPos = baseSlime.blockPosition();
        boolean baseIsTopOnly = isTopOnlySlimeType(baseSlime.getType());
        double stepHeight = 0.52D * stackSize;

        // 下段（0 ～ actualCount - 2）に配置する「頭が平らなスライム」の種類を先に抽選
        List<EntityType<? extends Slime>> lowerTypes = new ArrayList<>();
        if (!baseIsTopOnly) {
            lowerTypes.add(baseSlime.getType());
        }
        while (lowerTypes.size() < actualCount - 1) {
            EntityType<? extends Slime> flatType = pickRandomSpawnableSlimeType(
                    serverLevel, spawnType, spawnPos, random, false
            );
            if (flatType == null) {
                return;
            }
            lowerTypes.add(flatType);
        }

        IS_BUILDING_STACK.set(true);
        try {
            if (!baseIsTopOnly) {
                // ケース1: baseSlime が頭が平らなスライム -> baseSlime を最下段にし、上に積む
                baseSlime.setSize(stackSize, true);
                Slime currentVehicle = baseSlime;

                for (int i = 1; i < lowerTypes.size(); i++) {
                    Slime middle = createStackedSlime(
                            serverLevel, lowerTypes.get(i),
                            baseSlime.getX(), baseSlime.getY() + stepHeight * i, baseSlime.getZ(),
                            spawnPos, spawnType, random, stackSize
                    );
                    if (middle == null) {
                        return;
                    }
                    middle.startRiding(currentVehicle, true);
                    currentVehicle = middle;
                }

                EntityType<? extends Slime> topType = pickRandomSpawnableSlimeType(
                        serverLevel, spawnType, spawnPos, random, true
                );
                if (topType == null) {
                    topType = baseSlime.getType();
                }

                int topIndex = actualCount - 1;
                Slime topSlime = createStackedSlime(
                        serverLevel, topType,
                        baseSlime.getX(), baseSlime.getY() + stepHeight * topIndex, baseSlime.getZ(),
                        spawnPos, spawnType, random, stackSize
                );
                if (topSlime != null) {
                    topSlime.startRiding(currentVehicle, true);
                }
            } else {
                // ケース2: baseSlime 自体が Witch / Stonecutter / Rabbit -> 下段を新規生成して先にワールドへ追加し、baseSlime を最上段に乗せる
                double baseX = baseSlime.getX();
                double baseY = baseSlime.getY();
                double baseZ = baseSlime.getZ();

                List<Slime> createdLowerSlimes = new ArrayList<>();
                Slime currentVehicle = null;

                for (int i = 0; i < lowerTypes.size(); i++) {
                    Slime lowerSlime = createStackedSlime(
                            serverLevel, lowerTypes.get(i),
                            baseX, baseY + stepHeight * i, baseZ,
                            spawnPos, spawnType, random, stackSize
                    );
                    if (lowerSlime == null) {
                        return;
                    }
                    if (currentVehicle != null) {
                        lowerSlime.startRiding(currentVehicle, true);
                    }
                    createdLowerSlimes.add(lowerSlime);
                    currentVehicle = lowerSlime;
                }

                // クライアントへの騎乗パケット同期がずれないよう、必ず先に下段をワールドへ登録してから ride する
                for (Slime lowerSlime : createdLowerSlimes) {
                    serverLevel.addFreshEntity(lowerSlime);
                }

                if (currentVehicle != null) {
                    int topIndex = actualCount - 1;
                    baseSlime.setSize(stackSize, true);
                    baseSlime.moveTo(baseX, baseY + stepHeight * topIndex, baseZ, baseSlime.getYRot(), baseSlime.getXRot());
                    baseSlime.startRiding(currentVehicle, true);
                }
            }
        } finally {
            IS_BUILDING_STACK.set(false);
        }
    }

    @Nullable
    private static Slime createStackedSlime(
            ServerLevel level,
            EntityType<? extends Slime> type,
            double x, double y, double z,
            BlockPos spawnPos,
            MobSpawnType spawnType,
            RandomSource random,
            int size
    ) {
        Slime slime = type.create(level);
        if (slime == null) {
            return null;
        }
        slime.moveTo(x, y, z, random.nextFloat() * 360.0F, 0.0F);
        slime.finalizeSpawn(level, level.getCurrentDifficultyAt(spawnPos), spawnType, null);
        slime.setSize(size, true);
        return slime;
    }

    private static int findAvailableStackCount(ServerLevel level, double x, double y, double z, int desiredCount, int stackSize) {
        double halfWidth = 0.26D * stackSize;
        double unitHeight = 0.525D * stackSize;
        int minCount = Math.max(2, SlimeAriseConfig.get().stacking.minStackCount);

        for (int count = desiredCount; count >= minCount; count--) {
            AABB stackBox = new AABB(
                    x - halfWidth, y, z - halfWidth,
                    x + halfWidth, y + unitHeight * count, z + halfWidth
            );
            if (level.noCollision(stackBox) && !level.containsAnyLiquid(stackBox)) {
                return count;
            }
        }
        return 1;
    }

    @Nullable
    @SuppressWarnings("unchecked")
    private static EntityType<? extends Slime> pickRandomSpawnableSlimeType(
            ServerLevel level,
            MobSpawnType spawnType,
            BlockPos pos,
            RandomSource random,
            boolean allowTopOnly
    ) {
        Holder<Biome> biome = level.getBiome(pos);
        WeightedRandomList<MobSpawnSettings.SpawnerData> spawnerList =
                NaturalSpawnerAccessor.slimearisemod$invokeMobsAt(
                        level,
                        level.structureManager(),
                        level.getChunkSource().getGenerator(),
                        MobCategory.MONSTER,
                        pos,
                        biome
                );

        List<MobSpawnSettings.SpawnerData> validCandidates = new ArrayList<>();
        int totalWeight = 0;

        for (MobSpawnSettings.SpawnerData data : spawnerList.unwrap()) {
            if (!isStackableSlimeType(data.type)) {
                continue;
            }
            if (!allowTopOnly && isTopOnlySlimeType(data.type)) {
                continue;
            }
            if (SpawnPlacements.checkSpawnRules(data.type, level, spawnType, pos, random)) {
                validCandidates.add(data);
                totalWeight += data.getWeight().asInt();
            }
        }

        if (validCandidates.isEmpty() || totalWeight <= 0) {
            return null;
        }

        int roll = random.nextInt(totalWeight);
        for (MobSpawnSettings.SpawnerData candidate : validCandidates) {
            roll -= candidate.getWeight().asInt();
            if (roll < 0) {
                return (EntityType<? extends Slime>) candidate.type;
            }
        }

        return (EntityType<? extends Slime>) validCandidates.getLast().type;
    }
}