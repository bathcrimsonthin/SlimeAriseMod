package com.bathcrimsonthin.slimearisemod.client.model;

import com.bathcrimsonthin.slimearisemod.entity.RabbitSlimeEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public class RabbitSlimeModel extends HierarchicalModel<RabbitSlimeEntity> {
    private final ModelPart root;

    public RabbitSlimeModel(ModelPart root) {
        this.root = root;
    }

    // --- 外殻（ゼリー体）：バニラスライム完全準拠 ---
    public static LayerDefinition createOuterBodyLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();

        partDefinition.addOrReplaceChild("cube",
                CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, 16.0F, -4.0F, 8.0F, 8.0F, 8.0F),
                PartPose.ZERO);

        return LayerDefinition.create(meshDefinition, 64, 32);
    }

    // --- 内部（核・顔・耳・尻尾）：バニラ構造 + Blockbench追加パーツ ---
    public static LayerDefinition createInnerBodyLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();

        // 1. バニラスライム内部構造 (核・目・口)
        partDefinition.addOrReplaceChild("cube",
                CubeListBuilder.create().texOffs(0, 16).addBox(-3.0F, 17.0F, -3.0F, 6.0F, 6.0F, 6.0F),
                PartPose.ZERO);
        partDefinition.addOrReplaceChild("right_eye",
                CubeListBuilder.create().texOffs(32, 0).addBox(-3.25F, 18.0F, -3.5F, 2.0F, 2.0F, 2.0F),
                PartPose.ZERO);
        partDefinition.addOrReplaceChild("left_eye",
                CubeListBuilder.create().texOffs(32, 4).addBox(1.25F, 18.0F, -3.5F, 2.0F, 2.0F, 2.0F),
                PartPose.ZERO);
        partDefinition.addOrReplaceChild("mouth",
                CubeListBuilder.create().texOffs(32, 8).addBox(0.0F, 21.0F, -3.5F, 1.0F, 1.0F, 1.0F),
                PartPose.ZERO);

        // 2. 追加パーツ：耳 (ear) とその傾き子パーツ (cube_r1 ~ cube_r3)
        PartDefinition ear = partDefinition.addOrReplaceChild("ear",
                CubeListBuilder.create()
                        .texOffs(58, 0).addBox(-2.5F, 10.5F, 0.0F, 2.0F, 5.0F, 1.0F)
                        .texOffs(51, 0).addBox(0.5F, 13.5F, 0.0F, 2.0F, 2.0F, 1.0F)
                        .texOffs(48, 30).addBox(-3.5F, 15.5F, 0.0F, 7.0F, 1.0F, 1.0F),
                PartPose.ZERO);

        ear.addOrReplaceChild("cube_r1",
                CubeListBuilder.create().texOffs(51, 27).addBox(-2.0F, 0.0F, -1.0F, 2.0F, 1.0F, 1.0F),
                PartPose.offsetAndRotation(-3.5F, 15.5F, 1.0F, 0.0F, 0.0F, -0.7854F));

        ear.addOrReplaceChild("cube_r2",
                CubeListBuilder.create().texOffs(58, 27).addBox(0.0F, 0.0F, -1.0F, 2.0F, 1.0F, 1.0F),
                PartPose.offsetAndRotation(3.5F, 15.5F, 1.0F, 0.0F, 0.0F, 0.7854F));

        ear.addOrReplaceChild("cube_r3",
                CubeListBuilder.create().texOffs(54, 7).addBox(-1.0F, 0.0F, -3.0F, 2.0F, 1.0F, 3.0F),
                PartPose.offsetAndRotation(1.5F, 13.5F, 1.0F, -0.7854F, 0.0F, 0.0F));

        // 3. 追加パーツ：尻尾 (tail)
        partDefinition.addOrReplaceChild("tail",
                CubeListBuilder.create().texOffs(52, 20).addBox(-1.5F, 20.5F, 2.5F, 3.0F, 3.0F, 3.0F),
                PartPose.ZERO);

        return LayerDefinition.create(meshDefinition, 64, 32);
    }

    @Override
    public void setupAnim(RabbitSlimeEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
    }

    @Override
    public ModelPart root() {
        return this.root;
    }
}