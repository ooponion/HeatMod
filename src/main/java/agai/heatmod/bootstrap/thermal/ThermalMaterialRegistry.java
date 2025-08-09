package agai.heatmod.bootstrap.thermal;

import agai.heatmod.annotators.ApiDoc;
import agai.heatmod.annotators.InTest;
import agai.heatmod.annotators.NeedImprovement;
import agai.heatmod.data.temperature.properties.BlockThermalProperties;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.Map;
@Deprecated
@NeedImprovement
/**改成使用recipe*/
@ApiDoc(description = " * 职责：管理和注册所有方块的热学属性\n" +
        " * 核心功能：\n" +
        " * 为不同方块 / 材料分配热学属性\n" +
        " * 提供按方块类型查询热学属性的接口\n" +
        " * 支持模组扩展注册新的材料属性")

public class ThermalMaterialRegistry {
    // 存储方块与热学属性的映射关系
    private static final Map<Block, BlockThermalProperties> BLOCK_THERMAL_PROPERTIES = new HashMap<>();

    // 默认属性（非热源方块）
    private static final BlockThermalProperties DEFAULT_PROPERTIES = new BlockThermalProperties(
            0.9f,    // 辐射率
            1000f,   // 比热容 (J/(kg·°C))
            0.5f,    // 导热系数 (W/(m·°C))
            2.5f,    // 质量 (kg)
            2,       // 隔热等级
            false,   // 非主动源
            0f,      // 功率
            25.0f    // 默认温度（环境温度）
    );

    /**
     * 注册原版方块属性（包含明确的热源温度）
     */
    public static void registerDefaultBlocks() {
        // 自然方块（非热源）
        register(Blocks.STONE, new BlockThermalProperties(
                0.9f,    // 辐射率
                800f,    // 比热容
                2.0f,    // 导热系数
                2.6f,    // 质量
                2,       // 隔热等级
                false,   // 非主动源
                0f,      // 功率
                25.0f    // 温度（默认环境温度）
        ));

        register(Blocks.DIRT, new BlockThermalProperties(
                0.9f, 1200f, 0.3f, 1.6f, 4,
                false, 0f, 25.0f
        ));

        register(Blocks.GRASS_BLOCK, new BlockThermalProperties(
                0.92f, 1300f, 0.25f, 1.5f, 5,
                false, 0f, 25.0f
        ));

        // 木质/隔热方块
        register(Blocks.OAK_PLANKS, new BlockThermalProperties(
                0.8f, 1700f, 0.12f, 0.7f, 6,
                false, 0f, 25.0f
        ));

        register(Blocks.BLACK_WOOL, new BlockThermalProperties(//暂时就加这个
                0.7f, 1300f, 0.04f, 0.3f, 8,
                false, 0f, 25.0f
        ));

        // 金属方块
        register(Blocks.IRON_BLOCK, new BlockThermalProperties(
                0.2f, 450f, 80f, 7.8f, 1,
                false, 0f, 25.0f
        ));

        // 液体（非主动热源但有温度）
        register(Blocks.WATER, new BlockThermalProperties(
                0.95f, 4186f, 0.6f, 1.0f, 2,
                false, 0f, 15.0f  // 水默认15°C
        ));

        // 主动热源（带明确温度）
        register(Blocks.LAVA, new BlockThermalProperties(
                0.98f,   // 辐射率（高温物体高）
                800f,    // 比热容
                1.0f,    // 导热系数
                2.5f,    // 质量
                1,       // 隔热等级
                true,    // 主动热源
                1000f,   // 功率（高）
                800.0f   // 岩浆温度（800°C）
        ));

        register(Blocks.FIRE, new BlockThermalProperties(
                0.98f, 1000f, 0.1f, 0.1f, 1,
                true, 500f, 600.0f  // 火焰温度（600°C）
        ));

        register(Blocks.TORCH, new BlockThermalProperties(
                0.9f, 1000f, 0.1f, 0.2f, 1,
                true, 100f, 300.0f  // 火把温度（300°C）
        ));

        register(Blocks.CAMPFIRE, new BlockThermalProperties(
                0.95f, 1000f, 0.2f, 0.5f, 1,
                true, 300f, 500.0f  // 营火温度（500°C）
        ));

        register(Blocks.FURNACE, new BlockThermalProperties(
                0.9f, 800f, 1.5f, 3.0f, 2,
                false, 0f, 25.0f  // 未激活时为环境温度
        ));

        register(Blocks.BLAST_FURNACE, new BlockThermalProperties(
                0.95f, 800f, 1.5f, 3.0f, 2,
                true, 200f, 400.0f  // 熔炉激活温度（400°C）
        ));

        // 主动冷源（带明确温度）
        register(Blocks.ICE, new BlockThermalProperties(
                0.9f, 2100f, 2.2f, 0.9f, 4,
                true, -50f, -5.0f  // 冰温度（-5°C）
        ));

        register(Blocks.SNOW_BLOCK, new BlockThermalProperties(
                0.85f, 2100f, 0.16f, 0.5f, 6,
                true, -30f, -10.0f  // 雪块温度（-10°C）
        ));

        register(Blocks.BLUE_ICE, new BlockThermalProperties(
                0.9f, 2100f, 0.05f, 0.9f, 7,
                true, -120f, -20.0f  // 蓝冰温度（-20°C）
        ));
    }
    /**
     * 注册方块的热学属性
     */
    public static void register(Block block, BlockThermalProperties properties) {
        BLOCK_THERMAL_PROPERTIES.put(block, properties);
    }

    /**
     * 根据方块获取热学属性
     */
    public static BlockThermalProperties getProperties(Block block) {
        if(BLOCK_THERMAL_PROPERTIES.containsKey(block)){
            return  BLOCK_THERMAL_PROPERTIES.get(block);
        }else{
            register(block, DEFAULT_PROPERTIES);
            return DEFAULT_PROPERTIES;
        }
    }

    /**
     * 根据方块状态获取热学属性
     */
    public static BlockThermalProperties getProperties(BlockState state) {
        return getProperties(state.getBlock());
    }

    /**
     * 判断是否为热源（主动产热）
     */
    public static boolean isHeatSource(BlockState state) {
        BlockThermalProperties props = getProperties(state);
        return props.isActiveSource() && props.getSourcePower() > 0;
    }

    /**
     * 判断是否为冷源（主动制冷）
     */
    public static boolean isColdSource(BlockState state) {
        BlockThermalProperties props = getProperties(state);
        return props.isActiveSource() && props.getSourcePower() < 0;
    }

    public static boolean isSource(BlockState state) {
        BlockThermalProperties props = getProperties(state);
        return props.isActiveSource();
    }

    /**
     * 获取所有注册的属性（调试用）
     */
    public static Map<Block, BlockThermalProperties> getAllRegisteredProperties() {
        return new HashMap<>(BLOCK_THERMAL_PROPERTIES);
    }
}
