package faewulf.itemrename.gametest;

import com.mojang.brigadier.tree.CommandNode;
import faewulf.itemrename.util.ownerCheck;
import faewulf.itemrename.util.permission;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import faewulf.itemrename.Itemrename;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.LevelBasedPermissionSet;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Headless functional tests for the ported commands.
 * <p>
 * Runs under the {@code gameTestServer} run configuration:
 * {@code ./gradlew runGameTestServer}
 * <p>
 * Notes:
 * <ul>
 *   <li>Next to this test, vanilla's built-in {@code minecraft:always_pass} test also runs,
 *       so the summary reports "2 tests" (1 of this mod + 1 vanilla builtin).</li>
 *   <li>{@code GameTestHelper#makeMockServerPlayerInLevel} is marked for removal in a future
 *       version, but it is the only way in 26.1.2 to obtain a {@code ServerPlayer} (with a
 *       command source) inside a game test.</li>
 * </ul>
 */
public class ItemrenameGameTests {
    public static final DeferredRegister<Consumer<GameTestHelper>> TEST_FUNCTIONS =
            DeferredRegister.create(Registries.TEST_FUNCTION, Itemrename.MODID);

    public static final Supplier<Consumer<GameTestHelper>> COMMANDS_TEST =
            TEST_FUNCTIONS.register("commands", () -> ItemrenameGameTests::testCommands);

    public static void init(IEventBus modEventBus) {
        TEST_FUNCTIONS.register(modEventBus);
        modEventBus.addListener(ItemrenameGameTests::onRegisterGameTests);
    }

    private static void onRegisterGameTests(RegisterGameTestsEvent event) {
        event.registerTest(
                Identifier.fromNamespaceAndPath(Itemrename.MODID, "commands"),
                new FunctionGameTestInstance(
                        ResourceKey.create(Registries.TEST_FUNCTION, Identifier.fromNamespaceAndPath(Itemrename.MODID, "commands")),
                        new TestData<>(
                                Holder.direct(new TestEnvironmentDefinition.AllOf()),
                                Identifier.withDefaultNamespace("empty"),
                                200,
                                0,
                                true
                        )
                )
        );
    }

    private static void runCommand(MinecraftServer server, ServerPlayer player, String command) {
        server.getCommands().performPrefixedCommand(player.createCommandSourceStack(), command);
    }

    private static Component singleSegment(ItemStack stack, String message) {
        Component name = stack.get(DataComponents.CUSTOM_NAME);
        if (name == null)
            throw new AssertionError(message + ": custom name was not set");
        if (name.getSiblings().size() != 1)
            throw new AssertionError(message + ": expected exactly 1 styled segment, got " + name.getSiblings().size());
        return name.getSiblings().getFirst();
    }

    private static void testCommands(GameTestHelper helper) {
        MinecraftServer server = helper.getLevel().getServer();
        Commands commandManager = server.getCommands();

        // 1) all commands are registered
        for (String name : List.of(
                "rename", "removename", "glow", "removeglow", "hideenchant", "unhideenchant",
                "lockitem", "unlockitem", "addloreline", "insertloreline", "setloreline",
                "removelore", "removeloreline")) {
            helper.assertTrue(commandManager.getDispatcher().getRoot().getChild(name) != null,
                    "Command is not registered: /" + name);
        }

        // 2) create mock players and permission gating
        // NOTE A: GameTestHelper#makeMockServerPlayerInLevel is marked for removal, but it is
        // the only way in 26.1.2 to obtain a ServerPlayer (with a command source) in a test.
        // NOTE B: the gametest server reports isDedicatedServer() == false, so in this test
        // environment the commands use the singleplayer fallback (usable by every player),
        // exactly like the original Fabric mod. The real dedicated-server gating logic
        // (permission nodes with vanilla op-level fallback) is therefore verified directly.
        ServerPlayer owner = helper.makeMockServerPlayerInLevel();
        server.getPlayerList().op(owner.nameAndId(), Optional.of(LevelBasedPermissionSet.OWNER), Optional.empty()); // like the default op level 4 of a regular dedicated server

        ServerPlayer guest = helper.makeMockServerPlayerInLevel(); // regular player without op status
        ServerPlayer moderator = helper.makeMockServerPlayerInLevel();
        server.getPlayerList().op(moderator.nameAndId(), Optional.of(LevelBasedPermissionSet.MODERATOR), Optional.empty());
        ServerPlayer admin = helper.makeMockServerPlayerInLevel();
        server.getPlayerList().op(admin.nameAndId(), Optional.of(LevelBasedPermissionSet.ADMIN), Optional.empty());

        CommandNode<CommandSourceStack> renameNode = commandManager.getDispatcher().getRoot().getChild("rename");
        helper.assertTrue(renameNode.canUse(owner.createCommandSourceStack()), "op'd player must be able to use /rename");

        helper.assertTrue(permission.check(owner.createCommandSourceStack(), permission.RENAME),
                "op'd player must pass the rename permission node");
        helper.assertTrue(permission.check(moderator.createCommandSourceStack(), permission.RENAME),
                "moderator (op level 1) must pass the rename permission node");
        helper.assertFalse(permission.check(guest.createCommandSourceStack(), permission.RENAME),
                "guest (no permissions) must fail the rename permission node");
        helper.assertTrue(permission.check(admin.createCommandSourceStack(), permission.ADMIN),
                "admin (op level 3) must pass the admin permission node");
        helper.assertFalse(permission.check(moderator.createCommandSourceStack(), permission.ADMIN),
                "moderator (op level 1) must fail the admin permission node");
        helper.assertFalse(permission.check(guest.createCommandSourceStack(), permission.ADMIN),
                "guest must fail the admin permission node");

        // 3) rename with legacy color + formatting codes
        ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
        owner.setItemInHand(InteractionHand.MAIN_HAND, sword);
        runCommand(server, owner, "rename &c&lMy Sword");
        Component segment = singleSegment(sword, "rename legacy");
        helper.assertTrue(segment.getStyle().isBold(), "rename legacy: segment must be bold");
        helper.assertTrue(Objects.equals(segment.getStyle().getColor(), TextColor.fromLegacyFormat(ChatFormatting.RED)),
                "rename legacy: segment must be red");

        // 4) rename with hex color
        runCommand(server, owner, "rename &#ff5500Hexed");
        segment = singleSegment(sword, "rename hex");
        helper.assertTrue(Objects.equals(segment.getStyle().getColor(), TextColor.fromRgb(0xff5500)),
                "rename hex: segment must have color #ff5500");

        // 5) force-straight mode (&o): each segment starts with a reset
        runCommand(server, owner, "rename &o&aStraight &lBold");
        Component name = sword.get(DataComponents.CUSTOM_NAME);
        helper.assertTrue(name != null && name.getSiblings().size() == 2,
                "force straight: expected 2 segments, got " + (name == null ? -1 : name.getSiblings().size()));
        Component first = name.getSiblings().get(0);
        Component second = name.getSiblings().get(1);
        helper.assertTrue(Objects.equals(first.getStyle().getColor(), TextColor.fromLegacyFormat(ChatFormatting.GREEN)),
                "force straight: first segment must be green");
        helper.assertTrue(!first.getStyle().isBold(), "force straight: first segment must not be bold");
        helper.assertTrue(second.getStyle().isBold(), "force straight: second segment must be bold");
        helper.assertTrue(second.getStyle().getColor() == null, "force straight: second segment must have no color (reset)");

        // 6) lock the item to its owner
        runCommand(server, owner, "lockitem");
        CompoundTag data = sword.get(DataComponents.CUSTOM_DATA).copyTag();
        helper.assertTrue(data.getString("itemrename:authorUUID").orElse("").equals(owner.getUUID().toString()),
                "lockitem: authorUUID must be the locking player");
        helper.assertTrue(data.getString("itemrename:authorName").orElse("").equals(owner.getName().getString()),
                "lockitem: authorName must be the locking player");
        helper.assertTrue(ownerCheck.getOwnerName(sword).equals(owner.getName().getString()),
                "getOwnerName must return the owner name");

        // 7) non-owner cannot edit the locked item (owner check)
        Component nameBefore = sword.get(DataComponents.CUSTOM_NAME);
        moderator.setItemInHand(InteractionHand.MAIN_HAND, sword);
        try {
            ownerCheck.check(moderator, sword);
            helper.fail("ownerCheck must reject a non-owner");
        } catch (com.mojang.brigadier.exceptions.CommandSyntaxException expected) {
            // expected
        }
        runCommand(server, moderator, "rename &cStolen");
        helper.assertTrue(Objects.equals(sword.get(DataComponents.CUSTOM_NAME), nameBefore),
                "moderator (non-owner) must not be able to rename a locked item");

        // 8) admin bypasses the owner check
        admin.setItemInHand(InteractionHand.MAIN_HAND, sword);
        runCommand(server, admin, "rename &6Admin");
        helper.assertTrue(!Objects.equals(sword.get(DataComponents.CUSTOM_NAME), nameBefore),
                "admin must be able to rename a locked item (bypass)");

        // 9) unlock the item
        runCommand(server, admin, "unlockitem");
        var customDataAfterUnlock = sword.get(DataComponents.CUSTOM_DATA);
        CompoundTag dataTag = customDataAfterUnlock == null ? new CompoundTag() : customDataAfterUnlock.copyTag();
        helper.assertTrue(!dataTag.contains("itemrename:authorUUID") && !dataTag.contains("itemrename:authorName"),
                "unlockitem: ownership data must be removed");
        helper.assertTrue(ownerCheck.getOwnerName(sword).equals("unknown"),
                "getOwnerName must return 'unknown' after unlock");

        // 10) lore editing (literal \n splits the text into multiple lines)
        owner.setItemInHand(InteractionHand.MAIN_HAND, sword);
        runCommand(server, owner, "addloreline &6First\\nSecond");
        var lore = sword.get(DataComponents.LORE);
        helper.assertTrue(lore != null && lore.lines().size() == 2,
                "addloreline: expected 2 lore lines, got " + (lore == null ? -1 : lore.lines().size()));
        helper.assertTrue(lore.lines().get(0).getSiblings().size() == 1
                        && Objects.equals(lore.lines().get(0).getSiblings().getFirst().getStyle().getColor(),
                        TextColor.fromLegacyFormat(ChatFormatting.GOLD)),
                "addloreline: first lore line must be gold");
        helper.assertTrue(lore.lines().get(1).getSiblings().size() == 1
                        && lore.lines().get(1).getSiblings().getFirst().getStyle().getColor() == null,
                "addloreline: second lore line must be plain");

        runCommand(server, owner, "setloreline 2 &bChanged");
        lore = sword.get(DataComponents.LORE);
        helper.assertTrue(lore != null && lore.lines().size() == 2, "setloreline: must keep 2 lines");
        helper.assertTrue(lore.lines().get(1).getSiblings().size() == 1
                        && Objects.equals(lore.lines().get(1).getSiblings().getFirst().getStyle().getColor(),
                        TextColor.fromLegacyFormat(ChatFormatting.AQUA)),
                "setloreline: second line must be aqua");

        runCommand(server, owner, "insertloreline 1 &cTop");
        lore = sword.get(DataComponents.LORE);
        helper.assertTrue(lore != null && lore.lines().size() == 3, "insertloreline: expected 3 lines");
        helper.assertTrue(lore.lines().get(0).getSiblings().size() == 1
                        && Objects.equals(lore.lines().get(0).getSiblings().getFirst().getStyle().getColor(),
                        TextColor.fromLegacyFormat(ChatFormatting.RED)),
                "insertloreline: first line must be red");

        runCommand(server, owner, "removeloreline 1");
        lore = sword.get(DataComponents.LORE);
        helper.assertTrue(lore != null && lore.lines().size() == 2, "removeloreline: expected 2 lines");

        runCommand(server, owner, "removelore");
        lore = sword.get(DataComponents.LORE);
        // reset to the prototype value (in 26.x the prototype contains an empty lore, not null)
        helper.assertTrue(Objects.equals(lore, sword.getPrototype().get(DataComponents.LORE)),
                "removelore: lore must be reset to the prototype value");
        helper.assertTrue(lore == null || lore.lines().isEmpty(), "removelore: lore lines must be empty");

        // 11) glow
        runCommand(server, owner, "glow");
        helper.assertTrue(Boolean.TRUE.equals(sword.get(DataComponents.ENCHANTMENT_GLINT_OVERRIDE)),
                "glow: enchantment glint override must be true");
        runCommand(server, owner, "removeglow");
        helper.assertTrue(sword.get(DataComponents.ENCHANTMENT_GLINT_OVERRIDE) == null,
                "removeglow: enchantment glint override must be removed");

        // 12) hide enchantments
        runCommand(server, owner, "hideenchant");
        var tooltip = sword.get(DataComponents.TOOLTIP_DISPLAY);
        helper.assertTrue(tooltip != null && !tooltip.shows(DataComponents.ENCHANTMENTS),
                "hideenchant: enchantments must be hidden from the tooltip");
        runCommand(server, owner, "unhideenchant");
        tooltip = sword.get(DataComponents.TOOLTIP_DISPLAY);
        helper.assertTrue(tooltip != null && tooltip.shows(DataComponents.ENCHANTMENTS),
                "unhideenchant: enchantments must be visible again");

        // 13) removename
        runCommand(server, owner, "removename");
        helper.assertTrue(sword.get(DataComponents.CUSTOM_NAME) == null, "removename: custom name must be removed");

        helper.succeed();
    }
}
