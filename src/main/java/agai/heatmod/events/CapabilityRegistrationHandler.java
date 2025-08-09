package agai.heatmod.events;


import agai.heatmod.bootstrap.block.CapabilityRegistry;
import agai.heatmod.utils.SystemOutHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static agai.heatmod.Heatmod.MODID;

@Mod.EventBusSubscriber(modid = MODID,bus = Mod.EventBusSubscriber.Bus.MOD)
public class CapabilityRegistrationHandler {
    @SubscribeEvent
    public void registerCaps(RegisterCapabilitiesEvent event) {
        CapabilityRegistry.registerCaps(event);
    }
}
