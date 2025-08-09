package agai.heatmod.data.temperature.recipeData;


import agai.heatmod.utils.recipe.CodecRecipeSerializer;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nonnull;
import java.util.Map;

public record BiomeTempData(ResourceLocation biome, float temperature) {
    public static final Codec<BiomeTempData> CODEC= RecordCodecBuilder.create(t->t.group(
            ResourceLocation.CODEC.fieldOf("biome").forGetter(o->o.biome),
            Codec.FLOAT.fieldOf("temperature").forGetter(o->o.temperature)
    ).apply(t,BiomeTempData::new));
    public static RegistryObject<CodecRecipeSerializer<BiomeTempData>> TYPE;
    public static Map<ResourceLocation,BiomeTempData> cacheList= ImmutableMap.of();

    @Nonnull
    public static Float getBiomeTemp(LevelReader w, Biome b) {
        if (b == null) return 0f;
        // For some fucking reason, you have to use the following
        ResourceLocation key = w.registryAccess().registryOrThrow(Registries.BIOME).getKey(b);
        // instead of the following
        // ResourceLocation key = ForgeRegistries.BIOMES.getKey(b);
        // to get ResourceKey at runtime for biomes (with datapacks augmented).
        // See https://forums.minecraftforge.net/topic/123341-1194how-can-i-get-the-name-of-a-biome/
        // And https://forums.minecraftforge.net/topic/113668-1182-force-clientsided-chunk-update/#comment-505208
        BiomeTempData data = cacheList.get(key);
        if (data != null)
            return data.temperature();
        return 0F;
    }
    public FinishedRecipe toFinished(ResourceLocation name) {
        return TYPE.get().toFinished(name, this);
    }
}
