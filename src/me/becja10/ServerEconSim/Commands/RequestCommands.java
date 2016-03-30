package me.becja10.ServerEconSim.Commands;

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
		
		if(args.length < 3)
			return false;
		
		int amount = 0;
		int price = 0;
		int value = 0;
		try{
			amount = Integer.parseInt(args[0]);
			price = Integer.parseInt(args[1]);
			value = Integer.parseInt(args[2]);
		} catch(NumberFormatException ex){
			sender.sendMessage(Messages.nan());
			return false;
		}
		
		if(amount <= 0 || price <= 0 || value < 0 || value > 5)
		{
			sender.sendMessage(Messages.invalid());
			return true;
		}
		
		ItemStack request = p.getItemInHand();
		if(request == null || request.getType() == Material.AIR){
			sender.sendMessage(Messages.noAir());
			return true;
		}
		
		String display = "";
		if(args.length > 3)
			for(int i = 3; i < args.length; i++){
				display += args[i];
			}
		else
			display = request.getType().toString();
		
		ItemStack dummy = new ItemStack(request);
		dummy.setAmount(1);
		
		String itemString = Serializer.toString(dummy);
		
		int id = RequestManager.addRequest(itemString, display, amount, price, value);
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
				if(pd != null && pd.finishedRequests.contains(r))
					completedAlready = true;
			}
			ChatColor color = (completedAlready) ? ChatColor.RED : ChatColor.GREEN;
			
			sender.sendMessage(Messages.requestList(color, r.tempId, r.amount, r.displayName, r.price));
		}
		return true;
	}
}
