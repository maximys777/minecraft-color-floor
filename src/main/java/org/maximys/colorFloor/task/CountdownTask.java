package org.maximys.colorFloor.task;

import org.bukkit.scheduler.BukkitRunnable;
import org.maximys.colorFloor.manager.GameManager;

public class CountdownTask extends BukkitRunnable {
    private final GameManager gameManager;
    private int secondsLeft;

    public CountdownTask(GameManager gameManager, int countdownSeconds) {
        this.gameManager = gameManager;
        this.secondsLeft = countdownSeconds;
    }

    @Override
    public void run() {
        if (secondsLeft <= 0) {
            gameManager.startGame();
            cancel();
        } else {
            gameManager.broadcast("Seconds left: " + secondsLeft);
            secondsLeft--;
        }
    }
}
