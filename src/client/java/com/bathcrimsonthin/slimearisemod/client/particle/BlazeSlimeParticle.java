package com.bathcrimsonthin.slimearisemod.client.particle;

import com.bathcrimsonthin.slimearisemod.item.ModItems;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.BreakingItemParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

public class BlazeSlimeParticle extends BreakingItemParticle {

    // package-private なコンストラクタを模倣
    public BlazeSlimeParticle(ClientLevel clientLevel, double d, double e, double f, double g, double h, double i, ItemStack itemStack) {
        this(clientLevel, d, e, f, itemStack);
        this.xd *= (double)0.1F;
        this.yd *= (double)0.1F;
        this.zd *= (double)0.1F;
        this.xd += g;
        this.yd += h;
        this.zd += i;
    }

    protected BlazeSlimeParticle(ClientLevel clientLevel, double d, double e, double f, ItemStack itemStack) {
        super(clientLevel, d, e, f, itemStack);
    }

    // 描画プロバイダ
    @Environment(value=EnvType.CLIENT)
    public static class Provider
            implements ParticleProvider<SimpleParticleType> {
        @Override
        public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
            return new BlazeSlimeParticle(clientLevel, d, e, f, new ItemStack((ItemLike) ModItems.BLAZE_SLIME_BALL));
        }
    }
}
