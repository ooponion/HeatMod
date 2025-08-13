package agai.heatmod.data.temperature.recipeData;

import agai.heatmod.data.temperature.properties.BlockThermalProperties;
import agai.heatmod.utils.SystemOutHelper;
import agai.heatmod.utils.recipe.CodecRecipeSerializer;
import agai.heatmod.utils.recipe.DataContainerRecipe;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class BlockTempData{
    public static final Codec<BlockTempData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    ForgeRegistries.BLOCKS.getCodec().fieldOf("block").forGetter(BlockTempData::getBlock),
                    Codec.FLOAT.fieldOf("temperature").forGetter(BlockTempData::getSourceTemperature),
                    Codec.FLOAT.fieldOf("emissivity").forGetter(BlockTempData::getEmissivity),
                    Codec.FLOAT.fieldOf("specificHeatCapacity").forGetter(BlockTempData::getSpecificHeatCapacity),
                    Codec.FLOAT.fieldOf("thermalConductivity").forGetter(BlockTempData::getThermalConductivity),
                    Codec.FLOAT.fieldOf("mass").forGetter(BlockTempData::getMass),
                    Codec.INT.fieldOf("insulationLevel").forGetter(BlockTempData::getInsulationLevel),
                    Codec.BOOL.fieldOf("isActiveSource").forGetter(BlockTempData::isActiveSource),
                    Codec.FLOAT.fieldOf("sourcePower").forGetter(BlockTempData::getSourcePower)
            ).apply(instance, BlockTempData::new)
    );

    public BlockTempData(Block block,float temperature, float emissivity, float specificHeatCapacity, float thermalConductivity, float mass, int insulationLevel, boolean isActiveSource, float sourcePower) {
        this.block = block;
        this.temperature = temperature;
        this.emissivity = emissivity;
        this.specificHeatCapacity = specificHeatCapacity;
        this.thermalConductivity = thermalConductivity;
        this.mass = mass;
        this.insulationLevel = insulationLevel;
        this.isActiveSource = isActiveSource;
        this.sourcePower = sourcePower;
    }

    public static RegistryObject<CodecRecipeSerializer<BlockTempData>> TYPE;
    private static Map<Block,BlockTempData> CACHE = ImmutableMap.of();

    private final Block block;
    private float temperature;

    private float emissivity;//(0~1)
    private float specificHeatCapacity;
    private float thermalConductivity;
    private float mass;// 1block=1kg

    private int insulationLevel;//0-10
    private boolean isActiveSource;
    private float sourcePower;  //kw

    public static BlockTempData getDefaultData(Block block){
        return new BlockTempData(block,getDefaultSourceTemperature(),0.2f,400f,0.1f,0.2f,3,false,getDefaultSourcePower());
    }
    public static BlockTempData getData(Block block) {
        BlockTempData data = CACHE.get(block);
        if(data == null){
            return getDefaultData(block);
        }
        return data;
    }

    public static void updateCache(RecipeManager manager) {
        Collection<Recipe<?>> recipes = manager.getRecipes();
        BlockTempData.CACHE = BlockTempData.TYPE.get().filterRecipes(recipes).collect(Collectors.toMap(t->t.getData().getBlock(), DataContainerRecipe::getData));
    }
    public FinishedRecipe toFinished(ResourceLocation name) {
        return TYPE.get().toFinished(name, this);
    }





    public float getSourceTemperature() {
        return temperature;
    }

    public void setSourceTemperature(float temperature) {
        this.temperature = temperature;
    }

    public float getEmissivity() {
        return emissivity;
    }

    public void setEmissivity(float emissivity) {
        this.emissivity = emissivity;
    }

    public float getSpecificHeatCapacity() {
        return specificHeatCapacity;
    }

    public void setSpecificHeatCapacity(float specificHeatCapacity) {
        this.specificHeatCapacity = specificHeatCapacity;
    }

    public float getThermalConductivity() {
        return thermalConductivity;
    }

    public void setThermalConductivity(float thermalConductivity) {
        this.thermalConductivity = thermalConductivity;
    }

    public float getMass() {
        return mass;
    }

    public void setMass(float mass) {
        this.mass = mass;
    }

    public int getInsulationLevel() {
        return insulationLevel;
    }

    public void setInsulationLevel(int insulationLevel) {
        this.insulationLevel = insulationLevel;
    }

    public float getHeatCapacity() {
        return specificHeatCapacity*mass;
    }

    public boolean isActiveSource() {
        return isActiveSource;
    }

    public void setActiveSource(boolean activeSource) {
        isActiveSource = activeSource;
    }

    public float getSourcePower() {
        return sourcePower;
    }
    public static float getDefaultSourcePower() {
        return 0;
    }
    public static float getDefaultSourceTemperature() {
        return 27;
    }

    public void setSourcePower(float sourcePower) {
        this.sourcePower = sourcePower;
    }

    public Block getBlock() {
        return block;
    }
}