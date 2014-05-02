package com.nocuous.genworld;

import net.minecraft.util.ChunkCoordinates;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.*;
import cpw.mods.fml.common.Mod;
//import cpw.mods.fml.common.Mod.Init;       //Used in 1.5.2 and before
import cpw.mods.fml.common.Mod.Instance;
//import cpw.mods.fml.common.Mod.PostInit;   //Used in 1.5.2 and before
//import cpw.mods.fml.common.Mod.PreInit;    //Used in 1.5.2 and before
import cpw.mods.fml.common.Mod.EventHandler; //Added for 1.6.x and 1.7.x
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartedEvent;
import cpw.mods.fml.common.event.FMLServerStoppedEvent;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;

@Mod(modid = GenWorldMod.MODID, version = GenWorldMod.VERSION)
public class GenWorldMod {
	public static final String MODID = "genworld";
	public static final String VERSION = "1.1";

	@Instance
	public static GenWorldMod instance;

	// Says where the client and server 'proxy' code is loaded.
	@SidedProxy(clientSide = "com.nocuous.genworld.client.ClientProxy", serverSide = "com.nocuous.genworld.CommonProxy")
	public static CommonProxy proxy;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {

	}

	@EventHandler
	public void serverStop(FMLServerStoppedEvent event) {
		proxy.close();
	}

	@EventHandler
	public void serverStart(FMLServerStartedEvent event) {
		proxy.serverStart();

	}

	@EventHandler
	public void load(FMLInitializationEvent event) {
		// Stub method
		proxy.registerRenderers();
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {

		// proxy.postInit();
	}
}
