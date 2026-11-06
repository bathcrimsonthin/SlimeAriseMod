package com.bathcrimsonthin.slimearisemod.recipe;

import com.bathcrimsonthin.slimearisemod.SlimeAriseMod;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;

public class ModRecipes {

    public static final RecipeSerializer<WitchSlimeBallRecipe> WITCH_SLIME_BALL_RECIPE_SERIALIZER =
            register("witch_slime_ball_crafting",
                    new SimpleCraftingRecipeSerializer<>(WitchSlimeBallRecipe::new));

    private static <T extends RecipeSerializer<?>> T register(String name, T serializer) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(SlimeAriseMod.MOD_ID, name);
        // Fabric 1.21.1 の標準レシピシリアライザー登録
        return Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, id, serializer);
    }

    public static void registerRecipes() {
        // メインModクラスの onInitialize() 等から呼び出し
    }
}