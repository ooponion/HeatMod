package agai.heatmod.data.temperature.recipeData.reloadListener;

import agai.heatmod.content.temperature.player.BodyPart;
import agai.heatmod.data.temperature.recipeData.*;
import agai.heatmod.utils.builder.EnumDefaultedMap;
import agai.heatmod.utils.recipe.DataContainerRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.registries.RegistryObject;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.mojang.text2speech.Narrator.LOGGER;

public class RecipeRebuiltHandler {

    public static void buildRecipeLists(RecipeManager recipeManager) {
        LOGGER.info("Building recipe lists");
        Collection<Recipe<?>> recipes = recipeManager.getRecipes();
        if (recipes.isEmpty())
            return;

        ArmorTempData.cacheList=new HashMap<>();
        Function<Item, EnumDefaultedMap<BodyPart, ArmorTempData>> armorMapGetter= t->new EnumDefaultedMap<>(BodyPart.class);
        ArmorTempData.TYPE.get().filterRecipes(recipes).forEach(t->{
            ArmorTempData.cacheList.computeIfAbsent(t.getData().item(), armorMapGetter).put(t.getData().slot().orElse(null), t.getData());
        });;
        BiomeTempData.cacheList=BiomeTempData.TYPE.get().filterRecipes(recipes).collect(Collectors.toMap(t->t.getData().biome(), DataContainerRecipe::getData));
        BlockTempData.updateCache(recipeManager);
        StateTransitionData.updateCache(recipeManager);
//        CupData.cacheList=CupData.TYPE.get().filterRecipes(recipes).collect(Collectors.toMap(t->t.getData().item(), t->t.getData()));
//        DrinkTempData.cacheList=DrinkTempData.TYPE.get().filterRecipes(recipes).collect(Collectors.toMap(t->t.getData().fluid(), t->t.getData()));
//        FoodTempData.cacheList=FoodTempData.TYPE.get().filterRecipes(recipes).collect(Collectors.toMap(t->t.getData().item(), t->t.getData()));
//        PlantTempData.cacheList=PlantTempData.TYPE.get().filterRecipes(recipes).collect(Collectors.toMap(t->t.getData().block(), t->t.getData()));
        DimensionTempData.cacheList=DimensionTempData.TYPE.get().filterRecipes(recipes).collect(Collectors.toMap(t->t.getData().world(), DataContainerRecipe::getData));
    }


    static <R extends Recipe<?>> Map<ResourceLocation, R> filterRecipes(Collection<Recipe<?>> recipes, Class<R> recipeClass) {
        return recipes.stream()
                .filter(iRecipe -> iRecipe.getClass() == recipeClass)
                .map(recipeClass::cast)
                .collect(Collectors.toMap(recipe -> recipe.getId(), recipe -> recipe));
    }

    static <R extends Recipe<?>> Map<ResourceLocation, R> filterRecipes(Collection<Recipe<?>> recipes, Class<R> recipeClass, RegistryObject<RecipeType<R>> recipeType) {
        return recipes.stream()
                .filter(iRecipe -> iRecipe.getType() == recipeType.get())
                .map(recipeClass::cast)
                .collect(Collectors.toMap(recipe -> recipe.getId(), recipe -> recipe));
    }
}
