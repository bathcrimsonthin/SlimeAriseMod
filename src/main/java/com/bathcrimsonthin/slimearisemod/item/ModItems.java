package com.bathcrimsonthin.slimearisemod.item;

import com.bathcrimsonthin.slimearisemod.SlimeAriseMod;
import com.bathcrimsonthin.slimearisemod.entity.ModEntities;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.SpawnEggItem;

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

    public static final Item BLAZE_SLIME_SPAWN_EGG = registerItem(
            "blaze_slime_spawn_egg",
            new SpawnEggItem(ModEntities.BLAZE_SLIME,
                    0xF6B201, // ブレイズのベース色
                    0x7ECF7D, // スライムのアクセント色
                    new Item.Properties())
    );

    public static final Item RABBIT_SLIME_SPAWN_EGG = registerItem(
            "rabbit_slime_spawn_egg",
            new SpawnEggItem(ModEntities.RABBIT_SLIME,
                    0xFFFFFF, // 白ウサギの色
                    0x7ECF7D, // スライムのアクセント色
                    new Item.Properties())
    );

    public static final Item BOMB_SLIME_SPAWN_EGG = registerItem(
            "bomb_slime_spawn_egg",
            new SpawnEggItem(ModEntities.BOMB_SLIME,
                    0x0DA70B, // クリーパーのベース色
                    0x7ECF7D, // スライムのアクセント色
                    new Item.Properties())
    );

    public static final Item DUMMY_BOMB_SLIME_BALL = Registry.register(
            BuiltInRegistries.ITEM,
            ResourceLocation.fromNamespaceAndPath(SlimeAriseMod.MOD_ID, "dummy_bomb_slime_ball"),
            new Item(new Item.Properties())
    );

    public static final Item WITCH_SLIME_BALL = registerItem(
            "witch_slime_ball", new WitchSlimeBallItem(new Item.Properties().food(
                    new FoodProperties.Builder()
                            .nutrition(0)
                            .saturationModifier(0)
                            .alwaysEdible()
                            .build()
            )));

    public static final Item WITCH_SLIME_SPAWN_EGG = registerItem(
            "witch_slime_spawn_egg",
            new SpawnEggItem(ModEntities.WITCH_SLIME,
                    0x340000, // ウィッチのベース色
                    0x7ECF7D, // スライムのアクセント色
                    new Item.Properties())
    );

    // 初期化メソッド(onInitializeから呼ぶ)
    public static void registerModItems() {
        SlimeAriseMod.LOGGER.info("Registering Mod Items for " + SlimeAriseMod.MOD_ID);

//        // クリエイティブタブへの追加(後で専用タブに変更)
//        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.INGREDIENTS).register(entries -> {
//            entries.accept(BLAZE_SLIME_BALL);
//        });
    }
}
