package agai.heatmod.content.temperature.modifier.entity;

import agai.heatmod.content.temperature.modifier.DynamicModifier;
import agai.heatmod.content.temperature.modifier.altitude.AltitudeTemperatureModifier;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;

/**
 * 职责：处理装备的隔热 / 保温效果
 * 核心功能：
 * 计算装备提供的热防护等级
 * 处理不同材料装备的热学特性
 * 管理特殊温控装备（如制冷服、加热甲）的效果*/
public class ThermalArmorModifier extends DynamicModifier<ThermalArmorModifier> {
    public static final Codec<ThermalArmorModifier> CODEC= RecordCodecBuilder.create( instance->
            instance.group(Codec.FLOAT.fieldOf("temp").forGetter(ThermalArmorModifier::getTest)).apply(instance,ThermalArmorModifier::new));

    public ThermalArmorModifier(float temperatureDelta) {
        super(temperatureDelta);
    }

    public ThermalArmorModifier() {
        super();
    }

    @Override
    public float getTemperatureDelta(LevelAccessor world, BlockPos pos) {
        return -4;
    }

    @Override
    public TempModifierType getTempModifierType() {
        return TempModifierType.CLOTHES;
    }

    @Override
    public Codec<ThermalArmorModifier> getCodec() {
        return CODEC;
    }
}
