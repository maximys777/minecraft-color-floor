package org.maximys.colorFloor.manager;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.maximys.colorFloor.ColorFloor;
import org.maximys.colorFloor.game.GameState;
import org.maximys.colorFloor.game.JoinResult;
import org.maximys.colorFloor.task.CountdownTask;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class GameManager {
    private final Map<UUID, Location> players = new HashMap<>();
    private final ArenaManager arenaManager;
    private final ColorFloor colorFloor;
    private GameState state = GameState.WAIT_FOR_PLAYERS;
    private BukkitTask countdownTask;

    public GameManager(ArenaManager arenaManager, ColorFloor colorFloor) {
        this.arenaManager = arenaManager;
        this.colorFloor = colorFloor;
    }

    public JoinResult join(Player player) {
        int playerMax = colorFloor.getConfig().getInt("maxPlayers");
        int playerMin = colorFloor.getConfig().getInt("minPlayers");

        if (players.containsKey(player.getUniqueId())) {
            return JoinResult.ALREADY_IN_GAME;
        }

        Optional<Location> center = arenaManager.getArenaCenter();

        if (!center.isPresent()) {
            return JoinResult.ARENA_NOT_SET;
        }

        if (state != GameState.WAIT_FOR_PLAYERS && state != GameState.START_SOON) {
            return JoinResult.GAME_IN_PROGRESS;
        }

        if (players.size() >= playerMax) {
            return JoinResult.FULL;
        }

        if (players.isEmpty() && state == GameState.WAIT_FOR_PLAYERS) {
            arenaManager.buildFloor();
        }

        players.put(player.getUniqueId(), player.getLocation());
        player.teleport(center.get().clone().add(0.5, 1, 0.5));

        if (players.size() >= playerMin && state == GameState.WAIT_FOR_PLAYERS) {
            state = GameState.START_SOON;
            int seconds = colorFloor.getConfig().getInt("countdownSeconds");

            countdownTask = new CountdownTask(this, seconds)
                    .runTaskTimer(colorFloor, 0, 20L);
        }

        return JoinResult.SUCCESS;
    }

    public boolean leave(Player player) {
        int playerMin = colorFloor.getConfig().getInt("minPlayers");

        Location location = players.remove(player.getUniqueId());

        if (location == null) {
            return false;
        }
        player.teleport(location);

        if (state == GameState.START_SOON && players.size() < playerMin) {
            state = GameState.WAIT_FOR_PLAYERS;

            cancelTimer();
        }

        if (players.isEmpty()) {
            state = GameState.WAIT_FOR_PLAYERS;

            cancelTimer();

            arenaManager.undoArena();
        }

        return true;
    }

    public void stopGame() {
        if (!players.isEmpty()) {
            for (Map.Entry<UUID, Location> entry : players.entrySet()) {
                Player player = Bukkit.getPlayer(entry.getKey());

                if (player != null) {
                    player.teleport(entry.getValue());
                }
            }
            players.clear();
            state = GameState.WAIT_FOR_PLAYERS;
            arenaManager.undoArena();

            cancelTimer();
        }
    }

    public void broadcast(String message) {
        if (!players.isEmpty()) {
            players.keySet().forEach(player -> Bukkit.getPlayer(player).sendMessage(message));
        }
    }

    public void startGame() {
        state = GameState.GAME;

        countdownTask = null;

        broadcast("Game started");
        colorFloor.getLogger().info("Game started");
    }

    private void cancelTimer() {
        if (countdownTask != null) {
            countdownTask.cancel();
            countdownTask = null;
        }
    }
}
