package agai.heatmod.content.temperature.modifier.environment;

import agai.heatmod.content.temperature.modifier.DynamicModifier;
import agai.heatmod.content.temperature.modifier.altitude.AltitudeTemperatureModifier;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;

/**
 * 职责：管理昼夜周期对温度的影响
 * 核心功能：
 * 根据时间计算太阳辐射强度
 * 模拟白天升温、夜间降温曲线
 * 考虑季节因素（如果有季节系统）
 * 处理不同纬度 / 生物群系的昼夜温差差异*/
public class DayNightTemperatureModifier extends DynamicModifier<DayNightTemperatureModifier> {
    public static final Codec<DayNightTemperatureModifier> CODEC= RecordCodecBuilder.create(instance->
            instance.group(Codec.FLOAT.fieldOf("temp").forGetter(DayNightTemperatureModifier::getTest)).apply(instance,DayNightTemperatureModifier::new));

    public DayNightTemperatureModifier(float temperatureDelta) {
        super(temperatureDelta);
    }

    public DayNightTemperatureModifier() {
        super();
    }

    @Override
    public float getTemperatureDelta(LevelAccessor world, BlockPos pos) {
        return -5;
    }

    @Override
    public TempModifierType getTempModifierType() {
        return TempModifierType.ENVIRONMENT;
    }

    @Override
    public Codec<DayNightTemperatureModifier> getCodec() {
        return CODEC;
    }
}
