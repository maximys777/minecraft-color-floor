package org.maximys.colorFloor.manager;

import org.bukkit.DyeColor;
import org.bukkit.Location;
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
import java.util.Optional;
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
        List<Block> floor = getFloor();

        if (floor.isEmpty()) {
            return FloorState.ARENA_NOT_SET;
        }

        generateFloor(floor);
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

    public Optional<Location> getArenaCenter() {
        FileConfiguration configuration = colorFloor.getConfig();
        String worldName = configuration.getString("arena.world");

        if (worldName == null) {
            return Optional.empty();
        }

        int x = configuration.getInt("arena.x");
        int y = configuration.getInt("arena.y");
        int z = configuration.getInt("arena.z");

        World world = colorFloor.getServer().getWorld(worldName);

        if (world == null) {
            colorFloor.getLogger().warning("World " + worldName + " not found");
            return Optional.empty();
        }

        return Optional.of(new Location(world, x, y, z));
    }

    private void generateFloor(List<Block> floor) {
        List<BlockState> blockStates = new ArrayList<>();

        for (Block block : floor) {
            blockStates.add(block.getState());
            paintRandom(block);
        }

        savedStates.push(blockStates);
    }

    private List<Block> getFloor() {
        List<Block> result = new ArrayList<>();

        Optional<Location> arenaCenter = getArenaCenter();

        if (!arenaCenter.isPresent()) {
            return result;
        }

        int floorSize = colorFloor.getConfig().getInt("floorSize");
        int floorRadius = floorSize / 2;

        Block blockCenter = arenaCenter.get().getBlock();

        for (int dx = -floorRadius; dx <= floorRadius; dx++) {
            for (int dz = -floorRadius; dz <= floorRadius; dz++) {
                Block floorBlock = blockCenter.getRelative(dx, 0, dz);
                result.add(floorBlock);
            }
        }
        return result;
    }

    private void paintRandom(Block block) {
        int index = random.nextInt(dyeColors.length);
        DyeColor dyeColor = dyeColors[index];
        block.setType(Material.WOOL);
        block.setData(dyeColor.getWoolData());
    }

    public void recolorFloor() {
        List<Block> blocks = getFloor();
        for (Block b : blocks) {
            paintRandom(b);
        }
    }

    public void removeAllExcept(DyeColor color) {
        List<Block> blocks = getFloor();
        for (Block b : blocks) {
            if (b.getData() != color.getWoolData()) {
                b.setType(Material.AIR);
            }
        }
    }

    public Optional<DyeColor> randomFloorColor() {
        List<Block> blocks = getFloor();
        if (blocks.isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(DyeColor.getByWoolData(blocks.get(random.nextInt(blocks.size())).getData()));
    }
}
