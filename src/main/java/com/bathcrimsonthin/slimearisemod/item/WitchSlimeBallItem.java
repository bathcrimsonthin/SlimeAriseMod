package com.bathcrimsonthin.slimearisemod.item;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;

public class WitchSlimeBallItem extends Item {

    public WitchSlimeBallItem(Properties properties) {
        super(properties
//                .stacksTo(64)
//                .food(new FoodProperties.Builder()
//                        .nutrition(0)
//                        .saturationModifier(0.0F)
//                        .alwaysEdible()
//                        .build())
        );
    }

//    @Override
//    public int getUseDuration(ItemStack stack, LivingEntity entity) {
//        return 24;
//    }
//
//    @Override
//    public UseAnim getUseAnimation(ItemStack stack) {
//        return UseAnim.EAT;
//    }
}