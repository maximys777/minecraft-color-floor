package org.maximys.colorFloor.task;

import org.bukkit.DyeColor;
import org.bukkit.scheduler.BukkitRunnable;
import org.maximys.colorFloor.ColorFloor;
import org.maximys.colorFloor.game.PhaseRound;
import org.maximys.colorFloor.manager.ArenaManager;
import org.maximys.colorFloor.manager.GameManager;

import java.util.Optional;

public class RoundTask extends BukkitRunnable {
    private final ColorFloor colorFloor;
    private final ArenaManager arenaManager;
    private final GameManager gameManager;
    private PhaseRound phaseRound = PhaseRound.NEW_ROUND;
    private int secondsLeft;
    private int numberOfRound;
    private Optional<DyeColor> currentColor;

    public RoundTask(ColorFloor colorFloor, ArenaManager arenaManager, GameManager gameManager) {
        this.colorFloor = colorFloor;
        this.arenaManager = arenaManager;
        this.gameManager = gameManager;
    }

    @Override
    public void run() {
        switch (phaseRound) {
            case NEW_ROUND:
                arenaManager.recolorFloor();
                currentColor = arenaManager.randomFloorColor();
                DyeColor color;
                if (!currentColor.isPresent()) {
                    gameManager.finishGame();
                    break;
                }
                color = currentColor.get();
                gameManager.broadcast("Color: " + color);

                int roundSeconds = colorFloor.getConfig().getInt("countdownSeconds");

                secondsLeft = Math.max(roundSeconds - numberOfRound, 4);

                phaseRound = PhaseRound.RUNNING;
                break;
            case RUNNING:
                if (secondsLeft > 0) {
                    gameManager.broadcast("Seconds: " + secondsLeft);
                    secondsLeft -= 1;
                } else {
                    currentColor.ifPresent(arenaManager::removeAllExcept);
                    secondsLeft = 3;
                    phaseRound = PhaseRound.FALLING;
                }
                break;
            case FALLING:
                gameManager.eliminateFallen();
                if (secondsLeft > 0) {
                    secondsLeft -= 1;
                } else {
                    if (gameManager.getPlayersCount() > 1) {
                        gameManager.finishGame();
                    } else {
                        numberOfRound++;
                        phaseRound = PhaseRound.NEW_ROUND;
                    }
                }
                break;
        }
    }
}
