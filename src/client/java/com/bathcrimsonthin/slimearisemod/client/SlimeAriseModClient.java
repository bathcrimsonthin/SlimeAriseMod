package com.bathcrimsonthin.slimearisemod.client;

import com.bathcrimsonthin.slimearisemod.SlimeAriseMod;
import com.bathcrimsonthin.slimearisemod.client.model.RabbitSlimeModel;
import com.bathcrimsonthin.slimearisemod.client.model.WitchSlimeModel;
import com.bathcrimsonthin.slimearisemod.client.particle.BlazeSlimeParticle;
import com.bathcrimsonthin.slimearisemod.client.renderer.BlazeSlimeRenderer;
import com.bathcrimsonthin.slimearisemod.client.renderer.BombSlimeRenderer;
import com.bathcrimsonthin.slimearisemod.client.renderer.RabbitSlimeRenderer;
import com.bathcrimsonthin.slimearisemod.client.renderer.WitchSlimeRenderer;
import com.bathcrimsonthin.slimearisemod.entity.ModEntities;
import com.bathcrimsonthin.slimearisemod.item.ModItems;
import com.bathcrimsonthin.slimearisemod.particle.ModParticles;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.particle.BreakingItemParticle;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class SlimeAriseModClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// モブに描画用レンダラーを紐付ける
		EntityRendererRegistry.register(ModEntities.BLAZE_SLIME, BlazeSlimeRenderer::new);
		EntityRendererRegistry.register(ModEntities.RABBIT_SLIME, RabbitSlimeRenderer::new);
		EntityRendererRegistry.register(ModEntities.BOMB_SLIME, BombSlimeRenderer::new);
		EntityRendererRegistry.register(ModEntities.WITCH_SLIME, WitchSlimeRenderer::new);

		// パーティクルのファクトリ登録
		ParticleFactoryRegistry.getInstance().register(
				ModParticles.BLAZE_SLIME_PARTICLE,
				new BlazeSlimeParticle.Provider()
		);

		// モデルのレイヤー登録
		EntityModelLayerRegistry.registerModelLayer(ModModelLayers.RABBIT_SLIME, RabbitSlimeModel::createInnerBodyLayer);
		EntityModelLayerRegistry.registerModelLayer(ModModelLayers.RABBIT_SLIME_OUTER, RabbitSlimeModel::createOuterBodyLayer);
		EntityRendererRegistry.register(ModEntities.RABBIT_SLIME, RabbitSlimeRenderer::new);
		// モデルレイヤーの登録
		EntityModelLayerRegistry.registerModelLayer(ModModelLayers.WITCH_SLIME_INNER, WitchSlimeModel::createInnerBodyLayer);
		EntityModelLayerRegistry.registerModelLayer(ModModelLayers.WITCH_SLIME_OUTER, WitchSlimeModel::createOuterBodyLayer);
	}

	public static class ModModelLayers {
		public static final ModelLayerLocation RABBIT_SLIME = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(SlimeAriseMod.MOD_ID, "rabbit_slime"), "main");
		public static final ModelLayerLocation RABBIT_SLIME_OUTER = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(SlimeAriseMod.MOD_ID, "rabbit_slime"), "outer");

		public static final ModelLayerLocation WITCH_SLIME_INNER =
				new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(SlimeAriseMod.MOD_ID, "witch_slime"), "inner");
		public static final ModelLayerLocation WITCH_SLIME_OUTER =
				new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(SlimeAriseMod.MOD_ID, "witch_slime"), "outer");
	}
}