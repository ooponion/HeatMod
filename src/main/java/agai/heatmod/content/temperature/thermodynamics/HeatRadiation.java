package agai.heatmod.content.temperature.thermodynamics;

import agai.heatmod.annotators.InWorking;
import agai.heatmod.annotators.NeedImprovement;
import agai.heatmod.config.TempConfig;
import agai.heatmod.data.temperature.ThermalDataManager;
import agai.heatmod.data.temperature.recipeData.BlockTempData;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraftforge.common.util.LazyOptional;

/**
 * 职责：处理热辐射效果
 * 核心功能：
 * 计算热源向周围的辐射热量
 * 考虑距离衰减（平方反比定律）
 * 处理不同方块对辐射的吸收 / 反射率*/
@InWorking
@NeedImprovement//需要考虑方块遮挡
public class HeatRadiation {
    public static final int MAX_RADIATION_DISTANCE=12;// it is constant now
    public static final LazyOptional<Double> INTERVAL_TIME = LazyOptional.of(()-> 0.05* TempConfig.Server.temperatureUpdateIntervalTicks.get()); // 1tick的时间（秒）

    public static final double STEFAN_BOLTZMANN = 5.67e-8;

    /**
     * 计算当前方块向指定位置的目标方块传递的辐射热量（非接触）
     * @param to 目标方块
     * @param from 辐射方块
     * @return 传递的热量（J），正值表示当前方块向目标传递热量
     */
    public float calculateNonContactRadiationHeat(LevelReader levelReader, BlockPos from, BlockPos to) {
        double distance=from.distSqr(to);

        if (distance > MAX_RADIATION_DISTANCE) return 0;

        float fromTemp= ThermalDataManager.INSTANCE.getTemperature((Level) levelReader,from);
        float toTemp=ThermalDataManager.INSTANCE.getTemperature((Level) levelReader,to);
        BlockTempData fromData=ThermalDataManager.INSTANCE.getBlockTempData((Level) levelReader,from);
        BlockTempData toData=ThermalDataManager.INSTANCE.getBlockTempData((Level) levelReader,to);
        double t1 = fromTemp + 273.15;
        double t2 = toTemp + 273.15;

        // 辐射基本能量（斯特藩-玻尔兹曼定律）
        // Q = ε × σ × A × (T₁⁴ - T₂⁴) × t
        double baseRadiation = fromData.getEmissivity()
                * STEFAN_BOLTZMANN
                * (Math.pow(t1, 4) - Math.pow(t2, 4))
                * INTERVAL_TIME.orElse(0.05D);

        // 距离衰减（平方反比定律：1/d²，d=1时无衰减）
        double distanceAttenuation = 1.0 / (distance * distance);

        // 目标方块的吸收系数（简化：使用目标方块的发射率，实际中发射率=吸收率）
        double absorptionFactor = toData.getEmissivity();

        // 最终辐射热量（考虑衰减和吸收）
        return (float)(baseRadiation * distanceAttenuation * absorptionFactor);
    }
    /**
     * 向周围一定范围内的所有方块传递辐射热量（非接触）
     * @param level 世界
     * @param pos 当前方块位置
     */
    public void radiateHeatToSurroundingsCache(Level level, BlockPos pos) {
        int range = MAX_RADIATION_DISTANCE;
        for(BlockPos targetPos :BlockPos.withinManhattan(pos,range,range,range)){
            if (targetPos.equals(pos)) continue;
            if(!level.isInWorldBounds(targetPos)){
                continue;
            }

            float radiatedHeat = calculateNonContactRadiationHeat(level,pos,targetPos);
            if (Math.abs(radiatedHeat) < 0.001) continue; // 忽略微小热量传递

            ThermalDataManager.INSTANCE.accumulateBlockHeatToCache(level,pos,-radiatedHeat);
            ThermalDataManager.INSTANCE.accumulateBlockHeatToCache(level,targetPos,radiatedHeat);
        }
        for (int dx = -range; dx <= range; dx++) {
            for (int dy = -range; dy <= range; dy++) {
                for (int dz = -range; dz <= range; dz++) {

                }
            }
        }
    }
}
