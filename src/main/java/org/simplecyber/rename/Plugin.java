package org.simplecyber.rename;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.java.JavaPlugin;

public class Plugin extends JavaPlugin {

    private FileConfiguration config = null;

    public static String translateColors(String text) {
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

    public static String strFill(String text, Object... replacements) {
        for (int i = 0; i < replacements.length; i++) {
            text = text.replace((CharSequence) ("%"+i), (CharSequence) String.valueOf(replacements[i]));
        }
        return text;
    }
    public static void sendMsg(Object target, String text, Object... replacements) {
        text = strFill(text, replacements);
        text = translateColors(text);
        if (target instanceof ConsoleCommandSender) {
            ConsoleCommandSender console = (ConsoleCommandSender) target;
            console.sendMessage(strFill("[%0] %1", Bukkit.getName(), text));
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
    public static void log(String type, String text) {
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
            sendMsg(Bukkit.getServer().getConsoleSender(), text);
        } else {
            Bukkit.getLogger().log(level, text);
        }
    }
    public static void log(String text) {
        log("info", text);
    }

    private void reload() {
        saveDefaultConfig();
        reloadConfig();
        config = getConfig();
        log("Config reloaded!");
    }

    private String[][] formatPermissions = new String[][] {
        {"&[0]", "color.black"},
        {"&[1]", "color.darkblue"},
        {"&[2]", "color.darkgreen"},
        {"&[3]", "color.cyan"},
        {"&[4]", "color.darkred"},
        {"&[5]", "color.purple"},
        {"&[6]", "color.orange"},
        {"&[7]", "color.gray"},
        {"&[8]", "color.darkgray"},
        {"&[9]", "color.blue"},
        {"&[aA]", "color.green"},
        {"&[bB]", "color.aqua"},
        {"&[cC]", "color.red"},
        {"&[dD]", "color.pink"},
        {"&[eE]", "color.yellow"},
        {"&[fF]", "color.white"},
        {"&#([A-Fa-f0-9]{6})", "color.rgb"},
        {"&[lL]", "format.bold"},
        {"&[mM]", "format.strikethrough"},
        {"&[nN]", "format.underline"},
        {"&[oO]", "format.italic"},
        {"&[kK]", "format.magic"},
        {"&[rR]", "format.reset"},
    };
    private void registerFormatPermissions() {
        for (String[] el : formatPermissions) {
            String node = el[1];
            if (getServer().getPluginManager().getPermission(strFill("cyberrename.name.%0", node)) != null) {
                continue;
            }
            getServer().getPluginManager().addPermission(
                new Permission(strFill("cyberrename.name.%0", node))
            );
            getServer().getPluginManager().addPermission(
                new Permission(strFill("cyberrename.lore.%0", node))
            );
        }
    }
    
    private String removeDisallowedFormatting(String str, CommandSender sender) {
        for (String[] el : formatPermissions) {
            String regex = el[0];
            String node = el[1];
            if (!sender.hasPermission("cyberrename.name." + node) && str.matches(strFill(".*%0.*", regex))) {
                str = str.replaceAll(regex, "");
                sendMsg(sender, config.getString("messages.disallowed_formatting"), node);
            }
        }
        return str;
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
                Runnable onConfirm = () -> {};
                switch (cmdName) {
                    case "setname": {
                        String name = null;
                        if (args.length > 0) {
                            name = String.join(" ", args);
                            int nameMaxLength = config.getInt("names.max_length");
                            if (name.length() > nameMaxLength && !player.hasPermission("cyberrename.name.bypassLengthLimit")) {
                                sendMsg(sender, config.getString("messages.name_too_long"), String.valueOf(nameMaxLength));
                                return true;
                            }
                            name = removeDisallowedFormatting(name, sender);
                            name = translateColors(strFill("&r%0", name));
                        }
                        final String nameFinal = name;
                        meta.setDisplayName(name);
                        item.setItemMeta(meta);
                        if (!oldItemInHand.equals(player.getInventory().getItemInMainHand())) {
                            sendMsg(sender, config.getString("messages.item_mismatch"));
                            return true;
                        }
                        onConfirm = () -> {
                            if (!oldItemInHand.equals(item) && config.getBoolean("names.cost.xp.enabled") && !player.hasPermission("cyberrename.name.bypassCost")) {
                                int costLevels = config.getInt("names.cost.xp.levels");
                                if (player.getLevel() < costLevels) {
                                    sendMsg(sender, config.getString("messages.not_enough_xp"), String.valueOf(costLevels));
                                    return;
                                }
                                player.setLevel(player.getLevel() - costLevels);
                                sendMsg(sender, config.getString("messages.charge_xp"), String.valueOf(costLevels));
                            }
                            player.getInventory().setItemInMainHand(item);
                            if (args.length > 0)
                                sendMsg(sender, config.getString("messages.name_changed"), nameFinal);
                            else
                                sendMsg(sender, config.getString("messages.name_removed"));
                            player.playSound(player, Sound.ENTITY_VILLAGER_WORK_CARTOGRAPHER, 1, 1);
                        };
                        break;
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
                                line = removeDisallowedFormatting(line, sender);
                                lore.add(translateColors(strFill("&r&7%0", line)));
                            }
                        }
                        meta.setLore((lore.size() == 0) ? null : lore);
                        item.setItemMeta(meta);
                        if (!oldItemInHand.equals(player.getInventory().getItemInMainHand())) {
                            sendMsg(sender, config.getString("messages.item_mismatch"));
                            return true;
                        }
                        onConfirm = () -> {
                            if (!oldItemInHand.equals(item) && config.getBoolean("lores.cost.xp.enabled") && !player.hasPermission("cyberrename.lore.bypassCost")) {
                                int costLevels = config.getInt("lores.cost.xp.levels");
                                if (player.getLevel() < costLevels) {
                                    sendMsg(sender, config.getString("messages.not_enough_xp"), String.valueOf(costLevels));
                                    return;
                                }
                                player.setLevel(player.getLevel() - costLevels);
                                sendMsg(sender, config.getString("messages.charge_xp"), String.valueOf(costLevels));
                            }
                            player.getInventory().setItemInMainHand(item);
                            if (args.length > 0)
                                sendMsg(sender, config.getString("messages.lore_changed"));
                            else
                                sendMsg(sender, config.getString("messages.lore_removed"));
                            player.playSound(player, Sound.ENTITY_VILLAGER_WORK_CARTOGRAPHER, 1, 1);
                        };
                        break;
                    }
                }
                if (config.getBoolean("preview_gui.enabled")) {
                    new PreviewGui(this, config, player, item, onConfirm);
                } else {
                    onConfirm.run();
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
        int version = 1;
        if (config.getInt("config_version") != version) {
            log("Config version mismatch! Renaming current config file and reloading...");
            File configFile = new File(getDataFolder(), "config.yml");
            File newFile = new File(getDataFolder(), strFill("config-%0.yml", System.currentTimeMillis()));
            configFile.renameTo(newFile);
            reload();
        }
        registerFormatPermissions();
        log("&aAll set!");
    }
    @Override public void onDisable() {
        log("&dCatch you later!");
    }
}