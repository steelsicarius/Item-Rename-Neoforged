# Item Rename Mod (NeoForge)

## Description

The Item Rename mod, ported from Fabric to **NeoForge** for Minecraft 26.1.2, allows players to edit various properties
of items in the game, such as their name, lore, and enchantment glow. With support for color and formatting, you can
personalize your items like a piece of cake.

This mod is heavily inspired by the plugin: [EpicRename](https://www.spigotmc.org/resources/epicrename.4341/)

Also you can use my tools to easily make your own custom name/lore: https://faewulf.xyz/projects/epicrename

Original Fabric mod: https://github.com/FaeWulf/item-rename

## Commands and Permissions

| Command                                     | Description                                                   | Permission        |
|---------------------------------------------|---------------------------------------------------------------|-------------------|
| `/rename <name>`                            | Renames the item in hand.                                     | itemrename.rename |
| `/setloreline <lineNumber> <text>`          | Sets a certain line of lore on the item in hand.              | itemrename.lore   |
| `/addloreline <text>`                       | Adds a line of lore on the item in hand.                      | itemrename.lore   |
| `/removeloreline <lineNumber>`              | Remove a certain line of lore on the item in hand.            | itemrename.lore   |
| `/insertloreline <beforeLineNumber> <text>` | Insert a certain line of lore on the item in hand.            | itemrename.lore   |
| `/glow`                                     | Adds an enchantment glow to the item in hand.                 | itemrename.glow   |
| `/removeglow`                               | Removes the enchantment glow from the item.                   | itemrename.glow   |
| `/removename`                               | Removes any custom name from the item in hand.                | itemrename.rename |
| `/removelore`                               | Removes any custom lore from the item in hand.                | itemrename.lore   |
| `/lockitem`                                 | Lock item edit, allowing only you to modify selected items.   | itemrename.lock   |
| `/unlockitem`                               | Unlock item edit, allowing everyone to modify selected items. | itemrename.lock   |
| `/hideenchant`                              | Hides the enchantment list of the item in hand.               | itemrename.hideenchant |
| `/unhideenchant`                            | Unhides the enchantment list of the item in hand.             | itemrename.hideenchant |

- **itemrename.admin:** Allows bypassing lock check for item editing.

## Additional Caution

If you did not install any permission manager mod (ex: LuckPerms) then the permission system will check for "Permission
level":

- Player with permission level 1 (moderators) can do all basic commands
- Player with permission level 3 (admins) and above can bypass lock check for item editing

In singleplayer, every player can use all commands (same behavior as the original Fabric mod).

## Color and Format Support

The mod supports color and formatting using the EssentialsEX color format, with the prefix `&`.
Custom hex colors are supported with `&#RRGGBB`.

## Porting notes (Fabric → NeoForge)

- The mod id is `itemrename`. The original Fabric mod id (`item-rename`) contains a hyphen, which is not a valid
  NeoForge mod id; the mod already used the `itemrename` namespace for permission nodes and item data keys.
- Permission nodes (`itemrename.*`) are kept identical, implemented with NeoForge's `PermissionAPI`
  (`PermissionNode` + `PermissionGatherEvent.Nodes`), with defaults based on vanilla permission levels
  (moderators = level 1, admins = level 3), mirroring the behavior of the `fabric-permissions-api` of the original.
- The original used ClassTweaker interface injection plus a Mixin into `net.minecraft.network.chat.Style` to build
  styles with custom formatting (notably full `&r` reset support). The public vanilla `Style#withXxx` API of 26.1.2
  allows an exact functional replica without any Mixin (`faewulf.itemrename.util.styleEditor`).
- The mod is server-side safe (no client-only code).

## Building

```
JAVA_HOME=/usr/java/jdk-25-oracle-aarch64 ./gradlew build
```

## Testing

A headless functional GameTest covers the commands, permissions, locking and text formatting:

```
JAVA_HOME=/usr/java/jdk-25-oracle-aarch64 ./gradlew runGameTestServer
```

## Feedback and Support

If you encounter any issues or have suggestions for improvement, please don't hesitate to reach out on the mod's GitHub
issues or join my [discord server](https://discord.com/invite/xZneCTcEvb)

Thank you so much 🛠️✨
