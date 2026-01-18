package ru.knyazin.advancementsswitcher;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class AdvancementsSwitcher extends JavaPlugin implements Listener, CommandExecutor, TabCompleter {

    private final NamespacedKey modeKey = new NamespacedKey(this, "adv_mode");

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        var cmd = getCommand("adv");
        if (cmd != null) {
            cmd.setExecutor(this);
            cmd.setTabCompleter(this);
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) return true;

        if (args.length < 2) {
            player.sendMessage(Component.text("§cUsage: /adv <on/off> <full/only>"));
            return true;
        }

        String action = args[0].toLowerCase();
        String type = args[1].toLowerCase();
        int currentMode = 0; // 0 = SHOW_ALL (default)

        if (action.equals("off")) {
            if (type.equals("full")) {
                currentMode = 2; // SHOW_NONE
                player.sendMessage(Component.text("§7Advancements hidden: §cALL"));
            } else if (type.equals("only")) {
                currentMode = 1; // SHOW_MINE_ONLY
                player.sendMessage(Component.text("§7Advancements hidden: §eOTHERS"));
            } else {
                player.sendMessage(Component.text("§cUsage: /adv off <full/only>"));
                return true;
            }
        } else if (action.equals("on")) {
            currentMode = 0;
            player.sendMessage(Component.text("§7Advancements visible: §aALL"));
        } else {
            player.sendMessage(Component.text("§cUsage: /adv <on/off> <full/only>"));
            return true;
        }

        player.getPersistentDataContainer().set(modeKey, PersistentDataType.INTEGER, currentMode);
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            String input = args[0].toLowerCase();
            if ("on".startsWith(input)) completions.add("on");
            if ("off".startsWith(input)) completions.add("off");
        } else if (args.length == 2) {
            String input = args[1].toLowerCase();
            if ("full".startsWith(input)) completions.add("full");
            if ("only".startsWith(input)) completions.add("only");
        }
        return completions;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onAdvancement(PlayerAdvancementDoneEvent event) {
        Component message = event.message();
        if (message == null) return;

        event.message(null);

        Player earner = event.getPlayer();

        for (Player receiver : Bukkit.getOnlinePlayers()) {
            int mode = receiver.getPersistentDataContainer().getOrDefault(modeKey, PersistentDataType.INTEGER, 0);

            switch (mode) {
                case 0:
                    receiver.sendMessage(message);
                    break;
                case 1:
                    if (receiver.getUniqueId().equals(earner.getUniqueId())) {
                        receiver.sendMessage(message);
                    }
                    break;
                case 2:
                    break;
            }
        }
    }
}