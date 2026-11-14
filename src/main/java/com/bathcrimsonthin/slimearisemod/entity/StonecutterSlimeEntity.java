package com.bathcrimsonthin.slimearisemod.entity;

import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.StonecutterRecipe;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class StonecutterSlimeEntity extends Slime {

    // 加工して排出したアイテムが即座に再加工されるのを防ぐためのUUID管理
    private final Set<UUID> processedItems = new HashSet<>();

    public StonecutterSlimeEntity(EntityType<? extends Slime> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes();
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide && this.level() instanceof ServerLevel serverLevel && this.isAlive()) {
            // 0.5秒（10tick）ごとに接触中のドロップアイテムを石切り加工する
            if (this.tickCount % 10 == 0) {
                this.processNearbyStoneItems(serverLevel);
            }

            // 消滅済みエンティティのUUIDを定期的に掃除
            if (this.tickCount % 200 == 0 && !this.processedItems.isEmpty()) {
                this.processedItems.removeIf(uuid -> serverLevel.getEntity(uuid) == null);
            }
        }
    }

    // 接触したドロップアイテムを石切り台レシピでランダムに加工して吐き出す
    private void processNearbyStoneItems(ServerLevel serverLevel) {
        List<ItemEntity> nearbyItems = serverLevel.getEntitiesOfClass(
                ItemEntity.class,
                this.getBoundingBox().inflate(0.35D),
                item -> item.isAlive() && !item.getItem().isEmpty() && !this.processedItems.contains(item.getUUID())
        );

        for (ItemEntity itemEntity : nearbyItems) {
            ItemStack stack = itemEntity.getItem();
            ItemStack singleInput = stack.copyWithCount(1);
            SingleRecipeInput recipeInput = new SingleRecipeInput(singleInput);

            List<RecipeHolder<StonecutterRecipe>> recipes = serverLevel.getRecipeManager()
                    .getRecipesFor(RecipeType.STONECUTTING, recipeInput, serverLevel);

            if (recipes.isEmpty()) {
                continue;
            }

            // 石切り台レシピの中からランダムに1つ選んで出力アイテムを生成
            StonecutterRecipe chosenRecipe = recipes.get(this.random.nextInt(recipes.size())).value();
            ItemStack outputStack = chosenRecipe.assemble(recipeInput, serverLevel.registryAccess());
            if (outputStack.isEmpty()) {
                continue;
            }

            // 元のアイテムを1個消費
            stack.shrink(1);
            if (stack.isEmpty()) {
                itemEntity.discard();
            } else {
                itemEntity.setItem(stack);
            }

            // 加工後のアイテムをスライムの上部（刃の位置）から弾き出す
            double spawnX = this.getX();
            double spawnY = this.getY(0.85D);
            double spawnZ = this.getZ();

            ItemEntity outputEntity = new ItemEntity(serverLevel, spawnX, spawnY, spawnZ, outputStack);
            outputEntity.setDeltaMovement(
                    (this.random.nextDouble() - 0.5D) * 0.25D,
                    0.22D + this.random.nextDouble() * 0.1D,
                    (this.random.nextDouble() - 0.5D) * 0.25D
            );
            outputEntity.setDefaultPickUpDelay();
            this.processedItems.add(outputEntity.getUUID());
            serverLevel.addFreshEntity(outputEntity);

            // 切断音・火花・削り屑パーティクルを発生させる
            this.playSound(SoundEvents.UI_STONECUTTER_TAKE_RESULT, 0.85F, 0.95F + this.random.nextFloat() * 0.2F);
            serverLevel.sendParticles(ParticleTypes.CRIT, spawnX, spawnY, spawnZ, 6, 0.15D, 0.15D, 0.15D, 0.15D);
            serverLevel.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, singleInput), spawnX, spawnY, spawnZ, 8, 0.15D, 0.15D, 0.15D, 0.1D);

            // 1回の判定につき1個ずつ加工する
            break;
        }
    }

    @Override
    protected void dealDamage(LivingEntity livingEntity) {
        if (this.isAlive() && this.isDealsDamage()) {
            int size = this.getSize();
            if (this.distanceToSqr(livingEntity) < 0.6D * (double) size * 0.6D * (double) size
                    && this.hasLineOfSight(livingEntity)) {
                // 無敵時間をリセットして連続ヒットさせる
                livingEntity.invulnerableTime = 0;

                // ノックバック打ち消し用に現在の速度を退避
                Vec3 prevMotion = livingEntity.getDeltaMovement();
                DamageSource damageSource = this.damageSources().mobAttack(this);

                // 威力を通常の半分にして攻撃
                if (livingEntity.hurt(damageSource, this.getAttackDamage() * 0.5F)) {
                    // ノックバックによる吹き飛びを無効化
                    livingEntity.setDeltaMovement(prevMotion);
                    livingEntity.hurtMarked = false;

                    // 金属切断音（連続ヒット時に音が割れすぎないよう3tickごとに再生）
                    if (this.tickCount % 3 == 0) {
                        this.playSound(SoundEvents.UI_STONECUTTER_TAKE_RESULT, 0.75F, 1.05F + (this.random.nextFloat() - 0.5F) * 0.2F);
                    }

                    if (this.level() instanceof ServerLevel serverLevel) {
                        // 接触地点に火花パーティクルを散らす
                        double hitX = (this.getX() + livingEntity.getX()) * 0.5D;
                        double hitY = (this.getY(0.5D) + livingEntity.getY(0.5D)) * 0.5D;
                        double hitZ = (this.getZ() + livingEntity.getZ()) * 0.5D;
                        serverLevel.sendParticles(ParticleTypes.CRIT, hitX, hitY, hitZ, 4, 0.15D, 0.15D, 0.15D, 0.15D);

                        EnchantmentHelper.doPostAttackEffects(serverLevel, livingEntity, damageSource);
                    }
                }
            }
        }
    }
}