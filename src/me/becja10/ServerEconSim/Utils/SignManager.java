package me.becja10.ServerEconSim.Utils;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import me.becja10.ServerEconSim.ServerEconSim;

import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class SignManager {

	private static FileConfiguration config = null;
	private static File signs = null;
	private static String path = ServerEconSim.instance.getDataFolder().getAbsolutePath() 
			+ File.separator + "signs.yml";
	
	private static int nextId;

	public static FileConfiguration getManager() {
		/*
		 * nextSignId:
		 * signs:
		 *   <id>:
		 *     location:
		 *     requestNum: 
		 */
		if (config == null)
			reloadSigns();
		return config;
	}

	public static void reloadSigns() {
		if (signs == null)
			signs = new File(path);
		config = YamlConfiguration.loadConfiguration(signs);
	}
	
	public static void saveSigns() {
		if ((config == null) || (signs == null))
			return;
		try {
			getManager().save(signs);
		} catch (IOException ex) {
			ServerEconSim.logger.warning("Unable to write to the sign file at \"" + path + "\"");
		}
	}
	
	public static void setUpManager(){
		reloadSigns();
		nextId = config.getInt("nextSignId", 1);
	}
	
	private static void incrementId(){
		nextId++;
		config.set("nextSignId", nextId);
	}

	public static void addSign(Location location, int rNum) {
		
		String str = "signs." + nextId;
		
		config.set(str + ".location", Serializer.toString(location));
		config.set(str + ".requestNum", rNum);
		
	    incrementId();
	    saveSigns();
	}
	
	public static Set<String> getSigns(){
		return (config.getConfigurationSection("signs") == null) ? null : config.getConfigurationSection("signs").getKeys(false);
	}

	public static int getRequestNumFor(String id) {
		return config.getInt("signs." + id + ".requestNum", 1) - 1;
	}

	public static Location getLocationFor(String id) {
		return Serializer.toLocation(config.getString("signs." + id + ".location", ""));
	}

	public static void removeSignAt(Location location) {
		for(String str : getSigns())
		{
			String loc = Serializer.toString(location);
			if(config.getString("signs." + str + ".location").equals(loc)){
				config.set("signs." + str, null);
				saveSigns();
				return;
			}
		}
	}
	
	public static void removeSign(String id)
	{
		config.set("signs."+id, null);
		saveSigns();
	}
	
}
