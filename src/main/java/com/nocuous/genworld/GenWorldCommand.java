package com.nocuous.genworld;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;

public class GenWorldCommand extends CommandBase {

	private transient List<Thread> worldLoaders;
	SaveChunksProgressUpdate output;

	public void close() {
		for (Iterator<Thread> thread = worldLoaders.iterator(); thread
				.hasNext();) {
			try {
				thread.next().join();
			} catch (Exception e) {

			}
		}
		worldLoaders.clear();
		worldLoaders = null;
	}

	@Override
	public String getCommandName() {
		// TODO Auto-generated method stub
		return "genworld";
	}

	@Override
	public String getCommandUsage(ICommandSender var1) {
		// TODO Auto-generated method stub
		return "/genworld - use without any parameters for help with the genworld mod";
	}

	private WorldServer getWorld(String worldName) {
		WorldServer[] worlds = DimensionManager.getWorlds();
		for (int i = 0; i < worlds.length; i++) {

			if (worlds[i].provider.getDimensionName().equalsIgnoreCase(
					worldName.replace("\"", ""))
					|| worlds[i].provider.getSaveFolder().equalsIgnoreCase(
							worldName.replace("\"", ""))) {
				return worlds[i];
			}
		}
		return null;
	}

	private void printWorlds() {
		WorldServer[] worlds_print = DimensionManager.getWorlds();
		for (int i = 0; i < worlds_print.length; i++) {
			printOutput("Found world: "
					+ worlds_print[i].provider.getDimensionName()
					+ " Save Folder: "
					+ worlds_print[i].provider.getSaveFolder());
		}
	}

	private void box(String worldName, int xMin, int yMin, int xMax, int yMax) {
		WorldServer world = getWorld(worldName);
		if (world == null) {
			printOutput("World not found: " + worldName);
			return;
		}

		printOutput("Pregenerating world: " + world.provider.getDimensionName());
		ChunkCoordinates spawn = world.getSpawnPoint();
		
		WorldSectorLoader wsl = new WorldSectorLoader(world, xMin / 16,
				yMin / 16, (xMax - xMin) / 16, (yMax - yMin) / 16, false, output);
		// world sector loader is a runnable due to previous attempts at
		// threading it out.
		wsl.run();
	}

	private void spawnSquare(String worldName, int size) {
		WorldServer world = getWorld(worldName);
		if (world == null) {
			printOutput("World not found: " + worldName);
			return;
		}

		printOutput("Pregenerating world: " + world.provider.getDimensionName());
		//get spawn point in block coordinates - DEFINITELY NOT CHUNK COORDINATES
		ChunkCoordinates spawn = world.getSpawnPoint();
		printOutput("Spawn coordinates: " + spawn.posX + "/" + spawn.posY + "/" + spawn.posX);
		int chunkDimension = size / 16;

		int offsetX = (spawn.posX/16) - (chunkDimension / 2);
		//MC uses Z like bitmap Y
		int offsetY = (spawn.posZ/16) - (chunkDimension / 2);
		WorldSectorLoader wsl = new WorldSectorLoader(world, offsetX,
				offsetY, chunkDimension, chunkDimension, false, output);
		// world sector loader is a runnable due to previous attempts at
		// threading it out.
		wsl.run();
	}
	
	private void spawnCircle(String worldName, int radius) {
		WorldServer world = getWorld(worldName);
		if (world == null) {
			printOutput("World not found: " + worldName);
			return;
		}

		printOutput("Pregenerating world: " + world.provider.getDimensionName());
		//get spawn point in block coordinates - DEFINITELY NOT CHUNK COORDINATES
		ChunkCoordinates spawn = world.getSpawnPoint();
		int size = radius * 2;
		int chunkDimension = size / 16;
		
		printOutput("Spawn coordinates: " + spawn.posX + "/" + spawn.posY + "/" + spawn.posX);
		int offsetX = (spawn.posX/16) - (chunkDimension / 2);
		//MC uses Z like bitmap Y
		int offsetY = (spawn.posZ/16) - (chunkDimension / 2);
		WorldSectorLoader wsl = new WorldSectorLoader(world, offsetX,
				offsetY, chunkDimension, chunkDimension, true, output);
		// world sector loader is a runnable due to previous attempts at
		// threading it out.
		wsl.run();
	}

	private void circle(String worldName, int x, int y, int radius) {
		WorldServer world = getWorld(worldName);
		if (world == null) {
			printOutput("World not found: " + worldName);
			return;
		}

		printOutput("Pregenerating world: " + world.provider.getDimensionName());
		ChunkCoordinates spawn = world.getSpawnPoint();
		int size = radius * 2;
		int chunkDimension = size / 16;

		int offsetX = x - (radius);
		int offsetY = y - (radius);
		WorldSectorLoader wsl = new WorldSectorLoader(world, offsetX / 16,
				offsetY / 16, chunkDimension, chunkDimension, true, output);
		// world sector loader is a runnable due to previous attempts at
		// threading it out.
		wsl.run();
	}
	
