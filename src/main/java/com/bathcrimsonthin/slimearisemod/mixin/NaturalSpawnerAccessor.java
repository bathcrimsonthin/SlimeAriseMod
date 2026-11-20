package com.bathcrimsonthin.slimearisemod.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.chunk.ChunkGenerator;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(NaturalSpawner.class)
public interface NaturalSpawnerAccessor {

    @Invoker("mobsAt")
    static WeightedRandomList<MobSpawnSettings.SpawnerData> slimearisemod$invokeMobsAt(
            ServerLevel level,
            StructureManager structureManager,
            ChunkGenerator generator,
            MobCategory category,
            BlockPos pos,
            @Nullable Holder<Biome> biome
    ) {
        throw new AssertionError();
    }
}