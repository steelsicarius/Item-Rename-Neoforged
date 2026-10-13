package faewulf.itemrename;

import com.mojang.brigadier.CommandDispatcher;
import faewulf.itemrename.command.addLoreLine;
import faewulf.itemrename.command.glow;
import faewulf.itemrename.command.hideEnchant;
import faewulf.itemrename.command.insertLoreLine;
import faewulf.itemrename.command.lockItem;
import faewulf.itemrename.command.removeGlow;
import faewulf.itemrename.command.removeLore;
import faewulf.itemrename.command.removeLoreLine;
import faewulf.itemrename.command.removeName;
import faewulf.itemrename.command.rename;
import faewulf.itemrename.command.setLoreLine;
import faewulf.itemrename.command.unhideEnchant;
import faewulf.itemrename.command.unlockItem;
import faewulf.itemrename.gametest.ItemrenameGameTests;
import faewulf.itemrename.util.permission;
import net.minecraft.commands.CommandSourceStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.server.permission.events.PermissionGatherEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(Itemrename.MODID)
public class Itemrename {
    public static final String MODID = "itemrename";
    public static final Logger LOGGER = LoggerFactory.getLogger("item-rename");

    public Itemrename(IEventBus modEventBus) {
        // Register ourselves for server and other game events we are interested in,
        // such as command registration and permission node gathering.
        NeoForge.EVENT_BUS.register(this);

        // Register the mod's game tests. The TEST_FUNCTION registry entry is always
        // registered (harmless in production); tests are only *executed* in a
        // gametest-enabled environment (RegisterGameTestsEvent is not fired otherwise).
        ItemrenameGameTests.init(modEventBus);

        LOGGER.info("Initializing...");
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        addLoreLine.register(dispatcher);
        glow.register(dispatcher);
        hideEnchant.register(dispatcher);
        insertLoreLine.register(dispatcher);
        lockItem.register(dispatcher);
        removeGlow.register(dispatcher);
        removeLore.register(dispatcher);
        removeLoreLine.register(dispatcher);
        removeName.register(dispatcher);
        rename.register(dispatcher);
        setLoreLine.register(dispatcher);
        unhideEnchant.register(dispatcher);
        unlockItem.register(dispatcher);
    }

    @SubscribeEvent
    public void onGatherPermissionNodes(PermissionGatherEvent.Nodes event) {
        permission.registerNodes(event);
    }
}
