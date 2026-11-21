package com.bathcrimsonthin.slimearisemod.config;

import com.bathcrimsonthin.slimearisemod.SlimeAriseMod;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class SlimeAriseConfig {

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();

    private static final Path CONFIG_PATH = FabricLoader.getInstance()
            .getConfigDir()
            .resolve(SlimeAriseMod.MOD_ID + ".json");

    private static SlimeAriseConfig instance = new SlimeAriseConfig();

    // --- 1. ディメンション別の湧き方モード ---

    public enum OverworldSpawnMode {
        /** 暗ければどこでも湧く（デフォルト・現状維持） */
        DARK_EVERYWHERE,
        /** スライムチャンクか特定バイオーム（湿地・マングローブの沼地）のみに湧く（バニラ準拠） */
        VANILLA,
        /** 湧かない */
        DISABLED
    }

    public enum NetherSpawnMode {
        /** 特定のバイオーム及びネザー要塞に湧く（デフォルト・現状維持） */
        BIOME_AND_FORTRESS,
        /** ネザー全域でマグマキューブと同じ湧き方をする */
        MAGMA_CUBE_LIKE,
        /** 湧かない */
        DISABLED
    }

    public enum EndSpawnMode {
        /** 湧く（デフォルト・現状維持） */
        ENABLED,
        /** 湧かない */
        DISABLED
    }

    public enum PufferfishSlimeAttackMode {
        /** プレイヤーおよび周囲のモブを無差別に攻撃する */
        INDISCRIMINATE,
        /** クリーパーやボムスライムなど爆発するモブを除いて攻撃する */
        EXCLUDE_EXPLOSIVE,
        /** 他のモブを攻撃しない（プレイヤーのみ攻撃する） */
        NO_MOBS
    }

    public String _comment_modes = "overworldSpawnMode: DARK_EVERYWHERE / VANILLA / DISABLED | netherSpawnMode: BIOME_AND_FORTRESS / MAGMA_CUBE_LIKE / DISABLED | endSpawnMode: ENABLED / DISABLED";
    public OverworldSpawnMode overworldSpawnMode = OverworldSpawnMode.DARK_EVERYWHERE;
    public NetherSpawnMode netherSpawnMode = NetherSpawnMode.BIOME_AND_FORTRESS;
    public EndSpawnMode endSpawnMode = EndSpawnMode.ENABLED;

    // --- 2. 全体倍率・環境・バイオーム制限 ---

    public double globalSpawnWeightScale = 0.75D;
    public int overworldMinY = -64;
    public int overworldMaxY = 320;
    public int darkSpawnMaxLightLevel = 0;

    public List<String> allowedOverworldDimensions = new ArrayList<>(List.of(
            "minecraft:overworld"
    ));

    public List<String> blacklistedBiomes = new ArrayList<>(List.of(
            "minecraft:mushroom_fields",
            "minecraft:deep_dark"
    ));

    public List<String> whitelistedOverworldBiomes = new ArrayList<>();

    // --- 3. 中サイズスライムのスタッキング（段積み）設定 ---

    public StackingSettings stacking = new StackingSettings();

    public static class StackingSettings {
        /** 積み重なってスポーンする確率 (0.0 ~ 1.0 / デフォルト 0.08 = 8%で2段以上、92%は1段) */
        public double stackSpawnChance = 0.08D;
        /** 2段になった後、さらに3段目以降が追加で積まれる確率 (デフォルト 0.15 = 全体で二段6.8%・三段1.2%) */
        public Double additionalStackChance = 0.15D;
        public int minStackCount = 2;
        public int maxStackCount = 3;
        public int stackSlimeSize = 2;
    }

    // --- 4. Mod固有ギミックのON/OFF・挙動設定 ---

    public BehaviorSettings behavior = new BehaviorSettings();

    public static class BehaviorSettings {
        /** エンダースライムがブロックを持ち去り・設置するか */
        public boolean allowEnderSlimeBlockGriefing = true;

        public String _comment_pufferfishAttackMode = "pufferfishSlimeAttackMode: INDISCRIMINATE / EXCLUDE_EXPLOSIVE / NO_MOBS";
        /** フグスライムの対モブ攻撃モード */
        public PufferfishSlimeAttackMode pufferfishSlimeAttackMode = PufferfishSlimeAttackMode.EXCLUDE_EXPLOSIVE;
    }

    // --- 5. スライム種類別の湧き率（weight=0 で個別無効化） ---

    public String _comment_spawns = "Setting weight to 0 will disable spawning of that specific slime.";
    public OverworldSpawns overworld = new OverworldSpawns();
    public NetherBiomeSpawns netherBiomeAndFortress = new NetherBiomeSpawns();
    public NetherMagmaCubeLikeSpawns netherMagmaCubeLike = new NetherMagmaCubeLikeSpawns();
    public EndSpawns end = new EndSpawns();

    public static class SpawnEntry {
        public int weight;
        public int minGroupSize;
        public int maxGroupSize;

        public SpawnEntry() {
        }

        public SpawnEntry(int weight, int minGroupSize, int maxGroupSize) {
            this.weight = weight;
            this.minGroupSize = minGroupSize;
            this.maxGroupSize = maxGroupSize;
        }
    }

    public static class OverworldSpawns {
        public SpawnEntry vanillaSlime = new SpawnEntry(100, 4, 4);
        public SpawnEntry witchSlime = new SpawnEntry(60, 4, 4);
        public SpawnEntry stonecutterSlime = new SpawnEntry(60, 4, 4);
        public SpawnEntry pufferfishSlime = new SpawnEntry(60, 4, 4);
        public SpawnEntry rabbitSlime = new SpawnEntry(60, 4, 4);
        public SpawnEntry bombSlime = new SpawnEntry(60, 4, 4);
        public SpawnEntry enderSlime = new SpawnEntry(60, 4, 4);
    }

    public static class NetherBiomeSpawns {
        public SpawnEntry blazeSlimeNetherWastes = new SpawnEntry(35, 2, 3);
        public SpawnEntry blazeSlimeBasaltDeltas = new SpawnEntry(35, 2, 3);
        public SpawnEntry blazeSlimeCrimsonForest = new SpawnEntry(10, 2, 3);
        public SpawnEntry blazeSlimeFortress = new SpawnEntry(10, 2, 3);
        public SpawnEntry witherSlimeSoulSandValley = new SpawnEntry(30, 2, 4);
        public SpawnEntry witherSlimeFortress = new SpawnEntry(10, 2, 4);
        public SpawnEntry enderSlimeWarpedForest = new SpawnEntry(1, 4, 4);
    }

    public static class NetherMagmaCubeLikeSpawns {
        public SpawnEntry blazeSlime = new SpawnEntry(15, 2, 4);
        public SpawnEntry witherSlime = new SpawnEntry(10, 2, 4);
        public SpawnEntry enderSlime = new SpawnEntry(5, 2, 4);
    }

    public static class EndSpawns {
        public SpawnEntry enderSlime = new SpawnEntry(10, 4, 4);
    }

    // --- 読み込み・保存・バリデーション ---

    public static SlimeAriseConfig get() {
        return instance;
    }

    public static void load() {
        if (Files.exists(CONFIG_PATH)) {
            try (Reader reader = Files.newBufferedReader(CONFIG_PATH, StandardCharsets.UTF_8)) {
                SlimeAriseConfig loaded = GSON.fromJson(reader, SlimeAriseConfig.class);
                if (loaded != null) {
                    instance = loaded;
                }
            } catch (IOException | JsonParseException e) {
                SlimeAriseMod.LOGGER.error("Failed to load config file {}, using defaults.", CONFIG_PATH, e);
                instance = new SlimeAriseConfig();
            }
        } else {
            instance = new SlimeAriseConfig();
        }

        instance.sanitize();
        save();
    }

    public static void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(CONFIG_PATH, StandardCharsets.UTF_8)) {
                GSON.toJson(instance, writer);
            }
        } catch (IOException e) {
            SlimeAriseMod.LOGGER.error("Failed to save config file {}", CONFIG_PATH, e);
        }
    }

    private void sanitize() {
        if (overworldSpawnMode == null) overworldSpawnMode = OverworldSpawnMode.DARK_EVERYWHERE;
        if (netherSpawnMode == null) netherSpawnMode = NetherSpawnMode.BIOME_AND_FORTRESS;
        if (endSpawnMode == null) endSpawnMode = EndSpawnMode.ENABLED;

        if (globalSpawnWeightScale < 0.0D) globalSpawnWeightScale = 0.0D;
        if (overworldMinY > overworldMaxY) {
            int tmp = overworldMinY;
            overworldMinY = overworldMaxY;
            overworldMaxY = tmp;
        }
        darkSpawnMaxLightLevel = Math.max(0, Math.min(15, darkSpawnMaxLightLevel));

        if (allowedOverworldDimensions == null) allowedOverworldDimensions = new ArrayList<>();
        if (blacklistedBiomes == null) blacklistedBiomes = new ArrayList<>();
        if (whitelistedOverworldBiomes == null) whitelistedOverworldBiomes = new ArrayList<>();

        if (stacking == null) stacking = new StackingSettings();
        stacking.stackSpawnChance = Math.max(0.0D, Math.min(1.0D, stacking.stackSpawnChance));
        if (stacking.additionalStackChance == null) {
            stacking.additionalStackChance = 0.15D;
        }
        stacking.additionalStackChance = Math.max(0.0D, Math.min(1.0D, stacking.additionalStackChance));
        stacking.minStackCount = Math.max(2, stacking.minStackCount);
        stacking.maxStackCount = Math.max(stacking.minStackCount, stacking.maxStackCount);
        stacking.stackSlimeSize = Math.max(1, stacking.stackSlimeSize);

        if (behavior == null) behavior = new BehaviorSettings();
        if (behavior.pufferfishSlimeAttackMode == null) {
            behavior.pufferfishSlimeAttackMode = PufferfishSlimeAttackMode.EXCLUDE_EXPLOSIVE;
        }

        if (overworld == null) overworld = new OverworldSpawns();
        if (netherBiomeAndFortress == null) netherBiomeAndFortress = new NetherBiomeSpawns();
        if (netherMagmaCubeLike == null) netherMagmaCubeLike = new NetherMagmaCubeLikeSpawns();
        if (end == null) end = new EndSpawns();
    }
}