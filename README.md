# Item Rename (NeoForge 26.3)

Port of the [item-rename](https://github.com/FaeWulf/item-rename) mod (Fabric, MC 26.1.2) to NeoForge 26.3.
Identical code base to the 26.1 port; see the [repository README](../README.md) for full
documentation (commands, permissions, porting notes and testing).

NOTE: this target uses a **beta** NeoForge build (26.3.0.69-beta, as shipped by the official MDK).

## Building

```
JAVA_HOME=/usr/java/jdk-25-oracle-aarch64 ./gradlew build
```

## Testing

```
JAVA_HOME=/usr/java/jdk-25-oracle-aarch64 ./gradlew runGameTestServer
```
