package agai.heatmod.content.temperature.modifier.altitude;

import agai.heatmod.content.temperature.modifier.DynamicModifier;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;

/**
 * 职责：处理海拔对温度的影响
 * 核心功能：
 * 根据 Y 坐标计算温度修正值
 * 模拟平流层 / 对流层温度变化规律
 * 考虑地形因素（如山谷、山顶）的微气候*/
public class AltitudeTemperatureModifier extends DynamicModifier<AltitudeTemperatureModifier> {
    public static final Codec<AltitudeTemperatureModifier> CODEC= RecordCodecBuilder.create( instance->
            instance.group(Codec.FLOAT.fieldOf("temp").forGetter(AltitudeTemperatureModifier::getTest)).apply(instance,AltitudeTemperatureModifier::new));

    public AltitudeTemperatureModifier(float temperatureDelta) {
        super(temperatureDelta);
    }
    public AltitudeTemperatureModifier() {
        super();
    }


    @Override
    public float getTemperatureDelta(LevelAccessor world, BlockPos pos) {
        return -1;
    }

    @Override
    public TempModifierType getTempModifierType() {
        return TempModifierType.ALTITUDE;
    }

    @Override
    public Codec<AltitudeTemperatureModifier> getCodec() {
        return CODEC;
    }
}
