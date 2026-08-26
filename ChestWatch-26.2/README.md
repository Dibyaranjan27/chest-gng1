# ChestWatch

Client-side Fabric mod for Minecraft 26.2.

## Features
- Remembers the contents of chests, trapped chests, barrels and shulker boxes you open.
- Stores snapshots locally in `.minecraft/config/chestwatch.json`.
- Compares a container against its previous snapshot when you open it again.
- Warns when an item count decreased.
- Press **H** to print tracked storage totals in chat.

## Important limitation
A client-only mod cannot know exactly who opened a chest while you were offline. It can only detect a change when your client next sees/opens that container.

## Build
Minecraft 26.2 uses Java 25 for Fabric development. Install JDK 25, then from this folder run:

`./gradlew build`

The built mod JAR will be in `build/libs/`.
