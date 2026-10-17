package com.bathcrimsonthin.slimearisemod.item;

import com.bathcrimsonthin.slimearisemod.SlimeAriseMod;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.resources.ResourceLocation;

public class ModItems {
    // アイテム登録用ヘルパーメソッド
    private static Item registerItem(String name, Item item) {
        return Registry.register(
                BuiltInRegistries.ITEM,
                ResourceLocation.fromNamespaceAndPath(SlimeAriseMod.MOD_ID, name),
                item
        );
    }

    // アイテムの定義
    public static final Item BLAZE_SLIME_BALL = registerItem(
            "blaze_slime_ball", new BlazeSlimeBallItem(new Item.Properties().food(
                    new FoodProperties.Builder()
                            .nutrition(0)
                            .saturationModifier(0)
                            .alwaysEdible()
                            .build()
            )));

    // 初期化メソッド(onInitializeから呼ぶ)
    public static void registerModItems() {
        SlimeAriseMod.LOGGER.info("Registering Mod Items for " + SlimeAriseMod.MOD_ID);

        // クリエイティブタブへの追加(後で専用タブに変更)
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.INGREDIENTS).register(entries -> {
            entries.accept(BLAZE_SLIME_BALL);
        });
    }
}
