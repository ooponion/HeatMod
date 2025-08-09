package agai.heatmod.data.temperature.capabilities;

import agai.heatmod.annotators.ApiDoc;
import agai.heatmod.annotators.InTest;
import agai.heatmod.data.temperature.data.impl.ChunkTemperatureIntf;
import agai.heatmod.data.temperature.data.ChunkTemperatureData;
import agai.heatmod.utils.SystemOutHelper;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@InTest
@ApiDoc(description = " * 职责：将ChunkTemperatureData存入capabilities\n")


public class ChunkTemperatureCapability  implements ICapabilityProvider, INBTSerializable<CompoundTag> {
//    public static final Codec<ChunkTemperatureCapability> CODEC = RecordCodecBuilder.create(instance -> // Given an instance
//            instance.group(
//                    ChunkTemperatureData.CODEC.fieldOf("chunk_temperature_data").forGetter(ChunkTemperatureCapability::getChunkTemperatureData)
//            ).apply(instance, ChunkTemperatureCapability::new)
//    );
    public static final Capability<ChunkTemperatureIntf> CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});
    private final LazyOptional<ChunkTemperatureData> chunkTemperatureDataLazyOptional=LazyOptional.of(()->this.chunkTemperatureData);


    private ChunkTemperatureData chunkTemperatureData ;
    public ChunkTemperatureCapability(ChunkTemperatureData chunkTemperatureData) {
        this.chunkTemperatureData = chunkTemperatureData;
    }

    public ChunkTemperatureIntf getChunkTemperatureData() {
        return chunkTemperatureData;
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if(cap == CAPABILITY){
            return chunkTemperatureDataLazyOptional.cast();
        }
        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        ChunkTemperatureData.CODEC.encodeStart(NbtOps.INSTANCE, chunkTemperatureData)
                .resultOrPartial(error -> SystemOutHelper.printfplain("Failed to serialize: %s", error))
                .ifPresent(nbt -> tag.put("data", nbt));
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        ChunkTemperatureData.CODEC.parse(NbtOps.INSTANCE, nbt.get("data"))
                .resultOrPartial(error -> SystemOutHelper.printfplain("Failed to deserialize: %s", error))
                .ifPresent(data -> this.chunkTemperatureData = data);
    }
}
