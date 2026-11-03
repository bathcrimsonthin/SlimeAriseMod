package com.bathcrimsonthin.slimearisemod.client.renderer;

import com.bathcrimsonthin.slimearisemod.SlimeAriseMod;
import com.bathcrimsonthin.slimearisemod.entity.BombSlimeEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.SlimeModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.SlimeOuterLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class BombSlimeRenderer extends MobRenderer<BombSlimeEntity, SlimeModel<BombSlimeEntity>> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(SlimeAriseMod.MOD_ID, "textures/entity/bomb_slime.png");

    public BombSlimeRenderer(EntityRendererProvider.Context context) {
        super(context, new SlimeModel<>(context.bakeLayer(ModelLayers.SLIME)), 0.25F);
        // バニラスライムの半透明外殻レイヤーを追加
        this.addLayer(new SlimeOuterLayer<>(this, context.getModelSet()));
    }

    // クリーパー同様の点滅エフェクトを適用
    @Override
    protected float getWhiteOverlayProgress(BombSlimeEntity entity, float partialTick) {
        float f = entity.getSwelling(partialTick);
        return (int) (f * 10.0F) % 2 == 0 ? 0.0F : Mth.clamp(f, 0.5F, 1.0F);
    }

    // サイズ調整と潰れアニメーション（バニラスライムと共通）
    @Override
    protected void scale(BombSlimeEntity entity, PoseStack poseStack, float partialTick) {
        poseStack.scale(0.999F, 0.999F, 0.999F);
        poseStack.translate(0.0F, 0.001F, 0.0F);
        float size = (float) entity.getSize();
        float squish = Mth.lerp(partialTick, entity.oSquish, entity.squish) / (size * 0.5F + 1.0F);
        float scaleFactor = 1.0F / (squish + 1.0F);
        poseStack.scale(scaleFactor * size, 1.0F / scaleFactor * size, scaleFactor * size);
    }

    @Override
    public ResourceLocation getTextureLocation(BombSlimeEntity entity) {
        return TEXTURE;
    }
}