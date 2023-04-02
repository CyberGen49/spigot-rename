package org.simplecyber.rename;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

public class Plugin extends JavaPlugin {

    FileConfiguration config = null;

    public String translateColors(String text) {
        text = ChatColor.translateAlternateColorCodes('&', text);
        final char COLOR_CHAR = ChatColor.COLOR_CHAR;
        final Pattern hexPattern = Pattern.compile("&#([A-Fa-f0-9]{6})");
        Matcher matcher = hexPattern.matcher(text);
        StringBuffer buffer = new StringBuffer(text.length() + 4 * 8);
        while (matcher.find()) {
            String group = matcher.group(1);
            matcher.appendReplacement(buffer, COLOR_CHAR + "x"
                    + COLOR_CHAR + group.charAt(0) + COLOR_CHAR + group.charAt(1)
                    + COLOR_CHAR + group.charAt(2) + COLOR_CHAR + group.charAt(3)
                    + COLOR_CHAR + group.charAt(4) + COLOR_CHAR + group.charAt(5)
                    );
        }
        return matcher.appendTail(buffer).toString();
    }

    public String strFill(String text, Object... replacements) {
        for (int i = 0; i < replacements.length; i++) {
            text = text.replace((CharSequence) ("%"+i), (CharSequence) String.valueOf(replacements[i]));
        }
        return text;
    }
    public void sendMsg(Object target, String text, Object... replacements) {
        text = strFill(text, replacements);
        text = translateColors(text);
        if (target instanceof ConsoleCommandSender) {
            ConsoleCommandSender console = (ConsoleCommandSender) target;
            console.sendMessage(strFill("[%0] %1", getName(), text));
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

    @Override public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        String cmdName = cmd.getName().toLowerCase();
        Runnable sendUsage;
        switch (cmdName) {
            case "crename": {
                sendUsage = () -> {
                    sendMsg(sender, "&b%0 &3v%1", getName(), getDescription().getVersion());
                    sendMsg(sender, "&9https://github.com/CyberGen49/spigot-rename");
                };
                if (args.length == 0 || !sender.hasPermission("cyberrename.admin")) {
                    sendUsage.run();
                    return true;
                }
                switch (args[0].toLowerCase()) {
                    case "reload":
                        reload();
                        sendMsg(sender, config.getString("messages.reloaded"));
                        return true;
                    default:
                        sendUsage.run();
                }
                return true;
            }
            case "setname": case "setlore": {
                if (!(sender instanceof Player)) {
                    sendMsg(sender, config.getString("messages.player_only"));
                    return true;
                }
                Player player = (Player) sender;
                ItemStack oldItemInHand = player.getInventory().getItemInMainHand();
                if (oldItemInHand.getType() == Material.AIR) {
                    sendMsg(sender, config.getString("messages.holding_air"));
                    return true;
                }
                ItemStack item = oldItemInHand.clone();
                ItemMeta meta = item.getItemMeta();
                switch (cmdName) {
                    case "setname": {
                        String name = null;
                        if (args.length > 0) {
                            name = translateColors(String.join(" ", args));
                            int nameMaxLength = config.getInt("names.max_length");
                            if (name.length() > nameMaxLength && !player.hasPermission("cyberrename.name.bypassLengthLimit")) {
                                sendMsg(sender, config.getString("messages.name_too_long"), String.valueOf(nameMaxLength));
                                return true;
                            }
                        }
                        meta.setDisplayName(name);
                        item.setItemMeta(meta);
                        if (!oldItemInHand.equals(player.getInventory().getItemInMainHand())) {
                            sendMsg(sender, config.getString("messages.item_mismatch"));
                            return true;
                        }
                        player.getInventory().setItemInMainHand(item);
                        if (args.length > 0)
                            sendMsg(sender, config.getString("messages.name_changed"), name);
                        else
                            sendMsg(sender, config.getString("messages.name_removed"));
                        return true;
                    }
                    case "setlore": {
                        List<String> lore = new ArrayList<>();
                        if (args.length > 0) {
                            String loreRaw = String.join(" ", args);
                            int loreMaxLength = config.getInt("lores.max_length");
                            if (loreRaw.length() > loreMaxLength && !player.hasPermission("cyberrename.lore.bypassLengthLimit")) {
                                sendMsg(sender, config.getString("messages.lore_too_long"), String.valueOf(loreMaxLength));
                                return true;
                            }
                            String newlineString = config.getString("lores.newline_string");
                            String[] loreLines = loreRaw.split("\\Q" + newlineString + "\\E");
                            for (String line : loreLines) {
                                lore.add(translateColors(line));
                            }
                        }
                        meta.setLore((lore.size() == 0) ? null : lore);
                        item.setItemMeta(meta);
                        if (!oldItemInHand.equals(player.getInventory().getItemInMainHand())) {
                            sendMsg(sender, config.getString("messages.item_mismatch"));
                            return true;
                        }
                        player.getInventory().setItemInMainHand(item);
                        if (args.length > 0)
                            sendMsg(sender, config.getString("messages.lore_changed"));
                        else
                            sendMsg(sender, config.getString("messages.lore_removed"));
                        return true;
                    }
                }
                return true;
            }
            default: return true;
        }
    }

    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        List<String> options = new ArrayList<>();
        switch (cmd.getName().toLowerCase()) {
            case "crename":
                if (args.length == 1 && sender.hasPermission("cyberrename.admin")) {
                    options.add("reload");
                }
                break;
        }
        return options;
    }

    @Override public void onEnable() {
        saveDefaultConfig();
        config = getConfig();
        int version = 5;
        if (config.getInt("config_version") != version) {
            log("Config version mismatch! Renaming current config file and reloading...");
            File configFile = new File(getDataFolder(), "config.yml");
            File newFile = new File(getDataFolder(), strFill("config-%0.yml", System.currentTimeMillis()));
            configFile.renameTo(newFile);
            reload();
        }
        log("&aAll set!");
    }
    @Override public void onDisable() {
        log("&dCatch you later!");
    }
}