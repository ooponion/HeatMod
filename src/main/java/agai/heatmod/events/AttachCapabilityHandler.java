package agai.heatmod.events;

import agai.heatmod.bootstrap.block.CapabilityRegistry;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static agai.heatmod.Heatmod.MODID;

@Mod.EventBusSubscriber(modid = MODID,bus = Mod.EventBusSubscriber.Bus.FORGE)
public class AttachCapabilityHandler {
    @SubscribeEvent
    public static void attachCapabilities(AttachCapabilitiesEvent event) {
        CapabilityRegistry.attachCaps(event);
    }
}
