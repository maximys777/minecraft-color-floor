package org.maximys.colorFloor.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.maximys.colorFloor.manager.GameManager;

public class PlayerLeaveListener implements Listener {
    private final GameManager gameManager;

    public PlayerLeaveListener(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent e) {
        gameManager.leave(e.getPlayer());
    }
}
