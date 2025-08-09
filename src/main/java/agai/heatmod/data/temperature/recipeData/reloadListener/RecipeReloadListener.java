package agai.heatmod.data.temperature.recipeData.reloadListener;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public class RecipeReloadListener implements ResourceManagerReloadListener {
    private ReloadableServerResources dataPackRegistries;
    public RecipeReloadListener(ReloadableServerResources dataPackRegistries) {
        this.dataPackRegistries = dataPackRegistries;
    }

    @Override
    public void onResourceManagerReload(@Nonnull ResourceManager resourceManager) {
        if(this.dataPackRegistries==null){
            return;
        }
        RecipeRebuiltHandler.buildRecipeLists(this.dataPackRegistries.getRecipeManager());
    }
}
