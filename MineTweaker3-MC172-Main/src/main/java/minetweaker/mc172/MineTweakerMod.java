/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package minetweaker.mc172;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerAboutToStartEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.event.FMLServerStoppedEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;
import java.io.File;
import minetweaker.MineTweakerAPI;
import minetweaker.MineTweakerImplementationAPI;
import minetweaker.api.logger.FileLogger;
import minetweaker.mc172.furnace.FuelTweaker;
import minetweaker.mc172.furnace.MCFurnaceManager;
import minetweaker.mc172.game.MCGame;
import minetweaker.mc172.mods.MCLoadedMods;
import minetweaker.mc172.network.MineTweakerLoadScriptsHandler;
import minetweaker.mc172.network.MineTweakerLoadScriptsPacket;
import minetweaker.mc172.network.MineTweakerOpenBrowserHandler;
import minetweaker.mc172.network.MineTweakerOpenBrowserPacket;
import minetweaker.mc172.oredict.MCOreDict;
import minetweaker.mc172.recipes.MCRecipeManager;
import minetweaker.mc172.server.MCServer;
import minetweaker.mc172.util.MineTweakerHacks;
import minetweaker.runtime.IScriptProvider;
import minetweaker.runtime.providers.ScriptProviderCascade;
import minetweaker.runtime.providers.ScriptProviderDirectory;
import net.minecraftforge.common.MinecraftForge;

/**
 * Main mod class. Performs some general logic, initialization of the API and
 * FML event handling.
 * 
 * @author Stan Hebben
 */
@Mod(modid = MineTweakerMod.MODID, version = MineTweakerMod.MCVERSION + "-3.0.3")
public class MineTweakerMod {
	public static final String MODID = "MineTweaker3";
	public static final String MCVERSION = "1.7.2";
	
	public static final SimpleNetworkWrapper NETWORK = NetworkRegistry.INSTANCE.newSimpleChannel(MODID);
	
	private static final String[] REGISTRIES = {
		"minetweaker.mods.ic2.ClassRegistry",
		"minetweaker.mods.nei.ClassRegistry",
		"minetweaker.mods.gregtech.ClassRegistry"
	};
	
	static {
		NETWORK.registerMessage(MineTweakerLoadScriptsHandler.class, MineTweakerLoadScriptsPacket.class, 0, Side.CLIENT);
		NETWORK.registerMessage(MineTweakerOpenBrowserHandler.class, MineTweakerOpenBrowserPacket.class, 1, Side.CLIENT);
	}
	
	@Mod.Instance(MODID)
	public static MineTweakerMod INSTANCE;
	
	public final MCRecipeManager recipes;
	private final IScriptProvider scriptsGlobal;
	
	public MineTweakerMod() {
		MineTweakerImplementationAPI.init(
				new MCOreDict(),
				recipes = new MCRecipeManager(),
				new MCFurnaceManager(),
				MCGame.INSTANCE,
				new MCLoadedMods());
		
		MineTweakerImplementationAPI.logger.addLogger(new FileLogger(new File("minetweaker.log")));
		MineTweakerImplementationAPI.platform = MCPlatformFunctions.INSTANCE;
		
		File globalDir = new File("scripts");
		if (!globalDir.exists()) {
			globalDir.mkdirs();
		}
		
		scriptsGlobal = new ScriptProviderDirectory(globalDir);
		MineTweakerImplementationAPI.setScriptProvider(scriptsGlobal);
	}
	
	// ##########################
	// ### FML Event Handlers ###
	// ##########################
	
	@EventHandler
	public void onLoad(FMLPreInitializationEvent ev) {
		MinecraftForge.EVENT_BUS.register(new ForgeEventHandler());
		FMLCommonHandler.instance().bus().register(new FMLEventHandler());
	}
	
	@EventHandler
	public void onPostInit(FMLPostInitializationEvent ev) {
		MineTweakerAPI.registerClassRegistry(MineTweakerRegistry.class);
		
		for (String registry : REGISTRIES) {
			MineTweakerAPI.registerClassRegistry(registry);
		}
		
		FuelTweaker.INSTANCE.register();
	}
	
	@EventHandler
	public void onServerAboutToStart(FMLServerAboutToStartEvent ev) {
		// starts before loading worlds
		// perfect place to start MineTweaker!
		
		File scriptsDir = new File(MineTweakerHacks.getWorldDirectory(ev.getServer()), "scripts");
		if (!scriptsDir.exists()) {
			scriptsDir.mkdir();
		}
		
		IScriptProvider scriptsLocal = new ScriptProviderDirectory(scriptsDir);
		IScriptProvider cascaded = new ScriptProviderCascade(scriptsGlobal, scriptsLocal);
		MineTweakerImplementationAPI.setScriptProvider(cascaded);
		MineTweakerImplementationAPI.onServerStart(new MCServer(ev.getServer()));
	}
	
	@EventHandler
	public void onServerStarting(FMLServerStartingEvent ev) {
		
	}
	
	@EventHandler
	public void onServerStopped(FMLServerStoppedEvent ev) {
		MineTweakerImplementationAPI.onServerStop();
		MineTweakerImplementationAPI.setScriptProvider(scriptsGlobal);
	}
}