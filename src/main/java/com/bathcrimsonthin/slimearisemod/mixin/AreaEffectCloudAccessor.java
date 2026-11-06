package com.bathcrimsonthin.slimearisemod.mixin;

import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.item.alchemy.PotionContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AreaEffectCloud.class)
public interface AreaEffectCloudAccessor {
    // 1.21.1 の private フィールド "potionContents" を直接取得
    @Accessor("potionContents")
    PotionContents getPotionContents();
}