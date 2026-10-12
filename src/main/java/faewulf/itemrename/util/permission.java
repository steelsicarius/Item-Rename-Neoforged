package faewulf.itemrename.util;

import static faewulf.itemrename.Itemrename.MODID;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.Permissions;
import net.neoforged.neoforge.server.permission.PermissionAPI;
import net.neoforged.neoforge.server.permission.events.PermissionGatherEvent;
import net.neoforged.neoforge.server.permission.nodes.PermissionNode;
import net.neoforged.neoforge.server.permission.nodes.PermissionTypes;

public class permission {
    // Permission nodes. The node names (e.g. "itemrename.rename") are identical to the
    // original Fabric mod, so existing permission setups (e.g. LuckPerms) keep working.
    // The default resolvers replicate the fabric-permissions-api fallback based on
    // vanilla command permission levels (1 = moderators, 3 = admins).

    public static final PermissionNode<Boolean> RENAME = node("rename", Permissions.COMMANDS_MODERATOR, "Allows using the /rename and /removename commands");
    public static final PermissionNode<Boolean> REMOVENAME = RENAME;

    public static final PermissionNode<Boolean> SETLORELINE = node("lore", Permissions.COMMANDS_MODERATOR, "Allows using the lore editing commands");
    public static final PermissionNode<Boolean> INSERTLORELINE = SETLORELINE;
    public static final PermissionNode<Boolean> REMOVELORELINE = SETLORELINE;
    public static final PermissionNode<Boolean> REMOVELORE = SETLORELINE;
    public static final PermissionNode<Boolean> ADDLORELINE = SETLORELINE;

    // for lock item modify name, lore
    public static final PermissionNode<Boolean> LOCK = node("lock", Permissions.COMMANDS_MODERATOR, "Allows using the /lockitem and /unlockitem commands");
    public static final PermissionNode<Boolean> UNLOCK = LOCK;

    // for force modify item name, lore
    public static final PermissionNode<Boolean> ADMIN = node("admin", Permissions.COMMANDS_ADMIN, "Bypasses the item ownership check");

    // enchant glint
    public static final PermissionNode<Boolean> GLOW = node("glow", Permissions.COMMANDS_MODERATOR, "Allows using the /glow and /removeglow commands");
    public static final PermissionNode<Boolean> REMOVEGLOW = GLOW;

    // hide enchant
    public static final PermissionNode<Boolean> HIDEENCHANT = node("hideenchant", Permissions.COMMANDS_MODERATOR, "Allows using the /hideenchant and /unhideenchant commands");
    public static final PermissionNode<Boolean> UNHIDEENCHANT = HIDEENCHANT;

    private static PermissionNode<Boolean> node(String name, Permission defaultPermission, String description) {
        return new PermissionNode<Boolean>(
                MODID,
                name,
                PermissionTypes.BOOLEAN,
                (player, uuid, contexts) -> player != null
                        && player.permissions().hasPermission(defaultPermission)
        ).setInformation(
                Component.literal("itemrename: " + name),
                Component.literal(description)
        );
    }

    public static void registerNodes(PermissionGatherEvent.Nodes event) {
        event.addNodes(
                RENAME,
                SETLORELINE,
                LOCK,
                GLOW,
                HIDEENCHANT,
                ADMIN
        );
    }

    /**
     * Checks the given permission node for the given player.
     * <p>
     * This mirrors the semantics of the fabric-permissions-api used by the original mod:
     * the active permission handler (e.g. LuckPerms) decides the result, falling back to the
     * node's default resolver (vanilla command permission level) when no custom handler is
     * installed.
     */
    public static boolean check(ServerPlayer player, PermissionNode<Boolean> node) {
        return Boolean.TRUE.equals(PermissionAPI.getPermission(player, node));
    }

    /**
     * Same as {@link #check(ServerPlayer, PermissionNode)}, for the player of a command source.
     * Returns false if the source has no player (e.g. console).
     */
    public static boolean check(CommandSourceStack source, PermissionNode<Boolean> node) {
        ServerPlayer player = source.getPlayer();
        return player != null && check(player, node);
    }
}
