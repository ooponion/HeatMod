package agai.heatmod.bootstrap.block;

import agai.heatmod.commands.TestCommand;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;

public class CommandRegistry {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        TestCommand.register(dispatcher);
    }
}
