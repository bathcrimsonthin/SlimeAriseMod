package com.bathcrimsonthin.slimearisemod.client;

import com.bathcrimsonthin.slimearisemod.SlimeAriseMod;
import com.bathcrimsonthin.slimearisemod.block.ModBlocks;
import com.bathcrimsonthin.slimearisemod.client.model.*;
import com.bathcrimsonthin.slimearisemod.client.particle.BlazeSlimeParticle;
import com.bathcrimsonthin.slimearisemod.client.renderer.*;
import com.bathcrimsonthin.slimearisemod.entity.ModEntities;
import com.bathcrimsonthin.slimearisemod.particle.ModParticles;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.resources.ResourceLocation;

public class SlimeAriseModClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// モブ・投擲物に描画用レンダラーを紐付ける
		EntityRendererRegistry.register(ModEntities.BLAZE_SLIME, BlazeSlimeRenderer::new);
		EntityRendererRegistry.register(ModEntities.RABBIT_SLIME, RabbitSlimeRenderer::new);
		EntityRendererRegistry.register(ModEntities.BOMB_SLIME, BombSlimeRenderer::new);
		EntityRendererRegistry.register(ModEntities.WITCH_SLIME, WitchSlimeRenderer::new);
		EntityRendererRegistry.register(ModEntities.ENDER_SLIME, EnderSlimeRenderer::new);
		EntityRendererRegistry.register(ModEntities.PUFFERFISH_SLIME, PufferfishSlimeRenderer::new);
		EntityRendererRegistry.register(ModEntities.ENDER_SLIME_BALL, ThrownItemRenderer::new);
		EntityRendererRegistry.register(ModEntities.STONECUTTER_SLIME, StonecutterSlimeRenderer::new);
		EntityRendererRegistry.register(ModEntities.WITHER_SLIME, WitherSlimeRenderer::new);
		EntityRendererRegistry.register(ModEntities.KING_SLIME, KingSlimeRenderer::new);
		EntityRendererRegistry.register(ModEntities.SLIME_FIELD, SlimeFieldRenderer::new);
		EntityRendererRegistry.register(ModEntities.SLIME_ARROW, SlimeArrowRenderer::new);
		EntityRendererRegistry.register(ModEntities.STICKY_TORCH_PROJECTILE, ThrownItemRenderer::new);
		EntityRendererRegistry.register(ModEntities.SLIME_BOTTLE_PROJECTILE, ThrownItemRenderer::new);
		// パーティクルのファクトリ登録
		ParticleFactoryRegistry.getInstance().register(
				ModParticles.BLAZE_SLIME_PARTICLE,
				new BlazeSlimeParticle.Provider()
		);

		// モデルのレイヤー登録
		EntityModelLayerRegistry.registerModelLayer(ModModelLayers.RABBIT_SLIME, RabbitSlimeModel::createInnerBodyLayer);
		EntityModelLayerRegistry.registerModelLayer(ModModelLayers.RABBIT_SLIME_OUTER, RabbitSlimeModel::createOuterBodyLayer);
		EntityModelLayerRegistry.registerModelLayer(ModModelLayers.WITCH_SLIME_INNER, WitchSlimeModel::createInnerBodyLayer);
		EntityModelLayerRegistry.registerModelLayer(ModModelLayers.WITCH_SLIME_OUTER, WitchSlimeModel::createOuterBodyLayer);
		EntityModelLayerRegistry.registerModelLayer(EnderSlimeModel.LAYER_LOCATION, EnderSlimeModel::createBodyLayer);
		EntityModelLayerRegistry.registerModelLayer(
				PufferfishSlimeRenderer.INNER_LAYER,
				PufferfishSlimeRenderer::createInnerBodyLayer
		);
		EntityModelLayerRegistry.registerModelLayer(StonecutterSlimeModel.INNER_LAYER, StonecutterSlimeModel::createInnerBodyLayer);
		EntityModelLayerRegistry.registerModelLayer(StonecutterSlimeModel.OUTER_LAYER, StonecutterSlimeModel::createOuterBodyLayer);
		EntityModelLayerRegistry.registerModelLayer(WitherSlimeModel.INNER_LAYER, WitherSlimeModel::createInnerBodyLayer);
		EntityModelLayerRegistry.registerModelLayer(WitherSlimeModel.OUTER_LAYER, WitherSlimeModel::createOuterBodyLayer);
		EntityModelLayerRegistry.registerModelLayer(WitherSlimeModel.ARMOR_LAYER, WitherSlimeModel::createArmorLayer);
		EntityModelLayerRegistry.registerModelLayer(KingSlimeModel.INNER_LAYER, KingSlimeModel::createInnerBodyLayer);
		EntityModelLayerRegistry.registerModelLayer(KingSlimeModel.OUTER_LAYER, KingSlimeModel::createOuterBodyLayer);
		// 粘着たいまつの炎・透過部分を正しく描画する
		BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.STICKY_TORCH, RenderType.cutout());
		BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.SLIME_CUSHION, RenderType.translucent());
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