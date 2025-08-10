package agai.heatmod.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import org.checkerframework.common.value.qual.IntVal;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class TempConfig {
    public static class Server {
        public static @NotNull ForgeConfigSpec.IntValue temperatureUpdateIntervalTicks;
        Server(ForgeConfigSpec.Builder builder){
            builder.push("Temperature");
            temperatureUpdateIntervalTicks = builder.comment("The interval of temperature update in ticks.")
                    .defineInRange("temperatureUpdateIntervalTicks", 20, 1, Integer.MAX_VALUE);
            builder.pop();
        }
    }
    public static class Common {
        Common(ForgeConfigSpec.Builder builder){

        }
    }
    public static class Client {
        Client(ForgeConfigSpec.Builder builder){

        }
    }

    public static final ForgeConfigSpec CLIENT_CONFIG;
    public static final ForgeConfigSpec COMMON_CONFIG;
    public static final ForgeConfigSpec SERVER_CONFIG;
    public static final Client CLIENT;
    public static final Common COMMON;
    public static final Server SERVER;

    public static ArrayList<String> DEFAULT_WHITELIST = new ArrayList<>();

    static {
        ForgeConfigSpec.Builder CLIENT_BUILDER = new ForgeConfigSpec.Builder();
        CLIENT = new Client(CLIENT_BUILDER);
        CLIENT_CONFIG = CLIENT_BUILDER.build();
        ForgeConfigSpec.Builder COMMON_BUILDER = new ForgeConfigSpec.Builder();
        COMMON = new Common(COMMON_BUILDER);
        COMMON_CONFIG = COMMON_BUILDER.build();
        ForgeConfigSpec.Builder SERVER_BUILDER = new ForgeConfigSpec.Builder();
        SERVER = new Server(SERVER_BUILDER);
        SERVER_CONFIG = SERVER_BUILDER.build();
    }

    public static void register() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, CLIENT_CONFIG);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, COMMON_CONFIG);
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, SERVER_CONFIG);
    }
}
