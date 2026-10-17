package com.bathcrimsonthin.slimearisemod.entity;

import com.bathcrimsonthin.slimearisemod.particle.ModParticles;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class BlazeSlimeEntity extends Slime {
    public BlazeSlimeEntity(EntityType<? extends Slime> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes(){
        return Monster.createMonsterAttributes();
    }

    @Override
    public void setSize(int size, boolean resetHealth) {
        super.setSize(size, resetHealth);
        // ヒットボックスの寸法を即座に最新の size に合わせて再計算する
        this.refreshDimensions();
    }

    @Override
    protected @NotNull ParticleOptions getParticleType() {
        return ModParticles.BLAZE_SLIME_PARTICLE;
    }
}
