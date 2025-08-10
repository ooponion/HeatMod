package agai.heatmod;

import agai.heatmod.bootstrap.thermal.TempRecipeRegistry;
import agai.heatmod.config.TempConfig;
import agai.heatmod.events.BootstrapEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import static com.mojang.text2speech.Narrator.LOGGER;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(Heatmod.MODID)
public class Heatmod {

    public static final String MODID = "heatmod";
    public static final Logger LOGGER = LogManager.getLogger(  " (" + MODID + ")");
    public static final Marker VERSION_CHECK = MarkerManager.getMarker("Version Check");
    public static final Marker INIT = MarkerManager.getMarker("Init");
    public static final Marker SETUP = MarkerManager.getMarker("Setup");
    public static final Marker COMMON_INIT = MarkerManager.getMarker("Common").addParents(INIT);
    public static final Marker CLIENT_INIT = MarkerManager.getMarker("Client").addParents(INIT);
    public static final Marker COMMON_SETUP = MarkerManager.getMarker("Common").addParents(SETUP);
    public static final Marker CLIENT_SETUP = MarkerManager.getMarker("Client").addParents(SETUP);


    public Heatmod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();//mod 总线
        registries(modEventBus);

        modEventBus.addListener(BootstrapEvent::onCommonSetup);
        modEventBus.addListener(BootstrapEvent::onClientSetup);


        MinecraftForge.EVENT_BUS.addListener(BootstrapEvent::onServerStarting);//forge总线
        MinecraftForge.EVENT_BUS.addListener(BootstrapEvent::onServerStarted);


        LOGGER.info(COMMON_INIT, "Loading Config");

        TempConfig.register();
    }
    public void registries(IEventBus modEventBus){
        TempRecipeRegistry.register(modEventBus);
    }
}
