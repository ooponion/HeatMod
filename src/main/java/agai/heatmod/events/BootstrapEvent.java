package agai.heatmod.events;

import agai.heatmod.Heatmod;
import agai.heatmod.annotators.DocsGenerator;
import agai.heatmod.bootstrap.thermal.TempRecipeRegistry;
import agai.heatmod.bootstrap.thermal.ThermalMaterialRegistry;
import agai.heatmod.bootstrap.thermal.DimensionTempRegistry;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLDedicatedServerSetupEvent;





public class BootstrapEvent {
    public static void onCommonSetup(FMLCommonSetupEvent event) {//modBus
        event.enqueueWork(() -> {
//            ThermalMaterialRegistry.registerDefaultBlocks();
//            DimensionTempRegistry.registry();

            Heatmod.LOGGER.info(Heatmod.COMMON_SETUP,"通用初始化完成（客户端和服务器都会执行）");
        });
    }

    public static void onServerStarting(ServerStartingEvent event) {//forge Bus
        Heatmod.LOGGER.info("服务器启动中...");
    }
    public static void onServerStarted(ServerStartedEvent event) {//forge Bus
        Heatmod.LOGGER.info("服务器启动完成...");
    }
    public static void onClientSetup(FMLClientSetupEvent event) {//modBus
        event.enqueueWork(() -> {
            Heatmod.LOGGER.info(Heatmod.CLIENT_SETUP,"客户端初始化完成，玩家名称：{}", Minecraft.getInstance().getUser().getName());
            DocsGenerator.genDocs();
        });
    }
}
