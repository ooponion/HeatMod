package agai.heatmod.commands;

import agai.heatmod.data.temperature.capabilities.ChunkTemperatureCapability;
import agai.heatmod.data.temperature.capabilities.GlobalTemperatureCapability;
import agai.heatmod.data.temperature.data.ChunkTemperatureData;
import agai.heatmod.utils.SystemOutHelper;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

public class TestCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal("capacities")
                .requires(source -> source.hasPermission(2)) // 要求管理员权限（等级2）
                    .then(Commands.literal("get")
                        .then(Commands.argument("type", IntegerArgumentType.integer())
                            .executes(context -> executeGetTemperature(
                                context,
                                IntegerArgumentType.getInteger(context, "type")
                                )
                            )
                        ));

        dispatcher.register(command);
    }

    // 执行获取温度的逻辑
    private static int executeGetTemperature(CommandContext<CommandSourceStack> context, int type) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        Level level = source.getLevel();
        ChunkPos pos=new ChunkPos(new BlockPos((int) source.getEntity().getX(),0, (int) source.getEntity().getZ()));
        BlockPos blockPos=new BlockPos((int) source.getEntity().getX(),(int) source.getEntity().getY(), (int) source.getEntity().getZ());
        LevelChunk levelChunk = level.getChunk(pos.x, pos.z);

        var globalLazy=level.getCapability(GlobalTemperatureCapability.CAPABILITY);
        var chunkLazy=levelChunk.getCapability(ChunkTemperatureCapability.CAPABILITY);
        SystemOutHelper.printfplain("registered?: chunk:%s  global:%s",ChunkTemperatureCapability.CAPABILITY.isRegistered()
        ,GlobalTemperatureCapability.CAPABILITY.isRegistered());
        SystemOutHelper.printfplain("registered2?: global:%s  chunk:%s  chunk_values:%s",globalLazy.orElse(null)
        ,chunkLazy.orElse(null),chunkLazy.orElse(new ChunkTemperatureData(pos)).getTemperature(blockPos));

        return 1; // 命令执行成功的返回值（约定为1）
    }

    // 从Capability获取指定位置的温度（需根据你的系统实现）
    private static float getTemperatureAtPos(Level level, BlockPos pos) {
        // 这里需要调用你的温度系统逻辑，例如：
        // 1. 获取区块温度Capability
        // 2. 计算指定坐标在区块内的温度
        // 示例返回值：
        return 20.0f;
    }
}
