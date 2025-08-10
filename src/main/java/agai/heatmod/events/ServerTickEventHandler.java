package agai.heatmod.events;

import agai.heatmod.schedulers.TemperatureScheduler;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static agai.heatmod.Heatmod.MODID;

@Mod.EventBusSubscriber(modid = MODID,bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ServerTickEventHandler {
    @SubscribeEvent
    public static void levelTick(TickEvent.LevelTickEvent event) {
        TemperatureScheduler.INSTANCE.onLevelTick(event);
    }
}

