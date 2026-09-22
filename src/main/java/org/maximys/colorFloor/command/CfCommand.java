package org.maximys.colorFloor.command;

import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.maximys.colorFloor.ColorFloor;

import java.util.Random;

public class CfCommand implements CommandExecutor {
    private final ColorFloor colorFloor;
    private final Random random = new Random();

    public CfCommand(ColorFloor colorFloor) {
        this.colorFloor = colorFloor;
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
        FileConfiguration configuration = colorFloor.getConfig();
        switch (subCommand) {
            case "setarena":
                Block blockUnderPlayer = player.getLocation().getBlock().getRelative(BlockFace.DOWN);
                configuration.set("arena.world", blockUnderPlayer.getWorld().getName());
                configuration.set("arena.x", blockUnderPlayer.getX());
                configuration.set("arena.y", blockUnderPlayer.getY());
                configuration.set("arena.z", blockUnderPlayer.getZ());
                colorFloor.saveConfig();
                player.sendMessage(ChatColor.YELLOW + "Arena set at world: " + blockUnderPlayer.getWorld().getName() + " coordinates: " + blockUnderPlayer.getX() + " " + blockUnderPlayer.getY() + " " + blockUnderPlayer.getZ());
                return true;
            case "build":
                String worldName = configuration.getString("arena.world");
                if (worldName == null) {
                    sender.sendMessage(ChatColor.RED + "first use /cf setarena");
                    return true;
                }


                int x = configuration.getInt("arena.x");
                int y = configuration.getInt("arena.y");
                int z = configuration.getInt("arena.z");

                World world = colorFloor.getServer().getWorld(worldName);

                if (world == null) {
                    player.sendMessage(ChatColor.RED + " world not found");
                    return true;
                }

                Block arenaCenter = world.getBlockAt(x, y, z);

                int floorSize = configuration.getInt("floorSize");
                int floorRadius = floorSize / 2;
                DyeColor[] dyeColors = DyeColor.values();

                for (int dx = -floorRadius; dx <= floorRadius; dx++) {
                    for (int dz = -floorRadius; dz <= floorRadius; dz++) {
                        int index = random.nextInt(dyeColors.length);
                        DyeColor dyeColor = dyeColors[index];
                        Block floorBlock = arenaCenter.getRelative(dx, 0, dz);
                        floorBlock.setType(Material.WOOL);
                        floorBlock.setData(dyeColor.getWoolData());
                    }
                }

                player.sendMessage(ChatColor.YELLOW + "Building floor");
                return true;
            default:
                return false;
        }
    }
}
