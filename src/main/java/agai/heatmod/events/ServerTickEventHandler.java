package agai.heatmod.events;

import agai.heatmod.content.temperature.controllers.ThermalEngine;
import agai.heatmod.debug.DebugConfig;
import agai.heatmod.schedulers.TemperatureScheduler;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static agai.heatmod.Heatmod.MODID;

@Mod.EventBusSubscriber(modid = MODID,bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ServerTickEventHandler {
    @SubscribeEvent
    public static void serverTick(TickEvent.ServerTickEvent event) {
        TemperatureScheduler.INSTANCE.onServerTick(event);

    }
}

