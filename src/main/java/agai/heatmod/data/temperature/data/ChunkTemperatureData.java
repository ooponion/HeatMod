package agai.heatmod.data.temperature.data;

import agai.heatmod.annotators.ApiDoc;
import agai.heatmod.annotators.DependsOn;
import agai.heatmod.annotators.InWorking;
import agai.heatmod.content.temperature.hotandcoolsources.ThermalChangeSource;
import agai.heatmod.data.temperature.ThermalDataManager;
import agai.heatmod.data.temperature.WorldTemperature;
import agai.heatmod.data.temperature.data.impl.ChunkTemperatureIntf;
import agai.heatmod.utils.SystemOutHelper;
import agai.heatmod.utils.codec.CompressDifferCodec;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.*;

@InWorking
@ApiDoc(description = " * 职责：管理区块级别的温度数据存储\n" +
        " * 核心功能：存储区块内所有方块的实时温度（高效存储，避免内存浪费）。\n" +
        "记录区块内的「活跃热源 / 冷源」（如岩浆、火把、冰块），用于快速热计算。\n" +
        "记录区块内的")
public class ChunkTemperatureData implements ChunkTemperatureIntf , Serializable {
    public static final Codec<ChunkTemperatureData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    CompressDifferCodec.ChunkPosCodec.CODEC.fieldOf("chunk_pos").forGetter(data -> data.chunkPos),
                    Codec.unboundedMap(Codec.INT, Codec.FLOAT).fieldOf("block_temperatures").forGetter(data -> data.blockTemperatures),
                    ThermalChangeSource.CODEC.listOf().fieldOf("active_heat_sources").forGetter(ChunkTemperatureData::getActiveThermalSources),
                    Codec.FLOAT.fieldOf("average_temperature").forGetter(data -> data.averageTemperature),
                    Codec.unboundedMap(Codec.INT, Codec.FLOAT).fieldOf("thermal_conduction_cache").forGetter(data -> data.thermalConductionCache),
                    Level.RESOURCE_KEY_CODEC.fieldOf("level").forGetter(data->data.levelResourceKey)
            ).apply(instance, ChunkTemperatureData::new)
    );
    // 所属区块坐标
    private final ChunkPos chunkPos;
    private final Level level;//not often used

    private final ResourceKey<Level> levelResourceKey;

    // key: 本地坐标编码, value: 温度(°C)
    private Map<Integer, Float> blockTemperatures=new HashMap<>();

    // 区块内活跃热源列表
    private List<ThermalChangeSource> activeThermalSources=new ArrayList<>();


    // 热传导缓存
    private Map<Integer, Float> thermalConductionCache = new HashMap<>();

    // 最后一次更新的游戏刻
    private long lastUpdatedTick = 0L;

    // 区块平均温度（缓存值）
    private float averageTemperature=27f;

    // 温度差异阈值：超过此值才会被存储（减少存储量）
    private static final float TEMP_STORAGE_THRESHOLD = 2.0f;

    public ChunkTemperatureData( @NotNull ChunkPos chunkPos, @NotNull ResourceKey<Level> levelResourceKey, @NotNull float initialAvgTemp,@NotNull Level level) {
        this.chunkPos = chunkPos;
        this.averageTemperature = initialAvgTemp;
        this.levelResourceKey = levelResourceKey;
        this.level = level;
    }
    public ChunkTemperatureData( @NotNull ChunkPos chunkPos, float initialAvgTemp,@NotNull Level level) {
        this(chunkPos, Level.OVERWORLD, initialAvgTemp,level);
    }
    public ChunkTemperatureData( @NotNull ChunkPos chunkPos,@NotNull Level level) {
        this(chunkPos, Level.OVERWORLD, 27f,level);
    }
    public ChunkTemperatureData(@NotNull Level level) {
        this(new ChunkPos(0,0),level);
    }


    public ChunkTemperatureData(ChunkPos chunkPos,
                                Map<Integer, Float> blockTemperatures,
                                List<ThermalChangeSource> activeThermalSources,
                                float averageTemperature,   Map<Integer, Float> thermalConductionCache,ResourceKey<Level> levelResourceKey) {
        this.chunkPos = chunkPos;
        this.blockTemperatures = new HashMap<>(blockTemperatures);
        this.activeThermalSources = new ArrayList<>(activeThermalSources);
        this.averageTemperature = averageTemperature;
        this.thermalConductionCache = new HashMap<>(thermalConductionCache);
        this.levelResourceKey = levelResourceKey;
        this.level= ServerLifecycleHooks.getCurrentServer().getLevel(levelResourceKey);
    }


    /**
     * 获取指定位置的温度
     */
    @DependsOn(clazz=WorldTemperature.class)//如果之后这个类的block()方法改了就不能用这个作为默认值
    public float getTemperature(BlockPos pos) {
        if (!isPosInChunk(pos)) {
            return WorldTemperature.block(level, pos);
        }


        int key = encodeLocalPos(pos);
        if(!blockTemperatures.containsKey(key)) {
            setTemperature(pos, WorldTemperature.block(level, pos));
        }
        return blockTemperatures.get(key);
    }

    /**
     * 设置指定位置的温度
     */
    public void setTemperature(BlockPos pos, float temperature) {
        if (!isPosInChunk(pos)) {
            return;
        }


        int key = encodeLocalPos(pos);
        blockTemperatures.put(key, temperature);
    }

    public void accumulateTempToCache(BlockPos pos, float delta) {
        int key = encodeLocalPos(pos);
        thermalConductionCache.put(key, delta + thermalConductionCache.getOrDefault(key,0f));
    }

    public void transferTempFromCache(BlockPos pos) {
        int key = encodeLocalPos(pos);
        blockTemperatures.put(key, thermalConductionCache.get(key) + blockTemperatures.get(key));
        thermalConductionCache.remove(key);
    }

    /**
     * 刷新区块内的热源和冷源列表
     */
    public void refreshSources(LevelChunk chunk) {
        activeThermalSources.clear();
        // 遍历区块内可能是热源/冷源的方块
        ChunkPos pos = chunk.getPos();
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = 0; y < chunk.getHeight(); y++) {
                    BlockPos blockPos = new BlockPos(
                            pos.getMinBlockX() + x,
                            y,
                            pos.getMinBlockZ() + z
                    );
                    addSource(chunk, blockPos);
                }
            }
        }
    }
    public void addSource(ChunkAccess access, BlockPos blockPos) {
        BlockState state = access.getBlockState(blockPos);
        var dimension=((LevelChunk)access).getLevel().dimension();
        if(ThermalDataManager.INSTANCE.isSource(access,blockPos)) {
            activeThermalSources.add(new ThermalChangeSource(
                    state,
                    blockPos,
                    dimension
            ));
        }
        SystemOutHelper.printfplain("add sources: ", blockPos);
    }
    public void removeSource(ChunkAccess access, final BlockPos blockPos) {

        activeThermalSources.removeIf(heatSourceData -> {return heatSourceData.getBlockPos().equals(blockPos);});
        SystemOutHelper.printfplain("delete sources: ", blockPos);
    }
    /**
     * 更新区块平均温度
     */
    public void updateAverageTemperature() {
        if (blockTemperatures.isEmpty()) {
            return; // 没有特殊温度数据，保持原有平均值
        }

        // 计算所有存储温度的平均值
        double sum =  blockTemperatures.values().stream().mapToDouble(Float::floatValue).sum();
        float storedAvg = (float) (sum / blockTemperatures.size());

        // 平滑更新平均温度（避免突变）
        averageTemperature = averageTemperature * 0.7f + storedAvg * 0.3f;
    }

    /**
     * 检查位置是否在当前区块内
     */
    public boolean isPosInChunk(BlockPos pos) {
        return ChunkPos.getX(pos.getX()) == chunkPos.x &&
                ChunkPos.getZ(pos.getZ()) == chunkPos.z;
    }

    /**
     * 编码本地坐标为整数键
     */
    private int encodeLocalPos(BlockPos pos) {
        // x和z: 0-15 (4位), y: -64到320 (9位，共384格)
        // 调整y值范围：将[-64,320]映射到[0,384]
        int localX = pos.getX() & 0xF;
        int localY = pos.getY();
        int localZ = pos.getZ() & 0xF;
        int adjustedY = localY + 64;  // 使y的最小值从-64变为0
        return  localX | (localZ << 4) | (adjustedY << 8);
    }

    /**
     * 解码整数键为本地坐标
     */
    private void decodeLocalPos(int encoded) {
        int x = encoded & 0xF;          // 取低4位
        int z = (encoded >> 4) & 0xF;   // 取中4位
        int y = (encoded >> 8) - 64;   // 还原原始Y坐标
    }

    /**
     * 生成两个位置对的缓存键
     */
//    private long getPosPairKey(BlockPos pos1, BlockPos pos2) {
//        long key1 = ((long)pos1.getX() << 32) | (pos1.getZ() & 0xFFFFFFFFL);
//        long key2 = ((long)pos2.getX() << 32) | (pos2.getZ() & 0xFFFFFFFFL);
//        return key1 < key2 ? (key1 << 32) | (key2 & 0xFFFFFFFFL) : (key2 << 32) | (key1 & 0xFFFFFFFFL);
//    }

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


    private Map<Integer, Float> getBlockTemperatures() {
        return blockTemperatures;
    }

    private long getLastUpdatedTick() {
        return lastUpdatedTick;
    }

    private Map<Integer, Float> getThermalConductionCache() {
        return Collections.unmodifiableMap(thermalConductionCache);
    }

    public ChunkPos getChunkPos() {
        return chunkPos;
    }

    public float getAverageTemperature() {
        return averageTemperature;
    }

    public List<ThermalChangeSource> getActiveThermalSources() {
        return activeThermalSources;
    }

    public ResourceKey<Level> getLevelResourceKey() {
        return levelResourceKey;
    }
}
