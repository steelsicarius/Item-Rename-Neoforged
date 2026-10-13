package faewulf.itemrename.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import faewulf.itemrename.util.loreEditor;
import faewulf.itemrename.util.ownerCheck;
import faewulf.itemrename.util.permission;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

public class removeLoreLine {

    static public void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("removeloreline")
                        .requires(
                                source -> {
                                    //this command only makes sense for players
                                    if (!source.isPlayer())
                                        return false;

                                    // multiplayer case
                                    if (source.getServer() != null && source.getServer().isDedicatedServer()) {
                                        return permission.check(source, permission.REMOVELORELINE);
                                    } else {
                                        // fallback true for single player world
                                        return true;
                                    }
                                }
                        )
                        .then(Commands.argument("line number", IntegerArgumentType.integer(1))
                                .executes(removeLoreLine::run)
                        )
        );
    }

    static private int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {

        ServerPlayer player = context.getSource().getPlayerOrException();

        //get holding item
        ItemStack holding = player.getItemInHand(InteractionHand.MAIN_HAND);

        //if not holding anything
        if (holding.isEmpty()) {
            throw new SimpleCommandExceptionType(
                    Component.nullToEmpty("You must hold an item to modify it.")).create();
        }

        ownerCheck.check(player, holding);

        int line = IntegerArgumentType.getInteger(context, "line number");

        //holding.set(DataComponentTypes.LORE, formatted);
        loreEditor.removeLoreLine(holding, line);

        return 0;
    }
}
