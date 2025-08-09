package agai.heatmod.bootstrap.thermal;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryBuilder;
import net.minecraftforge.registries.RegistryObject;

import static agai.heatmod.Heatmod.MODID;

@Deprecated
public class DimensionTempRegistry {
    public enum WorldType {
        OVERWORLD(27),
        Nether(150),
        END(0);
        private final int basicTemperature;
        WorldType(int basicTemperature) {
            this.basicTemperature = basicTemperature;
        }
        public int getBasicTemperature() {
            return basicTemperature;
        }
    }

    // 创建 DeferredRegister
    public static final DeferredRegister<WorldType> WORLD_TYPES =
            DeferredRegister.create(
                    new ResourceLocation(MODID, "world_type"),
                    MODID
            );

    // 配置 RegistryBuilder 并注册对象
    public static final RegistryObject<WorldType> OVERWORLD_TEMP = WORLD_TYPES.register(MODID,()->WorldType.OVERWORLD);
    public static final RegistryObject<WorldType> NETHER_TEMP = WORLD_TYPES.register(MODID,()->WorldType.Nether);
    public static final RegistryObject<WorldType> END_TEMP = WORLD_TYPES.register(MODID,()->WorldType.END);

    // 在Mod构造函数中绑定事件总线
    public static void registry() {
        WORLD_TYPES.makeRegistry(RegistryBuilder::new);
        WORLD_TYPES.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
    public static float getWorldTemperature(LevelReader levelReader) {
        var resourceKey=((ServerLevel) levelReader).dimension();
        if(resourceKey == Level.OVERWORLD) {
            return END_TEMP.get().getBasicTemperature();
        }else if(resourceKey == Level.NETHER) {
            return NETHER_TEMP.get().getBasicTemperature();
        }else if(resourceKey == Level.END) {
            return END_TEMP.get().getBasicTemperature();
        }
        return 0;
    }
    //RegistryManager.ACTIVE.getRegistry(OVERWORLD_TEMP.getId());
}
