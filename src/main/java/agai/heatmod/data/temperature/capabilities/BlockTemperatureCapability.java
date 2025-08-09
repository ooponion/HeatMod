package agai.heatmod.data.temperature.capabilities;

import agai.heatmod.annotators.ApiDoc;
import agai.heatmod.annotators.InTest;
import agai.heatmod.annotators.InWorking;
import agai.heatmod.data.temperature.properties.BlockThermalProperties;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.mojang.text2speech.Narrator.LOGGER;

@Deprecated
@ApiDoc(description = "* 核心功能：\n" +
        " * 将温度数据(BlockThermalProperties)写入BlockCapabilities.\n")

public class BlockTemperatureCapability implements ICapabilityProvider, INBTSerializable<CompoundTag> {
    public static final Codec<BlockTemperatureCapability> CODEC = RecordCodecBuilder.create(instance -> // Given an instance
            instance.group(
                    BlockThermalProperties.CODEC.fieldOf("blockThermalProperties").forGetter(BlockTemperatureCapability::getBlockThermalProperties)
            ).apply(instance, BlockTemperatureCapability::new)
    );
    private static final Capability<BlockThermalProperties> BLOCK_THERMAL_PROPERTIES_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});
    private final LazyOptional<BlockThermalProperties> blockThermalPropertiesLazyOptional=LazyOptional.of(()->this.blockThermalProperties);


    private BlockThermalProperties blockThermalProperties;
    public BlockTemperatureCapability(BlockThermalProperties blockThermalProperties) {
        this.blockThermalProperties = blockThermalProperties;
    }

    public BlockThermalProperties getBlockThermalProperties() {
        return blockThermalProperties;
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if(cap == BLOCK_THERMAL_PROPERTIES_CAPABILITY){
            return blockThermalPropertiesLazyOptional.cast();
        }
        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        BlockThermalProperties.CODEC.encodeStart(NbtOps.INSTANCE, blockThermalProperties)
                .resultOrPartial(error -> LOGGER.error("Failed to serialize: {}", error))
                .ifPresent(nbt -> tag.put("Data", nbt));
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        BlockThermalProperties.CODEC.parse(NbtOps.INSTANCE, nbt.get("Data"))
                .resultOrPartial(error -> LOGGER.error("Failed to deserialize: {}", error))
                .ifPresent(data -> this.blockThermalProperties = data);
    }
}
