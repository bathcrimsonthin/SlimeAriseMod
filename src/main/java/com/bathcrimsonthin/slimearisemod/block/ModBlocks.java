package com.bathcrimsonthin.slimearisemod.block;

import com.bathcrimsonthin.slimearisemod.SlimeAriseMod;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.PushReaction;

public final class ModBlocks {

    public static final Block STICKY_TORCH = registerBlock(
            "sticky_torch",
            new StickyTorchBlock(
                    BlockBehaviour.Properties.of()
                            .noCollission()
                            .instabreak()
                            .lightLevel(state -> 14)
                            .sound(SoundType.WOOD)
                            .pushReaction(PushReaction.DESTROY)
            )
    );

    public static final Block SLIME_CUSHION = registerBlock(
            "slime_cushion",
            new SlimeCushionBlock(
                    BlockBehaviour.Properties.of()
                            .instabreak()
                            .noOcclusion()
                            .sound(SoundType.SLIME_BLOCK)
                            .pushReaction(PushReaction.DESTROY)
            )
    );

    private ModBlocks() {
    }

    private static Block registerBlock(String name, Block block) {
        return Registry.register(
                BuiltInRegistries.BLOCK,
                ResourceLocation.fromNamespaceAndPath(SlimeAriseMod.MOD_ID, name),
                block
        );
    }

    public static void registerModBlocks() {
        // クラスロードを発火させて静的フィールドのブロックを登録する
    }
}