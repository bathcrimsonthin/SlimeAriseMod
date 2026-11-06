package com.bathcrimsonthin.slimearisemod.recipe;

import com.bathcrimsonthin.slimearisemod.item.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public class WitchSlimeBallRecipe extends CustomRecipe {

    public WitchSlimeBallRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        // クラフト枠内のアイテム数が正確に2つであることを確認
        if (input.ingredientCount() != 2) {
            return false;
        }

        boolean hasBall = false;
        boolean hasPotion = false;

        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) continue;

            // 無色の WitchSlimeBall かどうか
            if (stack.is(ModItems.WITCH_SLIME_BALL) && !stack.has(DataComponents.POTION_CONTENTS)) {
                if (hasBall) return false;
                hasBall = true;
            }
            // ポーション成分（DataComponents.POTION_CONTENTS）を所有しているか
            else if (stack.has(DataComponents.POTION_CONTENTS)) {
                if (hasPotion) return false;
                hasPotion = true;
            } else {
                return false;
            }
        }

        return hasBall && hasPotion;
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        ItemStack potionStack = ItemStack.EMPTY;

        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.has(DataComponents.POTION_CONTENTS)) {
                potionStack = stack;
                break;
            }
        }

        if (potionStack.isEmpty()) {
            return ItemStack.EMPTY;
        }

        // 新しい WitchSlimeBall にポーションデータ（効果およびグラデーションカラー）を移植
        ItemStack result = new ItemStack(ModItems.WITCH_SLIME_BALL);
        PotionContents contents = potionStack.get(DataComponents.POTION_CONTENTS);
        if (contents != null) {
            result.set(DataComponents.POTION_CONTENTS, contents);
        }

        return result;
    }

    // クラフト完了時の残余アイテム処理
    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
        NonNullList<ItemStack> remainders = NonNullList.withSize(input.size(), ItemStack.EMPTY);

        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);

            // ポーション成分を持つアイテム（ポーション瓶）があるスロットには「空のガラス瓶」を返却
            if (stack.has(DataComponents.POTION_CONTENTS)) {
                remainders.set(i, new ItemStack(Items.GLASS_BOTTLE));
            }
            // WitchSlimeBall 側のスロットはそのまま消費されるため EMPTY のまま
        }

        return remainders;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.WITCH_SLIME_BALL_RECIPE_SERIALIZER;
    }
}