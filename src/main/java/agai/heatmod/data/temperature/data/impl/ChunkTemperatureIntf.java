package agai.heatmod.data.temperature.data.impl;

import agai.heatmod.annotators.ApiDoc;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.common.capabilities.AutoRegisterCapability;

@ApiDoc(description = "给forge自动注册用。也是capability用的接口,规定cap能使用的方法。")

@AutoRegisterCapability
public interface ChunkTemperatureIntf {
    float getTemperature(BlockPos pos) ;
    void setTemperature(BlockPos pos, float temperature) ;


    void refreshSources(LevelChunk chunk) ;
    void addSource(ChunkAccess access, BlockPos blockPos);
    void removeSource(ChunkAccess access, final BlockPos blockPos);

    void updateAverageTemperature() ;
    /**不是热量!是温度变化!*/
    void accumulateTempToCache(BlockPos pos, float heat);
    void transferTempFromCache(BlockPos pos);
}
