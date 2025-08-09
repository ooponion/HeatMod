package agai.heatmod.events;

import agai.heatmod.data.temperature.capabilities.ChunkTemperatureCapability;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static agai.heatmod.Heatmod.MODID;

@Mod.EventBusSubscriber(modid = MODID,bus = Mod.EventBusSubscriber.Bus.FORGE)
public class BlockEventHandler {
//    @SubscribeEvent
//    public static void onBlockPlaced(BlockEvent.EntityPlaceEvent event) {
//        // 获取放置的方块
//        BlockState placedBlock = event.getPlacedBlock();
//        BlockPos pos = event.getPos();
//        LevelAccessor level = event.getLevel();
//        Entity placer = event.getEntity();
//        LevelChunk chunk = (LevelChunk) level.getChunk(pos);
//        if(level.isClientSide()){
//            return;
//        }
//        chunk.getCapability(ChunkTemperatureCapability.CAPABILITY).ifPresent(cap -> {cap.addSource(chunk,pos);});
//    }
//    @SubscribeEvent
//    public static void onMutiBlockPlaced(BlockEvent.EntityMultiPlaceEvent event) {
//       onBlockPlaced(event);
//    }
//    @SubscribeEvent
//    public static void onBlockBreak(BlockEvent.BreakEvent event) {
//        onBlockRemoved(event.getPos(), event.getLevel());
//    }
//    @SubscribeEvent
//    public static void onNeighborNotify(BlockEvent.NeighborNotifyEvent event) {
//        onBlockRemoved(event.getPos(), event.getLevel());
//
//    }
//
//    private static void onBlockRemoved(BlockPos pos, LevelAccessor level) {
//        if(level.isClientSide()){
//            return;
//        }
//        LevelChunk chunk = (LevelChunk) level.getChunk(pos);
//        chunk.getCapability(ChunkTemperatureCapability.CAPABILITY).ifPresent(cap -> {cap.removeSource(chunk, pos);});
//    }
}

