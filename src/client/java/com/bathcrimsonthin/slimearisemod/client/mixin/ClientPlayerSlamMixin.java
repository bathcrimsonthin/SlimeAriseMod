package com.bathcrimsonthin.slimearisemod.client.mixin;

import com.bathcrimsonthin.slimearisemod.client.SlamCameraShake;
import com.bathcrimsonthin.slimearisemod.entity.SlamAnimationHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractClientPlayer.class)
public abstract class ClientPlayerSlamMixin {

    @Unique
    private int slimearisemod$lastClientSlamTicks = 0;

    @Inject(method = "tick", at = @At("HEAD"))
    private void slimearisemod$onClientPlayerTick(CallbackInfo ci) {
        AbstractClientPlayer self = (AbstractClientPlayer) (Object) this;
        int ticks = SlamAnimationHelper.getSlamTicks(self);

        if (this.slimearisemod$lastClientSlamTicks > SlamAnimationHelper.SLAM_IMPACT_TICK
                && ticks <= SlamAnimationHelper.SLAM_IMPACT_TICK
                && ticks > 0) {
            Player localPlayer = Minecraft.getInstance().player;
            if (localPlayer != null) {
                if (localPlayer == self) {
                    SlamCameraShake.trigger(1.0F);
                } else {
                    double dist = localPlayer.distanceTo(self);
                    if (dist < 12.0D) {
                        SlamCameraShake.trigger((float) (0.75D * (1.0D - dist / 12.0D)));
                    }
                }
            }
        }
        this.slimearisemod$lastClientSlamTicks = ticks;
    }
}