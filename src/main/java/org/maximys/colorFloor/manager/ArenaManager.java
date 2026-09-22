package org.maximys.colorFloor.manager;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.configuration.file.FileConfiguration;
import org.maximys.colorFloor.ColorFloor;
import org.maximys.colorFloor.game.FloorState;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ArenaManager {
    private final ColorFloor colorFloor;
    private final Random random = new Random();
    private final DyeColor[] dyeColors = DyeColor.values();
    private final ArrayDeque<List<BlockState>> savedStates = new ArrayDeque<>();

    public ArenaManager(ColorFloor colorFloor) {
        this.colorFloor = colorFloor;
    }

    public FloorState buildFloor() {
        FileConfiguration configuration = colorFloor.getConfig();
        String worldName = configuration.getString("arena.world");

        if (worldName == null) {
            return FloorState.ARENA_NOT_SET;
        }

        int x = configuration.getInt("arena.x");
        int y = configuration.getInt("arena.y");
        int z = configuration.getInt("arena.z");

        World world = colorFloor.getServer().getWorld(worldName);

        if (world == null) {
            return FloorState.WORLD_NOT_FOUND;
        }

        Block arenaCenter = world.getBlockAt(x, y, z);

        int floorSize = configuration.getInt("floorSize");
        int floorRadius = floorSize / 2;

        generateFloor(floorRadius, arenaCenter);
        return FloorState.SUCCESS;
    }

    public void setArena(Block block) {
        FileConfiguration configuration = colorFloor.getConfig();

        configuration.set("arena.world", block.getWorld().getName());
        configuration.set("arena.x", block.getX());
        configuration.set("arena.y", block.getY());
        configuration.set("arena.z", block.getZ());

        colorFloor.saveConfig();
    }

    public boolean undoArena() {
        if (savedStates.isEmpty()) {
            return false;
        } else {
            List<BlockState> blockStates = savedStates.pop();
            for (BlockState bs : blockStates) {
                bs.update(true);
            }
            return true;
        }
    }

    private void generateFloor(int floorRadius, Block arenaCenter) {
        List<BlockState> blockStates = new ArrayList<>();
        for (int dx = -floorRadius; dx <= floorRadius; dx++) {
            for (int dz = -floorRadius; dz <= floorRadius; dz++) {
                int index = random.nextInt(dyeColors.length);
                DyeColor dyeColor = dyeColors[index];
                Block floorBlock = arenaCenter.getRelative(dx, 0, dz);
                blockStates.add(floorBlock.getState());
                floorBlock.setType(Material.WOOL);
                floorBlock.setData(dyeColor.getWoolData());
            }
        }
        savedStates.push(blockStates);
    }
}
