package org.maximys.colorFloor.command;

import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class FloorCommand implements CommandExecutor {
    private final Random random = new Random();
    private final List<BlockState> savedStates = new ArrayList<>();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can run this command");
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("undo")) {
            if (savedStates.isEmpty()) {
                sender.sendMessage(ChatColor.WHITE + "First use /floor");
            } else {
                for (BlockState blockState : savedStates) {
                    blockState.update(true);
                }
                sender.sendMessage(ChatColor.GREEN + "Floor undo");
                savedStates.clear();
            }
            return true;
        }
        
        if (args.length > 0) {
            return false;
        }
        savedStates.clear();

        Player player = (Player) sender;
        Location location = player.getLocation();
        Block block = location.getBlock();

        DyeColor[] dyeColors = DyeColor.values();
        Block blockUnderPlayer = block.getRelative(BlockFace.DOWN);

        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {

                int index = random.nextInt(dyeColors.length);
                DyeColor dyeColor = dyeColors[index];
                Block floorBlock = blockUnderPlayer.getRelative(dx, 0, dz);
                savedStates.add(floorBlock.getState());
                floorBlock.setType(Material.WOOL);
                floorBlock.setData(dyeColor.getWoolData());
            }
        }
        player.sendMessage(ChatColor.GOLD + "Floor has been generated");

        return true;
    }
}
