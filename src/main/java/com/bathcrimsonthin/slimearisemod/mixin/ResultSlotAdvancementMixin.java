package com.bathcrimsonthin.slimearisemod.mixin;

import com.bathcrimsonthin.slimearisemod.advancement.ModCriteriaTriggers;
import com.bathcrimsonthin.slimearisemod.item.ModItems;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ResultSlot.class)
public abstract class ResultSlotAdvancementMixin {

    @Shadow
    @Final
    private CraftingContainer craftSlots;

    @Shadow
    @Final
    private Player player;

    @Inject(method = "checkTakeAchievements", at = @At("HEAD"))
    private void slimearisemod$onTakeCraftedResult(ItemStack stack, CallbackInfo ci) {
        if (this.player instanceof ServerPlayer serverPlayer && stack.is(Items.LINGERING_POTION)) {
            for (int i = 0; i < this.craftSlots.getContainerSize(); i++) {
                if (this.craftSlots.getItem(i).is(ModItems.WITCH_SLIME_BALL)) {
                    ModCriteriaTriggers.WITCH_LINGERING_POTION.trigger(serverPlayer);
                    break;
                }
            }
        }
    }
}