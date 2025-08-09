package agai.heatmod.content.temperature.hotandcoolsources;

import agai.heatmod.annotators.InTest;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.io.Serializable;

@InTest
public class ThermalChangeSource implements Serializable {
    public static Codec<ThermalChangeSource> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    BlockState.CODEC.fieldOf("block_state").forGetter(ThermalChangeSource::getBlockstate),
                    BlockPos.CODEC.fieldOf("Block_pos").forGetter(ThermalChangeSource::getBlockPos),
                    Level.RESOURCE_KEY_CODEC.fieldOf("level").forGetter(ThermalChangeSource::getLevelResourceKey)
            ).apply(instance, ThermalChangeSource::new)
    );

    private BlockState blockstate;
    private BlockPos blockPos;
    private ResourceKey<Level> levelResourceKey;

    public ThermalChangeSource(BlockState blockstate, BlockPos blockPos, ResourceKey<Level> levelResourceKey) {
        this.blockstate = blockstate;
        this.blockPos = blockPos;
        this.levelResourceKey = levelResourceKey;
    }

    public BlockState getBlockstate() {
        return blockstate;
    }

    public void setBlockstate(BlockState blockstate) {
        this.blockstate = blockstate;
    }

    public BlockPos getBlockPos() {
        return blockPos;
    }

    public void setBlockPos(BlockPos blockPos) {
        this.blockPos = blockPos;
    }

    public ResourceKey<Level> getLevelResourceKey() {
        return levelResourceKey;
    }

    public void setLevelResourceKey(ResourceKey<Level> levelResourceKey) {
        this.levelResourceKey = levelResourceKey;
    }
}
