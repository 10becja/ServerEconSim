package me.becja10.ServerEconSim.Commands;

import java.util.List;

import me.becja10.ServerEconSim.ServerEconSim;
import me.becja10.ServerEconSim.Utils.Messages;
import me.becja10.ServerEconSim.Utils.PlayerData;
import me.becja10.ServerEconSim.Utils.Request;
import me.becja10.ServerEconSim.Utils.RequestManager;
import me.becja10.ServerEconSim.Utils.Serializer;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class RequestCommands{

	public static boolean setRequest(CommandSender sender, String[] args) {
		
		if(!(sender instanceof Player))
		{
			sender.sendMessage(Messages.playersOnly());
			return true;
		}
		Player p = (Player) sender;
		
		if(!p.hasPermission("ses.setrequest"))
		{
			sender.sendMessage(Messages.noPermission());
			return true;
		}
		
		if(args.length < 4)
			return false;
		
		int amount = 0;
		int price = 0;
		int value = 0;
		int limit = 1;
		try{
			amount = Integer.parseInt(args[0]);
			price = Integer.parseInt(args[1]);
			value = Integer.parseInt(args[2]);
			limit = Integer.parseInt(args[3]);
		} catch(NumberFormatException ex){
			sender.sendMessage(Messages.nan());
			return false;
		}
		
		if(amount <= 0 || price <= 0 || value < 0 || value > 5 || limit < 1)
		{
			sender.sendMessage(Messages.invalid());
			return true;
		}
		
		ItemStack request = p.getInventory().getItemInMainHand();
		if(request == null || request.getType() == Material.AIR){
			sender.sendMessage(Messages.noAir());
			return true;
		}
		
		String display = "";
		if(args.length > 4)
			for(int i = 4; i < args.length; i++){
				display += args[i];
			}
		else
			display = request.getType().toString();
		
		ItemStack dummy = new ItemStack(request);
		dummy.setAmount(1);
		
		String itemString = Serializer.toString(dummy);
		
		int id = RequestManager.addRequest(itemString, display, amount, price, value, limit);
		sender.sendMessage(Messages.requestAdded(id, itemString));

		return true;
	}

	public static boolean viewRequests(CommandSender sender) {
		if(!sender.hasPermission("ses.viewrequests"))
		{
			sender.sendMessage(Messages.noPermission());
			return true;
		}
		sender.sendMessage(Messages.requestHeader());
		for(Request r : ServerEconSim.requests)
		{
			Boolean completedAlready = false;
			if(sender instanceof Player)
			{
				
				PlayerData pd = ServerEconSim.players.get(((Player)sender).getUniqueId());
				if(pd != null && pd.finishedRequests.containsKey(r.id) && pd.finishedRequests.get(r.id) >= r.limit)
					completedAlready = true;
			}
			ChatColor color = (completedAlready) ? ChatColor.RED : ChatColor.GREEN;
			
			sender.sendMessage(Messages.requestList(color, r.tempId, r.amount, r.displayName, r.price));
		}
		int timeSinceLastRefresh = (int) (System.currentTimeMillis() - ServerEconSim.lastBatch)/1000;
		int remainingTime = ServerEconSim.timeUntilRefresh - timeSinceLastRefresh;
		String str = (remainingTime > 0) ? "in " + generateTimeString(remainingTime) : "soon!";
		sender.sendMessage(Messages.timeLeft(str));
		return true;
	}
	
	public static boolean listRequests(CommandSender sender, String[] args)
	{
		if(sender instanceof Player && !sender.hasPermission("ses.listrequests"))
		{
			sender.sendMessage(Messages.noPermission());
		}
		else{
			String arg1 = (args.length > 0) ? args[0] : "";
			String arg2 = (args.length > 1) ? args[1] : "";
			
			int page = 1;
			boolean arg1IsPage = true;
			try{ //see if arg1 is the page number
				page = Integer.parseInt(arg1);
			}
			catch(NumberFormatException ex){
				arg1IsPage = false;
				try{
					page = Integer.parseInt(arg2);
				}
				catch(NumberFormatException ex1){
					
				}
			}
			List<Request> allReqs = RequestManager.getAllRequests();
			
			String filter = (arg1IsPage) ? "" : arg1;
			
			int start = Math.max(0, 10*(page-1));
			int end = Math.min(start + 10, allReqs.size());
			
			sender.sendMessage(ChatColor.AQUA + "Requests page " + page);
			sender.sendMessage(ChatColor.AQUA + "-----------------------------");
			for(int i = start; i < end; i++){
				Request req = allReqs.get(i);
				if(filter == "" || req.displayName.toLowerCase().contains(filter.toLowerCase())){
					String temp = ChatColor.GREEN + ""+ req.id + ". ";
					temp += ChatColor.BLUE + req.displayName + " ";
					temp += ChatColor.GOLD + "amount: " + ChatColor.BLUE + req.amount + " ";
					temp += ChatColor.GOLD + "price: " + ChatColor.BLUE + req.price + " ";
					temp += ChatColor.GOLD + "flux: " + ChatColor.BLUE + req.value + " ";
					temp += ChatColor.GOLD + "limit: " + ChatColor.BLUE + req.limit + " ";
					
					sender.sendMessage(temp);
				}	
			}			
		}
		return true;
	}
	
	public static boolean requestDetails(CommandSender sender, String[] args){
		
		if(sender instanceof Player && !sender.hasPermission("ses.requestdetails")){
			sender.sendMessage(Messages.noPermission());
		}
		else{
			if(args.length != 1)
				return false;
			int id = 1;
			try {
				id = Integer.parseInt(args[0]);
			}catch(NumberFormatException ex){
				sender.sendMessage(Messages.nan());
				return false;
			}
			Request req = RequestManager.getRawRequest(id);
			if(req != null){
				sender.sendMessage(ChatColor.AQUA + "Request Details");
				sender.sendMessage(ChatColor.AQUA + "-----------------------------");
				String item =  Serializer.toString(req.item);
				sender.sendMessage(ChatColor.GREEN + "Id: " + ChatColor.BLUE + req.id);
				sender.sendMessage(ChatColor.GREEN + "Item: " + ChatColor.BLUE + item);
				sender.sendMessage(ChatColor.GREEN + "Amount: " + ChatColor.BLUE + req.amount);
				sender.sendMessage(ChatColor.GREEN + "Price: " + ChatColor.BLUE + req.price);
				sender.sendMessage(ChatColor.GREEN + "Flux Value: " + ChatColor.BLUE + req.value);
				sender.sendMessage(ChatColor.GREEN + "Limit: " + ChatColor.BLUE + req.limit);		
				sender.sendMessage(ChatColor.GREEN + "Times Bought: " + ChatColor.BLUE + req.timesBought);				
			}
			else{
				sender.sendMessage(Messages.prefix + ChatColor.RED + "A request with that ID could not be found. Use /listrequests to see all available requests.");
			}
		}
		return true;
	}
	
	public static boolean editRequest(CommandSender sender, String[] args) {
		
		if(!(sender instanceof Player))
		{
			sender.sendMessage(Messages.playersOnly());
			return true;
		}
		Player p = (Player) sender;
		
		if(!p.hasPermission("ses.setrequest"))
		{
			sender.sendMessage(Messages.noPermission());
			return true;
		}
		
		if(args.length < 5)
			return false;
		
		int id = 0;
		int amount = 0;
		int price = 0;
		int value = 0;
		int limit = 1;
		try{
			id = Integer.parseInt(args[0]);
			amount = Integer.parseInt(args[1]);
			price = Integer.parseInt(args[2]);
			value = Integer.parseInt(args[3]);
			limit = Integer.parseInt(args[4]);
		} catch(NumberFormatException ex){
			sender.sendMessage(Messages.nan());
			return false;
		}
		
		if(amount <= 0 || price <= 0 || value < 0 || value > 5 || limit < 1)
		{
			sender.sendMessage(Messages.invalid());
			return true;
		}
		
		ItemStack request = p.getInventory().getItemInMainHand();
		if(request == null || request.getType() == Material.AIR){
			sender.sendMessage(Messages.noAir());
			return true;
		}
		
		String display = "";
		if(args.length > 5)
			for(int i = 5; i < args.length; i++){
				display += args[i];
			}
		else
			display = request.getType().toString();
		
		ItemStack dummy = new ItemStack(request);
		dummy.setAmount(1);
		
		String itemString = Serializer.toString(dummy);
		
		if(RequestManager.editRequest(id, itemString, display, amount, price, value, limit)){
			sender.sendMessage(Messages.prefix + ChatColor.GREEN + "Successfully updated request " + id);
		}
		else{
			sender.sendMessage(Messages.prefix + ChatColor.RED + "Failed to update request " + id);
		}

		return true;
	}
	
	private static String generateTimeString(int t){
		String ret = "";
		if(t >= 3600)
		{
			int hours = t/3600;
			int min = (t - (hours*3600))/60;
			ret = hours + " hour";
			ret += (hours > 1) ? "s " : " ";
			ret += min + " minute";
			ret += (min > 1) ? "s" : "";
		}
		else if(t >= 60)
		{
			int min = t /60;
			ret = min + " minute";
			ret += (min > 1) ? "s" : "";
		}
		else{
			ret = t + " second";
			if(t > 1) ret += "s";
		}
		return ret;
	}
}
