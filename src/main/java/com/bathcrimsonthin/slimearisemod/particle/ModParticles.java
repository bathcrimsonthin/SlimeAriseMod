package com.bathcrimsonthin.slimearisemod.particle;

import com.bathcrimsonthin.slimearisemod.SlimeAriseMod;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

public class ModParticles {

    public static final SimpleParticleType BLAZE_SLIME_PARTICLE = FabricParticleTypes.simple();

    public static void registerModParticles() {
        Registry.register(
                BuiltInRegistries.PARTICLE_TYPE,
                ResourceLocation.fromNamespaceAndPath(SlimeAriseMod.MOD_ID, "blaze_slime_particle"),
                BLAZE_SLIME_PARTICLE
        );
    }
}
