package com.bathcrimsonthin.slimearisemod.recipe;

import com.bathcrimsonthin.slimearisemod.item.ModItems;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Optional;

public class WitchSlimeBallRecipe extends CustomRecipe {

    public static final String RANDOM_POTION_TAG = "slimearisemod:random_witch_potion";

    private static final List<Holder<MobEffect>> WITCH_EFFECTS = List.of(
            MobEffects.WATER_BREATHING,
            MobEffects.FIRE_RESISTANCE,
            MobEffects.REGENERATION,
            MobEffects.OOZING,
            MobEffects.WEAVING,
            MobEffects.POISON,
            MobEffects.MOVEMENT_SLOWDOWN,
            MobEffects.WEAKNESS,
            MobEffects.JUMP,
            MobEffects.DAMAGE_BOOST,
            MobEffects.MOVEMENT_SPEED
    );

    public WitchSlimeBallRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        if (input.ingredientCount() != 4) {
            return false;
        }

        int ballCount = 0;
        int bottleCount = 0;

        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) continue;

            if (stack.is(ModItems.WITCH_SLIME_BALL) && !stack.has(DataComponents.POTION_CONTENTS)) {
                ballCount++;
            } else if (stack.is(Items.GLASS_BOTTLE)) {
                bottleCount++;
            } else {
                return false;
            }
        }

        return ballCount == 3 && bottleCount == 1;
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        // 作業台プレビュー時点では効果を付与せず、識別タグ付きの未確定残留ポーションを返す
        ItemStack result = new ItemStack(Items.LINGERING_POTION);
        result.set(DataComponents.ITEM_NAME, Component.translatable("item.slimearisemod.mystery_lingering_potion"));
        CustomData.update(DataComponents.CUSTOM_DATA, result, tag -> tag.putBoolean(RANDOM_POTION_TAG, true));
        return result;
    }

    // 実際に作業台から取り出された瞬間にランダムな効果（残留ポーションで60秒 = 4800 ticks）へ確定させる
    public static void resolveRandomPotion(ItemStack stack, RandomSource random) {
        if (stack.isEmpty() || !stack.is(Items.LINGERING_POTION)) {
            return;
        }

        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData == null || !customData.contains(RANDOM_POTION_TAG)) {
            return;
        }

        stack.remove(DataComponents.CUSTOM_DATA);
        stack.remove(DataComponents.ITEM_NAME);

        Holder<MobEffect> selectedEffect = WITCH_EFFECTS.get(random.nextInt(WITCH_EFFECTS.size()));
        // 残留ポーションは効果時間が1/4になるため、60秒(1200 ticks)にするには4倍の4800 ticksを指定する
        MobEffectInstance effectInstance = new MobEffectInstance(selectedEffect, 1200, 0);

        PotionContents contents = new PotionContents(
                Optional.empty(),
                Optional.of(selectedEffect.value().getColor()),
                List.of(effectInstance)
        );
        stack.set(DataComponents.POTION_CONTENTS, contents);
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 4;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.WITCH_SLIME_BALL_RECIPE_SERIALIZER;
    }
}