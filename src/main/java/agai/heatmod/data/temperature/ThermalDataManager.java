package agai.heatmod.data.temperature;


import agai.heatmod.annotators.ApiDoc;
import agai.heatmod.annotators.InWorking;
import agai.heatmod.data.temperature.capabilities.ChunkTemperatureCapability;
import agai.heatmod.data.temperature.data.ChunkTemperatureData;
import agai.heatmod.data.temperature.data.impl.ChunkTemperatureIntf;
import agai.heatmod.utils.ChunkUtils;
import agai.heatmod.utils.SystemOutHelper;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;

@InWorking


//**
// 与区块的 NBT 数据交互（保存 / 加载温度数据）*/
@ApiDoc(description = "作用:能访问世界上所有方块的温度属性,全局属性等,协调区块数据,储存3级区块,管理数据等。\n" +
        "warning:暂时没优化,不需要存储capability的chunk没被重置")

public class ThermalDataManager {
    public static final ThermalDataManager INSTANCE = new ThermalDataManager();
    private final int primeRange=3;
    private final int secondRange=10;
    private final int thirdRange=32;
    private final Cache<Long, ChunkTemperatureIntf> chunkCache = CacheBuilder.newBuilder()
            .maximumSize(1000)
            .build();

    public float getTemperature(@NotNull ResourceKey<Level> levelResourceKey, BlockPos pos) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        ServerLevel level = server.getLevel(levelResourceKey);
        if (level == null) {
            throw new IllegalStateException("Level is null");
        }
        return getTemperature(level, pos);
    }

    public float getTemperature(Level level, BlockPos pos) {
        LevelChunk chunk = (LevelChunk) level.getChunk(pos);
        var capability = chunk.getCapability(ChunkTemperatureCapability.CAPABILITY);
        return capability.orElse(new ChunkTemperatureData()).getTemperature(pos);
    }
    public void setTemperature(@NotNull ResourceKey<Level> levelResourceKey, BlockPos pos,float temperature) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        ServerLevel level = server.getLevel(levelResourceKey);
        if (level == null) {
            throw new IllegalStateException("Level is null");
        }
        setTemperature(levelResourceKey, pos, temperature);
    }
    public void setTemperature(Level level, BlockPos pos,float temperature) {
        LevelChunk chunk= (LevelChunk) level.getChunk(pos);
        var capability =chunk.getCapability(ChunkTemperatureCapability.CAPABILITY);
        if(capability.isPresent()) {
            capability.ifPresent(cap->cap.setTemperature(pos, temperature));
        }else{
            SystemOutHelper.printfplain("setTemperature(), this chunk("+pos+") has no capability!");
        }
    }

    /**刷新一个范围的数据*/
    public void refreshSources(Level level, BlockPos pos) {
        for(ChunkPos chunkPos: ChunkUtils.findCircularChunks(pos,thirdRange)) {
            LevelChunk chunk= level.getChunk(chunkPos.x,chunkPos.z);
            var capability =chunk.getCapability(ChunkTemperatureCapability.CAPABILITY);
            if(capability.isPresent()) {
                capability.ifPresent(cap->cap.refreshSources(chunk));
            }else{
                SystemOutHelper.printfplain("refreshSources(), this chunk("+chunk.getPos()+") has no capability!");
            }
        }
    }

    public void addSource(Level level, BlockPos blockPos) {
        LevelChunk chunk= (LevelChunk) level.getChunk(blockPos);
        var capability =chunk.getCapability(ChunkTemperatureCapability.CAPABILITY);
        if(capability.isPresent()) {
            capability.ifPresent(cap->cap.addSource(chunk,blockPos));
        }else{
            SystemOutHelper.printfplain("addSource(), this chunk("+chunk.getPos()+") has no capability!");
        }
    }

    public void removeSource(Level level, BlockPos blockPos) {
        LevelChunk chunk= (LevelChunk) level.getChunk(blockPos);
        var capability =chunk.getCapability(ChunkTemperatureCapability.CAPABILITY);
        if(capability.isPresent()) {
            capability.ifPresent(cap->cap.removeSource(chunk,blockPos));
        }else{
            SystemOutHelper.printfplain("removeSource(), this chunk("+chunk.getPos()+") has no capability!");
        }
    }

    public void updateAverageTemperature() {

    }

    public void transferHeatFromCache(Level level,ChunkPos chunkPos) {
        LevelChunk chunk= level.getChunk(chunkPos.x,chunkPos.z);
        var capability =chunk.getCapability(ChunkTemperatureCapability.CAPABILITY);
        if(capability.isPresent()) {
            capability.ifPresent(cap->{
                for(BlockPos blockPos:ChunkUtils.getAllBlockPosInChunk(chunk)) {
                    cap.transferHeatFromCache(blockPos);
                }
            });
        }else{
            SystemOutHelper.printfplain("transferHeatFromCache(), this chunk("+chunk.getPos()+") has no capability!");
        }
    }
}