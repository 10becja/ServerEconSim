package me.becja10.ServerEconSim;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.logging.Logger;

import me.becja10.ServerEconSim.Commands.RequestCommands;
import me.becja10.ServerEconSim.Events.SignEventHandler;
import me.becja10.ServerEconSim.Utils.Messages;
import me.becja10.ServerEconSim.Utils.PlayerData;
import me.becja10.ServerEconSim.Utils.Request;
import me.becja10.ServerEconSim.Utils.RequestManager;
import me.becja10.ServerEconSim.Utils.SignManager;
import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class ServerEconSim extends JavaPlugin implements Listener{

	public static ServerEconSim instance;
	public final static Logger logger = Logger.getLogger("Minecraft");
	public static Economy econ;
	
	public static HashMap<Location, Request> signs;
	public static HashMap<UUID, PlayerData> players;
	public static List<Request> requests;	
	public static Random gen;
	
	public static long lastBatch;
	
	private String configPath;
	private FileConfiguration config;
	private FileConfiguration outConfig;
	
	//Config Settings
	public static List<Integer> valueFlux; private String valueFluxStr = "Value fluxuations";
	public static int timeUntilRefresh; private String timeUntilRefreshStr = "Seconds until requests are refreshed";
	public int numRequests; private String numRequestsStr = "Number of requests";
	public boolean broadcastRefresh; private String broadcastRefreshStr = "Broadcast new requests";
	
		
	private void loadConfig(){
		configPath = this.getDataFolder().getAbsolutePath() + File.separator + "config.yml";
		config = YamlConfiguration.loadConfiguration(new File(configPath));
		outConfig = new YamlConfiguration();
				
		valueFlux = config.getIntegerList(valueFluxStr);
		if(valueFlux.isEmpty())
		{
			valueFlux.add(0);
			valueFlux.add(10);
			valueFlux.add(25);
			valueFlux.add(50);
			valueFlux.add(100);
			valueFlux.add(500);
		}		
		
		timeUntilRefresh = config.getInt(timeUntilRefreshStr, 86400);
		numRequests = config.getInt(numRequestsStr, 5);
		broadcastRefresh = config.getBoolean(broadcastRefreshStr, true);

		outConfig.set(valueFluxStr, valueFlux);
		outConfig.set(timeUntilRefreshStr, timeUntilRefresh);
		outConfig.set(numRequestsStr, numRequests);
		outConfig.set(broadcastRefreshStr, broadcastRefresh);
				
		saveConfig(outConfig, configPath);
	}
	
	private void saveConfig(FileConfiguration config, String path)
	{
        try{config.save(path);}
        catch(IOException exception){logger.info("Unable to write to the configuration file at \"" + path + "\"");}
	}
	
	@Override
	public void onEnable(){
		PluginDescriptionFile pdfFile = this.getDescription();
		PluginManager manager = getServer().getPluginManager();

		if (!setupEconomy() ) {
			logger.severe(pdfFile.getName() + " - Disabled due to no Vault dependency found!");
            manager.disablePlugin(this);
            return;
        }
		
		logger.info(pdfFile.getName() + " Version " + pdfFile.getVersion() + " has been enabled!");
		instance = this;
		
		gen = new Random(System.currentTimeMillis());		
		signs = new HashMap<Location, Request>();
		players = new HashMap<UUID, PlayerData>();
		requests = new ArrayList<Request>();
		
		
		manager.registerEvents(new SignEventHandler(), this);
		
		loadConfig();
		RequestManager.setUpManager();
		SignManager.setUpManager();
		lastBatch = 0L;
		checkAndSetTicker();		
	}
		
	@Override
	public void onDisable(){
		PluginDescriptionFile pdfFile = this.getDescription();
		logger.info(pdfFile.getName() + " Has Been Disabled!");
		saveConfig(outConfig, configPath);
		RequestManager.saveRequests();
		SignManager.saveSigns();
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
	{
		switch(cmd.getName().toLowerCase()){ 
			case "reloadrequests":
				if(sender instanceof Player && !sender.hasPermission("ses.admin"))
					sender.sendMessage(Messages.noPermission());
				else{
					RequestManager.reloadRequests();
					SignManager.reloadSigns();
					loadConfig();
					pickRequests(5);
					initalizeSigns();
					sender.sendMessage(Messages.reloadSuccessful());
				}
				return true;
			case "setrequest":
				return RequestCommands.setRequest(sender, args);
			case "viewrequests":
				return RequestCommands.viewRequests(sender);
		}
		return true;
	}
	
	private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }
	
	private static void pickRequests(int numberOf){
		requests.clear();
		int totReqs = RequestManager.getTotalRequests();
		if(totReqs <= 0)
		{
			logger.warning(instance.getDescription().getName() + " no requests configured.");
			return;
		}
		int numToGet = Math.min(totReqs, numberOf);
		List<Integer> ids = new ArrayList<Integer>();
		boolean done = false;
		int attempts = 1000;
		while(!done && attempts > 0)
		{
			attempts--;
			
			int id = gen.nextInt(totReqs) + 1;
			if(ids.contains(id)){
				continue;				
			}
			
			ids.add(id);
			Request reqToAdd = RequestManager.getRequest(id);
			if(reqToAdd != null){
				reqToAdd.tempId = requests.size() + 1;
				requests.add(reqToAdd);
			}
			
			done = requests.size() >= numToGet;							
		}
	}
	
	private static void initalizeSigns(){
		signs.clear();
		if(requests.size() <= 0) return;
		for(String id : SignManager.getSigns()){
			int rNum = SignManager.getRequestNumFor(id);
			if(rNum < 0 || rNum >= requests.size())
				continue;
			Location loc = SignManager.getLocationFor(id);
			if(loc == null || !(loc.getBlock().getType() == Material.WALL_SIGN || loc.getBlock().getType() == Material.SIGN_POST))
			{
				SignManager.removeSign(id);
				continue;
			}
			
			Request req = requests.get(rNum);			
			if(req != null)
			{
				signs.put(loc, req);
				Sign sign = (Sign) loc.getBlock().getState();
				sign.setLine(0, "[Request "+ req.tempId +"]");
				sign.setLine(1, req.amount + "");
				sign.setLine(2, req.displayName);
				sign.setLine(3, "$" + req.price);
				if(!sign.update(true)){
					logger.warning(instance.getDescription().getName() + "Error updating sign at " + loc);
				}			
			}
		}
	}
	
	
	private void checkAndSetTicker(){
		boolean shouldRefresh = System.currentTimeMillis() - lastBatch > timeUntilRefresh * 1000;
		if(!shouldRefresh)
		{
			long timeRemaining = (timeUntilRefresh * 1000) - (System.currentTimeMillis() - lastBatch);
			shouldRefresh = timeRemaining < 60000; //if timeRemaining is less than a minute, refresh
		}
		if(shouldRefresh)
		{
			logger.info("[Server Economy] ------REQUESTS REFRESHING------");
			if(broadcastRefresh)
			{
				for(Player p : Bukkit.getOnlinePlayers())
					p.sendMessage(Messages.prefix + " New server requests are available!");
			}
			players.clear();
			pickRequests(numRequests);
			initalizeSigns();
			lastBatch = System.currentTimeMillis();
		}
		
		Bukkit.getScheduler().runTaskLater(this, new Runnable(){
			public void run(){
				checkAndSetTicker();
			}
		}, Math.min(timeUntilRefresh, 600) * 20L);
	}
	
}
