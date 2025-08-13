package agai.heatmod.data.temperature;


import agai.heatmod.annotators.ApiDoc;
import agai.heatmod.annotators.InTest;
import agai.heatmod.annotators.InWorking;
import agai.heatmod.content.temperature.hotandcoolsources.ThermalChangeSource;
import agai.heatmod.content.temperature.thermodynamics.HeatConduction;
import agai.heatmod.content.temperature.thermodynamics.HeatConvection;
import agai.heatmod.content.temperature.thermodynamics.HeatRadiation;
import agai.heatmod.data.temperature.capabilities.ChunkTemperatureCapability;
import agai.heatmod.data.temperature.data.ChunkTemperatureData;
import agai.heatmod.data.temperature.data.impl.ChunkTemperatureIntf;
import agai.heatmod.data.temperature.recipeData.BlockTempData;
import agai.heatmod.debug.DebugConfig;
import agai.heatmod.utils.BlockPosUtils;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@InWorking

//**
// 与区块的 NBT 数据交互（保存 / 加载温度数据）*/
@ApiDoc(description = "作用:能访问世界上所有方块的温度属性,全局属性等,协调区块数据,储存3级区块,管理数据等。\n" +
        "warning:暂时没优化,不需要存储capability的chunk没被重置")

public class ThermalDataManager {

    public static final ThermalDataManager INSTANCE = new ThermalDataManager();

    // 缓存ChunkTemperatureIntf，设置过期时间避免内存泄漏
    private final Cache<ChunkWithLevelKey, ChunkTemperatureIntf> chunkCache = CacheBuilder.newBuilder()
            .maximumSize(1000)
            .expireAfterAccess(10, TimeUnit.MINUTES)
            .build();

    /**
     * 获取方块温度（优化版：减少区块获取和Capability解析开销）
     */
    public float getTemperature(@NotNull ResourceKey<Level> levelResourceKey, BlockPos pos) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        ServerLevel level = server.getLevel(levelResourceKey);
        if (level == null) {
            throw new IllegalStateException("Level is null");
        }
        return getTemperature(level, pos);
    }

    public float getTemperature(Level level, BlockPos pos) {
        ChunkPos chunkPos = new ChunkPos(pos);
        LevelChunk chunk = level.getChunk(chunkPos.x, chunkPos.z);


        ChunkTemperatureIntf tempData = getChunkTempData(chunk);
        return tempData.getTemperature(pos);
    }

    /**
     * 设置方块温度（修复递归错误，优化性能）
     */
    public void setTemperature(@NotNull ResourceKey<Level> levelResourceKey, BlockPos pos, float temperature) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        ServerLevel level = server.getLevel(levelResourceKey);
        if (level == null) {
            throw new IllegalStateException("Level is null");
        }
        setTemperature(level, pos, temperature);
    }

    public void setTemperature(Level level, BlockPos pos, float temperature) {
        ChunkPos chunkPos = new ChunkPos(pos);
        LevelChunk chunk = level.getChunk(chunkPos.x, chunkPos.z);

        ChunkTemperatureIntf tempData = getChunkTempData(chunk);
        tempData.setTemperature(pos, temperature);
    }

    /**
     * 从缓存转移区块热量（优化方块遍历效率）
     */
    public void transferChunkHeatFromCache(Level level, ChunkPos chunkPos) {
        LevelChunk chunk = level.getChunk(chunkPos.x, chunkPos.z);
        ChunkTemperatureIntf tempData = getChunkTempData(chunk);

        for(BlockPos pos: ChunkUtils.getAllBlockPosInChunk(chunk)) {
            tempData.transferTempFromCache(pos);
        }
    }

    /**
     * 累积方块热量到缓存（移除高频调试输出）
     */
    public void accumulateBlockHeatToCache(Level level, BlockPos pos, float joule) {
        ChunkPos chunkPos = new ChunkPos(pos);
        LevelChunk chunk = level.getChunk(chunkPos.x, chunkPos.z);
        ChunkTemperatureIntf tempData = getChunkTempData(chunk);

//         DebugConfig.debug(()->SystemOutHelper.printfplain("accumulateBlockHeatToCache!%s", pos));

        tempData.accumulateTempToCache(pos, calBlockTempDiffFromJoule(level, pos, joule));
    }

    /**
     * 获取方块温度数据（减少重复获取BlockState）
     */
    public BlockTempData getBlockTempData(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return BlockTempData.getData(state.getBlock());
    }

    public BlockTempData getBlockTempData(ChunkAccess chunkAccess, BlockPos pos) {
        BlockState state = chunkAccess.getBlockState(pos);
        return BlockTempData.getData(state.getBlock());
    }

    public boolean isSource(ChunkAccess chunkAccess, BlockPos blockPos) {
        BlockTempData data = getBlockTempData(chunkAccess, blockPos);
        return data != null && data.isActiveSource();
    }

    public float calBlockTempDiffFromJoule(Level level, BlockPos pos, float joule) {
        BlockTempData data = getBlockTempData(level, pos);
        return joule / (data.getSpecificHeatCapacity() * data.getMass());
    }

    /**
     * 内部工具：获取区块温度数据（优先从缓存获取，减少Capability解析）
     */
    public ChunkTemperatureIntf getChunkTempData(LevelChunk chunk) {
        try {
//            DebugConfig.debug(()->{
//                SystemOutHelper.printCallers("ChunkWithLevelKey.keyOf(%s",0, ChunkWithLevelKey.keyOf(chunk).toString());
//            });
//            if(DebugConfig.ENABLE_HEAT_DEBUG){
//                return chunkCache.get(ChunkWithLevelKey.keyOf(chunk),()->chunk.getCapability(ChunkTemperatureCapability.CAPABILITY)
//                        .orElseGet(() -> new ChunkTemperatureData(new ChunkPos(100,100), chunk.getLevel())));
//            }
            return chunkCache.get(ChunkWithLevelKey.keyOf(chunk), () ->
                    chunk.getCapability(ChunkTemperatureCapability.CAPABILITY)
                            .orElseGet(() -> new ChunkTemperatureData(chunk.getPos(), chunk.getLevel()))
            );
        } catch (Exception e) {
            return chunk.getCapability(ChunkTemperatureCapability.CAPABILITY)
                    .orElse(new ChunkTemperatureData(chunk.getPos(), chunk.getLevel()));
        }
    }
    public ChunkTemperatureIntf getChunkTempData(ServerLevel level,ChunkPos chunkPos) {
        return getChunkTempData(level.getChunk(chunkPos.x, chunkPos.z));
    }

    public static class ChunkWithLevelKey {
        private final ResourceKey<Level> dimension; // 世界维度的唯一键
        private final ChunkPos chunkPos;

        public ChunkWithLevelKey(Level level, ChunkPos chunkPos) {
            this.dimension = level.dimension();
            this.chunkPos = chunkPos;
        }

        // 重写 equals 用于判断是否为同一世界的同一区块
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ChunkWithLevelKey that = (ChunkWithLevelKey) o;
            return dimension.equals(that.dimension) && chunkPos.equals(that.chunkPos);
        }

        // 重写 hashCode 用于哈希表存储
        @Override
        public int hashCode() {
            return 31 * dimension.hashCode() + chunkPos.hashCode();
        }

        public static ChunkWithLevelKey keyOf(LevelChunk chunk){
            return new ChunkWithLevelKey(chunk.getLevel(),chunk.getPos());
        }

        @Override
        public String toString() {
            return "dimension:"+dimension.toString()+", chunkPos:"+chunkPos;
        }
    }
}