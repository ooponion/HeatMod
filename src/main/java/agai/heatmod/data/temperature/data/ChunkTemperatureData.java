package agai.heatmod.data.temperature.data;

import agai.heatmod.annotators.ApiDoc;
import agai.heatmod.annotators.DependsOn;
import agai.heatmod.annotators.InWorking;
import agai.heatmod.content.temperature.hotandcoolsources.ThermalChangeSource;
import agai.heatmod.data.temperature.ThermalDataManager;
import agai.heatmod.data.temperature.WorldTemperature;
import agai.heatmod.data.temperature.data.impl.ChunkTemperatureIntf;
import agai.heatmod.data.temperature.recipeData.BlockTempData;
import agai.heatmod.debug.DebugConfig;
import agai.heatmod.utils.SystemOutHelper;
import agai.heatmod.utils.codec.CompressDifferCodec;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

@InWorking
@ApiDoc(description = " * 职责：管理区块级别的温度数据存储\n" +
        " * 核心功能：存储区块内所有方块的实时温度（高效存储，避免内存浪费）。\n" +
        "记录区块内的「活跃热源 / 冷源」（如岩浆、火把、冰块），用于快速热计算。\n" +
        "记录区块内的")
public class ChunkTemperatureData implements ChunkTemperatureIntf , Serializable {
//    private static final Codec<ResourceKey<Level>> LEVEL_KEY_CODEC = Codec.STRING.xmap(
//            // 解码：从字符串解析为 ResourceKey（如 "minecraft:overworld" → ResourceKey）
//            str -> ResourceKey.create(Registries.DIMENSION, new ResourceLocation(str)),
//            // 编码：将 ResourceKey 转为字符串（如 ResourceKey → "minecraft:overworld"）
//            key -> key.location().toString()
//    );
    public static final Codec<ChunkTemperatureData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    CompressDifferCodec.ChunkPosCodec.CODEC.fieldOf("chunk_pos").forGetter(data -> data.chunkPos),
                    // 改用List存储（编码时转换为Map，解码时转换为数组）
                    Codec.unboundedMap(Codec.STRING, Codec.FLOAT).fieldOf("block_temperatures").forGetter(data ->
                            Arrays.stream(data.blockTemperatures)
                                    .filter(Objects::nonNull)
                                    .collect(Collectors.toMap(
                                            temp -> Integer.toString(data.encodeLocalPos(temp.pos)),
                                            temp -> temp.value
                                    ))
                    ),
                    ThermalChangeSource.CODEC.listOf().fieldOf("active_heat_sources").forGetter(ChunkTemperatureData::getActiveThermalSources),
                    Codec.FLOAT.fieldOf("average_temperature").forGetter(data -> data.averageTemperature),
                    Codec.unboundedMap(Codec.STRING, Codec.FLOAT).fieldOf("thermal_conduction_cache").forGetter(data ->
                            Arrays.stream(data.thermalConductionCache)
                                    .filter(Objects::nonNull)
                                    .collect(Collectors.toMap(
                                            cache -> Integer.toString(cache.encodePos),
                                            cache -> cache.value
                                    ))
                    ),
                    Level.RESOURCE_KEY_CODEC.fieldOf("level").forGetter(data -> data.levelResourceKey)
            ).apply(instance, (pos, temps, sources, avg, cache, levelKey) -> {
                Level level = ServerLifecycleHooks.getCurrentServer().getLevel(levelKey);
                ChunkTemperatureData data = new ChunkTemperatureData(pos, levelKey, avg, level);
                // 解码时将Map转换为数组
                temps.forEach((key, value) -> {
                    int encodedKey = Integer.parseInt(key);
                    BlockPos blockPos = data.decodeLocalPos(encodedKey);
                    data.setTemperature(blockPos, value);
                });
                cache.forEach((key, value) -> {
                    int key_ = Integer.parseInt(key);
                    BlockPos blockPos = data.decodeLocalPos(key_);
                    int encodePos=data.encodeLocalPos(blockPos);
                    data.thermalConductionCache[encodePos] = new CacheEntry(encodePos, value);
                });
                data.activeThermalSources.addAll(sources);
                return data;
            })
    );
