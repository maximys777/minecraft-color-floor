package org.maximys.colorFloor;

import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;
import org.maximys.colorFloor.command.CfCommand;
import org.maximys.colorFloor.listener.PlayerLeaveListener;
import org.maximys.colorFloor.manager.ArenaManager;
import org.maximys.colorFloor.manager.GameManager;

public final class ColorFloor extends JavaPlugin {
    private ArenaManager arenaManager;
    private GameManager gameManager;

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        arenaManager = new ArenaManager(this);
        gameManager = new GameManager(arenaManager, this);

        getCommand("cf").setExecutor(new CfCommand(arenaManager, gameManager));

        getServer().getPluginManager().registerEvents(new PlayerLeaveListener(gameManager), this);
        getLogger().info(ChatColor.GOLD + "Plugin is working");
    }

    @Override
    public void onDisable() {
        getLogger().info(ChatColor.GOLD + "Plugin has been disabled");
        gameManager.stopGame();
    }
}
