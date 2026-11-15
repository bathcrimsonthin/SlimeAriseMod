package com.bathcrimsonthin.slimearisemod.mixin;

import com.bathcrimsonthin.slimearisemod.entity.ModEntitySpawns;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Slime.class)
public abstract class SlimeSpawnMixin {

    @Inject(method = "checkSlimeSpawnRules", at = @At("HEAD"), cancellable = true)
    private static void slimearisemod$spawnInDarkLikeZombie(EntityType<Slime> slime,
                                                            LevelAccessor level,
                                                            MobSpawnType spawnType,
                                                            BlockPos pos,
                                                            RandomSource random,
                                                            CallbackInfoReturnable<Boolean> cir) {
        if (slime == EntityType.SLIME && level instanceof ServerLevelAccessor serverLevel) {
            cir.setReturnValue(ModEntitySpawns.checkSlimeDarkSpawnRules(slime, serverLevel, spawnType, pos, random));
        }
    }
}