package agai.heatmod.schedulers;

import agai.heatmod.annotators.NeedImprovement;
import agai.heatmod.content.temperature.controllers.ThermalEngine;
import agai.heatmod.debug.DebugConfig;
import agai.heatmod.utils.SystemOutHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.*;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeConfig;
import net.minecraftforge.event.TickEvent;

@NeedImprovement
public class TemperatureScheduler {
    public static final TemperatureScheduler INSTANCE = new TemperatureScheduler();
    private long tick=1100;
//    public static final TicketType<TemperatureUpdateTicket> TEMP_TICKET =
//            TicketType.create("temperature", Comparator.comparingInt(t -> t.priority));
//
//    public static class TemperatureUpdateTicket {
//        public final int priority=30;
//        public final UUID playerId;
//        public TemperatureUpdateTicket(UUID playerId) {
//            this.playerId = playerId;
//        }
//        @Override
//        public boolean equals(Object other){
//            if(other == this) return true;
//            if(!(other instanceof TemperatureUpdateTicket t)) return false;
//            return t.playerId.equals(this.playerId);
//        }
//    }
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.START && !event.player.level().isClientSide) {
            ServerPlayer player = (ServerPlayer) event.player;
            ChunkPos centerPos = player.chunkPosition();
            ChunkPos lastPlayerChunkPos = new ChunkPos(new BlockPos((int) player.xOld, (int) player.yOld, (int) player.zOld));
            if (!lastPlayerChunkPos.equals(centerPos)) {
//                DistanceManager distanceManager =((ServerLevel)event.player.level()).getChunkSource().chunkMap.getDistanceManager();
//                TemperatureUpdateTicket ticket = new TemperatureUpdateTicket( player.getUUID());
//                distanceManager.removeRegionTicket(TEMP_TICKET, lastPlayerChunkPos, 5,ticket);
//                distanceManager.addRegionTicket(TEMP_TICKET, centerPos, 5, ticket);
            }
        }
    }
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            if(tick%20==0){
                SystemOutHelper.printfplain("Seconds:::::%s",tick/20);
            }
            if(tick%1200==0){
                var server=event.getServer();
                for(ServerLevel level:server.getAllLevels()){
                    ThermalEngine.INSTANCE.applyThermodynamics(level,new ChunkPos(0,0));
                }
            }
            tick++;
        }
    }
}
