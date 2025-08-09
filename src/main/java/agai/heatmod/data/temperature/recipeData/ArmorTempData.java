package agai.heatmod.data.temperature.recipeData;

import agai.heatmod.content.temperature.player.BodyPart;
import agai.heatmod.utils.CodecUtil;
import agai.heatmod.utils.builder.EnumDefaultedMap;
import agai.heatmod.utils.recipe.CodecRecipeSerializer;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.Map;
import java.util.Optional;

public record ArmorTempData(Item item, Optional<BodyPart> slot, float insulation, float heat_proof, float wind_proof){
    public static final Codec<ArmorTempData> CODEC= RecordCodecBuilder.create(t->t.group(
            ForgeRegistries.ITEMS.getCodec().fieldOf("item").forGetter(o->o.item),
            CodecUtil.enumCodec(BodyPart.class).optionalFieldOf("slot").forGetter(o->o.slot),
            Codec.FLOAT.optionalFieldOf("factor",0f).forGetter(o->o.insulation),
            Codec.FLOAT.optionalFieldOf("heat_proof",0f).forGetter(o->o.heat_proof),
            Codec.FLOAT.optionalFieldOf("wind_proof",0f).forGetter(o->o.wind_proof)).apply(t, ArmorTempData::new));

    public static RegistryObject<CodecRecipeSerializer<ArmorTempData>> TYPE;
    public static Map<Item, EnumDefaultedMap<BodyPart,ArmorTempData>> cacheList= ImmutableMap.of();
    public static ArmorTempData getData(ItemStack is, BodyPart part) {
        EnumDefaultedMap<BodyPart, ArmorTempData> map=cacheList.get(is.getItem());
        if(map==null)return null;
        return map.get(part);
    }

    /**
     * a non-negative value inversely proportional to conductivity--which is (0,1]
     */
    public float getInsulation() {
        return insulation;
    }

    /**
     * A [0,1] value representing heat resistance
     */
    public float getHeatProof() {
        return heat_proof;
    }

    /**
     * A [0,1] value representing fluid resistance
     */
    public float getFluidResistance() {
        return wind_proof;
    }
    public FinishedRecipe toFinished(ResourceLocation name) {
        return TYPE.get().toFinished(name, this);
    }

}
