package agai.heatmod.content.temperature.modifier.weather;

import agai.heatmod.content.temperature.modifier.DynamicModifier;
import agai.heatmod.content.temperature.modifier.altitude.AltitudeTemperatureModifier;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;

/**
 * 职责：管理天气对温度的影响
 * 核心功能：
 * 处理原版天气（雨、雪、雷暴）的温度影响
 * 管理增强天气（热浪、寒潮、沙尘暴）
 * 计算天气持续时间和强度
 * 天气变化时的温度过渡效果*/
public class WeatherTemperatureModifier extends DynamicModifier<WeatherTemperatureModifier> {
    public static final Codec<WeatherTemperatureModifier> CODEC= RecordCodecBuilder.create( instance->
            instance.group(Codec.FLOAT.fieldOf("temp").forGetter(WeatherTemperatureModifier::getTest)).apply(instance, WeatherTemperatureModifier::new));

    public WeatherTemperatureModifier(float temperatureDelta) {
        super(temperatureDelta);
    }

    public WeatherTemperatureModifier() {
        super();
    }

    @Override
    public float getTemperatureDelta(LevelAccessor world, BlockPos pos) {
        return -6;
    }

    @Override
    public TempModifierType getTempModifierType() {
        return TempModifierType.WEATHER;
    }

    @Override
    public Codec<WeatherTemperatureModifier> getCodec() {
        return CODEC;
    }
}
