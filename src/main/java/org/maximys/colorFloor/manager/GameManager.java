package org.maximys.colorFloor.manager;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.maximys.colorFloor.ColorFloor;
import org.maximys.colorFloor.game.GameState;
import org.maximys.colorFloor.game.JoinResult;
import org.maximys.colorFloor.task.CountdownTask;
import org.maximys.colorFloor.task.RoundTask;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class GameManager {
    private final Map<UUID, Location> players = new HashMap<>();
    private final ArenaManager arenaManager;
    private final ColorFloor colorFloor;
    private GameState state = GameState.WAIT_FOR_PLAYERS;
    private BukkitTask countdownTask;
    private BukkitTask roundTask;

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

            cancelTask();
        }

        if (players.isEmpty()) {
            state = GameState.WAIT_FOR_PLAYERS;

            cancelTask();

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

            cancelTask();
        }
    }

    public void broadcast(String message) {
        if (!players.isEmpty()) {
            players.keySet()
                    .forEach(uuid -> {
                        Player player = Bukkit.getPlayer(uuid);
                        if (player != null) {
                            player.sendMessage(message);
                        }
                    });
        }
    }

    public void startGame() {
        state = GameState.GAME;

        countdownTask = null;

        broadcast("Game started");
        colorFloor.getLogger().info("Game started");

        roundTask = new RoundTask(colorFloor, arenaManager, this).runTaskTimer(colorFloor, 0, 20L);
    }

    public void eliminateFallen() {
        Iterator<Map.Entry<UUID, Location>> iterator = players.entrySet().iterator();

        Optional<Location> center = arenaManager.getArenaCenter();

        if (!center.isPresent()) {
            return;
        }
        while (iterator.hasNext()) {
            Map.Entry<UUID, Location> entry = iterator.next();
            Player player = Bukkit.getPlayer(entry.getKey());

            if (player == null) {
                iterator.remove();
                continue;
            }

            if (player.getLocation().getY() < center.get().getY()) {
                player.teleport(entry.getValue());
                player.sendMessage("Player " + player.getName() + " has been eliminated");
                iterator.remove();
            }
        }
    }

    public void finishGame() {
        state = GameState.FINISHED;

        if (players.size() == 1) {
            Player player = Bukkit.getPlayer(players.keySet().iterator().next());
            broadcast("Player " + player.getName() + " win");
        } else {
            broadcast("nobody won");
        }
        stopGame();
    }

    public int getPlayersCount() {
        return players.size();
    }

    private void cancelTask() {
        if (countdownTask != null) {
            countdownTask.cancel();
            countdownTask = null;
        }

        if (roundTask != null) {
            roundTask.cancel();
            roundTask = null;
        }
    }
}
