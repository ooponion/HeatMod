package agai.heatmod.content.temperature.controllers;


import agai.heatmod.annotators.InTest;
import agai.heatmod.content.temperature.hotandcoolsources.ThermalChangeSource;
import agai.heatmod.content.temperature.thermodynamics.HeatConduction;
import agai.heatmod.content.temperature.thermodynamics.HeatConvection;
import agai.heatmod.content.temperature.thermodynamics.HeatRadiation;
import agai.heatmod.data.temperature.ThermalDataManager;
import agai.heatmod.data.temperature.capabilities.ChunkTemperatureCapability;
import agai.heatmod.data.temperature.data.impl.ChunkTemperatureIntf;
import agai.heatmod.utils.ChunkUtils;
import agai.heatmod.utils.SystemOutHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.common.util.LazyOptional;
import org.antlr.v4.parse.v4ParserException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;

/**职责：温度系统的总控制器，协调所有温度相关计算和更新
 核心功能：
 管理所有温度数据的更新周期
 协调热力学过程（传导、对流、辐射）的执行顺序
 处理世界加载 / 卸载时的温度数据初始化和保存
 提供温度查询的统一接口*/
public class ThermalEngine {
    public static final ThermalEngine INSTANCE = new ThermalEngine();


    private HeatConduction heatConduction=new HeatConduction();
    private HeatConvection heatConvection=new HeatConvection();
    private HeatRadiation heatRadiation=new HeatRadiation();
    public ThermalEngine() {}
    /**This method will update the temperature of each visible block through thermodynamics.*/
    @InTest
    public void applyThermodynamics(ServerLevel level, ChunkPos chunkPos) {

        LevelChunk chunk= level.getChunk(chunkPos.x, chunkPos.z);
        var capability =chunk.getCapability(ChunkTemperatureCapability.CAPABILITY);
        if(capability.isPresent()) {
            capability.ifPresent(cap->{
                SystemOutHelper.printfplain("applyThermodynamics:%s",chunk.getPos());
                for(BlockPos blockPos:ChunkUtils.getAllBlockPosInChunk(chunk)) {
                    heatConduction.conductHeatToNeighboursCache(level,blockPos);
                    heatRadiation.radiateHeatToSurroundingsCache(level,blockPos);
                    heatConvection.convectHeatToAirCache(level,blockPos);
                }
                SystemOutHelper.printfplain("applyThermodynamics2");
            });
        }else{
            SystemOutHelper.printfplain("applyThermodynamics(), this chunk("+chunk.getPos()+") has no capability!");
        }
        SystemOutHelper.printfplain("applyThermodynamics-Chunk:%s",chunk.getPos());
        ThermalDataManager.INSTANCE.transferChunkHeatFromCache(level,chunk.getPos());
        SystemOutHelper.printfplain("applyThermodynamics-Chunk2");
//        for (LevelChunk chunk:ChunkUtils.getAllLoadedLevelChunks(level)){
//            var capability =chunk.getCapability(ChunkTemperatureCapability.CAPABILITY);
//            if(capability.isPresent()) {
//                capability.ifPresent(cap->{
//                    SystemOutHelper.printfplain("applyThermodynamics:%s",chunk.getPos());
//                    for(BlockPos blockPos:ChunkUtils.getAllBlockPosInChunk(chunk)) {
//                        heatConduction.conductHeatToNeighboursCache(level,blockPos);
//                        heatRadiation.radiateHeatToSurroundingsCache(level,blockPos);
//                        heatConvection.convectHeatToAirCache(level,blockPos);
//                    }
//                    SystemOutHelper.printfplain("applyThermodynamics2");
//                });
//            }else{
//                SystemOutHelper.printfplain("applyThermodynamics(), this chunk("+chunk.getPos()+") has no capability!");
//            }
//        }
//        for (LevelChunk chunk:ChunkUtils.getAllLoadedLevelChunks(level)){
//            SystemOutHelper.printfplain("applyThermodynamics-Chunk:%s",chunk.getPos());
//            ThermalDataManager.INSTANCE.transferChunkHeatFromCache(level,chunk.getPos());
//            SystemOutHelper.printfplain("applyThermodynamics-Chunk2:%s");
//        }
    }
    /**
     * 中距离区块的简化热力学更新（比完整计算轻量）
     * @param level 世界实例
     * @param chunkPos 目标区块坐标
     * @param includeRadiation 是否保留辐射计算（部分中距离区块可能需要）
     */
    private void applySimplifiedThermodynamics(ServerLevel level, ChunkPos chunkPos, boolean includeRadiation) {
        LevelChunk chunk = level.getChunk(chunkPos.x, chunkPos.z);
        chunk.getCapability(ChunkTemperatureCapability.CAPABILITY).ifPresent(cap -> {

            for (BlockPos pos :  cap.getActiveThermalSources().stream().map(ThermalChangeSource::getBlockPos).toList()) {
                heatConduction.conductHeatToNeighboursCache(level, pos);

                if (includeRadiation) {
                    heatRadiation.radiateHeatToSurroundingsCache(level, pos);
                }
            }
            ThermalDataManager.INSTANCE.transferChunkHeatFromCache(level, chunkPos);

            cap.updateAverageTemperature();
        });
    }
    /**
     * 远距离区块的平均温度更新（仅计算区块级温度，忽略内部方块）
     * @param level 世界实例
     * @param chunkPos 目标区块坐标
     */
    private void updateChunkAverageTemperature(ServerLevel level, ChunkPos chunkPos) {
        LevelChunk chunk = level.getChunk(chunkPos.x, chunkPos.z);
        chunk.getCapability(ChunkTemperatureCapability.CAPABILITY).ifPresent(cap -> {
            // 1. 计算周围8个方向区块的平均温度影响（只取已加载的区块）
            float neighborSum = 0f;
            int validNeighbors = 0;
            int[][] directions = {{-1, -1}, {-1, 0}, {-1, 1},
                    {0, -1},          {0, 1},
                    {1, -1},  {1, 0}, {1, 1}};

            for (int[] dir : directions) {
                ChunkPos neighborPos = new ChunkPos(chunkPos.x + dir[0], chunkPos.z + dir[1]);
                if (level.getChunkSource().hasChunk(neighborPos.x, neighborPos.z)) {
                    // 获取邻居区块的平均温度
                    LevelChunk neighborChunk = level.getChunk(neighborPos.x, neighborPos.z);
                    float neighborAvg = neighborChunk.getCapability(ChunkTemperatureCapability.CAPABILITY)
                            .map(ChunkTemperatureIntf::getAverageTemperature)
                            .orElse(27f); // 默认为室温

                    neighborSum += neighborAvg;
                    validNeighbors++;
                }
            }

            // 2. 计算邻居平均温度（若无邻居则保持当前温度）
            float neighborAvg = validNeighbors > 0 ? (neighborSum / validNeighbors) : cap.getAverageTemperature();

            float newAverage = cap.getAverageTemperature() * 0.8f    // 保留80%当前温度
                    + neighborAvg * 0.2f;                   // 15%受邻居影响


            // 4. 限制温度变化幅度（避免突变，更符合热力学规律）
            float maxDelta = 0.5f; // 最大每步变化（°C）
            newAverage = Math.max(
                    cap.getAverageTemperature() - maxDelta,
                    Math.min(newAverage, cap.getAverageTemperature() + maxDelta)
            );
            cap.setAverageTemperature(newAverage);

//            // 5. 保存结果（若在异步线程中，先暂存到 ThermalDataManager；否则直接更新）
//            if (Thread.currentThread().getName().contains("ForkJoinPool")) {
//                // 异步线程：暂存到缓存，等待 syncAsyncResults 同步
//                ThermalDataManager.INSTANCE.setAsyncChunkAverage(chunkPos, newAverage);
//            } else {
//                // 主线程：直接更新
//
//            }
        });
    }
    public List<ChunkPos> updateChunks(ServerLevel level, Player player) {
        BlockPos playerPos = player.blockPosition();
        ChunkPos playerChunkPos = new ChunkPos(playerPos);
        List<ChunkPos> chunkPosList=new ArrayList<>();
        // 遍历以玩家为中心的区块范围（根据性能需求调整半径）
        int maxDistance = 32; // 最大更新半径（区块数）
        for (int dx = -maxDistance; dx <= maxDistance; dx++) {
            for (int dz = -maxDistance; dz <= maxDistance; dz++) {
                ChunkPos targetChunkPos = new ChunkPos(playerChunkPos.x + dx, playerChunkPos.z + dz);
                int distance = Math.abs(dx) + Math.abs(dz); // 曼哈顿距离（简化计算）

                // 根据距离选择更新策略
                if (distance <= 4) {
                    // 近距离：高精度实时更新（已有的 applyThermodynamics 逻辑）
                    applyThermodynamics(level, targetChunkPos);
                    chunkPosList.add(targetChunkPos);
                } else if (distance <= 16) {
                    // 中距离：每 20 游戏刻（1秒）更新一次，简化计算（忽略部分细节）
                    if (level.getGameTime() % 20 == 0) {
                        applySimplifiedThermodynamics(level, targetChunkPos, false); // 关闭辐射/对流等细节
                        chunkPosList.add(targetChunkPos);
                    }
                } else if (distance <= 32&&isChunkActive(level, targetChunkPos)) {
                    // 远距离：每 200 游戏刻（10秒）更新一次，只计算区块平均温度变化
                    if (level.getGameTime() % 200 == 0) {
                        updateChunkAverageTemperature(level, targetChunkPos); // 仅更新平均值
                        chunkPosList.add(targetChunkPos);
                    }
                }
            }
        }
        return chunkPosList;
    }
    private boolean isChunkActive(ServerLevel level, ChunkPos chunkPos) {
        LevelChunk chunk = level.getChunk(chunkPos.x, chunkPos.z);
        return chunk.getCapability(ChunkTemperatureCapability.CAPABILITY)
                .map(cap -> !cap.getActiveThermalSources().isEmpty()) // 有活跃热源
                .orElse(false);
    }
    public void asyncUpdateChunks(ServerLevel level,Player player) {
        ForkJoinPool.commonPool().execute(() -> {
            var chunkPosList=updateChunks(level, player);
            List<ChunkTemperatureIntf> capacities=chunkPosList
                    .stream()
                    .map(chunkPos->level.getChunk(chunkPos.x,chunkPos.z).getCapability(ChunkTemperatureCapability.CAPABILITY))
                    .filter(LazyOptional::isPresent)
                    .map(lazy->lazy.resolve().get())
                    .toList();
            level.getServer().execute(() -> syncAsyncResults(level, capacities));
        });
    }
    public void syncAsyncResults(ServerLevel level, List<ChunkTemperatureIntf> capacities) {
        for (ChunkTemperatureIntf chunkTemperatureIntf : capacities) {
            var chunkPos=chunkTemperatureIntf.getChunkPos();
            ThermalDataManager.INSTANCE.getChunkTempData(level,chunkPos).syncFromOther(chunkTemperatureIntf);
        }
    }
}


