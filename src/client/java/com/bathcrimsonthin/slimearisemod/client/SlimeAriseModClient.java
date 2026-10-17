package com.bathcrimsonthin.slimearisemod.client;

import com.bathcrimsonthin.slimearisemod.client.particle.BlazeSlimeParticle;
import com.bathcrimsonthin.slimearisemod.client.renderer.BlazeSlimeRenderer;
import com.bathcrimsonthin.slimearisemod.entity.ModEntities;
import com.bathcrimsonthin.slimearisemod.item.ModItems;
import com.bathcrimsonthin.slimearisemod.particle.ModParticles;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.particle.BreakingItemParticle;
import net.minecraft.world.item.ItemStack;

public class SlimeAriseModClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// モブに描画用レンダラーを紐付ける
		EntityRendererRegistry.register(ModEntities.BLAZE_SLIME, BlazeSlimeRenderer::new);

		// パーティクルのファクトリ登録
		ParticleFactoryRegistry.getInstance().register(
				ModParticles.BLAZE_SLIME_PARTICLE,
				new BlazeSlimeParticle.Provider()
		);
	}
}