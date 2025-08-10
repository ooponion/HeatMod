package agai.heatmod.mixins;

import agai.heatmod.config.TempConfig;
import agai.heatmod.data.temperature.recipeData.ArmorTempData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.common.ForgeConfigSpec;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLevel.class)
public class ServalLevel_ChunkUpdate_mixin {
    @Inject(
            method = "tickChunk",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/profiling/ProfilerFiller;popPush(Ljava/lang/String;)V",
                    ordinal = 0, // This targets the profiler.popPush("iceandsnow") call
                    shift = At.Shift.BEFORE
            ),
            cancellable = true)
    private void addTemperatureSection(LevelChunk pChunk, int pRandomTickSpeed, CallbackInfo ci) {
        ServerLevel level = (ServerLevel)(Object)this;
        final long now = level.getGameTime();
        ChunkPos chunkpos = pChunk.getPos();
        boolean updateTempBlock = (now + (chunkpos.x)+(chunkpos.z)) % TempConfig.SERVER.temperatureUpdateIntervalTicks.get() == 0;
        boolean isRaining = level.isRaining();
        int i = chunkpos.getMinBlockX();
        int j = chunkpos.getMinBlockZ();

    }
}
