package faewulf.itemrename.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import faewulf.itemrename.util.ownerCheck;
import faewulf.itemrename.util.permission;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.TooltipDisplay;


public class hideEnchant {
    static public void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("hideenchant")
                        .requires(
                                source -> {
                                    //this command only makes sense for players
                                    if (!source.isPlayer())
                                        return false;

                                    // multiplayer case
                                    if (source.getServer() != null && source.getServer().isDedicatedServer()) {
                                        return permission.check(source, permission.HIDEENCHANT);
                                    } else {
                                        // fallback true for single player world
                                        return true;
                                    }
                                }
                        )
                        .executes(hideEnchant::run)
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

        holding.update(DataComponents.TOOLTIP_DISPLAY, TooltipDisplay.DEFAULT, tooltipDisplayComponent -> tooltipDisplayComponent.withHidden(DataComponents.ENCHANTMENTS, true));

        return 0;
    }
}
