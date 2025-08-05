//package agai.heatmod.examples;
//
//public class CodecUtil {
//    public static final Codec<PlayerStats> CODEC = RecordCodecBuilder.create(instance ->
//            instance.group(
//                    // 定义每个字段的编解码规则：类型 + 字段名 +  getter 方法
//                    Codec.INT.fieldOf("kills").forGetter(PlayerStats::kills),          // 整数类型字段 "kills"
//                    Codec.FLOAT.fieldOf("health").forGetter(PlayerStats::health),     // 浮点型字段 "health"
//                    Codec.BOOL.fieldOf("is_vip").forGetter(PlayerStats::isVIP)        // 布尔型字段 "is_vip"
//            ).apply(instance, PlayerStats::new)  // 用字段值构建 PlayerStats 对象
//    );
//}