//*package agai.heatmod.data.temperature.data;
//
//import agai.heatmod.annotators.ApiDoc;
//import agai.heatmod.annotators.DependsOn;
//import agai.heatmod.annotators.InWorking;
//import agai.heatmod.bootstrap.thermal.ThermalMaterialRegistry;
//import agai.heatmod.data.temperature.properties.BlockThermalProperties;
//import agai.heatmod.events.ForcedChunkTickHandler;
//import agai.heatmod.utils.codec.CompressDifferCodec.ChunkPosCodec;
//import agai.heatmod.utils.SystemOutHelper;
//import com.mojang.serialization.Codec;
//import com.mojang.serialization.codecs.RecordCodecBuilder;
//import net.minecraft.core.BlockPos;
//import net.minecraft.core.Direction;
//import net.minecraft.server.level.ChunkHolder;
//import net.minecraft.server.level.ChunkLevel;
//import net.minecraft.world.level.ChunkPos;
//import net.minecraft.world.level.LevelAccessor;
//import net.minecraft.world.level.block.state.BlockState;
//import net.minecraft.world.level.chunk.ChunkAccess;
//import net.minecraft.world.level.chunk.LevelChunk;
//
//import java.io.Serializable;
//import java.util.*;
//
//@InWorking
//@ApiDoc(description = " * 职责：管理区块级别的温度数据存储\n" +
//        " * 核心功能：存储区块内所有方块的实时温度（高效存储，避免内存浪费）。\n" +
//        "记录区块内的「活跃热源 / 冷源」（如岩浆、火把、冰块），用于快速热计算。\n" +
//        "记录区块内的")
//public class ChunkTemperatureData implements Serializable {
//    public static final Codec<ChunkTemperatureData> CODEC = RecordCodecBuilder.create(instance ->
//            instance.group(
//                    ChunkPosCodec.CODEC.fieldOf("chunk_pos").forGetter(data -> data.chunkPos),
//                    Codec.unboundedMap(Codec.INT, Codec.FLOAT).fieldOf("block_temperatures").forGetter(data -> data.blockTemperatures),
//                    HeatSourceData.CODEC.listOf().fieldOf("active_heat_sources").forGetter(data -> data.activeHeatSources),
//                    ColdSourceData.CODEC.listOf().fieldOf("active_cold_sources").forGetter(data -> data.activeColdSources),
//                    Codec.FLOAT.fieldOf("average_temperature").forGetter(data -> data.averageTemperature),
//                    Codec.unboundedMap(Codec.LONG, Codec.FLOAT).fieldOf("thermal_conduction_cache").forGetter(data -> data.thermalConductionCache)
//            ).apply(instance, ChunkTemperatureData::new)
//    );
//    // 所属区块坐标
//    private final ChunkPos chunkPos;
//
//    // 方块温度存储（稀疏存储：仅记录与平均温度差异较大的方块）
//    // key: 本地坐标编码, value: 温度(°C)
//    private Map<Integer, Float> blockTemperatures=new HashMap<>();
//
//    // 区块内活跃热源列表
//    private List<HeatSourceData> activeHeatSources=new ArrayList<>();
//
//    // 区块内活跃冷源列表
//    private List<ColdSourceData> activeColdSources=new ArrayList<>();
//
//    // 热传导缓存
//    private Map<Long, Float> thermalConductionCache = new HashMap<>();
//
//    // 最后一次更新的游戏刻
//    private long lastUpdatedTick = 0;
//
//    // 区块平均温度（缓存值）
//    private float averageTemperature;
//
//    // 温度差异阈值：超过此值才会被存储（减少存储量）
//    private static final float TEMP_STORAGE_THRESHOLD = 2.0f;
//
//    public ChunkTemperatureData(ChunkPos chunkPos, float initialAvgTemp) {
//        this.chunkPos = chunkPos;
//        this.averageTemperature = initialAvgTemp;
//    }
//    public ChunkTemperatureData(ChunkPos chunkPos) {
//        this.chunkPos = chunkPos;
//        this.averageTemperature = 37;
//    }
//
//    public ChunkTemperatureData(ChunkPos chunkPos,
//                                Map<Integer, Float> blockTemperatures,
//                                List<HeatSourceData> activeHeatSources,
//                                List<ColdSourceData> activeColdSources,
//                                float averageTemperature,   Map<Long, Float> thermalConductionCache) {
//        this.chunkPos = chunkPos;
//        this.blockTemperatures = blockTemperatures;
//        this.activeHeatSources = activeHeatSources;
//        this.activeColdSources = activeColdSources;
//        this.averageTemperature = averageTemperature;
//        this.thermalConductionCache = thermalConductionCache;
//    }
//
//
//    /**
//     * 获取指定位置的温度
//     */
//    public float getTemperature(BlockPos pos) {
//        if (!isPosInChunk(pos)) {
//            return averageTemperature;
//        }
//
//        int localX = pos.getX() & 0xF; // 转换为区块内本地坐标(0-15)
//        int localY = pos.getY();
//        int localZ = pos.getZ() & 0xF;
//
//        int key = encodeLocalPos(localX, localY, localZ);
//        return blockTemperatures.getOrDefault(key, averageTemperature);
//    }
//
//    /**
//     * 设置指定位置的温度
//     */
//    public void setTemperature(BlockPos pos, float temperature) {
//        if (!isPosInChunk(pos)) {
//            return;
//        }
//
//        int localX = pos.getX() & 0xF;
//        int localY = pos.getY();
//        int localZ = pos.getZ() & 0xF;
//        int key = encodeLocalPos(localX, localY, localZ);
//
//        // 仅当与平均温度差异超过阈值时才存储
//        if (Math.abs(temperature - averageTemperature) > TEMP_STORAGE_THRESHOLD) {
//            blockTemperatures.put(key, temperature);
//        } else {
//            // 差异过小则移除存储（使用平均温度）
//            blockTemperatures.remove(key);
//        }
//    }
//
//    /**
//     * 处理区块内的局部热传导
//     */
//
//    public void processLocalHeatConduction(LevelChunk chunk, long currentTick) {
//        SystemOutHelper.printfplain("delete sources: ",chunk.getPos().x,chunk.getPos().z);
//        // 控制更新频率（每20刻更新一次，约1秒）
//        if (currentTick - lastUpdatedTick < 20) {
//            return;
//        }
//        lastUpdatedTick = currentTick;
//
//        // 处理热源的热传导
//        for (HeatSourceData source : activeHeatSources) {
//            propagateHeat(source, chunk);
//        }
//
//        // 处理冷源的热传导
//        for (ColdSourceData source : activeColdSources) {
//            absorbHeat(source, chunk);
//        }
//
//        // 更新区块平均温度
//        updateAverageTemperature();
//    }
//
//    /**
//     * 从热源传播热量
//     */
//    private void propagateHeat(HeatSourceData source, LevelChunk chunk) {
//        BlockPos sourcePos = source.pos;
//        float sourceTemp = source.temperature;
//
//        // 检查周围6个方向的方块
//        for (Direction dir : Direction.values()) {
//            BlockPos neighborPos = sourcePos.relative(dir);
//            if (!isPosInChunk(neighborPos)) continue;
//
//            BlockState neighborState = chunk.getBlockState(neighborPos);
//            BlockThermalProperties props = ThermalMaterialRegistry.getProperties(neighborState);
//
//            // 计算当前温度
//            float neighborTemp = getTemperature(neighborPos);
//            float deltaT = sourceTemp - neighborTemp;
//
//            // 计算传导的热量 (Q = k * A * ΔT * t / d)
//            // 简化计算：忽略面积和距离，使用导热系数和时间系数
//            float heatTransfer = props.getThermalConductivity() * deltaT * 0.1f;
//
//            // 计算温度变化 (ΔT = Q / C)
//            float newTemp = neighborTemp + (heatTransfer / props.getHeatCapacity());
//
//            // 应用隔热效果
//            newTemp = applyInsulation(newTemp, neighborTemp, props.getInsulationLevel());
//
//            setTemperature(neighborPos, newTemp);
//
//            // 更新缓存
//            long cacheKey = getPosPairKey(sourcePos, neighborPos);
//            thermalConductionCache.put(cacheKey, heatTransfer);
//        }
//    }
//
//    /**
//     * 冷源吸收热量
//     */
//    private void absorbHeat(ColdSourceData source, LevelChunk chunk) {
//        BlockPos sourcePos = source.pos;
//        float sourceTemp = source.temperature;
//
//        for (Direction dir : Direction.values()) {
//            BlockPos neighborPos = sourcePos.relative(dir);
//            if (!isPosInChunk(neighborPos)) continue;
//
//            float neighborTemp = getTemperature(neighborPos);
//            float deltaT = neighborTemp - sourceTemp;
//
//            if (deltaT <= 0) continue; // 只有当邻居温度更高时才吸收热量
//
//            BlockState neighborState = chunk.getBlockState(neighborPos);
//            BlockThermalProperties props = ThermalMaterialRegistry.getProperties(neighborState);
//
//            // 计算吸收的热量
//            float heatAbsorbed = props.getThermalConductivity() * deltaT * 0.1f;
//            float newTemp = neighborTemp - (heatAbsorbed / props.getHeatCapacity());
//
//            // 应用隔热效果
//            newTemp = applyInsulation(newTemp, neighborTemp, props.getInsulationLevel());
//
//            setTemperature(neighborPos, newTemp);
//        }
//    }
//
//    /**
//     * 应用隔热效果
//     */
//    private float applyInsulation(float newTemp, float originalTemp, int insulationLevel) {
//        if (insulationLevel <= 0) return newTemp;
//
//        // 隔热等级越高，温度变化越小 (0-10级)
//        float insulationFactor = 1.0f - (insulationLevel * 0.08f);
//        return originalTemp + (newTemp - originalTemp) * insulationFactor;
//    }
//
//    /**
//     * 刷新区块内的热源和冷源列表
//     */
//    public void refreshSources(LevelChunk chunk) {
//        activeHeatSources.clear();
//        activeColdSources.clear();
//
//        // 遍历区块内可能是热源/冷源的方块
//        ChunkPos pos = chunk.getPos();
//        for (int x = 0; x < 16; x++) {
//            for (int z = 0; z < 16; z++) {
//                for (int y = 0; y < chunk.getHeight(); y++) {
//                    BlockPos blockPos = new BlockPos(
//                            pos.getMinBlockX() + x,
//                            y,
//                            pos.getMinBlockZ() + z
//                    );
//                    BlockState state = chunk.getBlockState(blockPos);
//
//                    // 检查是否为热源
//                    if (ThermalMaterialRegistry.isHeatSource(state)) {
//                        BlockThermalProperties props = ThermalMaterialRegistry.getProperties(state);
//                        activeHeatSources.add(new HeatSourceData(
//                                blockPos,
//                                props.getSourceTemperature(),
//                                props.getSourcePower()
//                        ));
//                    }
//                    // 检查是否为冷源
//                    else if (ThermalMaterialRegistry.isColdSource(state)) {
//                        BlockThermalProperties props = ThermalMaterialRegistry.getProperties(state);
//                        activeColdSources.add(new ColdSourceData(
//                                blockPos,
//                                props.getSourceTemperature(),
//                                props.getSourcePower()
//                        ));
//                    }
//                }
//            }
//        }
//    }
//    public void AddSources(ChunkAccess access, BlockPos blockPos) {
//        BlockState state = access.getBlockState(blockPos);
//        if (ThermalMaterialRegistry.isHeatSource(state)) {
//            BlockThermalProperties props = ThermalMaterialRegistry.getProperties(state);
//            activeHeatSources.add(new HeatSourceData(
//                    blockPos,
//                    props.getSourceTemperature(),
//                    props.getSourcePower()
//            ));
//        }
//        else if (ThermalMaterialRegistry.isColdSource(state)) {
//            BlockThermalProperties props = ThermalMaterialRegistry.getProperties(state);
//            activeColdSources.add(new ColdSourceData(
//                    blockPos,
//                    props.getSourceTemperature(),
//                    props.getSourcePower()
//            ));
//        }
//        SystemOutHelper.printfplain("add sources: ", blockPos);
//    }
//    public void DeleteSources(ChunkAccess access, final BlockPos blockPos) {
//        BlockState state = access.getBlockState(blockPos);
//        if (ThermalMaterialRegistry.isHeatSource(state)) {
//            BlockThermalProperties props = ThermalMaterialRegistry.getProperties(state);
//            activeHeatSources.removeIf(heatSourceData -> {return heatSourceData.pos.equals(blockPos);});
//        }
//        else if (ThermalMaterialRegistry.isColdSource(state)) {
//            BlockThermalProperties props = ThermalMaterialRegistry.getProperties(state);
//            activeColdSources.removeIf(coldSourceData -> {return coldSourceData.pos.equals(blockPos);});
//        }
//        SystemOutHelper.printfplain("delete sources: ", blockPos);
//    }
//    /**
//     * 更新区块平均温度
//     */
//    private void updateAverageTemperature() {
//        if (blockTemperatures.isEmpty()) {
//            return; // 没有特殊温度数据，保持原有平均值
//        }
//
//        // 计算所有存储温度的平均值
//        double sum =  blockTemperatures.values().stream().mapToDouble(Float::floatValue).sum();
//        float storedAvg = (float) (sum / blockTemperatures.size());
//
//        // 平滑更新平均温度（避免突变）
//        averageTemperature = averageTemperature * 0.7f + storedAvg * 0.3f;
//    }
//
//    /**
//     * 检查位置是否在当前区块内
//     */
//    public boolean isPosInChunk(BlockPos pos) {
//        return ChunkPos.getX(pos.getX()) == chunkPos.x &&
//                ChunkPos.getZ(pos.getZ()) == chunkPos.z;
//    }
//
//    /**
//     * 编码本地坐标为整数键
//     */
//    private int encodeLocalPos(int x, int y, int z) {
//        // x和z: 0-15 (4位), y: 0-255 (8位)
//        return x | (z << 4) | (y << 8);
//    }
//
//    /**
//     * 解码整数键为本地坐标
//     */
//    private int[] decodeLocalPos(int key) {
//        int x = key & 0xF;
//        int z = (key >> 4) & 0xF;
//        int y = (key >> 8) & 0xFF;
//        return new int[]{x, y, z};
//    }
//
//    /**
//     * 生成两个位置对的缓存键
//     */
//    private long getPosPairKey(BlockPos pos1, BlockPos pos2) {
//        long key1 = ((long)pos1.getX() << 32) | (pos1.getZ() & 0xFFFFFFFFL);
//        long key2 = ((long)pos2.getX() << 32) | (pos2.getZ() & 0xFFFFFFFFL);
//        return key1 < key2 ? (key1 << 32) | (key2 & 0xFFFFFFFFL) : (key2 << 32) | (key1 & 0xFFFFFFFFL);
//    }
//
//    // 内部类：热源数据
//    public static class HeatSourceData {
//        public static final Codec<HeatSourceData> CODEC = RecordCodecBuilder.create(instance ->
//                instance.group(
//                        BlockPos.CODEC.fieldOf("pos").forGetter(data -> data.pos),
//                        Codec.FLOAT.fieldOf("temperature").forGetter(data -> data.temperature),
//                        Codec.FLOAT.fieldOf("power").forGetter(data -> data.power)
//                ).apply(instance, HeatSourceData::new)
//        );
//        public final BlockPos pos;
//        public final float temperature;
//        public final float power;
//
//        public HeatSourceData(BlockPos pos, float temperature, float power) {
//            this.pos = pos;
//            this.temperature = temperature;
//            this.power = power;
//        }
//    }
//
//    // 内部类：冷源数据
//    public static class ColdSourceData {
//        public static final Codec<ColdSourceData> CODEC = RecordCodecBuilder.create(instance ->
//                instance.group(
//                        BlockPos.CODEC.fieldOf("pos").forGetter(data -> data.pos),
//                        Codec.FLOAT.fieldOf("temperature").forGetter(data -> data.temperature),
//                        Codec.FLOAT.fieldOf("power").forGetter(data -> data.power)
//                ).apply(instance, ColdSourceData::new)
//        );
//        public final BlockPos pos;
//        public final float temperature;
//        public final float power;
//
//        public ColdSourceData(BlockPos pos, float temperature, float power) {
//            this.pos = pos;
//            this.temperature = temperature;
//            this.power = power;
//        }
//    }
//
//
//    private Map<Integer, Float> getBlockTemperatures() {
//        return blockTemperatures;
//    }
//
//    private long getLastUpdatedTick() {
//        return lastUpdatedTick;
//    }
//
//    private Map<Long, Float> getThermalConductionCache() {
//        return Collections.unmodifiableMap(thermalConductionCache);
//    }
//
//    public ChunkPos getChunkPos() {
//        return chunkPos;
//    }
//
//    public float getAverageTemperature() {
//        return averageTemperature;
//    }
//
//    public List<HeatSourceData> getActiveHeatSources() {
//        return Collections.unmodifiableList(activeHeatSources);
//    }
//
//    public List<ColdSourceData> getActiveColdSources() {
//        return Collections.unmodifiableList(activeColdSources);
//    }
//}*//