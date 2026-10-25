package com.bathcrimsonthin.slimearisemod.item;

import com.bathcrimsonthin.slimearisemod.SlimeAriseMod;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class ModItemGroups {
    public static final CreativeModeTab SLIME_ARISE_TAB = Registry.register(
            BuiltInRegistries.CREATIVE_MODE_TAB,
            ResourceLocation.fromNamespaceAndPath(SlimeAriseMod.MOD_ID, "slime_arise_tab"),
            FabricItemGroup.builder()
                    // タブの表示名 (langファイルで多言語化)
                    .title(Component.translatable("itemGroup.slimearisemod.slime_arise_tab"))
                    // タブのアイコン画像アイテム
                    .icon(() -> new ItemStack(ModItems.BLAZE_SLIME_BALL))
                    // タブ内に表示するアイテムリスト
                    .displayItems((displayParameters, output) -> {
                        output.accept(ModItems.BLAZE_SLIME_BALL);
                        output.accept(ModItems.BLAZE_SLIME_SPAWN_EGG);
                    })
                    .build()
    );

    public static void registerItemGroups() {
        SlimeAriseMod.LOGGER.info("Registering Item Groups for " + SlimeAriseMod.MOD_ID);
    }
}