package agai.heatmod.data.temperature.recipeData;

import agai.heatmod.data.temperature.WorldTemperature;
import agai.heatmod.utils.recipe.CodecRecipeSerializer;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nonnull;
import java.util.Map;

public record DimensionTempData(ResourceLocation world, float temperature){
    public static final Codec<DimensionTempData> CODEC= RecordCodecBuilder.create(t->t.group(
            ResourceLocation.CODEC.fieldOf("world").forGetter(o->o.world),
            Codec.FLOAT.optionalFieldOf("temperature",0f).forGetter(o->o.temperature)).apply(t, DimensionTempData::new));
    public static RegistryObject<CodecRecipeSerializer<DimensionTempData>> TYPE;
    public static Map<ResourceLocation, DimensionTempData> cacheList= ImmutableMap.of();
    @Nonnull
    public static Float getWorldTemp(Level w) {
        DimensionTempData data = cacheList.get(w.dimension().location());
        if (data != null)
            return data.getTemp();
        return WorldTemperature.OVERWORLD_BASELINE;
    }
    public float getTemp() {
        return temperature;
    }
    public FinishedRecipe toFinished(ResourceLocation name) {
        return TYPE.get().toFinished(name, this);
    }
}