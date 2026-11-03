package com.bathcrimsonthin.slimearisemod.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.level.Level;

public class RabbitSlimeEntity extends Slime {

    public RabbitSlimeEntity(EntityType<? extends Slime> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes(){
        return Monster.createMonsterAttributes();
    }

    // 初速（ジャンプ力）の倍率変更
    // 初速を約 1.732 倍にすると跳躍高度が約3倍になり、初速を 3.0 倍にすると跳躍力が3倍になります
    @Override
    protected float getJumpPower() {
        return super.getJumpPower() * 1.732F;
    }

    @Override
    protected int calculateFallDamage(float fallDistance, float damageMultiplier) {
        // 通常の落下距離計算から自身の大ジャンプ分 (3.0F) を差し引く
        return super.calculateFallDamage(fallDistance - 3.0F, damageMultiplier);
    }
}