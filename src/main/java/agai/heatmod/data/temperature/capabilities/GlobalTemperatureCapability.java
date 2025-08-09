package agai.heatmod.data.temperature.capabilities;

import agai.heatmod.annotators.ApiDoc;
import agai.heatmod.annotators.InTest;
import agai.heatmod.data.temperature.data.impl.GlobalTemperatureIntf;
import agai.heatmod.data.temperature.data.GlobalTemperatureData;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.mojang.text2speech.Narrator.LOGGER;

@ApiDoc(description = " * 职责：将GlobalTemperatureData存入capabilities\n")
@InTest


public class GlobalTemperatureCapability implements ICapabilityProvider, INBTSerializable<CompoundTag> {
//    public static final Codec<GlobalTemperatureCapability> CODEC = RecordCodecBuilder.create(instance -> // Given an instance
//            instance.group(
//                    GlobalTemperatureData.CODEC.fieldOf("global_temperature_data").forGetter(GlobalTemperatureCapability::getGlobalTemperatureData)
//            ).apply(instance, GlobalTemperatureCapability::new)
//    );
    public static final Capability<GlobalTemperatureIntf> CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});
    private final LazyOptional<GlobalTemperatureData> GlobalTemperatureDataLazyOptional=LazyOptional.of(()->this.globalTemperatureData);


    private GlobalTemperatureData globalTemperatureData= new GlobalTemperatureData();
    public GlobalTemperatureCapability(GlobalTemperatureData GlobalTemperatureData) {
        this.globalTemperatureData = GlobalTemperatureData;
    }
    public GlobalTemperatureCapability() {
    }

    public GlobalTemperatureIntf getGlobalTemperatureData() {
        return globalTemperatureData;
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if(cap == CAPABILITY){
            return GlobalTemperatureDataLazyOptional.cast();
        }
        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        GlobalTemperatureData.CODEC.encodeStart(NbtOps.INSTANCE, globalTemperatureData)
                .resultOrPartial(error -> LOGGER.error("Failed to serialize: {}", error))
                .ifPresent(nbt -> tag.put("Data", nbt));
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        GlobalTemperatureData.CODEC.parse(NbtOps.INSTANCE, nbt.get("Data"))
                .resultOrPartial(error -> LOGGER.error("Failed to deserialize: {}", error))
                .ifPresent(data -> this.globalTemperatureData = data);
    }
}