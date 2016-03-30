package me.becja10.ServerEconSim.Utils;

import me.becja10.ServerEconSim.ServerEconSim;

import org.bukkit.inventory.ItemStack;

public class Request {

	public ItemStack item;
	public String displayName;
	public int amount;
	public int price;
	public int value;
	public int id;
	public int tempId;
	
	public Request(ItemStack item, String displayName, int amount, int price, int value, int id){
		this.item = item;
		this.displayName = displayName;
		this.amount = amount;
		this.value = value;
		this.price = price + priceFlux(value);
		this.id = id;
	}
	
	private int priceFlux(int value)
	{
		int ret = (value < ServerEconSim.valueFlux.size()) ? ServerEconSim.valueFlux.get(value) : 0;
		ret = ServerEconSim.gen.nextInt(ret + 1);
		if(ServerEconSim.gen.nextBoolean())
			ret *= -1;
		return ret;
	}
}
