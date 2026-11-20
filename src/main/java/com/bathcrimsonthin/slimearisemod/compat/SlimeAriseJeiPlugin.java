package com.bathcrimsonthin.slimearisemod.compat;

import com.bathcrimsonthin.slimearisemod.SlimeAriseMod;
import com.bathcrimsonthin.slimearisemod.item.ModItems;
import com.bathcrimsonthin.slimearisemod.recipe.WitchSlimeBallRecipe;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.ShapelessRecipe;

import java.util.ArrayList;
import java.util.List;

@JeiPlugin
public class SlimeAriseJeiPlugin implements IModPlugin {

    @Override
    public ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(SlimeAriseMod.MOD_ID, "jei_plugin");
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        List<RecipeHolder<CraftingRecipe>> recipes = new ArrayList<>();

        NonNullList<Ingredient> inputs = NonNullList.of(
                Ingredient.EMPTY,
                Ingredient.of(ModItems.WITCH_SLIME_BALL),
                Ingredient.of(ModItems.WITCH_SLIME_BALL),
                Ingredient.of(ModItems.WITCH_SLIME_BALL),
                Ingredient.of(Items.GLASS_BOTTLE)
        );

        for (Holder<MobEffect> effect : WitchSlimeBallRecipe.WITCH_EFFECTS) {
            ItemStack output = WitchSlimeBallRecipe.createLingeringPotionForEffect(effect);
            String effectPath = effect.unwrapKey()
                    .map(key -> key.location().getPath())
                    .orElse("unknown");

            ResourceLocation recipeId = ResourceLocation.fromNamespaceAndPath(
                    SlimeAriseMod.MOD_ID,
                    "jei_witch_lingering_" + effectPath
            );

            recipes.add(new RecipeHolder<>(
                    recipeId,
                    new ShapelessRecipe("witch_lingering_potion", CraftingBookCategory.MISC, output, inputs)
            ));
        }

        registration.addRecipes(RecipeTypes.CRAFTING, recipes);
    }
}