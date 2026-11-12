package com.bathcrimsonthin.slimearisemod.client.model;

import com.bathcrimsonthin.slimearisemod.SlimeAriseMod;
import com.bathcrimsonthin.slimearisemod.entity.EnderSlimeEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;

public class EnderSlimeModel<T extends EnderSlimeEntity> extends EntityModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(SlimeAriseMod.MOD_ID, "ender_slime"), "main");

    private final ModelPart cube;
    private final ModelPart core;
    private final ModelPart outer;
    private final ModelPart eye0;
    private final ModelPart eye1;
    private final ModelPart mouth;

    public EnderSlimeModel(ModelPart root) {
        this.cube = root.getChild("cube");
        this.core = this.cube.getChild("core");
        this.outer = this.cube.getChild("outer");
        this.eye0 = this.cube.getChild("eye0");
        this.eye1 = this.cube.getChild("eye1");
        this.mouth = this.cube.getChild("mouth");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition cube = partdefinition.addOrReplaceChild("cube", CubeListBuilder.create(), PartPose.ZERO);

        // コア (6x6x6) - Y: 9.0F ~ 15.0F
        cube.addOrReplaceChild("core", CubeListBuilder.create()
                        .texOffs(0, 24).addBox(-3.0F, 9.0F, -3.0F, 6.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)),
                PartPose.ZERO);

        // 外側粘膜 (8x16x8) - Y: 8.0F ~ 24.0F
        cube.addOrReplaceChild("outer", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-4.0F, 8.0F, -4.0F, 8.0F, 16.0F, 8.0F, new CubeDeformation(0.0F)),
                PartPose.ZERO);

        // 目 - Y: 10.0F
        cube.addOrReplaceChild("eye0", CubeListBuilder.create()
                        .texOffs(32, 0).addBox(-3.3F, 10.0F, -3.5F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.ZERO);

        cube.addOrReplaceChild("eye1", CubeListBuilder.create()
                        .texOffs(32, 4).addBox(1.3F, 10.0F, -3.5F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.ZERO);

        // 口 - X: -0.5F, Y: 13.0F
        cube.addOrReplaceChild("mouth", CubeListBuilder.create()
                        .texOffs(32, 8).addBox(-0.5F, 13.0F, -3.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.ZERO);

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        // 敵対時も含め、モデルの位置変更は行わない
        this.mouth.y = 0.0F;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        this.core.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.eye0.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.eye1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.mouth.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }

    public void renderOuterToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        this.outer.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }

    public void renderEyesToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        this.eye0.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.eye1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.mouth.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}