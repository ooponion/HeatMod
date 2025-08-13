package agai.heatmod.commands;

import agai.heatmod.content.temperature.controllers.ThermalEngine;
import agai.heatmod.content.temperature.thermodynamics.HeatConduction;
import agai.heatmod.content.temperature.thermodynamics.HeatConvection;
import agai.heatmod.content.temperature.thermodynamics.HeatRadiation;
import agai.heatmod.data.temperature.ThermalDataManager;
import agai.heatmod.data.temperature.capabilities.ChunkTemperatureCapability;
import agai.heatmod.data.temperature.capabilities.GlobalTemperatureCapability;
import agai.heatmod.data.temperature.data.ChunkTemperatureData;
import agai.heatmod.debug.DebugConfig;
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
import net.minecraft.server.level.ServerLevel;
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
        LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal("test")
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
        ServerLevel level = source.getLevel();
        DebugConfig.debug(DebugConfig.enumDebug.CHUNKCAPA,(t)->{
            ThermalEngine.INSTANCE.applyThermodynamics(level,new ChunkPos(0,0));
        });

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
