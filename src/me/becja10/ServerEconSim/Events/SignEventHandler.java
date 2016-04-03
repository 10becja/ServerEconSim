package me.becja10.ServerEconSim.Events;

import me.becja10.ServerEconSim.ServerEconSim;
import me.becja10.ServerEconSim.Utils.Messages;
import me.becja10.ServerEconSim.Utils.PlayerData;
import me.becja10.ServerEconSim.Utils.Request;
import me.becja10.ServerEconSim.Utils.RequestManager;
import me.becja10.ServerEconSim.Utils.SignManager;
import net.milkbowl.vault.economy.EconomyResponse;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class SignEventHandler implements Listener{
	
	@EventHandler
	public void onChange(SignChangeEvent event){
		Player p = event.getPlayer();
		if(!p.hasPermission("ses.makesign"))
			return;
		if(event.getLine(1).equals("{RB}")){
			int rNum = 0;
			try{
				rNum = Integer.parseInt(event.getLine(2));
			}
			catch(NumberFormatException ex){
				return;
			}
			if(rNum < 0)
				return;
			
			SignManager.addSign(event.getBlock().getLocation(), rNum);
			int reqNum = rNum - 1;
			Request req = (reqNum < ServerEconSim.requests.size()) ? ServerEconSim.requests.get(reqNum) : null;
			String amount = "", price = "", item = "";
			if(req != null){
				amount = req.amount + "";
				price = req.price + "";
				item = (req.displayName != "") ? req.displayName : req.item.getType().toString();
			}
			event.setLine(0, "[Request "+ rNum+"]");
			event.setLine(1, amount);
			event.setLine(2, item);
			event.setLine(3, "$"+price);
			
			ServerEconSim.signs.put(event.getBlock().getLocation(), req);			
		}		
	}
	
	@EventHandler
	public void onBreak(BlockBreakEvent event){
		Block b = event.getBlock();
		if(!(b.getType() == Material.SIGN_POST || b.getType() == Material.WALL_SIGN))
			return;
		if(!ServerEconSim.signs.containsKey(b.getLocation()))
			return;
		if(!event.isCancelled()){
			if(!event.getPlayer().hasPermission("ses.breaksign")){
				event.getPlayer().sendMessage(Messages.noBreakPermission());
				event.setCancelled(true);
			}
			else{
				SignManager.removeSignAt(event.getBlock().getLocation());
			}
		}
	}
	
	@EventHandler
	public void onClick(PlayerInteractEvent event)
	{
		if(!(event.getAction() == Action.RIGHT_CLICK_BLOCK))
			return;
		Block b = event.getClickedBlock();
		if(!(b.getType() == Material.SIGN_POST || b.getType() == Material.WALL_SIGN))
			return;
		Request req = ServerEconSim.signs.get(b.getLocation());
		if(req == null)
			return;
		Player player = event.getPlayer();
		PlayerData pd = ServerEconSim.players.get(player.getUniqueId());
		if(pd == null)
			pd = new PlayerData(player.getUniqueId());
		if(pd.finishedRequests.contains(req))
		{
			player.sendMessage(Messages.alreadyDone());
			return;
		}		
		if(!player.getInventory().containsAtLeast(req.item, req.amount))
		{
			player.sendMessage(Messages.notEnough());
			return;
		}		
		
		//at this point, we are ready to remove the item and credit the player
		ItemStack dummy = new ItemStack(req.item);
		dummy.setAmount(req.amount);
		EconomyResponse r = ServerEconSim.econ.depositPlayer(player, req.price);
		if(r.transactionSuccess())
		{
			player.getInventory().removeItem(dummy);
			player.updateInventory();
			player.sendMessage(Messages.requestSuccessful(req.amount, req.displayName, req.price, "You"));
			ServerEconSim.logger.info(Messages.requestSuccessful(req.amount, req.displayName, req.price, player.getName()));
			pd.finishedRequests.add(req);
			ServerEconSim.players.put(player.getUniqueId(), pd);
			RequestManager.requestDone(req.id);
		}		
	}
}
