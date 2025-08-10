package agai.heatmod.data.temperature.data;

import agai.heatmod.annotators.ApiDoc;
import agai.heatmod.content.temperature.modifier.DynamicModifier;
import agai.heatmod.content.temperature.modifier.altitude.AltitudeTemperatureModifier;
import agai.heatmod.content.temperature.modifier.biome.BiomeTemperatureModifier;
import agai.heatmod.content.temperature.modifier.entity.EntityTemperatureModifier;
import agai.heatmod.content.temperature.modifier.entity.ThermalArmorModifier;
import agai.heatmod.content.temperature.modifier.environment.DayNightTemperatureModifier;
import agai.heatmod.content.temperature.modifier.weather.WeatherTemperatureModifier;
import agai.heatmod.data.temperature.data.impl.GlobalTemperatureIntf;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.*;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

@ApiDoc(description = "* 职责：管理全局温度配置和状态\n" +
        " * 核心功能：\n" +
        " * 存储世界范围的温度参数\n" +
        " * 管理当前全球天气状态\n" +
        " * 保存温度系统的全局设置")

public class GlobalTemperatureData implements GlobalTemperatureIntf, Serializable {
    public static final Codec<GlobalTemperatureData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    // 使用中间变量明确类型
                    Codec.list(DynamicModifier.CODEC)
                            .xmap(
                                    list -> {return (Set<DynamicModifier<?>>) new HashSet<DynamicModifier<?>>(list);},
                                    ArrayList::new // 自动类型推断
                            )
                            .optionalFieldOf("modifiers", Collections.emptySet())
                            .forGetter(GlobalTemperatureData::getModifiers)
            ).apply(instance, GlobalTemperatureData::new)
    );
    private final Set<DynamicModifier<?>> modifiers;

    public GlobalTemperatureData(Set<DynamicModifier<?>> dynamicModifiers) {
        this.modifiers=new HashSet<>(dynamicModifiers);
    }

    public Set<DynamicModifier<?>> getModifiers() {
        return Collections.unmodifiableSet(modifiers);
    }
    public GlobalTemperatureData() {
        this.modifiers=new HashSet<DynamicModifier<?>>();
        this.modifiers.add(new AltitudeTemperatureModifier());
        this.modifiers.add(new BiomeTemperatureModifier());
        this.modifiers.add(new EntityTemperatureModifier());
        this.modifiers.add(new ThermalArmorModifier());
        this.modifiers.add(new DayNightTemperatureModifier());
        this.modifiers.add(new WeatherTemperatureModifier());
    }

    public float getGlobalTemperature(LevelAccessor world, BlockPos pos) {
        AtomicReference<Float> temperature= new AtomicReference<>(0f);
        modifiers.forEach((modifier)->{
            temperature.updateAndGet(v -> (v + modifier.getTemperatureDelta(world, pos)));});
        return temperature.get();
    }
}
