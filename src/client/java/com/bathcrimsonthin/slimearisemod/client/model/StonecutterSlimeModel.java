package com.bathcrimsonthin.slimearisemod.client.model;

import com.bathcrimsonthin.slimearisemod.SlimeAriseMod;
import com.bathcrimsonthin.slimearisemod.entity.StonecutterSlimeEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;

public class StonecutterSlimeModel extends EntityModel<StonecutterSlimeEntity> {

    public static final ModelLayerLocation INNER_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(SlimeAriseMod.MOD_ID, "stonecutter_slime"), "main");
    public static final ModelLayerLocation OUTER_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(SlimeAriseMod.MOD_ID, "stonecutter_slime"), "outer");

    private final ModelPart root;

    public StonecutterSlimeModel(ModelPart root) {
        this.root = root;
    }

    // 内部コア・目・口・石切り台本体＆刃（不透明 / カットアウト描画）
    public static LayerDefinition createInnerBodyLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();

        PartDefinition cube = partDefinition.addOrReplaceChild("cube",
                CubeListBuilder.create()
                        .texOffs(0, 16).addBox(-3.0F, 17.0F, -3.0F, 6.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        cube.addOrReplaceChild("eye0",
                CubeListBuilder.create()
                        .texOffs(32, 0).addBox(-3.3F, 18.0F, -3.5F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        cube.addOrReplaceChild("eye1",
                CubeListBuilder.create()
                        .texOffs(32, 4).addBox(1.3F, 18.0F, -3.5F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        cube.addOrReplaceChild("mouth",
                CubeListBuilder.create()
                        .texOffs(32, 8).addBox(0.0F, 21.0F, -3.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        partDefinition.addOrReplaceChild("stonecutter",
                CubeListBuilder.create()
                        .texOffs(48, 16).addBox(0.0F, -12.1F, -8.1F, 0.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
                        .texOffs(36, 13).addBox(-3.6F, -8.5F, -4.5F, 7.0F, 4.0F, 7.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 24.0F, 0.0F));

        return LayerDefinition.create(meshDefinition, 64, 32);
    }

    // 半透明の外皮（RenderType.entityTranslucent で描画）
    public static LayerDefinition createOuterBodyLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();

        partDefinition.addOrReplaceChild("cube",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-4.0F, 16.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        return LayerDefinition.create(meshDefinition, 64, 32);
    }

    @Override
    public void setupAnim(StonecutterSlimeEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        this.root.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}