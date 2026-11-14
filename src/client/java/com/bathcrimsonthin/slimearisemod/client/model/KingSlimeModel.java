package com.bathcrimsonthin.slimearisemod.client.model;

import com.bathcrimsonthin.slimearisemod.SlimeAriseMod;
import com.bathcrimsonthin.slimearisemod.entity.KingSlimeEntity;
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

public class KingSlimeModel extends EntityModel<KingSlimeEntity> {

    public static final ModelLayerLocation INNER_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(SlimeAriseMod.MOD_ID, "king_slime"), "main");
    public static final ModelLayerLocation OUTER_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(SlimeAriseMod.MOD_ID, "king_slime"), "outer");

    private final ModelPart root;

    public KingSlimeModel(ModelPart root) {
        this.root = root;
    }

    // 内部コア・目・口 ＋ 斜め王冠パーツ（半透明外皮は createOuterBodyLayer へ分離）
    public static LayerDefinition createInnerBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition cube = partdefinition.addOrReplaceChild("cube",
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

        PartDefinition crown = partdefinition.addOrReplaceChild("crown",
                CubeListBuilder.create()
                        .texOffs(0, 30).addBox(-1.75F, 0.0667F, -2.4167F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(53, 28).addBox(-0.75F, -6.4333F, 0.75F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(9, 30).addBox(-1.75F, -5.4333F, 0.75F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(50, 19).addBox(-1.25F, -2.3333F, -1.25F, 2.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-1.15F, 15.3333F, -0.0833F, -0.0873F, 0.0F, -0.1745F));

        PartDefinition bone10 = crown.addOrReplaceChild("bone10",
                CubeListBuilder.create().texOffs(58, 28).addBox(-0.5F, -12.0F, -3.0F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-0.25F, 9.0667F, 0.5833F));

        PartDefinition bone8 = bone10.addOrReplaceChild("bone8",
                CubeListBuilder.create().texOffs(58, 28).addBox(-1.0F, -1.0F, 0.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.5F, -12.0F, -3.0F, -0.7854F, 0.0F, 0.0F));

        bone8.addOrReplaceChild("bone9",
                CubeListBuilder.create().texOffs(58, 28).addBox(-1.0F, -3.0F, 0.0F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, -1.0F, 0.0F, -0.7854F, 0.0F, 0.0F));

        PartDefinition bone = crown.addOrReplaceChild("bone",
                CubeListBuilder.create()
                        .texOffs(0, 30).addBox(0.0F, -1.0F, 0.0F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(50, 19).addBox(0.5F, -3.4F, 1.1667F, 2.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(1.25F, 1.0667F, -2.4167F, 0.0F, -0.7854F, 0.0F));

        PartDefinition bone2 = bone.addOrReplaceChild("bone2",
                CubeListBuilder.create()
                        .texOffs(0, 30).addBox(0.0F, -1.0F, 0.0F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(50, 19).addBox(0.5F, -3.4F, 1.1667F, 2.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(3.0F, 0.0F, 0.0F, 0.0F, -0.7854F, 0.0F));

        PartDefinition bone3 = bone2.addOrReplaceChild("bone3",
                CubeListBuilder.create()
                        .texOffs(0, 30).addBox(0.0F, -1.0F, 0.0F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(50, 19).addBox(0.5F, -3.4F, 1.1667F, 2.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(3.0F, 0.0F, 0.0F, 0.0F, -0.7854F, 0.0F));

        PartDefinition bone4 = bone3.addOrReplaceChild("bone4",
                CubeListBuilder.create().texOffs(0, 30).addBox(0.0F, -1.0F, 0.0F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(3.0F, 0.0F, 0.0F, 0.0F, -0.7854F, 0.0F));

        PartDefinition bone5 = bone4.addOrReplaceChild("bone5",
                CubeListBuilder.create().texOffs(0, 30).addBox(0.0F, -1.0F, 0.0F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(3.0F, 0.0F, 0.0F, 0.0F, -0.7854F, 0.0F));

        PartDefinition bone6 = bone5.addOrReplaceChild("bone6",
                CubeListBuilder.create().texOffs(0, 30).addBox(0.0F, -1.0F, 0.0F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(3.0F, 0.0F, 0.0F, 0.0F, -0.7854F, 0.0F));

        PartDefinition bone7 = bone6.addOrReplaceChild("bone7",
                CubeListBuilder.create().texOffs(0, 30).addBox(0.0F, -1.0F, 0.0F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(3.0F, 0.0F, 0.0F, 0.0F, -0.7854F, 0.0F));

        PartDefinition bone29 = bone7.addOrReplaceChild("bone29",
                CubeListBuilder.create().texOffs(58, 28).addBox(-0.5F, -12.0F, -3.0F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offset(1.5F, 8.0F, 3.0F));

        PartDefinition bone30 = bone29.addOrReplaceChild("bone30",
                CubeListBuilder.create().texOffs(58, 28).addBox(-1.0F, -1.0F, 0.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.5F, -12.0F, -3.0F, -0.7854F, 0.0F, 0.0F));

        bone30.addOrReplaceChild("bone31",
                CubeListBuilder.create().texOffs(58, 28).addBox(-1.0F, -3.0F, 0.0F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, -1.0F, 0.0F, -0.7854F, 0.0F, 0.0F));

        PartDefinition bone26 = bone6.addOrReplaceChild("bone26",
                CubeListBuilder.create().texOffs(60, 28).addBox(-0.5F, -12.0F, -3.0F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offset(1.5F, 8.0F, 3.0F));

        PartDefinition bone27 = bone26.addOrReplaceChild("bone27",
                CubeListBuilder.create().texOffs(60, 28).addBox(-1.0F, -1.0F, 0.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.5F, -12.0F, -3.0F, -0.7854F, 0.0F, 0.0F));

        bone27.addOrReplaceChild("bone28",
                CubeListBuilder.create().texOffs(60, 28).addBox(-1.0F, -3.0F, 0.0F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, -1.0F, 0.0F, -0.7854F, 0.0F, 0.0F));

        PartDefinition bone23 = bone5.addOrReplaceChild("bone23",
                CubeListBuilder.create().texOffs(60, 28).addBox(-0.5F, -12.0F, -3.0F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offset(1.5F, 8.0F, 3.0F));

        PartDefinition bone24 = bone23.addOrReplaceChild("bone24",
                CubeListBuilder.create().texOffs(60, 28).addBox(-1.0F, -1.0F, 0.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.5F, -12.0F, -3.0F, -0.7854F, 0.0F, 0.0F));

        bone24.addOrReplaceChild("bone25",
                CubeListBuilder.create().texOffs(60, 28).addBox(-1.0F, -3.0F, 0.0F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, -1.0F, 0.0F, -0.7854F, 0.0F, 0.0F));

        PartDefinition bone20 = bone4.addOrReplaceChild("bone20",
                CubeListBuilder.create().texOffs(60, 28).addBox(-0.5F, -12.0F, -3.0F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offset(1.5F, 8.0F, 3.0F));

        PartDefinition bone21 = bone20.addOrReplaceChild("bone21",
                CubeListBuilder.create().texOffs(60, 28).addBox(-1.0F, -1.0F, 0.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.5F, -12.0F, -3.0F, -0.7854F, 0.0F, 0.0F));

        bone21.addOrReplaceChild("bone22",
                CubeListBuilder.create().texOffs(60, 28).addBox(-1.0F, -3.0F, 0.0F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, -1.0F, 0.0F, -0.7854F, 0.0F, 0.0F));

        PartDefinition bone17 = bone3.addOrReplaceChild("bone17",
                CubeListBuilder.create().texOffs(60, 28).addBox(-0.5F, -12.0F, -3.0F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offset(1.5F, 8.0F, 3.0F));

        PartDefinition bone18 = bone17.addOrReplaceChild("bone18",
                CubeListBuilder.create().texOffs(60, 28).addBox(-1.0F, -1.0F, 0.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.5F, -12.0F, -3.0F, -0.7854F, 0.0F, 0.0F));

        bone18.addOrReplaceChild("bone19",
                CubeListBuilder.create().texOffs(60, 28).addBox(-1.0F, -3.0F, 0.0F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, -1.0F, 0.0F, -0.7854F, 0.0F, 0.0F));

        PartDefinition bone14 = bone2.addOrReplaceChild("bone14",
                CubeListBuilder.create().texOffs(60, 28).addBox(-0.5F, -12.0F, -3.0F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offset(1.5F, 8.0F, 3.0F));

        PartDefinition bone15 = bone14.addOrReplaceChild("bone15",
                CubeListBuilder.create().texOffs(60, 28).addBox(-1.0F, -1.0F, 0.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.5F, -12.0F, -3.0F, -0.7854F, 0.0F, 0.0F));

        bone15.addOrReplaceChild("bone16",
                CubeListBuilder.create().texOffs(60, 28).addBox(-1.0F, -3.0F, 0.0F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, -1.0F, 0.0F, -0.7854F, 0.0F, 0.0F));

        PartDefinition bone11 = bone.addOrReplaceChild("bone11",
                CubeListBuilder.create().texOffs(60, 28).addBox(-0.5F, -12.0F, -3.0F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offset(1.5F, 8.0F, 3.0F));

        PartDefinition bone12 = bone11.addOrReplaceChild("bone12",
                CubeListBuilder.create().texOffs(60, 28).addBox(-1.0F, -1.0F, 0.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.5F, -12.0F, -3.0F, -0.7854F, 0.0F, 0.0F));

        bone12.addOrReplaceChild("bone13",
                CubeListBuilder.create().texOffs(60, 28).addBox(-1.0F, -3.0F, 0.0F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, -1.0F, 0.0F, -0.7854F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 64, 32);
    }

    // 半透明の外皮
    public static LayerDefinition createOuterBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild("cube",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-4.0F, 16.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 64, 32);
    }

    @Override
    public void setupAnim(KingSlimeEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        this.root.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}