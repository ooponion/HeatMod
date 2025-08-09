package agai.heatmod.content.temperature.modifier.entity;

import agai.heatmod.content.temperature.modifier.DynamicModifier;
import agai.heatmod.content.temperature.modifier.altitude.AltitudeTemperatureModifier;
import agai.heatmod.content.temperature.modifier.biome.BiomeTemperatureModifier;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;

/**
 * 职责：管理实体（包括玩家）的体温
 * 核心功能：
 * 计算实体当前体温
 * 处理环境温度对实体的影响
 * 管理体温调节机制（如生物的自身调节）
 * 计算体温过高 / 过低的影响*/
public class EntityTemperatureModifier extends DynamicModifier<EntityTemperatureModifier> {
    public static final Codec<EntityTemperatureModifier> CODEC= RecordCodecBuilder.create( instance->
            instance.group(Codec.FLOAT.fieldOf("temp").forGetter(EntityTemperatureModifier::getTest)).apply(instance,EntityTemperatureModifier::new));

    public EntityTemperatureModifier(float temperatureDelta) {
        super(temperatureDelta);
    }

    public EntityTemperatureModifier() {
        super();
    }

    @Override
    public float getTemperatureDelta(LevelAccessor world, BlockPos pos) {
        return -3;
    }

    @Override
    public TempModifierType getTempModifierType() {
        return TempModifierType.ENTITY;
    }

    @Override
    public Codec<EntityTemperatureModifier> getCodec() {
        return CODEC;
    }
}
