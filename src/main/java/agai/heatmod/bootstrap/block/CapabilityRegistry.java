package agai.heatmod.bootstrap.block;

import agai.heatmod.data.temperature.capabilities.ChunkTemperatureCapability;
import agai.heatmod.data.temperature.capabilities.GlobalTemperatureCapability;
import agai.heatmod.data.temperature.data.impl.ChunkTemperatureIntf;
import agai.heatmod.data.temperature.data.ChunkTemperatureData;
import agai.heatmod.utils.SystemOutHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;

import static agai.heatmod.Heatmod.MODID;

public class CapabilityRegistry {
    public static void registerCaps(RegisterCapabilitiesEvent event) {
        SystemOutHelper.printfplain("registerCaps?");
        SystemOutHelper.printfplain("注册前状态: " +
                ChunkTemperatureCapability.CAPABILITY.isRegistered());
        event.register(ChunkTemperatureIntf.class);
        event.register(ChunkTemperatureIntf.class);
        SystemOutHelper.printfplain("当前注册状态: " +
                ChunkTemperatureCapability.CAPABILITY.isRegistered());
        SystemOutHelper.printfplain("registerCaps");
    }
    public static void attachCaps(AttachCapabilitiesEvent event) {
        if(event.getObject() instanceof LevelChunk chunk) {
            if(!chunk.getCapability(ChunkTemperatureCapability.CAPABILITY).isPresent()) {
                event.addCapability(new ResourceLocation(MODID,"chunk_cap"),new ChunkTemperatureCapability(new ChunkTemperatureData(chunk.getPos(),chunk.getLevel().dimension(),37f)));
                SystemOutHelper.printfplain("attachCaps_1");
            }else{
                SystemOutHelper.printfplain("attachCaps Fails Level:%s",chunk.getPos().toString());
            }
        }
        else if(event.getObject() instanceof Level level) {
            if(!level.getCapability(GlobalTemperatureCapability.CAPABILITY).isPresent()) {
                event.addCapability(new ResourceLocation(MODID,"level_cap"),new GlobalTemperatureCapability());
                SystemOutHelper.printfplain("attachCaps_2");
            }else{
                SystemOutHelper.printfplain("attachCaps Fails Level:%s",level.isClientSide,level.dimension().location().toString());
            }
        }else{
            SystemOutHelper.printfplain("attachCaps Fails:%s",event.getObject().getClass().getName());
        }
    }
}
