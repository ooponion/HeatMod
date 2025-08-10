package agai.heatmod.content.temperature.thermodynamics;

import agai.heatmod.annotators.ApiDoc;
import agai.heatmod.annotators.InWorking;
import agai.heatmod.config.TempConfig;
import agai.heatmod.data.temperature.ThermalDataManager;
import agai.heatmod.data.temperature.recipeData.BlockTempData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.LazyOptional;

/**
 * 职责：处理方块间的热传导计算
 * 核心功能：
 * 计算相邻方块间的热量传递
 * 考虑方块材料的导热系数
 * 处理不同介质（固体、液体、气体）间的传导差异*/
@InWorking
@ApiDoc(description = " * 职责：处理方块间的热传导计算\n" +
        " * 核心功能：\n" +
        " * 计算相邻方块间的热量传递\n" +
        " * 考虑方块材料的导热系数\n" +
        " * 处理不同介质（固体、液体、气体）间的传导差异")
public class HeatConduction {

    public static final LazyOptional<Double> INTERVAL_TIME = LazyOptional.of(()-> 0.05*TempConfig.Server.temperatureUpdateIntervalTicks.get()); // 1tick的时间（秒）
    /**从from传递到to的热量
     * @param from
     * @param to <br>*/
    private float calculateConductionHeat(LevelReader levelReader, BlockPos from, BlockPos to){
        float fromTemp=ThermalDataManager.INSTANCE.getTemperature((Level) levelReader,from);
        float toTemp=ThermalDataManager.INSTANCE.getTemperature((Level) levelReader,to);
        BlockTempData fromData=ThermalDataManager.INSTANCE.getBlockTempData((Level) levelReader,from);
        BlockTempData toData=ThermalDataManager.INSTANCE.getBlockTempData((Level) levelReader,to);
        if(fromData==null||toData==null){
            return 0;
        }
        float tempDiff = fromTemp-toTemp;
        // 绝缘衰减：等级越高，衰减越强
        float insulationFactor = (10 - fromData.getInsulationLevel()) / 10.0f;
        //Joule
        return (float)(
                fromData.getThermalConductivity()
                        * tempDiff
                        * INTERVAL_TIME.orElse(0.05D)
                        * insulationFactor
        );
    }
//    private void conductHeatToNeighbour(Level level, BlockPos from, BlockPos to){
//        float joule=calculateConductionHeat(level,from,to);
//        ThermalDataManager.INSTANCE.accumulateBlockHeatToCache(level,from,-joule);
//        ThermalDataManager.INSTANCE.accumulateBlockHeatToCache(level,to,joule);
//    }
    public void conductHeatToNeighboursCache(Level level, BlockPos pos){
        for (Direction dy: Direction.values()) {
            BlockPos to= pos.relative(dy);
            if(!level.isInWorldBounds(pos)){
                continue;
            }
            float joule=calculateConductionHeat(level,pos,to);
            ThermalDataManager.INSTANCE.accumulateBlockHeatToCache(level,pos,-joule);
            ThermalDataManager.INSTANCE.accumulateBlockHeatToCache(level,to,joule);
        }
    }
}
