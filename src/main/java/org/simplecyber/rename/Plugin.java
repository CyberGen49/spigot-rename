package org.simplecyber.rename;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class Plugin extends JavaPlugin {

    FileConfiguration config = null;

    public void sendMsg(Object target, String text, String... replacements) {
        for (int i = 0; i < replacements.length; i++) {
            text = text.replace((CharSequence) ("%"+i), (CharSequence) String.valueOf(replacements[i]));
        }
        text = ChatColor.translateAlternateColorCodes('&', text);
        if (target instanceof ConsoleCommandSender) {
            ConsoleCommandSender console = (ConsoleCommandSender) target;
            console.sendMessage("[" + getName() + "] " + text);
            return;
        }
        if (target instanceof CommandSender) {
            ((CommandSender) target).sendMessage(text);
            return;
        }
        if (target instanceof Player) {
            ((Player) target).sendMessage(text);
            return;
        }
    }
    public void log(String type, String text) {
        Level level;
        switch (type) {
            case "info":
                level = Level.INFO;
                break;
            case "warning":
                level = Level.WARNING;
                break;
            default:
                level = Level.INFO;
                break;
        }
        if (type == "info") {
            sendMsg(getServer().getConsoleSender(), text);
        } else {
            getLogger().log(level, text);
        }
    }
    public void log(String text) {
        log("info", text);
    }

    public void reload() {
        saveDefaultConfig();
        reloadConfig();
        config = getConfig();
        log("Config reloaded!");
    }

    public boolean cmd_cyberrename(CommandSender sender, Command cmd, String[] args) {
        if (args.length == 0 || !sender.hasPermission("cybertpr.admin")) {
            sendMsg(sender, "&bCyberRename &3v" + getDescription().getVersion());
            sendMsg(sender, "&9https://github.com/CyberGen49/spigot-rename");
            return true;
        }
        switch (args[0]) {
            case "reload":
                reload();
                sendMsg(sender, config.getString("messages.reloaded"));
                break;
            default:
                return cmd_cyberrename(sender, cmd, new String[0]);
        }
        return true;
    }

    @Override public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        switch (cmd.getName().toLowerCase()) {
            case "cyberrename":
                return cmd_cyberrename(sender, cmd, args);
        }
        return false;
    }

    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        List<String> options = new ArrayList<>();
        String cmdName = cmd.getName().toLowerCase();
        if (cmdName == "cyberrename") {
            if (args.length == 1 && sender.hasPermission("cyberrename.admin")) {
                options.add("reload");
            }
        }
        if (cmdName == "rename" || cmdName == "relore") {
            boolean canRename = sender.hasPermission("cyberrename.rename");
            boolean canRelore = sender.hasPermission("cyberrename.relore");
            if (args.length == 1 && (canRename || canRelore)) {
                options.add("set");
                options.add("remove");
            }
        }
        return options;
    }

    @Override public void onEnable() {
        saveDefaultConfig();
        config = getConfig();
        int version = 1;
        if (config.getInt("config_version") != version) {
            log("Config version mismatch! Renaming current config file and reloading...");
            File configFile = new File(getDataFolder(), "config.yml");
            File newFile = new File(getDataFolder(), "config-" + System.currentTimeMillis() + ".yml");
            configFile.renameTo(newFile);
            reload();
        }
        log("&aAll set!");
    }
    @Override public void onDisable() {
        log("&dCatch you later!");
    }
}