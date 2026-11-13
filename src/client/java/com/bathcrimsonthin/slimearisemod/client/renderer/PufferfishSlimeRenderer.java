package com.bathcrimsonthin.slimearisemod.client.renderer;

import com.bathcrimsonthin.slimearisemod.SlimeAriseMod;
import com.bathcrimsonthin.slimearisemod.client.layer.PufferfishSlimeRiderLayer;
import com.bathcrimsonthin.slimearisemod.entity.PufferfishSlimeEntity;
import net.minecraft.client.model.SlimeModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.SlimeOuterLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import com.mojang.blaze3d.vertex.PoseStack;

public class PufferfishSlimeRenderer extends MobRenderer<PufferfishSlimeEntity, SlimeModel<PufferfishSlimeEntity>> {

    private static final ResourceLocation SLIME_LOCATION =
            ResourceLocation.fromNamespaceAndPath(SlimeAriseMod.MOD_ID, "textures/entity/pufferfish_slime.png");

    public static final ModelLayerLocation INNER_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(SlimeAriseMod.MOD_ID, "pufferfish_slime"), "main");

    public PufferfishSlimeRenderer(EntityRendererProvider.Context context) {
        super(context, new SlimeModel<>(context.bakeLayer(INNER_LAYER)), 0.25F);
        // 1. コアサイズのフグを先に描画
        this.addLayer(new PufferfishSlimeRiderLayer(this, context.getEntityRenderDispatcher()));
        // 2. その上から半透明の外側粘膜を描画
        this.addLayer(new SlimeOuterLayer<>(this, context.getModelSet()));
    }

    public static LayerDefinition createInnerBodyLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();

        // 不透明な緑のコアは描画せず、目と口のみ配置（コアはフグが担う）
        partDefinition.addOrReplaceChild("cube", CubeListBuilder.create(), PartPose.ZERO);
        partDefinition.addOrReplaceChild("right_eye", CubeListBuilder.create().texOffs(32, 0).addBox(-3.25F, 18.0F, -3.5F, 2.0F, 2.0F, 2.0F), PartPose.ZERO);
        partDefinition.addOrReplaceChild("left_eye", CubeListBuilder.create().texOffs(32, 4).addBox(1.25F, 18.0F, -3.5F, 2.0F, 2.0F, 2.0F), PartPose.ZERO);
        partDefinition.addOrReplaceChild("mouth", CubeListBuilder.create().texOffs(32, 8).addBox(0.0F, 21.0F, -3.5F, 1.0F, 1.0F, 1.0F), PartPose.ZERO);

        return LayerDefinition.create(meshDefinition, 64, 32);
    }

    @Override
    protected void scale(PufferfishSlimeEntity slime, PoseStack poseStack, float partialTick) {
        poseStack.scale(0.999F, 0.999F, 0.999F);
        poseStack.translate(0.0F, 0.001F, 0.0F);
        float size = (float) slime.getSize();
        float squish = Mth.lerp(partialTick, slime.oSquish, slime.squish) / (size * 0.5F + 1.0F);
        float inv = 1.0F / (squish + 1.0F);
        poseStack.scale(inv * size, 1.0F / inv * size, inv * size);
    }

    @Override
    public ResourceLocation getTextureLocation(PufferfishSlimeEntity entity) {
        return SLIME_LOCATION;
    }
}