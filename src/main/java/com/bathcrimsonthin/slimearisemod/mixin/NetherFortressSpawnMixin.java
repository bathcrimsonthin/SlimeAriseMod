package com.bathcrimsonthin.slimearisemod.mixin;

import com.bathcrimsonthin.slimearisemod.entity.ModEntities;
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
import net.minecraft.world.level.levelgen.structure.structures.NetherFortressStructure;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(NaturalSpawner.class)
public abstract class NetherFortressSpawnMixin {

    @Unique
    private static WeightedRandomList<MobSpawnSettings.SpawnerData> slimearisemod$fortressEnemies = null;

    @Inject(
            method = "mobsAt(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/level/StructureManager;Lnet/minecraft/world/level/chunk/ChunkGenerator;Lnet/minecraft/world/entity/MobCategory;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Holder;)Lnet/minecraft/util/random/WeightedRandomList;",
            at = @At("RETURN"),
            cancellable = true
    )
    private static void slimearisemod$addFortressSlimes(ServerLevel level,
                                                        StructureManager structureManager,
                                                        ChunkGenerator generator,
                                                        MobCategory category,
                                                        BlockPos pos,
                                                        @Nullable Holder<Biome> biome,
                                                        CallbackInfoReturnable<WeightedRandomList<MobSpawnSettings.SpawnerData>> cir) {
        if (category == MobCategory.MONSTER && cir.getReturnValue() == NetherFortressStructure.FORTRESS_ENEMIES) {
            if (slimearisemod$fortressEnemies == null) {
                List<MobSpawnSettings.SpawnerData> entries = new ArrayList<>(NetherFortressStructure.FORTRESS_ENEMIES.unwrap());
                entries.add(new MobSpawnSettings.SpawnerData(ModEntities.BLAZE_SLIME, 10, 2, 3));
                entries.add(new MobSpawnSettings.SpawnerData(ModEntities.WITHER_SLIME, 10, 2, 4));
                slimearisemod$fortressEnemies = WeightedRandomList.create(entries);
            }
            cir.setReturnValue(slimearisemod$fortressEnemies);
        }
    }
}