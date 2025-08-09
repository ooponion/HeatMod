package agai.heatmod.events;

import agai.heatmod.annotators.InTest;
import agai.heatmod.data.temperature.capabilities.ChunkTemperatureCapability;
import agai.heatmod.data.temperature.data.ChunkTemperatureData;
import agai.heatmod.utils.ChunkUtils;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.ServerLifecycleHooks;

@Deprecated
@Mod.EventBusSubscriber
public class ForcedChunkTickHandler {
//    private static long lastUpdatedTick=0;
//
//    @SubscribeEvent
//    public static void onServerTick(TickEvent.ServerTickEvent event) {
//        if (event.phase != TickEvent.Phase.END) {
//            return;
//        }
//        lastUpdatedTick++;
//        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
//        for(ServerLevel level : server.getAllLevels()) {
//            Long2ObjectLinkedOpenHashMap<ChunkHolder> forcedChunks = ChunkUtils.getForcedChunks(level);
//            for (Long2ObjectMap.Entry<ChunkHolder> entry : forcedChunks.long2ObjectEntrySet()) {
//                long chunkPosLong = entry.getLongKey();
//                ChunkPos chunkPos = new ChunkPos(chunkPosLong);
//                LevelChunk chunk = level.getChunk(chunkPos.x, chunkPos.z);
//                LazyOptional<ChunkTemperatureData> dataLazyOptional = chunk.getCapability(ChunkTemperatureCapability.CAPABILITY);
//                if(dataLazyOptional.isPresent()) {
//                    ChunkTemperatureData data = dataLazyOptional.orElseThrow(IllegalStateException::new);
//                    data.processLocalHeatConduction(chunk,lastUpdatedTick);
//                }
//            }
//        }
//    }

}