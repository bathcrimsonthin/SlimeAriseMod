package com.bathcrimsonthin.slimearisemod.client.model;

import com.bathcrimsonthin.slimearisemod.SlimeAriseMod;
import com.bathcrimsonthin.slimearisemod.entity.WitherSlimeEntity;
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

public class WitherSlimeModel extends EntityModel<WitherSlimeEntity> {

    public static final ModelLayerLocation INNER_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(SlimeAriseMod.MOD_ID, "wither_slime"), "main");
    public static final ModelLayerLocation OUTER_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(SlimeAriseMod.MOD_ID, "wither_slime"), "outer");
    public static final ModelLayerLocation ARMOR_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(SlimeAriseMod.MOD_ID, "wither_slime"), "armor");

    private final ModelPart root;

    public WitherSlimeModel(ModelPart root) {
        this.root = root;
    }

    // 顔パーツ（目・口）と背面の肋骨パーツ（中央の6x6x6コアはウィザースケルトン頭に置き換えるため除外）
    public static LayerDefinition createInnerBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition cube = partdefinition.addOrReplaceChild("cube", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        cube.addOrReplaceChild("eye0", CubeListBuilder.create().texOffs(32, 0).addBox(-3.3F, 18.0F, -3.5F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));
        cube.addOrReplaceChild("eye1", CubeListBuilder.create().texOffs(32, 4).addBox(1.3F, 18.0F, -3.5F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));
        cube.addOrReplaceChild("mouth", CubeListBuilder.create().texOffs(32, 8).addBox(0.0F, 21.0F, -3.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition bone = partdefinition.addOrReplaceChild("bone", CubeListBuilder.create().texOffs(58, 24).addBox(-1.0F, -7.5F, 4.0F, 2.0F, 7.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(48, 21).addBox(-3.5F, -5.0F, 3.5F, 7.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(48, 21).addBox(-3.5F, -2.5F, 3.5F, 7.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(1, 0).addBox(3.9142F, -7.5F, 1.0858F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.25F));

        bone.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(52, 29).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -7.5F, 5.0F, 0.7854F, 0.0F, 0.0F));

        PartDefinition ul1 = bone.addOrReplaceChild("ul1", CubeListBuilder.create().texOffs(48, 21).addBox(-3.5F, -7.5F, 3.5F, 7.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition ul2 = ul1.addOrReplaceChild("ul2", CubeListBuilder.create().texOffs(48, 21).addBox(0.0F, -7.5F, -1.0F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.5F, 0.0F, 4.5F, 0.0F, 0.7854F, 0.0F));
        PartDefinition ul3 = ul2.addOrReplaceChild("ul3", CubeListBuilder.create().texOffs(48, 21).addBox(0.0F, -2.0F, -1.0F, 7.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.0F, -5.5F, 0.0F, 0.0F, 0.7854F, 0.0F));
        ul3.addOrReplaceChild("ul4", CubeListBuilder.create().texOffs(48, 21).addBox(0.0F, -2.0F, -1.0F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.0F, 0.0F, 0.0F, 0.0F, 0.7854F, 0.0F));

        PartDefinition ml1 = bone.addOrReplaceChild("ml1", CubeListBuilder.create().texOffs(48, 21).addBox(-3.5F, -7.5F, 3.5F, 7.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 2.5F, 0.0F));
        PartDefinition ml2 = ml1.addOrReplaceChild("ml2", CubeListBuilder.create().texOffs(48, 21).addBox(0.0F, -7.5F, -1.0F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.5F, 0.0F, 4.5F, 0.0F, 0.7854F, 0.0F));
        PartDefinition ml3 = ml2.addOrReplaceChild("ml3", CubeListBuilder.create().texOffs(48, 21).addBox(0.0F, -2.0F, -1.0F, 7.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.0F, -5.5F, 0.0F, 0.0F, 0.7854F, 0.0F));
        ml3.addOrReplaceChild("ml4", CubeListBuilder.create().texOffs(48, 21).addBox(0.0F, -2.0F, -1.0F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.0F, 0.0F, 0.0F, 0.0F, 0.7854F, 0.0F));

        PartDefinition dl1 = bone.addOrReplaceChild("dl1", CubeListBuilder.create().texOffs(48, 21).addBox(-3.5F, -7.5F, 3.5F, 7.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 5.0F, 0.0F));
        PartDefinition dl2 = dl1.addOrReplaceChild("dl2", CubeListBuilder.create().texOffs(48, 21).addBox(0.0F, -7.5F, -1.0F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.5F, 0.0F, 4.5F, 0.0F, 0.7854F, 0.0F));
        PartDefinition dl3 = dl2.addOrReplaceChild("dl3", CubeListBuilder.create().texOffs(48, 21).addBox(0.0F, -2.0F, -1.0F, 7.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.0F, -5.5F, 0.0F, 0.0F, 0.7854F, 0.0F));
        dl3.addOrReplaceChild("dl4", CubeListBuilder.create().texOffs(48, 21).addBox(0.0F, -2.0F, -1.0F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.0F, 0.0F, 0.0F, 0.0F, 0.7854F, 0.0F));

        PartDefinition dr1 = bone.addOrReplaceChild("dr1", CubeListBuilder.create().texOffs(48, 21).addBox(-3.5F, -7.5F, 3.5F, 7.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 5.0F, 0.0F));
        PartDefinition dr2 = dr1.addOrReplaceChild("dr2", CubeListBuilder.create().texOffs(48, 21).mirror().addBox(-2.0F, -7.5F, -1.0F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-3.5F, 0.0F, 4.5F, 0.0F, -0.7854F, 0.0F));
        PartDefinition dr3 = dr2.addOrReplaceChild("dr3", CubeListBuilder.create().texOffs(48, 21).mirror().addBox(-7.0F, -2.0F, -1.0F, 7.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-2.0F, -5.5F, 0.0F, 0.0F, -0.7854F, 0.0F));
        dr3.addOrReplaceChild("dr4", CubeListBuilder.create().texOffs(48, 21).mirror().addBox(-2.0F, -2.0F, -1.0F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-7.0F, 0.0F, 0.0F, 0.0F, -0.7854F, 0.0F));

        PartDefinition mr1 = bone.addOrReplaceChild("mr1", CubeListBuilder.create().texOffs(48, 21).addBox(-3.5F, -7.5F, 3.5F, 7.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 2.5F, 0.0F));
        PartDefinition mr2 = mr1.addOrReplaceChild("mr2", CubeListBuilder.create().texOffs(48, 21).mirror().addBox(-2.0F, -7.5F, -1.0F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-3.5F, 0.0F, 4.5F, 0.0F, -0.7854F, 0.0F));
        PartDefinition mr3 = mr2.addOrReplaceChild("mr3", CubeListBuilder.create().texOffs(48, 21).mirror().addBox(-7.0F, -2.0F, -1.0F, 7.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-2.0F, -5.5F, 0.0F, 0.0F, -0.7854F, 0.0F));
        mr3.addOrReplaceChild("mr4", CubeListBuilder.create().texOffs(48, 21).mirror().addBox(-2.0F, -2.0F, -1.0F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-7.0F, 0.0F, 0.0F, 0.0F, -0.7854F, 0.0F));

        PartDefinition ur1 = bone.addOrReplaceChild("ur1", CubeListBuilder.create().texOffs(48, 21).addBox(-3.5F, -7.5F, 3.5F, 7.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition ur2 = ur1.addOrReplaceChild("ur2", CubeListBuilder.create().texOffs(48, 21).mirror().addBox(-2.0F, -7.5F, -1.0F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-3.5F, 0.0F, 4.5F, 0.0F, -0.7854F, 0.0F));
        PartDefinition ur3 = ur2.addOrReplaceChild("ur3", CubeListBuilder.create().texOffs(48, 21).mirror().addBox(-7.0F, -2.0F, -1.0F, 7.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-2.0F, -5.5F, 0.0F, 0.0F, -0.7854F, 0.0F));
        ur3.addOrReplaceChild("ur4", CubeListBuilder.create().texOffs(48, 21).mirror().addBox(-2.0F, -2.0F, -1.0F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-7.0F, 0.0F, 0.0F, 0.0F, -0.7854F, 0.0F));

        return LayerDefinition.create(meshdefinition, 64, 32);
    }

    // 半透明の外皮
    public static LayerDefinition createOuterBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild("cube",
                CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, 16.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 64, 32);
    }

    // HP半分以下で纏うウィザーアーマーのオーラ層
    public static LayerDefinition createArmorLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild("cube",
                CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, 16.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.5F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 64, 32);
    }

    @Override
    public void setupAnim(WitherSlimeEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        this.root.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}