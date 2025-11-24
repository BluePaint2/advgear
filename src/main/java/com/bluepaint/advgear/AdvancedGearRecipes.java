package com.bluepaint.advgear;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllTags;
import com.simibubi.create.api.data.recipe.DeployingRecipeGen;
import com.simibubi.create.api.data.recipe.SequencedAssemblyRecipeGen;
import com.simibubi.create.content.kinetics.deployer.DeployerApplicationRecipe;
import com.simibubi.create.foundation.data.recipe.CreateRecipeProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.concurrent.CompletableFuture;

import static net.minecraft.data.recipes.RecipeBuilder.getDefaultRecipeId;

@EventBusSubscriber(modid = CreateAdvancedGearbox.MODID)
public class AdvancedGearRecipes {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();
        generator.addProvider(event.includeServer(), new AdvGearCraftingRecipe(output, lookupProvider));
        generator.addProvider(event.includeServer(), new AdvGearDeployerRecipe(output, lookupProvider));
    }

    private static void saveRecipe(RecipeOutput output, String recipeType, String recipeName, RecipeBuilder recipeBuilder) {
        String path = String.format("%s/%s", recipeType, recipeName);
        recipeBuilder.save(output, ResourceLocation.fromNamespaceAndPath(CreateAdvancedGearbox.MODID, path));
    }

    public static class AdvGearCraftingRecipe extends RecipeProvider {

        public AdvGearCraftingRecipe(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
            super(output, registries);
        }

        @Override
        protected void buildRecipes(RecipeOutput recipeOutput) {
            AdvancedGearRecipes.saveRecipe(recipeOutput, "crafting", "advanced_gearbox_horizontal", ShapedRecipeBuilder.shaped(RecipeCategory.MISC, AdvancedGearbox.ADVANCED_GEARBOX.asItem(), 1)
                    .pattern("CAC")
                    .define('C', AllBlocks.COGWHEEL.get())
                    .define('A', AllBlocks.ANDESITE_CASING.get())
                    .unlockedBy("has_cogwheel", has(AllBlocks.COGWHEEL.asItem()))
                    .unlockedBy("has_casing", has(AllTags.AllItemTags.CASING.tag))
            );
            AdvancedGearRecipes.saveRecipe(recipeOutput, "crafting", "advanced_gearbox_vertical", ShapedRecipeBuilder.shaped(RecipeCategory.MISC, AdvancedGearbox.ADVANCED_GEARBOX.asItem(), 1)
                    .pattern("C")
                    .pattern("A")
                    .pattern("C")
                    .define('C', AllBlocks.COGWHEEL.get())
                    .define('A', AllBlocks.ANDESITE_CASING.get())
                    .unlockedBy("has_cogwheel", has(AllBlocks.COGWHEEL.asItem()))
                    .unlockedBy("has_casing", has(AllTags.AllItemTags.CASING.tag))
            );
        }
    }

    public static class AdvGearDeployerRecipe extends SequencedAssemblyRecipeGen {

        public AdvGearDeployerRecipe(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
            super(output, registries, CreateAdvancedGearbox.MODID);
        }

        GeneratedRecipe ADVANCED_GEARBOX = create("advanced_gearbox", b -> b
                .transitionTo(AdvancedGearbox.INCOMPLETE_ADVANCED_GEARBOX.get())
                .require(AllBlocks.ANDESITE_CASING.get())
                .addOutput(AdvancedGearbox.ADVANCED_GEARBOX.asItem(), 1)
                .loops(2)
                .addStep(DeployerApplicationRecipe::new, rb -> rb.require(AllBlocks.COGWHEEL.get()))
        );
    }
}
