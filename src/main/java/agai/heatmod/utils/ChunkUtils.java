package agai.heatmod.utils;

import agai.heatmod.annotators.InTest;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.ChunkPos;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

@InTest
public class ChunkUtils {
    private static final Method getChunks;

    static {
        try {
            getChunks = ChunkMap.class.getDeclaredMethod("getChunks");
            getChunks.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * 查找圆形范围内的所有区块坐标
     * @param centerPos 中心方块坐标
     * @param chunkRadius 区块半径（单位：区块数）
     * @return 圆形范围内的区块列表
     */
    public static List<ChunkPos> findCircularChunks(BlockPos centerPos, int chunkRadius) {
        List<ChunkPos> result = new ArrayList<>();

        int centerChunkX = centerPos.getX() >> 4;
        int centerChunkZ = centerPos.getZ() >> 4;

        for (int x = centerChunkX - chunkRadius; x <= centerChunkX + chunkRadius; x+=1) {
            for (int z = centerChunkZ - chunkRadius; z <= centerChunkZ + chunkRadius; z+=1) {
                double distance = Math.sqrt(Math.pow(x - centerChunkX, 2) + Math.pow(z - centerChunkZ, 2));
                if (distance <= chunkRadius) {
                    result.add(new ChunkPos(x, z));
                }
            }
        }
        return result;
    }
    /**默认y=0*/
    public static BlockPos toBlockPos(ChunkPos chunkPos) {
        return new BlockPos(chunkPos.x << 4, 0, chunkPos.z << 4);
    }
    /**反射获取visibleChunkMap*/
    public static Iterable<ChunkHolder> getVisibleChunkMap(ServerLevel serverLevel) {
        try {
            ServerChunkCache chunkManager =serverLevel.getChunkSource();
            ChunkMap chunkStorage = chunkManager.chunkMap;
            return (Iterable<ChunkHolder>) getChunks.invoke(chunkStorage);
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException("Failed to reflect chunk map", e);
        }
    }
    public static List<LevelChunk> getAllLoadedLevelChunks(ServerLevel serverLevel) {
        var visibleChunkMap = ChunkUtils.getVisibleChunkMap(serverLevel);
        List<LevelChunk> chunks = new ArrayList<>();
        for (ChunkHolder holder : visibleChunkMap) {
            LevelChunk chunk = holder.getTickingChunk();
            if (chunk == null) {
                chunk = holder.getFullChunk();
            }
            if (chunk != null) {
                chunks.add(chunk);
            }
        }
        return chunks;
    }
    public static List<LevelChunk> getAllFullLevelChunks(ServerLevel serverLevel) {
        var visibleChunkMap = ChunkUtils.getVisibleChunkMap(serverLevel);
        List<LevelChunk> chunks = new ArrayList<>();
        for (ChunkHolder holder : visibleChunkMap) {
            LevelChunk chunk = holder.getFullChunk();
            if (chunk != null) {
                chunks.add(chunk);
            }
        }
        return chunks;
    }
    public static List<LevelChunk> getAllTickingLevelChunks(ServerLevel serverLevel) {
        var visibleChunkMap = ChunkUtils.getVisibleChunkMap(serverLevel);
        List<LevelChunk> chunks = new ArrayList<>();
        for (ChunkHolder holder : visibleChunkMap) {
            LevelChunk chunk = holder.getTickingChunk();
            if (chunk != null) {
                chunks.add(chunk);
            }
        }
        return chunks;
    }
    public static List<BlockPos> getAllBlockPosInChunk(LevelChunk chunk) {
        List<BlockPos> blockPos = new ArrayList<>();
        ChunkPos chunkPos = chunk.getPos();
        int startX = chunkPos.getMinBlockX();
        int startZ = chunkPos.getMinBlockZ();
        int minY = chunk.getMinBuildHeight();
        int maxY = chunk.getMaxBuildHeight();
        for (int x = startX; x < startX + 16; x++) {
            for (int z = startZ; z < startZ + 16; z++) {
                for (int y = minY; y < maxY; y++) {
                    blockPos.add(new BlockPos(x, y, z));
                }
            }
        }
        return  blockPos;
    }
}