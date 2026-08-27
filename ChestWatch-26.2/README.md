# 📦 ChestWatch

A powerful, client-side Fabric mod for Minecraft 26.2 that automatically keeps track of your storage network so you don't have to. 

## ✨ Features

- **Automated Memory**: Automatically remembers the contents of every Chest, Trapped Chest, Barrel, and Shulker Box you open.
- **Change Detection**: Compares a container's current inventory against its previous snapshot every time you open it.
- **Theft/Loss Warnings**: Instantly warns you in chat if an item count has decreased since you last checked. 
- **Global Storage Totals**: Press **`H`** (default keybind) at any time to print a grand total of all tracked items across all known containers directly into your chat.
- **Local Persistence**: All storage data is safely saved locally to `.minecraft/config/chestwatch.json`.

## ⚠️ Limitations

Because this is a **strictly client-side** mod, it cannot magically track chests in real-time when you are offline or away. It relies on you opening the chest to take a "snapshot". Any changes made by other players will only be detected the *next* time your client opens that specific container.

## 🚀 Installation (For Players)

1. Make sure you have the [Fabric Loader](https://fabricmc.net/use/) installed for Minecraft 26.2.
2. Download the [Fabric API](https://modrinth.com/mod/fabric-api) mod.
3. Download the latest `ChestWatch` `.jar` file.
4. Place both `.jar` files into your `.minecraft/mods` folder (usually located at `%appdata%\.minecraft\mods` on Windows).
5. Launch the game and enjoy!

## 🛠️ Building from Source (For Developers)

This project requires **Java 25**.

1. Clone this repository to your local machine.
2. Open a terminal/command prompt in the `ChestWatch-26.2` directory.
3. Run the Gradle build command:
   - On Windows: `gradlew build`
   - On Mac/Linux: `./gradlew build`
4. Retrieve the compiled `.jar` file from `build/libs/`.

---

*Built for Minecraft 26.2 using the Fabric Mod Loader.*
