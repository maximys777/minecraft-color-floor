package org.maximys.colorFloor;

import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

public final class ColorFloor extends JavaPlugin {

    @Override
    public void onEnable() {
        getLogger().info(ChatColor.GOLD + "Plugin is working");
    }

    @Override
    public void onDisable() {
        getLogger().info(ChatColor.GOLD + "Plugin has been disabled");
    }
}
