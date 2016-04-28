package me.becja10.ServerEconSim.Utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import me.becja10.ServerEconSim.ServerEconSim;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

public class RequestManager {

	private static FileConfiguration config = null;
	private static File requests = null;
	private static String path = ServerEconSim.instance.getDataFolder().getAbsolutePath() 
			+ File.separator + "requests.yml";
	
	private static int nextId;

	public static FileConfiguration getRequests() {
		/*
		 * nextRequestId:
		 * requests:
		 *   <id>:
		 *     item: <item stack and full meta>
		 *     displayName: 
		 *     amount: <number requested>
		 *     price: 
		 *     timesBought: 
		 *     value: <a ranking of how this request is>
		 *     limit: <how many times this request can be done per cycle>
		 *     
		 */
		if (config == null)
			reloadRequests();
		return config;
	}

	public static void reloadRequests() {
		if (requests == null)
			requests = new File(path);
		config = YamlConfiguration.loadConfiguration(requests);
	}
	
	public static void saveRequests() {
		if ((config == null) || (requests == null))
			return;
		try {
			getRequests().save(requests);
		} catch (IOException ex) {
			ServerEconSim.logger.warning("Unable to write to the request file at \"" + path + "\"");
		}
	}
	
	public static void setUpManager(){
		reloadRequests();
		
		nextId = config.getInt("nextRequestId", 1);
	}
	
	private static void incrementId(){
		nextId++;
		config.set("nextRequestId", nextId);
	}

	public static int addRequest(String itemString, String display, int amount, int price, int value, int limit) {
		
		String str = "requests." + nextId;
		config.set(str + ".item", itemString);
		config.set(str + ".displayName", display);
		config.set(str + ".amount", amount);
		config.set(str + ".price", price);
		config.set(str+ ".value", value);
		config.set(str + ".timesBought", 0);
		config.set(str + ".limit", limit);
		
		int ret = nextId;
		incrementId();
		saveRequests();
		return ret;
	}
	
	public static boolean editRequest(int id, String itemString, String display, int amount, int price, int value, int limit){
		String str = "requests." + id;
		if(config.contains(str)){
			config.set(str + ".item", itemString);
			config.set(str + ".displayName", display);
			config.set(str + ".amount", amount);
			config.set(str + ".price", price);
			config.set(str+ ".value", value);
			config.set(str + ".limit", limit);
			saveRequests();
			return true;
		}
		return false;
	}

	public static int getTotalRequests() {
		return (config.getConfigurationSection("requests") != null) ? config.getConfigurationSection("requests").getKeys(false).size() : 0;
	}
	
	public static List<Request> getAllRequests(){
		List<Request> ret = new ArrayList<Request>();
		for(String key : config.getConfigurationSection("requests").getKeys(false))
		{
			int id = 0;
			try{
				id = Integer.parseInt(key);
			}
			catch(NumberFormatException ex)
			{
				continue;
			}
			ret.add(getRawRequest(id));
		}
		return ret;
	}

	public static Request getRequest(int id) {
		Request ret = null;
		String str = "requests." + id;
		if(config.contains(str))
		{
			ItemStack item = Serializer.toItemStack(config.getString(str + ".item", ""));
			ret = new Request(item, config.getString(str + ".displayName", ""),
					config.getInt(str + ".amount", 0),
					config.getInt(str + ".price", 0),
					config.getInt(str + ".value", 0),
					config.getInt(str + ".limit", 1),
					id);
		}
		return ret;
	}
	
	public static Request getRawRequest(int id){
		Request ret = null;
		String str = "requests." + id;
		if(config.contains(str))
		{
			ItemStack item = Serializer.toItemStack(config.getString(str + ".item", ""));
			ret = new Request(item, config.getString(str + ".displayName", ""),
					config.getInt(str + ".amount", 0),
					9999,
					config.getInt(str + ".value", 0),
					config.getInt(str + ".limit", 1),
					id);
			ret.price = config.getInt(str + ".price", 0);
			ret.timesBought = config.getInt(str+".timesBought", 0);
			
		}
		return ret;
	}
	
	public static void requestDone(int id)
	{
		int count = config.getInt("requests." + id+".timesBought", 0);
		count++;
		config.set("requests." + id+".timesBought", count);
		saveRequests();
	}
	
}
