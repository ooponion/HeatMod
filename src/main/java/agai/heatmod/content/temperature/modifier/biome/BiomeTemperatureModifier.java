package agai.heatmod.content.temperature.modifier.biome;

import agai.heatmod.content.temperature.modifier.DynamicModifier;
import agai.heatmod.content.temperature.modifier.altitude.AltitudeTemperatureModifier;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;

/**
 * 职责：管理Biome对温度的影响
 * 核心功能：
 * 处理原版Biome的温度影响*/
public class BiomeTemperatureModifier extends DynamicModifier<BiomeTemperatureModifier> {
    public static final Codec<BiomeTemperatureModifier> CODEC= RecordCodecBuilder.create(instance->
            instance.group(Codec.FLOAT.fieldOf("temp").forGetter(BiomeTemperatureModifier::getTest)).apply(instance,BiomeTemperatureModifier::new));

    public BiomeTemperatureModifier(float temperatureDelta) {
        super(temperatureDelta);
    }

    public BiomeTemperatureModifier() {
        super();
    }

    @Override
    public float getTemperatureDelta(LevelAccessor world, BlockPos pos) {
        return -2;
    }

    @Override
    public TempModifierType getTempModifierType() {
        return TempModifierType.BIOME;
    }

    @Override
    public Codec<BiomeTemperatureModifier> getCodec() {
        return CODEC;
    }
}
