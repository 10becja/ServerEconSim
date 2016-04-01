package me.becja10.ServerEconSim.Utils;

import org.bukkit.ChatColor;

public class Messages {

	public static String prefix = ChatColor.GREEN + "[" + ChatColor.DARK_GREEN + "Server Economy" + ChatColor.GREEN + "] ";
	
	private static Message noPermission = new Message(ChatColor.DARK_RED + "You do not have permission for this command");
	private static Message reloadSuccessful = new Message(prefix + ChatColor.GREEN + "Reload successful.");
	private static Message playersOnly = new Message(ChatColor.DARK_RED + "This can only be run by players.");
	private static Message nan = new Message(ChatColor.DARK_RED + "Not a number.");
	private static Message noAir = new Message(ChatColor.DARK_RED + "You can't make a request for air!");
	private static Message invalid = new Message(ChatColor.DARK_RED + "A number you entered is invalid.");
	private static Message requestAdded = new Message(prefix + ChatColor.AQUA + "Item "+ ChatColor.GOLD +"{1}"+ ChatColor.AQUA +" has been added at id "+ ChatColor.GOLD +"{0}");
	private static Message noBreakPermission = new Message(prefix + ChatColor.DARK_RED + "You do not have permission to break request boards!");
	private static Message alreadyDone = new Message(prefix + ChatColor.RED + "I'm sorry, but you've already completed this request.");
	private static Message notEnough = new Message(prefix + ChatColor.RED + "You don't have enough of this item.");
	private static Message requestSuccessful = new Message(prefix + "{3} sold "+ChatColor.GOLD+"{0} {1}"+ChatColor.GREEN+" for "+
															ChatColor.GOLD+"${2}"+ChatColor.GREEN+".");
	private static Message requestHeader = new Message(prefix + ChatColor.AQUA + "These are the current requests. " + ChatColor.GREEN + "Green "
			+ChatColor.AQUA + "requests are available to you, " + ChatColor.RED + "Red" + ChatColor.AQUA + " means you've already completed it.");
	private static Message requestList = new Message("{0}{1}. {2} {3} for ${4}");
	private static Message timeLeft = new Message(prefix + ChatColor.AQUA + "These requests will reset {0}.");
	
	public static String noPermission(){ return noPermission.getMsg();}
	public static String reloadSuccessful(){return reloadSuccessful.getMsg();}
	public static String playersOnly(){return playersOnly.getMsg();}
	public static String nan(){return nan.getMsg();}
	public static String noAir(){return noAir.getMsg();}
	public static String invalid(){return invalid.getMsg();}
	public static String requestAdded(int id, String item){return requestAdded.format(id, item);}
	public static String noBreakPermission(){return noBreakPermission.getMsg();}
	public static String alreadyDone(){return alreadyDone.getMsg();}
	public static String notEnough(){return notEnough.getMsg();}
	public static String requestSuccessful(int amount, String item, int price, String who){return requestSuccessful.format(amount, item, price, who);}
	public static String requestHeader(){return requestHeader.getMsg();}
	public static String requestList(ChatColor color, int id, int amount, String item, int price){return requestList.format(color, id, amount, item, price);}
	public static String timeLeft(String time){return timeLeft.format(time);}
	
	private static class Message{
		String msg;
		
		Message(String str)
		{
			msg = str;
		}
		
		String format(Object... args){
			String ret = msg;
			for(int i = 0; i < args.length; i++)
			{
				ret = ret.replace("{" + i + "}", args[i]+"");
			}
			return ret;
		}
		
		String getMsg()
		{
			return msg;
		}
	}
}
