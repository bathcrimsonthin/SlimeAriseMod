package com.bathcrimsonthin.slimearisemod.mixin;

import com.bathcrimsonthin.slimearisemod.entity.SlamAnimationHelper;
import com.bathcrimsonthin.slimearisemod.item.SlimySwordItem;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class PlayerSlamMixin implements SlamAnimationHelper.SlamPlayerAccess {

    @Unique
    private static final EntityDataAccessor<Integer> SLIMEARISEMOD_SLAM_TICKS =
            SynchedEntityData.defineId(Player.class, EntityDataSerializers.INT);

    @Unique
    private static final EntityDataAccessor<Boolean> SLIMEARISEMOD_SLAM_MAIN_HAND =
            SynchedEntityData.defineId(Player.class, EntityDataSerializers.BOOLEAN);

    @Inject(method = "defineSynchedData", at = @At("TAIL"))
    private void slimearisemod$defineSlamData(SynchedEntityData.Builder builder, CallbackInfo ci) {
        builder.define(SLIMEARISEMOD_SLAM_TICKS, 0);
        builder.define(SLIMEARISEMOD_SLAM_MAIN_HAND, true);
    }

    @Override
    public int slimearisemod$getSlamTicks() {
        Player self = (Player) (Object) this;
        return self.getEntityData().get(SLIMEARISEMOD_SLAM_TICKS);
    }

    @Override
    public void slimearisemod$setSlamTicks(int ticks) {
        Player self = (Player) (Object) this;
        self.getEntityData().set(SLIMEARISEMOD_SLAM_TICKS, Math.max(0, ticks));
    }

    @Override
    public boolean slimearisemod$isSlamMainHand() {
        Player self = (Player) (Object) this;
        return self.getEntityData().get(SLIMEARISEMOD_SLAM_MAIN_HAND);
    }

    @Override
    public void slimearisemod$setSlamMainHand(boolean mainHand) {
        Player self = (Player) (Object) this;
        self.getEntityData().set(SLIMEARISEMOD_SLAM_MAIN_HAND, mainHand);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void slimearisemod$onPlayerTick(CallbackInfo ci) {
        Player self = (Player) (Object) this;
        if (self.level().isClientSide) {
            return;
        }

        int ticks = this.slimearisemod$getSlamTicks();
        if (ticks > 0) {
            if (ticks == SlamAnimationHelper.SLAM_IMPACT_TICK && self.level() instanceof ServerLevel serverLevel) {
                SlimySwordItem.executeSlamImpact(serverLevel, self);
            }
            this.slimearisemod$setSlamTicks(ticks - 1);
        }
    }
}