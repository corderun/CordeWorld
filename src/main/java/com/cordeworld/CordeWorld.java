package com.cordeworld;

import io.papermc.paper.event.world.border.WorldBorderEvent;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.*;

public final class CordeWorld extends JavaPlugin {

    private Map<String, Long> cooldowns = new HashMap<>();
    private String worldName;
    private int cooldownSeconds;
    private YamlConfiguration langConfig;
    @Override
    public void onEnable() {
        saveDefaultConfig();
        createLangConfig();
        cooldownSeconds = getConfig().getInt("cooldown");
        getCommand("world").setTabCompleter(this);
        getCommand("world").setExecutor(new CommandExecutor() {

            @Override
            public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
                if(args.length == 0){
                    sender.sendMessage("§2§lCordeWorld §21.0 §fСпециально для Meltarion Network");
                    sender.sendMessage(Objects.requireNonNull(langConfig.getString("prefix")).replace("&", "§") + Objects.requireNonNull(langConfig.getString("usage")).replace("&", "§"));
                    cooldowns.clear();
                    return true;
                }
                Player player = (Player) sender;
                if (cooldowns.containsKey(player.getName())) {
                    long secondsLeft = ((cooldowns.get(player.getName()) / 1000) + cooldownSeconds) - (System.currentTimeMillis() / 1000);
                    if (secondsLeft > 0) {
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', langConfig.getString("prefix")) + ChatColor.translateAlternateColorCodes('&', langConfig.getString("cooldown-message").replace("%time%", String.valueOf(secondsLeft))));
                        return true;
                    }
                }
                if(args[0].equalsIgnoreCase("reload")){
                    if(sender.hasPermission("cordeworld.reload")){
                        reloadConfig();
                        createLangConfig();
                        sender.sendMessage(Objects.requireNonNull(langConfig.getString("prefix")).replace("&", "§") + Objects.requireNonNull(langConfig.getString("reload")).replace("&", "§"));
                    }
                    else{
                        sender.sendMessage(Objects.requireNonNull(langConfig.getString("prefix")).replace("&", "§") + Objects.requireNonNull(langConfig.getString("no-perm")).replace("&", "§"));
                        cooldowns.clear();
                        return true;
                    }
                }
                if(sender.hasPermission("cordeworld.use")) {
                    if (args[0].equalsIgnoreCase("world") || args[0].equalsIgnoreCase("overworld")) {
                        World targetWorld = Bukkit.getWorld("world");
                        if (player.getWorld() == Bukkit.getWorld("world")) {
                            sender.sendMessage(Objects.requireNonNull(langConfig.getString("prefix")).replace("&", "§") + Objects.requireNonNull(langConfig.getString("same-world")).replace("&", "§"));
                            cooldowns.clear();
                            return true;
                        }
                        if (player.getWorld() == Bukkit.getWorld("world_nether")) {
                            Location desiredLocation = new Location(player.getWorld(), player.getLocation().x() * 8, player.getLocation().y(), player.getLocation().z() * 8);
                            Location safeLocation = findSafeLocation(targetWorld, desiredLocation);
                            if (safeLocation != null) {
                                player.teleport(safeLocation);
                                sender.sendMessage(Objects.requireNonNull(langConfig.getString("prefix")).replace("&", "§") + Objects.requireNonNull(langConfig.getString("teleport.successful")).replace("&", "§"));
                                cooldowns.put(player.getName(), System.currentTimeMillis());
                                return true;
                            } else {
                                sender.sendMessage(Objects.requireNonNull(langConfig.getString("prefix")).replace("&", "§") + Objects.requireNonNull(langConfig.getString("teleport.unsafe")).replace("&", "§"));
                                cooldowns.clear();
                                return true;
                            }
                        } else {
                            Location desiredLocation = new Location(player.getWorld(), player.getLocation().x(), player.getLocation().y(), player.getLocation().z());
                            Location safeLocation = findSafeLocation(targetWorld, desiredLocation);
                            if (safeLocation != null) {
                                player.teleport(safeLocation);
                                sender.sendMessage(Objects.requireNonNull(langConfig.getString("prefix")).replace("&", "§") + Objects.requireNonNull(langConfig.getString("teleport.successful")).replace("&", "§"));
                                cooldowns.put(player.getName(), System.currentTimeMillis());
                                return true;
                            } else {
                                sender.sendMessage(Objects.requireNonNull(langConfig.getString("prefix")).replace("&", "§") + Objects.requireNonNull(langConfig.getString("teleport.unsafe")).replace("&", "§"));
                                cooldowns.clear();
                                return true;
                            }
                        }
                    }
                }
                else{
                    sender.sendMessage(Objects.requireNonNull(langConfig.getString("prefix")).replace("&", "§") + Objects.requireNonNull(langConfig.getString("no-perm")).replace("&", "§"));
                    cooldowns.clear();
                    return true;
                }
                if(sender.hasPermission("cordeworld.use")) {
                    if (args[0].equalsIgnoreCase("nether") || args[0].equalsIgnoreCase("world_nether")) {
                        if (player.getWorld() == Bukkit.getWorld("world_nether")) {
                            sender.sendMessage(Objects.requireNonNull(langConfig.getString("prefix")).replace("&", "§") + Objects.requireNonNull(langConfig.getString("same-world")).replace("&", "§"));
                            cooldowns.clear();
                            return true;
                        }
                        World targetWorld = Bukkit.getWorld("world_nether");
                        if (player.getWorld() == Bukkit.getWorld("world")) {
                            Location desiredLocation = new Location(player.getWorld(), player.getLocation().x() / 8, player.getLocation().y(), player.getLocation().z() / 8);
                            Location safeLocation = findSafeLocation(targetWorld, desiredLocation);
                            if (safeLocation != null) {
                                player.teleport(safeLocation);
                                sender.sendMessage(Objects.requireNonNull(langConfig.getString("prefix")).replace("&", "§") + Objects.requireNonNull(langConfig.getString("teleport.successful")).replace("&", "§"));
                                cooldowns.put(player.getName(), System.currentTimeMillis());
                                return true;
                            } else {
                                sender.sendMessage(Objects.requireNonNull(langConfig.getString("prefix")).replace("&", "§") + Objects.requireNonNull(langConfig.getString("teleport.unsafe")).replace("&", "§"));
                                cooldowns.clear();
                                return true;
                            }
                        } else {
                            Location desiredLocation = new Location(player.getWorld(), player.getLocation().x() / 8, player.getLocation().y(), player.getLocation().z() / 8);
                            Location safeLocation = findSafeLocation(targetWorld, desiredLocation);
                            if (safeLocation != null) {
                                player.teleport(safeLocation);
                                sender.sendMessage(Objects.requireNonNull(langConfig.getString("prefix")).replace("&", "§") + Objects.requireNonNull(langConfig.getString("teleport.successful")).replace("&", "§"));
                                cooldowns.put(player.getName(), System.currentTimeMillis());
                                return true;
                            } else {
                                sender.sendMessage(Objects.requireNonNull(langConfig.getString("prefix")).replace("&", "§") + Objects.requireNonNull(langConfig.getString("teleport.unsafe")).replace("&", "§"));
                                cooldowns.clear();
                                return true;
                            }
                        }
                    }
                }
                else{
                    sender.sendMessage(Objects.requireNonNull(langConfig.getString("prefix")).replace("&", "§") + Objects.requireNonNull(langConfig.getString("no-perm")).replace("&", "§"));
                    cooldowns.clear();
                    return true;
                }
                if(sender.hasPermission("cordeworld.use")) {
                    if (args[0].equalsIgnoreCase("end") || args[0].equalsIgnoreCase("world_the_end") || args[0].equalsIgnoreCase("the_end")) {
                        if (player.getWorld() == Bukkit.getWorld("world_the_end")) {
                            sender.sendMessage(Objects.requireNonNull(langConfig.getString("prefix")).replace("&", "§") + Objects.requireNonNull(langConfig.getString("same-world")).replace("&", "§"));
                            cooldowns.clear();
                            return true;
                        }
                        World targetWorld = Bukkit.getWorld("world_the_end");
                        Location currentLoc = player.getLocation();
                        Location safeLocation;
                        if (player.getWorld() == Bukkit.getWorld("world_nether")) {
                            Location desiredLocation = new Location(targetWorld, currentLoc.getX() * 8, currentLoc.getY(), currentLoc.getZ() * 8);
                            safeLocation = findSafeLocationInEnd(desiredLocation);
                        } else {
                            Location desiredLocation = new Location(targetWorld, currentLoc.getX(), currentLoc.getY(), currentLoc.getZ());
                            safeLocation = findSafeLocationInEnd(desiredLocation);
                        }
                        if (safeLocation != null) {
                            player.teleport(safeLocation);
                            sender.sendMessage(Objects.requireNonNull(langConfig.getString("prefix")).replace("&", "§") + Objects.requireNonNull(langConfig.getString("teleport.successful")).replace("&", "§"));
                            cooldowns.put(player.getName(), System.currentTimeMillis());
                            return true;
                        } else {
                            sender.sendMessage(Objects.requireNonNull(langConfig.getString("prefix")).replace("&", "§") + Objects.requireNonNull(langConfig.getString("teleport.unsafe")).replace("&", "§"));
                            cooldowns.clear();
                            return true;
                        }
                    }
                }
                else{
                    sender.sendMessage(Objects.requireNonNull(langConfig.getString("prefix")).replace("&", "§") + Objects.requireNonNull(langConfig.getString("no-perm")).replace("&", "§"));
                    cooldowns.clear();
                    return true;
                }
                return true;
            }

            private Location findSafeLocation(World world, Location location) {
                int x = location.getBlockX();
                int z = location.getBlockZ();
                int y = world.getHighestBlockYAt(x, z);

                for (; y >= 0; y--) {
                    Location checkLocation = new Location(world, x, y, z);
                    if (isSafeLocation(checkLocation)) {
                        return checkLocation;
                    }
                }

                return null;
            }

            private boolean isSafeLocation(Location location) {
                Material feet = location.getBlock().getType();
                Material head = location.clone().add(new Vector(0, 1, 0)).getBlock().getType();
                Material below = location.clone().subtract(new Vector(0, 1, 0)).getBlock().getType();
                boolean safe = !feet.isSolid() && !head.isSolid() && below.isSolid() && location.getWorld().getWorldBorder().isInside(location);
                boolean notLava = feet != Material.LAVA && head != Material.LAVA && below != Material.LAVA;
                boolean notFire = feet != Material.FIRE && head != Material.FIRE;
                boolean notVoid = location.getY() > 0;

                return safe && notLava && notFire && notVoid;
            }

            private Location findSafeLocationInEnd(Location location) {
                World world = location.getWorld();
                int x = location.getBlockX();
                int z = location.getBlockZ();

                int y = 50;

                Location checkLocation = new Location(world, x, y, z);
                if (isSafeLocation(checkLocation)) {
                    return checkLocation;
                }

                for (y = 51; y <= world.getMaxHeight(); y++) {
                    checkLocation = new Location(world, x, y, z);
                    if (isSafeLocation(checkLocation)) {
                        return checkLocation;
                    }
                }

                for (y = 49; y >= 0; y--) {
                    checkLocation = new Location(world, x, y, z);
                    if (isSafeLocation(checkLocation)) {
                        return checkLocation;
                    }
                }

                return null;
            }
        });
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> tabCompletions = new ArrayList<>();
        if (args.length == 1) {
            tabCompletions.add("world");
            tabCompletions.add("world_nether");
            tabCompletions.add("world_the_end");
            tabCompletions.add("nether");
            tabCompletions.add("end");
        }
        return tabCompletions;
    }

    private void createLangConfig() {
        File file = new File(getDataFolder(), "lang.yml");
        if (!file.exists()) {
            saveResource("lang.yml", false);
        }

        langConfig = YamlConfiguration.loadConfiguration(file);
    }

    @Override
    public void onDisable() {
        // shutdown
    }
}
