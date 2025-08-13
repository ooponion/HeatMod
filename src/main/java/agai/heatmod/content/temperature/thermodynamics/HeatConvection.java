package agai.heatmod.content.temperature.thermodynamics;

import agai.heatmod.annotators.InWorking;
import agai.heatmod.config.TempConfig;
import agai.heatmod.data.temperature.ThermalDataManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.LazyOptional;

/**
 * 职责：模拟流体（主要是空气）中的热对流
 * 核心功能：
 * 计算温度差异导致的空气流动
 * 模拟热空气上升、冷空气下沉
 * 考虑风力对热对流的影响
 * 处理封闭空间与开放空间的对流差异*/
@InWorking
public class HeatConvection {
    public static final Direction[] directions=new Direction[]{Direction.UP};
    public static final LazyOptional<Double> INTERVAL_TIME = LazyOptional.of(()-> 0.05* TempConfig.Server.temperatureUpdateIntervalTicks.get()); // 1tick的时间（秒）

    // 空气对流核心参数

    public static final float AIR_CONVECTION_COEFFICIENT = 3.0f; // 空气对流系数（W/(m²·K)）
//    public static final int AIR_CONVECTION_VERTICAL_RANGE = 3;   // 垂直方向影响范围（3格）
//    public static final int AIR_CONVECTION_HORIZONTAL_RANGE = 1; // 水平方向影响范围（1格）
    public static final float MIN_TEMP_DIFF_FOR_CONVECTION = 0.5f; // 最小温差阈值（℃），低于此值无明显对流
    public static final float AIR_DENSITY = 1.225f; // 空气密度（kg/m³）
    public static final float AIR_SPECIFIC_HEAT = 1005f; // 空气比热容（J/(kg·K)）
    public static final float AIR_MASS_PER_BLOCK = AIR_DENSITY * 1.0f; // 1m³空气的质量（kg）
    /**
     * 计算两个空气团之间的对流换热量
     * @return 传递的热量（J），正值表示当前空气团向目标传递热量
     */
    private float calculateAirConvectionHeat(LevelReader levelReader, BlockPos from, BlockPos to) {
        float fromTemp=ThermalDataManager.INSTANCE.getTemperature((Level) levelReader,from);
        float toTemp=ThermalDataManager.INSTANCE.getTemperature((Level) levelReader,to);

        float tempDiff = fromTemp - toTemp;

        // 温差过小，无明显对流
        if (Math.abs(tempDiff) < MIN_TEMP_DIFF_FOR_CONVECTION) {
            return 0;
        }
        float yDiff = to.getY()-from.getY();
        // 方向修正系数：垂直方向（热升冷降）增强对流
        float directionFactor = 0.5f; // 水平方向默认系数
        // 热空气（高温）向上运动，冷空气（低温）向下运动时，对流增强
        if ((tempDiff > 0 && yDiff > 0) || (tempDiff < 0 && yDiff < 0)) {
            directionFactor = 0.5f; // 符合热升冷降，增强对流
        } else {
            directionFactor = 0f; // 逆着自然对流方向，减弱对流
        }

        // 计算对流热量（J）
        return (float)(
                AIR_CONVECTION_COEFFICIENT
                        * tempDiff
                        * directionFactor
                        * INTERVAL_TIME.orElse(0.05D)
        );
    }
    /**
     * 执行空气之间的对流换热（核心方法）
     * @param level 世界
     * @param pos 当前方块位置
     */
    public void convectHeatToAirCache(Level level, BlockPos pos) {
        BlockState state =level.getBlockState(pos);
        if(!(state.is(Blocks.AIR)||state.is(Blocks.WATER))) {
            return;
        }
        for (Direction direction: directions) {
            BlockPos neighborPos = pos.relative(direction);
            if(!level.isInWorldBounds(neighborPos)){
                continue;
            }
            state =level.getBlockState(neighborPos);
            if(!(state.is(Blocks.AIR)||state.is(Blocks.WATER))) {
                continue;
            }
            float convectionHeat = calculateAirConvectionHeat(level, pos, neighborPos);
            if (Math.abs(convectionHeat) < 0.1) continue; // 忽略微小热量
            ThermalDataManager.INSTANCE.accumulateBlockHeatToCache(level,pos,-convectionHeat);
            ThermalDataManager.INSTANCE.accumulateBlockHeatToCache(level,neighborPos,convectionHeat);
        }
    }
}
