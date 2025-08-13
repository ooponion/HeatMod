package agai.heatmod.data.temperature.data.impl;

import agai.heatmod.annotators.ApiDoc;
import agai.heatmod.content.temperature.hotandcoolsources.ThermalChangeSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.common.capabilities.AutoRegisterCapability;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

@ApiDoc(description = "给forge自动注册用。也是capability用的接口,规定cap能使用的方法。")

@AutoRegisterCapability
public interface ChunkTemperatureIntf {
    float getTemperature(BlockPos pos) ;
    void setTemperature(BlockPos pos, float temperature) ;


    void refreshSources(LevelChunk chunk) ;
    void addSource(ChunkAccess access, BlockPos blockPos);
    void removeSource(ChunkAccess access, final BlockPos blockPos);
    List<ThermalChangeSource> getActiveThermalSources();

    void updateAverageTemperature() ;
    float getAverageTemperature();
    void setAverageTemperature(float newAverage);
    /**不是热量!是温度变化!*/
    void accumulateTempToCache(BlockPos pos, float heat);
    void transferTempFromCache(BlockPos pos);

    boolean isPosInChunk(BlockPos pos);
    ChunkPos getChunkPos();

    void syncFromOther(ChunkTemperatureIntf chunkTemperatureIntf);
}
