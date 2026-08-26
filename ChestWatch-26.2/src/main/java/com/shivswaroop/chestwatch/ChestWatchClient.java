package com.shivswaroop.chestwatch;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.ChestScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.TrappedChestBlock;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class ChestWatchClient implements ClientModInitializer {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Map<String, ChestRecord> CHESTS = new HashMap<>();
    private static Path DATA_FILE;
    private static KeyMapping totalsKey;

    private static String lastScreenId = null;

    @Override
    public void onInitializeClient() {
        DATA_FILE = Minecraft.getInstance().gameDirectory.toPath()
                .resolve("config").resolve("chestwatch.json");
        load();

        totalsKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.chestwatch.open_totals",
                GLFW.GLFW_KEY_H,
                "category.chestwatch"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (totalsKey.consumeClick()) {
                showTotals(client);
            }
            inspectCurrentChest(client);
        });
    }

    private static void inspectCurrentChest(Minecraft client) {
        if (!(client.screen instanceof ChestScreen screen)) {
            lastScreenId = null;
            return;
        }

        BlockPos pos = findTargetContainer(client);
        if (pos == null || client.level == null) return;

        String dimension = client.level.dimension().identifier().toString();
        String id = dimension + "|" + pos.getX() + "|" + pos.getY() + "|" + pos.getZ();

        if (id.equals(lastScreenId)) return;
        lastScreenId = id;

        Map<String, Integer> now = new HashMap<>();
        var menu = screen.getMenu();

        // Chest inventories occupy the first 27 or 54 slots in a chest menu.
        int containerSlots = Math.min(menu.slots.size(), 54);
        for (int i = 0; i < containerSlots; i++) {
            ItemStack stack = menu.slots.get(i).getItem();
            if (!stack.isEmpty()) {
                String key = stack.getItem().toString();
                now.merge(key, stack.getCount(), Integer::sum);
            }
        }

        ChestRecord old = CHESTS.get(id);
        if (old != null) {
            warnAboutChanges(client, old, now, pos);
        }

        ChestRecord record = new ChestRecord(dimension, pos.getX(), pos.getY(), pos.getZ());
        record.items.putAll(now);
        CHESTS.put(id, record);
        save();
    }

    private static BlockPos findTargetContainer(Minecraft client) {
        if (client.hitResult == null || client.hitResult.getType() != HitResult.Type.BLOCK) {
            return null;
        }
        BlockPos pos = ((BlockHitResult) client.hitResult).getBlockPos();
        if (client.level == null) return null;

        BlockState state = client.level.getBlockState(pos);
        var block = state.getBlock();

        if (block instanceof ChestBlock ||
            block instanceof TrappedChestBlock ||
            block instanceof BarrelBlock ||
            block instanceof ShulkerBoxBlock) {
            return pos;
        }
        return null;
    }

    private static void warnAboutChanges(Minecraft client, ChestRecord old, Map<String, Integer> now, BlockPos pos) {
        Set<String> keys = new HashSet<>(old.items.keySet());
        keys.addAll(now.keySet());

        String coords = pos.getX() + ", " + pos.getY() + ", " + pos.getZ();

        for (String key : keys) {
            int before = old.items.getOrDefault(key, 0);
            int after = now.getOrDefault(key, 0);
            if (after < before) {
                int removed = before - after;
                notifyPlayer(client, "§cChestWatch: §f" + removed + "x " + pretty(key)
                        + " §ctaken from §7" + coords);
            } else if (after > before) {
                int added = after - before;
                notifyPlayer(client, "§aChestWatch: §f" + added + "x " + pretty(key)
                        + " §aadded to §7" + coords);
            }
        }
    }

    private static void notifyPlayer(Minecraft client, String message) {
        if (client.player != null) {
            client.player.displayClientMessage(net.minecraft.network.chat.Component.literal(message), false);
        }
    }

    private static String pretty(String id) {
        int slash = id.lastIndexOf(':');
        String s = slash >= 0 ? id.substring(slash + 1) : id;
        return s.replace('_', ' ');
    }

    private static void showTotals(Minecraft client) {
        Map<String, Integer> totals = new TreeMap<>();
        for (ChestRecord chest : CHESTS.values()) {
            chest.items.forEach((k, v) -> totals.merge(k, v, Integer::sum));
        }

        if (totals.isEmpty()) {
            notifyPlayer(client, "§eChestWatch: No containers have been scanned yet.");
            return;
        }

        notifyPlayer(client, "§6ChestWatch §f— tracked storage totals:");
        totals.entrySet().stream()
                .sorted(Map.Entry.<String,Integer>comparingByValue().reversed())
                .limit(12)
                .forEach(e -> notifyPlayer(client, "§f" + pretty(e.getKey()) + ": §a" + e.getValue()));
        if (totals.size() > 12) {
            notifyPlayer(client, "§7...and " + (totals.size() - 12) + " more item types. Full data is in config/chestwatch.json");
        }
    }

    private static void save() {
        try {
            Files.createDirectories(DATA_FILE.getParent());
            Files.writeString(DATA_FILE, GSON.toJson(CHESTS));
        } catch (IOException ignored) {}
    }

    private static void load() {
        if (!Files.exists(DATA_FILE)) return;
        try {
            String json = Files.readString(DATA_FILE);
            Map<String, ChestRecord> loaded = GSON.fromJson(
                    json, new TypeToken<Map<String, ChestRecord>>(){}.getType());
            if (loaded != null) CHESTS.putAll(loaded);
        } catch (Exception ignored) {}
    }
}
