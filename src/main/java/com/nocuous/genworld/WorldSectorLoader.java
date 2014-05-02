package com.nocuous.genworld;

import net.minecraft.util.IProgressUpdate;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.WorldServer;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraftforge.common.ForgeChunkManager;

import org.dynmap.*;
import org.dynmap.forge.DynmapMod;
import org.dynmap.forge.DynmapPlugin;

public class WorldSectorLoader implements Runnable {

	private transient DynmapListener dylistener;
	private transient int startX;
	private transient int startY;
	private transient int width;
	private transient int height;
	private transient boolean circleMode;
	private transient int centerX;
	private transient int centerY;
	private transient double halfWidth;
	private transient net.minecraft.world.WorldServer server;
	private transient IProgressUpdate output;

	public WorldSectorLoader(net.minecraft.world.WorldServer server,
			int startx, int starty, int width, int height,boolean circle,
			IProgressUpdate progressUpdate) {
		output = progressUpdate;

		dylistener = new DynmapListener();
		DynmapCommonAPIListener.register(dylistener);
		this.circleMode = circle;
		this.width = width;
		this.height = height;
		this.startX = startx;
		this.startY = starty;
		this.server = server;
		this.centerX = startX + width/2;
		this.centerY = startY + height/2;
		this.halfWidth = (((double)width)/2.0) * 16.0;
		System.out.println("WSL-'" + circleMode + "," +width + "," +height + "," +startX + "," +startY + "," +centerX+ "," +centerY);
	}

	private boolean isPointWithinCircle(int x, int y)
	{
		double dist = Math.sqrt(Math.pow(centerX*16.0 - x*16.0, 2) + Math.pow(centerY*16.0 - y*16.0, 2));
		if( dist < halfWidth )
		{
			return true;
		}
		return false;
	}
	
	public void printOutput(String text) {
		if (output != null)
			output.displayProgressMessage(text);
	}
	
	
	@Override
	public void run() {

		DynmapCommonAPI api = null;
		if (dylistener.apiAvailable) {
			api = dylistener.api;
		}

		int minX = startX;
		int maxX = startX + width;
		int minY = startY;
		int maxY = startY + height;
		int genCount = 0;
		int count = 0;
		long startTime = System.currentTimeMillis();
		int totalChunks = (maxX - minX) * (maxY - minY);
		ChunkProviderServer cps = ((ChunkProviderServer) server.theChunkProviderServer);
		int skippedCount = 0;
		int cleanCount = 0;
		cps.loadChunkOnProvideRequest = false;
		boolean unsaved = false;
		int saveCount = 0;
		int baseLoadedChunks = 0;
		int loadedSkippedCount = 0;
		//loop through all chunks
		for (int x = minX; x < maxX; ++x) {
			for (int y = minY; y < maxY; ++y) {
				
				if( circleMode == true) {
					//skip positions that are not within the circle when circle mode is enabled.
					if(!isPointWithinCircle(x,y)) {
						continue;
					}
				}
				
				//see if the chunk exists already
				//if provide chunk returns an empty chunk then the chunk doesn't already exist
				if (cps.provideChunk(x, y).isEmpty()) {
					
					//load chunk will force generate chunks if they do not exist
				
					//considering experimenting with ForgeChunkLoader.
					//Chunk c = ForgeChunkManager.fetchDormantChunk(ChunkCoordIntPair.chunkXZ2Int(x, y), server);
					Chunk c = cps.loadChunk(x, y);
					//chunks from load chunk should never be empty.
					if (c.isEmpty()) {
						printOutput("loadchunk returned empty chunk " + x + " "
								+ y);
					}
					c.populateChunk(cps, cps, x, y);
					c.setChunkModified();
					unsaved = true;
					saveCount++;
					
					//save all chunks loaded every 200 chunks generated.
					//or when this is the last chunks
					if (saveCount > 200 || y == (maxY - 1)) {
						printOutput("Saving Chunks: " + saveCount + " Loaded: "
								+ cps.getLoadedChunkCount());
	
						cps.saveChunks(true, output);
						
						//unload all chunks we just saved.
						long unloadStartTime = System.currentTimeMillis();
						baseLoadedChunks = cps.getLoadedChunkCount();
						while (cps.getLoadedChunkCount() > (baseLoadedChunks - saveCount)) {
							cps.unloadAllChunks();
							cps.unloadQueuedChunks();
							//add an abort condition for when we can't unload chunks - arbitrary time abort
							if( System.currentTimeMillis() - unloadStartTime > 100)
							{
								//more than 100ms has passed abort unload
								break;
							}
						}

						unsaved = false;
						saveCount = 0;
						// cps.unloadAllChunks();
						// cps.unloadQueuedChunks();
					}
					
					//every 20 generated chunks print a status message
					if (genCount % 20 == 0) {
						long lapTime = System.currentTimeMillis();
						double timePerChunk = 0.0;
						if (count != 0) {
							timePerChunk = ((lapTime - startTime) / count);
						} else {
							timePerChunk = (lapTime - startTime);
						}
						printOutput("L X: "
								+ x
								+ " Y: "
								+ y
								+ " ETA: "
								+ ((timePerChunk * (totalChunks - count)) / 1000)
								+ " sec");
					}

					genCount++;
				} else {
					skippedCount++;
					loadedSkippedCount++;
					//every 20 skipped chunks print a status message 
					if (skippedCount % 20 == 0) {
						printOutput("L X: " + x + " Y: " + y + " Skipped: "
								+ skippedCount);
					}
				}

				// clean up chunks that we've loaded that we don't need anymore
				// This should mostly be skipped chunks loaded by the provideChunk call
				if (unsaved == false && cleanCount > 2000) {
					cleanCount = 0;
					long unloadStartTime = System.currentTimeMillis();
					int chunkCount = cps.getLoadedChunkCount();
					//keep unloading until we have less than the skipped count.
					while (cps.getLoadedChunkCount() > (chunkCount - loadedSkippedCount)) {
						cps.unloadAllChunks();
						cps.unloadQueuedChunks();
						//add an abort condition for when we can't unload chunks - arbitrary time abort
						if( System.currentTimeMillis() - unloadStartTime > 100)
						{
							//more than 100ms has passed abort unload
							break;
						}
					}
					loadedSkippedCount = 0;
					//force garbage collection
					System.gc();
					//print out the result of the cleaning
					int finalChunkCount = cps.getLoadedChunkCount();
					printOutput("cleaning: " + chunkCount + " To: "
							+ finalChunkCount);
				}
				count++;
				cleanCount++;
			}
			// api.triggerRenderOfVolume(server.provider.getSaveFolder(), x*16,
			// 0, 1, x*16+15, (startY+height)*16, 256 );
		}
		

		long endTime = System.currentTimeMillis();
		printOutput("genworld - " + server.provider.getDimensionName() + " - "
				+ server.provider.getSaveFolder() + "  - Done in "
				+ ((endTime - startTime) / 1000) + " sec Skipped: " + skippedCount + " Generated: " + (count - skippedCount));
		startTime = endTime;

		if( api != null ) {
			printOutput("genworld - calling dynamp draw");
			
			String worldName = server.provider.getSaveFolder();
			if( worldName == null || worldName == "")
				worldName = "world";
			
			int dynResult = api.triggerRenderOfVolume(
					worldName, minX * 16, minY * 16, 0,
					maxX * 16, maxY * 16, 255);
			printOutput("genworld - dynamp result: " + dynResult);
		}
		
		DynmapCommonAPIListener.unregister(dylistener);
	}

}
