package com.bathcrimsonthin.slimearisemod.client.model;

import com.bathcrimsonthin.slimearisemod.entity.WitchSlimeEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public class WitchSlimeModel extends HierarchicalModel<WitchSlimeEntity> {
    private final ModelPart root;

    public WitchSlimeModel(ModelPart root) {
        this.root = root;
    }

    // --- 外殻（半透明ゼリー）---
    public static LayerDefinition createOuterBodyLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        partDefinition.addOrReplaceChild("cube",
                CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, 16.0F, -4.0F, 8.0F, 8.0F, 8.0F),
                PartPose.ZERO);
        return LayerDefinition.create(meshDefinition, 64, 64);
    }

    // --- 内部（核・顔・魔女の帽子）---
    public static LayerDefinition createInnerBodyLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();

        // 1. スライム内部（核・目・口）
        partDefinition.addOrReplaceChild("cube",
                CubeListBuilder.create().texOffs(0, 16).addBox(-3.0F, 17.0F, -3.0F, 6.0F, 6.0F, 6.0F), PartPose.ZERO);
        partDefinition.addOrReplaceChild("right_eye",
                CubeListBuilder.create().texOffs(32, 0).addBox(-3.3F, 18.0F, -3.5F, 2.0F, 2.0F, 2.0F), PartPose.ZERO);
        partDefinition.addOrReplaceChild("left_eye",
                CubeListBuilder.create().texOffs(32, 4).addBox(1.3F, 18.0F, -3.5F, 2.0F, 2.0F, 2.0F), PartPose.ZERO);
        partDefinition.addOrReplaceChild("mouth",
                CubeListBuilder.create().texOffs(32, 8).addBox(0.0F, 21.0F, -3.5F, 1.0F, 1.0F, 1.0F), PartPose.ZERO);

        // 2. 魔女の帽子構造 (Blockbench提示データ)
        PartDefinition hat = partDefinition.addOrReplaceChild("hat",
                CubeListBuilder.create().texOffs(0, 30).mirror().addBox(-10.0F, -2.02F, 0.0F, 10.0F, 2.0F, 10.0F, new CubeDeformation(0.0F)).mirror(false),
                PartPose.offset(5.0F, 17.97F, -5.0F));

        PartDefinition hat2 = hat.addOrReplaceChild("hat2",
                CubeListBuilder.create().texOffs(0, 42).mirror().addBox(-2.0F, -5.5F, -5.0F, 7.0F, 4.0F, 7.0F, new CubeDeformation(0.0F)).mirror(false),
                PartPose.offsetAndRotation(-6.75F, 0.03F, 7.0F, -0.0524F, 0.0F, 0.0262F));

        PartDefinition hat3 = hat2.addOrReplaceChild("hat3",
                CubeListBuilder.create().texOffs(0, 53).mirror().addBox(-0.75F, -5.5F, -3.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false),
                PartPose.offsetAndRotation(0.0F, -3.0F, 0.0F, -0.1047F, 0.0F, 0.0524F));

        hat3.addOrReplaceChild("hat4",
                CubeListBuilder.create().texOffs(0, 61).mirror().addBox(0.5F, -4.0F, -1.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.25F)).mirror(false),
                PartPose.offsetAndRotation(0.0F, -3.0F, 0.0F, -0.2094F, 0.0F, 0.1047F));

        return LayerDefinition.create(meshDefinition, 64, 64);
    }

    @Override
    public void setupAnim(WitchSlimeEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
    }

    @Override
    public ModelPart root() {
        return this.root;
    }
}