Unoficial Neoforge Fork from FaeWulf/item-rename

# Item Rename Mod (NeoForge)

## Description

The Item Rename mod, ported from Fabric to **NeoForge** for Minecraft 26.1.2, allows players to edit various properties
of items in the game, such as their name, lore, and enchantment glow. With support for color and formatting, you can
personalize your items like a piece of cake.

This mod is heavily inspired by the plugin: [EpicRename](https://www.spigotmc.org/resources/epicrename.4341/)

Also you can use my tools to easily make your own custom name/lore: https://faewulf.xyz/projects/epicrename

Original Fabric mod: https://github.com/FaeWulf/item-rename

## Supported NeoForge versions

This repository contains the same mod built against three NeoForge targets:

| Location         | Minecraft | NeoForge        | Jar                          |
|------------------|-----------|-----------------|------------------------------|
| repository root  | 26.1.2    | 26.1.2.114      | item-rename-1.3.2+26.1.2.jar |
| `neoforge-26.2/` | 26.2      | 26.2.0.90       | item-rename-1.3.2+26.2.jar   |
| `neoforge-26.3/` | 26.3      | 26.3.0.69-beta  | item-rename-1.3.2+26.3.jar   |

Each directory is a self-contained Gradle project (based on the corresponding ModDevGradle MDK) with identical
sources; only the version properties in `gradle.properties` differ. Build and test each one from its own
directory (see below). The sources compile unchanged against the API surface of all three targets (verified
against the patched sources of each version).

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
- Networking: the mod registers no network payloads, so clients without the mod can join a server with it (same as
  the original). In Minecraft 26.x the server-list indicator is the vanilla `isModded` flag; the old NeoForge
  `displayTest` metadata field no longer exists, so no extra metadata is required.
- Permission semantics: like `fabric-permissions-api`, the active permission handler (e.g. LuckPerms) decides the
  result for a *defined* permission node; for *undefined* nodes the vanilla permission level fallback applies
  (moderators = level 1, admins = level 3). Explicit grants/denials from the permission manager therefore take
  precedence over op levels, and setups that only rely on op levels behave exactly like the original.
- The original had a bug where the second `.requires(...)` call (brigadier overwrites the predicate) silently
  disabled the `isPlayer` check of the commands; this port combines both checks into one predicate, which was the
  clear intention of the original code.

## Building

Build the version you want from its directory, e.g. for the 26.1.2 port (repository root):

```
JAVA_HOME=/usr/java/jdk-25-oracle-aarch64 ./gradlew build
```

## Testing

A headless functional GameTest covers the commands, permissions, locking and text formatting
(run it from the directory of the version you want to test):

```
JAVA_HOME=/usr/java/jdk-25-oracle-aarch64 ./gradlew runGameTestServer
```

The dev runs are started with `-Djava.net.preferIPv4Stack=true` (see `build.gradle`), which only affects the
development JVM: some build/test hosts have IPv6 disabled at kernel level and the dedicated dev server would
otherwise fail to bind its port. Set it to false in `~/.gradle/gradle.properties` (`devPreferIPv4`) if your
environment needs IPv6... or simply remove the `jvmArgument` line from `build.gradle`.

## Feedback and Support

If you encounter any issues or have suggestions for improvement, please don't hesitate to reach out on the mod's GitHub
issues or join my [discord server](https://discord.com/invite/xZneCTcEvb)

Thank you so much 🛠️✨
