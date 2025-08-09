package agai.heatmod;

import agai.heatmod.events.BootstrapEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(Heatmod.MODID)
public class Heatmod {

    public static final String MODID = "heatmod";

    public Heatmod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();//mod 总线
        modEventBus.addListener(BootstrapEvent::onCommonSetup);
        modEventBus.addListener(BootstrapEvent::onClientSetup);


        MinecraftForge.EVENT_BUS.addListener(BootstrapEvent::onServerStarting);//forge总线
        MinecraftForge.EVENT_BUS.addListener(BootstrapEvent::onServerStarted);


    }
}
