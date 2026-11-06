package com.bathcrimsonthin.slimearisemod;

import com.bathcrimsonthin.slimearisemod.entity.ModEntities;
import com.bathcrimsonthin.slimearisemod.item.ModItemGroups;
import com.bathcrimsonthin.slimearisemod.item.ModItems;
import com.bathcrimsonthin.slimearisemod.mixin.AreaEffectCloudAccessor;
import com.bathcrimsonthin.slimearisemod.particle.ModParticles;
import com.bathcrimsonthin.slimearisemod.recipe.ModRecipes;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;

import java.util.List;
import java.util.Optional;

import net.minecraft.resources.ResourceLocation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SlimeAriseMod implements ModInitializer {
	public static final String MOD_ID = "slimearisemod";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ModItems.registerModItems();
		ModItemGroups.registerItemGroups();
		ModEntities.registerModEntities();
		ModParticles.registerModParticles();
		ModRecipes.registerRecipes();
	}

}
