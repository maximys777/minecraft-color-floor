package org.maximys.colorFloor;

import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;
import org.maximys.colorFloor.command.CfCommand;
import org.maximys.colorFloor.manager.ArenaManager;
import org.maximys.colorFloor.manager.GameManager;

public final class ColorFloor extends JavaPlugin {

    @Override
    public void onEnable() {
        this.saveDefaultConfig();

        ArenaManager arenaManager = new ArenaManager(this);
        GameManager gameManager = new GameManager(arenaManager, this);

        getCommand("cf").setExecutor(new CfCommand(arenaManager, gameManager));
        getLogger().info(ChatColor.GOLD + "Plugin is working");
    }

    @Override
    public void onDisable() {
        getLogger().info(ChatColor.GOLD + "Plugin has been disabled");
    }
}
