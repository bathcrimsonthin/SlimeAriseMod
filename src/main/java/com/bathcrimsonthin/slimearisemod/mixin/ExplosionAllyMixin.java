package com.bathcrimsonthin.slimearisemod.mixin;

import com.bathcrimsonthin.slimearisemod.entity.AlliedSlimeHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ServerLevel.class)
public abstract class ExplosionAllyMixin {

    @ModifyVariable(
            method = "explode(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/damagesource/DamageSource;Lnet/minecraft/world/level/ExplosionDamageCalculator;DDDFZLnet/minecraft/world/level/Level$ExplosionInteraction;Lnet/minecraft/core/particles/ParticleOptions;Lnet/minecraft/core/particles/ParticleOptions;Lnet/minecraft/core/Holder;)Lnet/minecraft/world/level/Explosion;",
            at = @At("HEAD"),
            argsOnly = true
    )
    private Level.ExplosionInteraction slimearisemod$preventAlliedBombBlockDamage(
            Level.ExplosionInteraction interaction,
            @Nullable Entity source
    ) {
        if (source != null && AlliedSlimeHelper.isAlliedSlime(source)) {
            return Level.ExplosionInteraction.NONE;
        }
        return interaction;
    }
}