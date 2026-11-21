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
                    .icon(() -> new ItemStack(ModItems.SLIMY_CROWN))
                    // タブ内に表示するアイテムリスト
                    .displayItems((displayParameters, output) -> {
                        output.accept(ModItems.SLIMY_CROWN);
                        output.accept(ModItems.SLIMY_SWORD);
                        output.accept(ModItems.BLAZE_SLIME_BALL);
                        output.accept(ModItems.WITCH_SLIME_BALL);
                        output.accept(ModItems.ENDER_SLIME_BALL);
                        output.accept(ModItems.WITHER_SLIME_BALL);
                        output.accept(ModItems.SLIME_GUMMY);
                        output.accept(ModItems.SLIME_PUDDING);
                        output.accept(ModItems.SLIME_ARROW);
                        output.accept(ModItems.STICKY_TORCH);
                        output.accept(ModItems.SLIME_BOTTLE);
                        output.accept(ModItems.SLIME_SOLVENT);
                        output.accept(ModItems.BLAZE_SLIME_SPAWN_EGG);
                        output.accept(ModItems.RABBIT_SLIME_SPAWN_EGG);
                        output.accept(ModItems.BOMB_SLIME_SPAWN_EGG);
                        output.accept(ModItems.WITCH_SLIME_SPAWN_EGG);
                        output.accept(ModItems.ENDER_SLIME_SPAWN_EGG);
                        output.accept(ModItems.PUFFERFISH_SLIME_SPAWN_EGG);
                        output.accept(ModItems.STONECUTTER_SLIME_SPAWN_EGG);
                        output.accept(ModItems.WITHER_SLIME_SPAWN_EGG);
                        output.accept(ModItems.KING_SLIME_SPAWN_EGG);
                    })
                    .build()
    );

    public static void registerItemGroups() {
        SlimeAriseMod.LOGGER.info("Registering Item Groups for " + SlimeAriseMod.MOD_ID);
    }
}