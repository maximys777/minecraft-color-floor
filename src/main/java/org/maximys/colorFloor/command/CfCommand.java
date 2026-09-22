package org.maximys.colorFloor.command;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.maximys.colorFloor.game.FloorState;
import org.maximys.colorFloor.manager.ArenaManager;

public class CfCommand implements CommandExecutor {
    private final ArenaManager arenaManager;

    public CfCommand(ArenaManager arenaManager) {
        this.arenaManager = arenaManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can run this command.");
            return true;
        }
        if (args.length == 0) {
            return false;
        }

        String subCommand = args[0].toLowerCase();
        Player player = (Player) sender;
        switch (subCommand) {
            case "setarena":
                if (args.length >= 2) {
                    return false;
                }

                Location playerLocation = player.getLocation();
                Block block = playerLocation.getBlock().getRelative(BlockFace.DOWN);

                arenaManager.setArena(block);

                player.sendMessage(ChatColor.GOLD + "Arena set at: " + block.getX() + " " + block.getY() + " " + block.getZ());
                return true;
            case "build":
                if (args.length >= 2) {
                    return false;
                }

                FloorState state = arenaManager.buildFloor();

                if (state == FloorState.ARENA_NOT_SET) {
                    player.sendMessage(ChatColor.YELLOW + "First set the arena");
                } else if (state == FloorState.WORLD_NOT_FOUND) {
                    player.sendMessage(ChatColor.YELLOW + "World not found");
                } else if (state == FloorState.SUCCESS) {
                    player.sendMessage(ChatColor.YELLOW + "Building floor");
                }
                return true;
            case "undo":
                if (args.length >= 2) {
                    return false;
                }
                boolean result = arenaManager.undoArena();

                if (!result) {
                    player.sendMessage(ChatColor.YELLOW + "First build the arena");
                } else {
                    player.sendMessage(ChatColor.GOLD + "Arena undo");
                }
                return true;
            default:
                return false;
        }
    }
}
