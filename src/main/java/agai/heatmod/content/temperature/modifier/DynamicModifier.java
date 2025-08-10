package agai.heatmod.content.temperature.modifier;

import agai.heatmod.annotators.ApiDoc;
import agai.heatmod.content.temperature.modifier.altitude.AltitudeTemperatureModifier;
import agai.heatmod.content.temperature.modifier.biome.BiomeTemperatureModifier;
import agai.heatmod.content.temperature.modifier.entity.EntityTemperatureModifier;
import agai.heatmod.content.temperature.modifier.entity.ThermalArmorModifier;
import agai.heatmod.content.temperature.modifier.environment.DayNightTemperatureModifier;
import agai.heatmod.content.temperature.modifier.weather.WeatherTemperatureModifier;
import agai.heatmod.utils.SystemOutHelper;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;

import java.util.*;
@Deprecated
@ApiDoc(description = "作用: 给modifiers一个抽象类")
public abstract class DynamicModifier<T>{
    public static final Codec<DynamicModifier<?>> CODEC = createPolymorphicCodec();


    protected float temperatureDelta;

    public DynamicModifier(float temperatureDelta) {
        this.temperatureDelta = temperatureDelta;
    }
    public DynamicModifier() {
        this.temperatureDelta = 0;
    }

    public float getTest(){
        return -114f;
    }
    public abstract float getTemperatureDelta(LevelAccessor world, BlockPos pos);
    public abstract TempModifierType getTempModifierType();
    public abstract Codec<? extends DynamicModifier<?>> getCodec();

    public enum TempModifierType {
        ALTITUDE(AltitudeTemperatureModifier.CODEC),
        BIOME(BiomeTemperatureModifier.CODEC),
        ENTITY(EntityTemperatureModifier.CODEC),
        CLOTHES(ThermalArmorModifier.CODEC),
        ENVIRONMENT(DayNightTemperatureModifier.CODEC),
        WEATHER(WeatherTemperatureModifier.CODEC);

        private final Codec<? extends DynamicModifier<?>> codec;

        TempModifierType(Codec<? extends DynamicModifier<?>> codec) {
            this.codec = codec;
        }

        public Codec<? extends DynamicModifier<?>> getCodec() {
            return codec;
        }

        public static final Codec<TempModifierType> CODEC = Codec.STRING.xmap(
                name -> valueOf(name.toUpperCase(Locale.ROOT)),
                type -> type.name().toLowerCase(Locale.ROOT)
        );
    }

    private static Codec<DynamicModifier<?>> createPolymorphicCodec() {
        return Codec.of(
                new Encoder<>() {
                    @Override
                    public <T> DataResult<T> encode(DynamicModifier<?> input, DynamicOps<T> ops, T prefix) {
                        try {
                            // 三层防御检查
                            if (input == null)
                                return DataResult.error(() -> "Input modifier is null");

                            TempModifierType type = input.getTempModifierType();
                            if (type == null)
                                return DataResult.error(() -> "Modifier type not defined: " + input.getClass());

                            @SuppressWarnings("unchecked")
                            Codec<DynamicModifier<?>> codec = (Codec<DynamicModifier<?>>) type.getCodec(); // 改用枚举中的codec

                            // 安全序列化
                            return codec.encode(input, ops, prefix)
                                    .flatMap(data -> ops.mapBuilder()
                                            .add("type", ops.createString(type.name().toLowerCase(Locale.ROOT)))
                                            .add("data", data)
                                            .build(prefix));
                        } catch (Exception e) {
                            return DataResult.error(() -> "Failed to encode modifier: " + e.getMessage());
                        }
                    }
                },
                new Decoder<>() {
                    @Override
                    public <T> DataResult<Pair<DynamicModifier<?>, T>> decode(DynamicOps<T> ops, T input) {
                        DataResult<Pair<DynamicModifier<?>, T>> result =ops.getMap(input).flatMap(map -> {
                            // 获取类型字段
                            T typeValue = map.get("type");
                            if (typeValue == null) {
                                return DataResult.error(() -> "缺少 'type' 字段");
                            }

                            DataResult<String> typeNameResult = ops.getStringValue(typeValue);
                            if (typeNameResult.error().isPresent()) {
                                return DataResult.error(() -> "无效的类型字段格式");
                            }
                            SystemOutHelper.printfplain("decode>1");
                            String typeName = typeNameResult.result().get();
                            TempModifierType type;
                            try {
                                type = TempModifierType.valueOf(typeName.toUpperCase(Locale.ROOT));
                            } catch (IllegalArgumentException e) {
                                return DataResult.error(() -> "无效的温度修改器类型: " + typeName);
                            }
                            SystemOutHelper.printfplain("decode>2");
                            // 获取数据字段
                            T data = map.get("data");
                            if (data == null) {
                                return DataResult.error(() -> "缺少 'data' 字段");
                            }
                            SystemOutHelper.printfplain("decode>3");
                            // 解码具体数据
                            return type.getCodec().decode(ops, data)
                                    .map(pair -> Pair.of(pair.getFirst(), pair.getSecond()));
                        });
                        SystemOutHelper.printfplain("decode>4");
                        return result;
                    }
                }
        );
    }
}
