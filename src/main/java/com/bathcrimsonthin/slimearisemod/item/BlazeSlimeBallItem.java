package com.bathcrimsonthin.slimearisemod.item;

import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class BlazeSlimeBallItem extends Item {
    public BlazeSlimeBallItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull ItemStack finishUsingItem(ItemStack itemStack, Level level, LivingEntity livingEntity) {
        if(!level.isClientSide){
            ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
            for(int i = 0;i < 3;i++){
                // 150ms毎に時間差実行
                scheduler.schedule(() -> {
                    if(level.getServer() != null) {
                        level.getServer().execute(() -> {
                            // Minecraftのメインスレッドで安全にエンティティを生成する
                            // 射出方向(ばらつきアリ)
                            Vec3 look = livingEntity.getLookAngle();
                            RandomSource random = level.getRandom();
                            double offsetX = (random.nextDouble() - 0.5) * 0.08;
                            double offsetY = (random.nextDouble() - 0.5) * 0.08;
                            double offsetZ = (random.nextDouble() - 0.5) * 0.08;
                            Vec3 targetVector = new Vec3(
                                    look.x + offsetX,
                                    look.y + offsetY,
                                    look.z + offsetZ
                            ).normalize();

                            // 出現位置(口元)
                            Vec3 eyePos = livingEntity.getEyePosition();
                            double spawnX = eyePos.x + targetVector.x * 0.8;
                            double spawnY = eyePos.y + targetVector.y * 0.8 - 0.2;
                            double spawnZ = eyePos.z + targetVector.z * 0.8;
                            // 世界, オーナー, 速度を設定
                            SmallFireball fireball = new SmallFireball(level, livingEntity, targetVector.scale(0.1));
                            // 出現位置を設定
                            fireball.setPos(spawnX, spawnY, spawnZ);
                            //　世界に追加
                            level.addFreshEntity(fireball);
                        });
                    }
                }, i * 150,  TimeUnit.MILLISECONDS);
            }
        }
        return super.finishUsingItem(itemStack, level, livingEntity);
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 24; // クッキー(16 ticks(0.8秒))とパン(32 ticks(1.6秒))の中間
    }
}
