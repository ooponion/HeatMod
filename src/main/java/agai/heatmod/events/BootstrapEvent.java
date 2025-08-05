package agai.heatmod.events;

import agai.heatmod.Heatmod;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import org.slf4j.Logger;


public class BootstrapEvent {
    private static final Logger LOGGER = LogUtils.getLogger();
    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            // 注意：涉及注册表操作的逻辑需要放在 enqueueWork 中
            LOGGER.info("通用初始化完成（客户端和服务器都会执行）");
        });
    }

    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("服务器启动中...");
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            LOGGER.info("客户端初始化完成，玩家名称：{}", Minecraft.getInstance().getUser().getName());
        });
    }
}
