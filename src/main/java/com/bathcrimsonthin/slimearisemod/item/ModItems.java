package com.bathcrimsonthin.slimearisemod.item;

import com.bathcrimsonthin.slimearisemod.SlimeAriseMod;
import com.bathcrimsonthin.slimearisemod.block.ModBlocks;
import com.bathcrimsonthin.slimearisemod.entity.ModEntities;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.DispenserBlock;

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

//    public static final Item DUMMY_BOMB_SLIME_BALL = Registry.register(
//            BuiltInRegistries.ITEM,
//            ResourceLocation.fromNamespaceAndPath(SlimeAriseMod.MOD_ID, "dummy_bomb_slime_ball"),
//            new Item(new Item.Properties())
//    );

    public static final Item WITCH_SLIME_BALL = registerItem(
            "witch_slime_ball", new WitchSlimeBallItem(new Item.Properties()
//                    .food(new FoodProperties.Builder()
////                            .nutrition(0)
////                            .saturationModifier(0)
////                            .alwaysEdible()
//                            .build()
//            )
            ));

    public static final Item WITCH_SLIME_SPAWN_EGG = registerItem(
            "witch_slime_spawn_egg",
            new SpawnEggItem(ModEntities.WITCH_SLIME,
                    0x340000, // ウィッチのベース色
                    0x7ECF7D, // スライムのアクセント色
                    new Item.Properties())
    );

    public static final Item ENDER_SLIME_BALL = registerItem(
            "ender_slime_ball",
            new EnderSlimeBallItem(new Item.Properties())
    );

    public static final Item ENDER_SLIME_SPAWN_EGG = registerItem(
            "ender_slime_spawn_egg",
            new SpawnEggItem(ModEntities.ENDER_SLIME,
                    0x161616, // エンダーマンのベース色
                    0x7ECF7D, // スライムのアクセント色
                    new Item.Properties())
    );

    public static final Item PUFFERFISH_SLIME_SPAWN_EGG = registerItem(
            "pufferfish_slime_spawn_egg",
            new SpawnEggItem(ModEntities.PUFFERFISH_SLIME,
                    0xF6B201, // フグのベース色
                    0x7ECF7D, // スライムのアクセント色
                    new Item.Properties())
    );

    public static final Item STONECUTTER_SLIME_SPAWN_EGG = registerItem(
            "stonecutter_slime_spawn_egg",
            new SpawnEggItem(ModEntities.STONECUTTER_SLIME,
                    0x7A7979, // 石切り台の石色
                    0x7ECF7D, // スライム色
                    new Item.Properties())
    );

    public static final Item WITHER_SLIME_SPAWN_EGG = registerItem(
            "wither_slime_spawn_egg",
            new SpawnEggItem(ModEntities.WITHER_SLIME,
                    0x141414, // ウィザーの黒色
                    0x7ECF7D, // スライムのアクセント色
                    new Item.Properties())
    );

    public static final Item WITHER_SLIME_BALL = registerItem(
            "wither_slime_ball",
            new WitherSlimeBallItem(new Item.Properties())
    );

    public static final Item KING_SLIME_SPAWN_EGG = registerItem(
            "king_slime_spawn_egg",
            new SpawnEggItem(ModEntities.KING_SLIME,
                    0x51A03E, // スライムグリーン
                    0xF5C724, // 王冠のゴールド
                    new Item.Properties())
    );

    public static final Item SLIMY_CROWN = registerItem(
            "slimy_crown",
            new SlimyCrownItem(new Item.Properties().stacksTo(1).rarity(Rarity.RARE))
    );

    public static final Item SLIMY_SWORD = registerItem(
            "slimy_sword",
            new SlimySwordItem(
                    new Item.Properties()
                            .rarity(Rarity.EPIC)
                            .attributes(SwordItem.createAttributes(SlimySwordItem.SLIMY_TIER, 6, -2.4F))
            )
    );

    public static final Item SLIME_GUMMY = registerItem(
            "slime_gummy",
            new SlimeGummyItem(new Item.Properties().food(SlimeGummyItem.SLIME_GUMMY_FOOD))
    );

    public static final Item SLIME_PUDDING = registerItem(
            "slime_pudding",
            new SlimePuddingItem(
                    new Item.Properties()
                            .food(SlimePuddingItem.SLIME_PUDDING_FOOD)
//                            .stacksTo(16)
            )
    );

    public static final Item SLIME_ARROW = registerItem(
            "slime_arrow",
            new SlimeArrowItem(new Item.Properties())
    );

    public static final Item STICKY_TORCH = registerItem(
            "sticky_torch",
            new StickyTorchItem(ModBlocks.STICKY_TORCH, new Item.Properties())
    );

    public static final Item SLIME_BOTTLE = registerItem(
            "slime_bottle",
            new SlimeBottleItem(new Item.Properties().stacksTo(16))
    );

    public static final Item SLIME_SOLVENT = registerItem(
            "slime_solvent",
            new SlimeSolventItem(new Item.Properties().stacksTo(16))
    );

    // 初期化メソッド(onInitializeから呼ぶ)
    public static void registerModItems() {
        SlimeAriseMod.LOGGER.info("Registering Mod Items for " + SlimeAriseMod.MOD_ID);

        DispenserBlock.registerProjectileBehavior(ModItems.SLIME_ARROW);
        DispenserBlock.registerProjectileBehavior(ModItems.STICKY_TORCH);
        DispenserBlock.registerProjectileBehavior(ModItems.SLIME_BOTTLE);
        DispenserBlock.registerProjectileBehavior(ModItems.SLIME_SOLVENT);

//        // クリエイティブタブへの追加(後で専用タブに変更)
//        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.INGREDIENTS).register(entries -> {
//            entries.accept(BLAZE_SLIME_BALL);
//        });
    }
}
