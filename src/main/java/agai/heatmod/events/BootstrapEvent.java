package agai.heatmod.events;

import agai.heatmod.annotators.DocsGenerator;
import agai.heatmod.bootstrap.thermal.ThermalMaterialRegistry;
import agai.heatmod.bootstrap.thermal.DimensionTempRegistry;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import org.slf4j.Logger;

public class BootstrapEvent {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static void onCommonSetup(FMLCommonSetupEvent event) {//modBus
        event.enqueueWork(() -> {
            ThermalMaterialRegistry.registerDefaultBlocks();
            DimensionTempRegistry.registry();
            LOGGER.info("通用初始化完成（客户端和服务器都会执行）");
        });
    }

    public static void onServerStarting(ServerStartingEvent event) {//forge Bus
        LOGGER.info("服务器启动中...");
    }
    public static void onServerStarted(ServerStartedEvent event) {//forge Bus
        LOGGER.info("服务器启动完成...");
    }

    public static void onClientSetup(FMLClientSetupEvent event) {//modBus
        event.enqueueWork(() -> {
            LOGGER.info("客户端初始化完成，玩家名称：{}", Minecraft.getInstance().getUser().getName());
            DocsGenerator.genDocs();
        });
    }
}
