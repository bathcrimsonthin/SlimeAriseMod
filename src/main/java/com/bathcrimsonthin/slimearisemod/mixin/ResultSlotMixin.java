package com.bathcrimsonthin.slimearisemod.mixin;

import com.bathcrimsonthin.slimearisemod.recipe.WitchSlimeBallRecipe;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ResultSlot.class)
public abstract class ResultSlotMixin {

    @Inject(method = "onTake", at = @At("HEAD"))
    private void slimearisemod$onTakeHead(Player player, ItemStack stack, CallbackInfo ci) {
        if (!player.level().isClientSide) {
            WitchSlimeBallRecipe.resolveRandomPotion(stack, player.getRandom());
        }
    }

    @Inject(method = "onTake", at = @At("TAIL"))
    private void slimearisemod$onTakeTail(Player player, ItemStack stack, CallbackInfo ci) {
        if (!player.level().isClientSide) {
            // Shift+クリックによる一括クラフトでインベントリに入った分も1つずつ確定させる
            Inventory inventory = player.getInventory();
            for (int i = 0; i < inventory.getContainerSize(); i++) {
                WitchSlimeBallRecipe.resolveRandomPotion(inventory.getItem(i), player.getRandom());
            }
        }
    }
}