package agai.heatmod.bootstrap.thermal;

import agai.heatmod.data.temperature.recipeData.*;
import agai.heatmod.utils.recipe.CodecRecipeSerializer;
import agai.heatmod.utils.recipe.DataContainerRecipe;
import com.mojang.serialization.Codec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import static agai.heatmod.Heatmod.MODID;

public class TempRecipeRegistry {

    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(
            ForgeRegistries.RECIPE_SERIALIZERS, MODID);
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister.create(
            ForgeRegistries.RECIPE_TYPES, MODID);

    static {

        ArmorTempData.TYPE = createCodecRecipeType("armor_temp", ArmorTempData.CODEC);
        BiomeTempData.TYPE = createCodecRecipeType("biome_temp", BiomeTempData.CODEC);
        BlockTempData.TYPE = createCodecRecipeType("block_temp", BlockTempData.CODEC);
        StateTransitionData.TYPE = createCodecRecipeType("state_transition", StateTransitionData.CODEC);
//        CupData.TYPE = createCodecRecipeType("cup_temp", CupData.CODEC);
//        DrinkTempData.TYPE = createCodecRecipeType("drink_temp", DrinkTempData.CODEC);
//        FoodTempData.TYPE = createCodecRecipeType("food_temp", FoodTempData.CODEC);
//        PlantTempData.TYPE = createCodecRecipeType("plant_temp", PlantTempData.CODEC);
        DimensionTempData.TYPE = createCodecRecipeType("dimension_temp", DimensionTempData.CODEC);

    }

    public static <T extends Recipe<?>> RegistryObject<RecipeType<T>> createRecipeType(String name) {
        return RECIPE_TYPES.register(name, () -> RecipeType.simple(new ResourceLocation(MODID, name)));
    }
    public static <T> RegistryObject<CodecRecipeSerializer<T>> createCodecRecipeType(String name, Codec<T> codec) {
        RegistryObject<RecipeType<DataContainerRecipe<T>>> rct = RECIPE_TYPES.register(name, () -> RecipeType.simple(new ResourceLocation(MODID, name)));
        return RECIPE_SERIALIZERS.register(name, () -> new CodecRecipeSerializer<T>(codec, rct.get()));

    }
}