//public static final Codec<ChunkTemperatureData> CODEC = RecordCodecBuilder.create(instance ->
//        instance.group(
//                CompressDifferCodec.ChunkPosCodec.CODEC.fieldOf("chunk_pos").forGetter(data -> data.chunkPos)
//                ,LEVEL_KEY_CODEC.fieldOf("level_key").forGetter(data->data.levelResourceKey),
//                // 改用List存储（编码时转换为Map，解码时转换为数组）
//                Codec.unboundedMap(Codec.STRING, Codec.FLOAT).fieldOf("block_temperatures").forGetter(data ->
//                        Arrays.stream(data.blockTemperatures)
//                                .filter(Objects::nonNull)
//                                .collect(Collectors.toMap(
//                                        temp -> Integer.toString(data.encodeLocalPos(temp.pos)),
//                                        temp -> temp.value
//                                ))
//                )
//        ).apply(instance, (pos,levelKey,temps) -> {
//            DebugConfig.debug(DebugConfig.enumDebug.CHUNKCAPA,(t)->{
//                SystemOutHelper.printCallers("Reserialize1(%s",0,pos);
//            });
//            Level level = ServerLifecycleHooks.getCurrentServer().getLevel(levelKey);
//            ChunkTemperatureData data = new ChunkTemperatureData(pos, levelKey, 27, level);
//            temps.forEach((key, value) -> {
//                int encodedKey = Integer.parseInt(key);
//                BlockPos blockPos = data.decodeLocalPos(encodedKey);
//                data.setTemperature(blockPos, value);
//            });
//            DebugConfig.debug(DebugConfig.enumDebug.CHUNKCAPA,(t)->{
//                SystemOutHelper.printCallers("Reserialize2(%s",0,data.getChunkPos());
//            });
//
//            return data;
//        })
//);

    // 所属区块坐标
    private final ChunkPos chunkPos;
    private final Level level;
    private final ResourceKey<Level> levelResourceKey;

    // 区块高度范围（缓存，避免重复计算）
    private final int minY;
    private final int maxY;
    private final int yRange; // maxY - minY + 1（用于数组大小计算）

    // 高效存储方块温度：使用数组替代HashMap（x:0-15, z:0-15, y:0-yRange-1）
    // 数组索引 = x + z*16 + y*16*16
    private final TempEntry[] blockTemperatures;
    private final CacheEntry[] thermalConductionCache;

    // 区块内活跃热源列表（只存储有效热源）
    private List<ThermalChangeSource> activeThermalSources = new ArrayList<>();

    // 最后一次更新的游戏刻
    private long lastUpdatedTick = 0L;

    // 区块平均温度（缓存值）及统计信息（避免每次遍历计算）
    private float averageTemperature = 27f;
    private int tempCount = 0; // 存储的非默认温度数量
    private float tempSum = 0f; // 存储的非默认温度总和

    // 温度差异阈值：超过此值才会被存储（减少存储量）
    private static final float TEMP_STORAGE_THRESHOLD = 2.0f;

    // 复用的可变坐标对象（减少对象创建）
    private final BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

    // 内部类：存储方块坐标和温度（数组用）
    private static class TempEntry {
        BlockPos pos;
        float value;
        TempEntry(BlockPos pos, float value) {
            this.pos = pos;
            this.value = value;
        }
    }

    // 内部类：热传导缓存条目
    private static class CacheEntry {
        int encodePos;
        float value;
        CacheEntry(int encodePos, float value) {
            this.encodePos = encodePos;
            this.value = value;
        }
    }

    public ChunkTemperatureData(@NotNull ChunkPos chunkPos, @NotNull ResourceKey<Level> levelResourceKey,
                                float initialAvgTemp, @NotNull Level level) {
        this.chunkPos = chunkPos;
        this.levelResourceKey = levelResourceKey;
        this.level = level;
        this.averageTemperature = initialAvgTemp;

        // 初始化高度范围（1.20.1通常为-64到320，共385格）
        this.minY = level.getMinBuildHeight();
        this.maxY = level.getMaxBuildHeight();
        this.yRange = maxY - minY + 1;

        // 初始化数组（16*x * 16*z * yRange）
        int arraySize = 16 * 16 * yRange;
        this.blockTemperatures = new TempEntry[arraySize];
        this.thermalConductionCache = new CacheEntry[arraySize];
    }

    public ChunkTemperatureData(@NotNull ChunkPos chunkPos, float initialAvgTemp, @NotNull Level level) {
        this(chunkPos, level.dimension(), initialAvgTemp, level);
    }

    public ChunkTemperatureData(@NotNull ChunkPos chunkPos, @NotNull Level level) {
        this(chunkPos, level.dimension(), 27f, level);
    }

    public ChunkTemperatureData(@NotNull Level level) {
        this(new ChunkPos(114, 114), level);
    }

    /**
     * 获取指定位置的温度（优化：数组直接访问，减少哈希计算）
     */
    @DependsOn(clazz = WorldTemperature.class)
    public float getTemperature(BlockPos pos) {
        if(pos.getY()<minY || pos.getY()>maxY){
            return 0;
        }

        if (!isPosInChunk(pos)) {
            return ThermalDataManager.INSTANCE.getTemperature(level, pos);
        }

        int index = encodeLocalPos(pos);

        TempEntry entry = blockTemperatures[index];
        if (entry != null) {
            // 调试输出仅在指定坐标且开启调试时生效
            debugTemperature(pos, entry.value);
            return entry.value;
        }

        // 计算默认温度并缓存
        float defaultTemp = WorldTemperature.block(level, pos);
        blockTemperatures[index] = new TempEntry(pos.immutable(), defaultTemp);


        // 更新统计信息（用于平均温度计算）
        tempSum += defaultTemp;
        tempCount++;

        debugTemperature(pos, defaultTemp);
        return defaultTemp;
    }

    public float getTemperature(int key) {
        BlockPos pos = decodeLocalPos(key);
        return getTemperature(pos);
    }

    /**
     * 设置指定位置的温度（优化：数组直接写入）
     */
    public void setTemperature(BlockPos pos, float temperature) {
        if(pos.getY()<minY || pos.getY()>maxY){
            return;
        }
        if (!isPosInChunk(pos)) {
            return;
        }

        int index = encodeLocalPos(pos);
        TempEntry entry = blockTemperatures[index];
        float oldValue = 0f;

        if (entry != null) {
            oldValue = entry.value;
            entry.value = temperature;
        } else {
            entry = new TempEntry(pos.immutable(), temperature);
            blockTemperatures[index] = entry;
            tempCount++; // 新增条目
        }

        // 更新温度总和（用于平均计算）
        tempSum = tempSum - oldValue + temperature;

        // 调试输出
        debugTemperature(pos, temperature);
    }

    public void setTemperature(int key, float temperature) {
        BlockPos pos = decodeLocalPos(key);
        setTemperature(pos, temperature);
    }

    /**
     * 累积热量到缓存（优化：数组访问替代HashMap）
     */
    public void accumulateTempToCache(BlockPos pos, float delta) {
        if(pos.getY()<minY || pos.getY()>maxY){
            return;
        }
        if (!isPosInChunk(pos)) {
            return;
        }

        int index = encodeLocalPos(pos);
        CacheEntry entry = thermalConductionCache[index];
        if (entry != null) {
            entry.value += delta;
        } else {
            thermalConductionCache[index] = new CacheEntry(index, delta);
        }

        debugCacheAccumulate(pos, delta, thermalConductionCache[index].value);
    }

    /**
     * 从缓存转移热量到实际温度（优化：减少Map操作）
     */
    public void transferTempFromCache(BlockPos pos) {
        if(pos.getY()<minY || pos.getY()>maxY){
            return;
        }
        if (!isPosInChunk(pos)) {
            return;
        }

        int index = encodeLocalPos(pos);
        CacheEntry entry = thermalConductionCache[index];
        if (entry == null || entry.value == 0) {
            return;
        }

        float newTemp = getTemperature(pos) + entry.value;
        setTemperature(pos, newTemp);

        entry.value = 0;

        debugCacheTransfer(pos, newTemp, entry.value);
    }

    /**
     * 刷新区块内的热源和冷源列表（修复Section遍历的API错误）
     */
    public void refreshSources(LevelChunk chunk) {
        activeThermalSources.clear();
        ChunkPos chunkPos = chunk.getPos();
        int chunkMinX = chunkPos.getMinBlockX();
        int chunkMinZ = chunkPos.getMinBlockZ();

        // 复用可变坐标
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

        // 遍历区块内的所有分段（Section）
        for (int sectionIndex = 0; sectionIndex < chunk.getSectionsCount(); sectionIndex++) {
            // 获取分段（LevelChunkSection），注意：chunk.getSections()返回的数组可能包含null
            var section = chunk.getSections()[sectionIndex];
            if (section == null) {
                continue;
            }

            // 计算分段的世界Y坐标范围（关键修正）
            int sectionY = chunk.getSectionYFromSectionIndex(sectionIndex);
            int minSectionY = SectionPos.sectionToBlockCoord(sectionY);

            // 遍历分段内的所有方块（x:0-15, z:0-15, y:0-15，对应世界坐标）
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    for (int yOffset = 0; yOffset < 16; yOffset++) {
                        // 计算世界Y坐标（分段起始Y + 偏移量）
                        int worldY = minSectionY + yOffset;

                        mutable.set(chunkMinX + x, worldY, chunkMinZ + z);

                        BlockState state = section.getBlockState(x, yOffset, z);

                        if (BlockTempData.getData(state.getBlock()).isActiveSource()) {
                            activeThermalSources.add(new ThermalChangeSource(
                                    state, mutable.immutable(), level.dimension()
                            ));
                        }
                    }
                }
            }
        }
    }

    public void addSource(ChunkAccess access, BlockPos blockPos) {
        if (!isPosInChunk(blockPos)) return;

        BlockState state = access.getBlockState(blockPos);
        if (BlockTempData.getData(state.getBlock()).isActiveSource()) {
            activeThermalSources.add(new ThermalChangeSource(
                    state, blockPos, ((LevelChunk) access).getLevel().dimension()
            ));
        }

        DebugConfig.debug(DebugConfig.enumDebug.CHUNKCAPA,(type)->
                SystemOutHelper.printfplain("Added source: %s", blockPos)
        );
    }

    public void removeSource(ChunkAccess access, final BlockPos blockPos) {
        activeThermalSources.removeIf(source -> source.getBlockPos().equals(blockPos));

        DebugConfig.debug(DebugConfig.enumDebug.CHUNKCAPA,(type)->
                SystemOutHelper.printfplain("Removed source: %s", blockPos)
        );

    }

    /**
     * 更新区块平均温度（优化：O(1)计算，避免遍历所有元素）
     */
    public void updateAverageTemperature() {
        if (tempCount == 0) {
            return; // 无数据时保持原值
        }

        // 基于缓存的总和和计数计算，而非遍历
        float storedAvg = tempSum / tempCount;
        // 平滑更新（保留70%旧值，30%新值）
        averageTemperature = averageTemperature * 0.7f + storedAvg * 0.3f;
    }

    /**
     * 检查位置是否在当前区块内（优化：直接计算ChunkPos）
     */

    public boolean isPosInChunk(BlockPos pos) {

        int posChunkX = pos.getX() >> 4;
        int posChunkZ = pos.getZ() >> 4;

        return (posChunkX == chunkPos.x && posChunkZ == chunkPos.z);
    }

    /**
     * 编码本地坐标为整数键（优化：确保无冲突）
     */
    private int encodeLocalPos(BlockPos pos) {
        int localX = pos.getX() & 0xF; // 0-15
        int localZ = pos.getZ() & 0xF; // 0-15
        int localY = pos.getY() - minY; // 映射到0-yRange-1
        return localX + (localZ << 4) + (localY << 8);
    }

    /**
     * 解码整数键为本地坐标（复用可变对象）
     */
    private BlockPos decodeLocalPos(int encoded) {
        int x = encoded & 0xF;
        int z = (encoded >> 4) & 0xF;
        int y = (encoded >> 8) + minY; // 还原为世界Y坐标
        return mutablePos.set(x + chunkPos.getMinBlockX(), y, z + chunkPos.getMinBlockZ());
    }


    /**
     * 调试输出（限制频率和范围，避免性能影响）
     */
    private void debugTemperature(BlockPos pos, float temp) {
        DebugConfig.debug(DebugConfig.enumDebug.CHUNKCAPA,(type) -> {
            if (pos.equals(new BlockPos(7, 134, 7))) {
                SystemOutHelper.printf("Temperature at %s: %s", pos, temp);
            }
        });
    }

    private void debugCacheAccumulate(BlockPos pos, float delta, float total) {
        DebugConfig.debug(DebugConfig.enumDebug.CHUNKCAPA,(type)-> {
            if (pos.equals(new BlockPos(7, 134, 7))) {
                SystemOutHelper.printCallers("Accumulated to cache %s: +%s (total: %s)", 0, pos, delta, total);
            }
        });
    }

    private void debugCacheTransfer(BlockPos pos, float newTemp, float cache) {
        DebugConfig.debug(DebugConfig.enumDebug.CHUNKCAPA,(type) -> {
            if (pos.equals(new BlockPos(7, 134, 7))) {
                SystemOutHelper.printfplain("Transferred from cache %s: new=%s, cache=%s", pos, newTemp, cache);
            }
        });
    }

    public void syncFromOther(ChunkTemperatureIntf chunkTemperatureIntf) {
        setAverageTemperature(chunkTemperatureIntf.getAverageTemperature());
        if(chunkTemperatureIntf instanceof ChunkTemperatureData chunkData) {
            var source=chunkData.blockTemperatures;
            System.arraycopy(source, 0, this.blockTemperatures, 0, source.length);
        }

        activeThermalSources.clear();
        activeThermalSources.addAll(chunkTemperatureIntf.getActiveThermalSources());
    }



    public ChunkPos getChunkPos() {
        return chunkPos;
    }

    public float getAverageTemperature() {
        return averageTemperature;
    }

    public void setAverageTemperature(float averageTemperature) {
        this.averageTemperature = averageTemperature;
    }


    public List<ThermalChangeSource> getActiveThermalSources() {
        return Collections.unmodifiableList(activeThermalSources);
    }

    public ResourceKey<Level> getLevelResourceKey() {
        return levelResourceKey;
    }
}