	private void printOutput(String text) {
		if (output != null)
			output.displayProgressMessage(text);
	}

	@Override
	public void processCommand(ICommandSender var1, String[] var2) {
		// TODO Auto-generated method stub
		output = new SaveChunksProgressUpdate(var1);

		if (var2.length < 1) {
			printOutput("Not Enough Arguments: /genworld {command arguments}");
			printOutput("Commands: ");
			printOutput("SpawnSquare - pregenerates a square around the spawn point.");
			printOutput("              SpawnSquare <world> <size in blocks>  - Ex: /genworld SpawnSquare Overworld 1000");
			printOutput("box - pregenerates a user defined rectangle using block coordinates.");
			printOutput("      box <world> <x min> <y min> <x max> <y max>  - Ex: /genworld box Overworld -500 -500 500 500");
			printOutput("SpawnCircle - pregenerates a circle around the spawn point.");
			printOutput("              SpawnCircle <world> <radius in blocks>  - Ex: /genworld SpawnCircle Overworld 500");
			printOutput("circle - pregenerates a circle at the given coordinates.");
			printOutput("              SpawnCircle <world> <x> <y> <radius in blocks>  - Ex: /genworld circle Overworld 0 0 500");
			printOutput("Available Worlds:");
			printWorlds();
			output = null;
			return;
		}

		if (var2[0].equalsIgnoreCase("spawnsquare")) {
			// pregen world in a square shape centered around the player start
			if (var2.length != 3) {
				printOutput("Not Enough Arguments: /genworld SpawnSquare <world> <size>");
				output = null;
				return;
			}

			printOutput("args: '" + var2[1] + "' '" + var2[2] + "'");

			String worldName = var2[1];

			int size = 0;
			try {
				size = Integer.parseInt(var2[2]);
			} catch (Exception e) {
				printOutput("Size was not a number");
				output = null;
				return;
			}

			spawnSquare(worldName, size);
		} else if (var2[0].equalsIgnoreCase("box")) {
			if (var2.length != 6) {
				printOutput("Not Enough Arguments: /genworld box <world> <x min> <y min> <x max> <y max>");
				output = null;
				return;
			}

			printOutput("args: '" + var2[1] + "' '" + var2[2] + "' '" + var2[3]
					+ "' '" + var2[4] + "' '" + var2[5] + "'");

			String worldName = var2[1];

			int xMin = 0;
			int xMax = 0;
			int yMin = 0;
			int yMax = 0;
			try {
				xMin = Integer.parseInt(var2[2]);
				yMin = Integer.parseInt(var2[3]);
				xMax = Integer.parseInt(var2[4]);
				yMax = Integer.parseInt(var2[5]);
			} catch (Exception e) {
				printOutput("Size was not a number");
				output = null;
				return;
			}

			box(worldName, xMin, yMin, xMax, yMax);
		} else if (var2[0].equalsIgnoreCase("spawncircle")) {
			// pregen world in a square shape centered around the player start
			if (var2.length != 3) {
				printOutput("Not Enough Arguments: /genworld SpawnRadius <world> <radius>");
				output = null;
				return;
			}

			printOutput("args: '" + var2[1] + "' '" + var2[2] + "'");

			String worldName = var2[1];

			int size = 0;
			try {
				size = Integer.parseInt(var2[2]);
			} catch (Exception e) {
				printOutput("Radius was not a number");
				output = null;
				return;
			}

			spawnCircle(worldName, size);
		}  else if (var2[0].equalsIgnoreCase("circle")) {
			if (var2.length != 5) {
				printOutput("Not Enough Arguments: /genworld circle <world> <x> <y> <radius>");
				output = null;
				return;
			}

			printOutput("args: '" + var2[1] + "' '" + var2[2] + "' '" + var2[3]
					+ "' '" + var2[4] + "'");

			String worldName = var2[1];

			int x = 0;
			int y = 0;
			int radius = 0;
			try {
				x = Integer.parseInt(var2[2]);
				y = Integer.parseInt(var2[3]);
				radius = Integer.parseInt(var2[4]);
			} catch (Exception e) {
				printOutput("Size was not a number");
				output = null;
				return;
			}

			circle(worldName, x, y, radius);
		} 
		else
		{
			printOutput("genworld - Invalid option: " + var2[0]);
		}
		printOutput("genworld done");
		output = null;
	}

}
